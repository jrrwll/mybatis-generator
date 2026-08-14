package com.example;

import static org.dreamcat.common.util.BeanUtil.copy;
import static org.dreamcat.common.util.DateUtil.addDay;
import static org.dreamcat.common.util.ReflectUtil.forName;
import static org.dreamcat.common.util.ReflectUtil.invoke;
import static org.dreamcat.common.util.ReflectUtil.newInstance;

import com.example.model.LiveRoom;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.DateUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Jerry Will
 * @version 2022-07-13
 */
@Tag("integration")
class ExampleTest {

    Class<?> liveRoomClass = forName("com.example.entity.LiveRoom");
    Class<?> liveRoomMapperClass = forName("com.example.mapper.LiveRoomMapper");
    Class<?> liveRoomConditionClass = forName("com.example.entity.condition.LiveRoomCondition");

    LiveRoom liveRoom1 = LiveRoom.builder()
            .tenantId("x").seqId(1L).recordDate(DateUtil.addDay(new Date(), -7))
            .roomId("a").build();
    LiveRoom liveRoom2 = LiveRoom.builder()
            .tenantId("x").seqId(2L).recordDate(DateUtil.addDay(new Date(), -6))
            .roomId("b").build();
    LiveRoom liveRoom3 = LiveRoom.builder()
            .tenantId("x").seqId(3L).recordDate(DateUtil.addDay(new Date(), -5))
            .roomId("c").build();
    LiveRoom liveRoom4 = LiveRoom.builder()
            .tenantId("x").seqId(4L).recordDate(DateUtil.addDay(new Date(), -4))
            .roomId("d").build();

    private void useMapper(Class<?> mapperClass, Consumer<Object> consumer) throws Exception {
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // before
            Statement stmt = session.getConnection().createStatement();
            String ddl = ClassLoaderUtil.getResourceAsString("ddl.sql");
            ddl = ddl.replace("comment =", "comment")
                    .replace("charset utf8mb4", "");
            stmt.execute(ddl);

            Object mapper = session.getMapper(mapperClass);
            consumer.accept(mapper);

            // after
            stmt.execute("drop table live_room; drop table way_bill");
            stmt.close();
        }
    }

    @Test
    void testLiveRoom() throws Exception {
        useMapper(liveRoomMapperClass, mapper -> {
            // insert
            invoke(mapper, "insert",
                    copy(liveRoom1, liveRoomClass));

            // insertSelective
            invoke(mapper, "insertSelective",
                    copy(liveRoom2, liveRoomClass));

            // batchInsert
            invoke(mapper, "batchInsert", Arrays.asList(
                    copy(liveRoom3, liveRoomClass), copy(liveRoom4, liveRoomClass)));

            // selectByPrimaryKey
            System.out.println("select:\t" + invoke(mapper, "selectByPrimaryKey",
                    "x", 1L, DateUtil.addDay(new Date(), -7)));

            // select
            Object cond1 = newInstance(liveRoomConditionClass);
            Object criteria1 = invoke(cond1, "createCriteria");
            invoke(criteria1, "andTenantIdEq", new Class[]{String.class},
                    "x");
            invoke(criteria1, "andSeqIdEq", new Class[]{Long.class},
                    2L);
            invoke(criteria1, "andRecordDateEq",
                    new Class[]{Date.class}, addDay(new Date(), -6));
            System.out.println("select:\t" + invoke(mapper, "select", cond1));

            // count
            System.out.println("count:\t" + invoke(mapper, "count", cond1));

            // updateByPrimaryKey
            liveRoom2.setRoomId("bb");
            invoke(mapper, "updateByPrimaryKey",
                    copy(liveRoom2, liveRoomClass));
            System.out.println("select after updateByPrimaryKey:\t" +
                    invoke(mapper, "select", cond1));

            // updateByPrimaryKeySelective
            liveRoom2.setCreatedAt(new Date(0L));
            invoke(mapper, "updateByPrimaryKeySelective",
                    copy(liveRoom2, liveRoomClass));
            System.out.println("select after updateByPrimaryKeySelective:\t" +
                    invoke(mapper, "select", cond1));

            // update
            liveRoom2.setCreatedAt(addDay(new Date(), -360));
            invoke(mapper, "update",
                    copy(liveRoom2, liveRoomClass), cond1);
            System.out.println("select after update:\t" +
                    invoke(mapper, "select", cond1));

            // updateSelective
            liveRoom2.setRoomId("bbb");
            invoke(mapper, "updateSelective",
                    copy(liveRoom2, liveRoomClass), cond1);
            System.out.println("select after updateSelective:\t" +
                    invoke(mapper, "select", cond1));

            // deleteByPrimaryKey
            invoke(mapper, "deleteByPrimaryKey",
                    "x", 4L, DateUtil.addDay(new Date(), -4));
            Object cond2 = newInstance(liveRoomConditionClass);
            System.out.println("count after deleteByPrimaryKey:\t" +
                    invoke(mapper, "count", cond2));

            // delete
            invoke(mapper, "delete", cond1);
            System.out.println("select after delete:\t" +
                    invoke(mapper, "select", cond1));
        });
    }
}
