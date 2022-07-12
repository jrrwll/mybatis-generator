package com.example;

import static org.dreamcat.common.util.ReflectUtil.*;
import static org.dreamcat.common.x.bean.BeanCopyUtil.*;
import static org.dreamcat.common.util.DateUtil.*;

import com.example.model.LiveRoom;
import java.io.InputStream;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jerry Will
 * @version 2022-07-13
 */
class ExampleTest {

    Class<?> liveRoomClass = forName("com.example.entity.LiveRoom");
    Class<?> liveRoomMapperClass = forName("com.example.mapper.LiveRoomMapper");
    Class<?> liveRoomConditionClass = forName("com.example.mapper.LiveRoomCondition");

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
            stmt.execute(ClassPathUtil.getResourceAsString("ddl.sql"));

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

            // select
            System.out.println("select:\t" + invoke(mapper, "select",
                    "x", 1L, DateUtil.addDay(new Date(), -7)));

            // selectBy
            Object cond1 = newInstance(liveRoomConditionClass);
            Object criteria1 = invoke(cond1, "createCriteria");
            invoke(criteria1, "andTenantIdEq", new Class[]{String.class},
                    "x");
            invoke(criteria1, "andSeqIdEq", new Class[]{Long.class},
                    2L);
            invoke(criteria1, "andRecordDateEq",
                    new Class[]{String.class},
                    formatDate(addDay(new Date(), -6)));
            System.out.println("selectBy:\t" + invoke(mapper, "selectBy", cond1));

            // countBy
            System.out.println("countBy:\t" + invoke(mapper, "countBy", cond1));

            // update
            liveRoom2.setRoomId("bb");
            invoke(mapper, "update",
                    copy(liveRoom2, liveRoomClass));
            System.out.println("selectBy after update:\t" +
                    invoke(mapper, "selectBy", cond1));

            // updateSelective
            liveRoom2.setCreatedAt(new Date(0L));
            invoke(mapper, "updateSelective",
                    copy(liveRoom2, liveRoomClass));
            System.out.println("selectBy after updateSelective:\t" +
                    invoke(mapper, "selectBy", cond1));

            // updateBy
            liveRoom2.setCreatedAt(addDay(new Date(), -360));
            invoke(mapper, "updateBy",
                    copy(liveRoom2, liveRoomClass), cond1);
            System.out.println("selectBy after updateBy:\t" +
                    invoke(mapper, "selectBy", cond1));

            // updateSelectiveBy
            liveRoom2.setRoomId("bbb");
            invoke(mapper, "updateSelectiveBy",
                    copy(liveRoom2, liveRoomClass), cond1);
            System.out.println("selectBy after updateSelectiveBy:\t" +
                    invoke(mapper, "selectBy", cond1));

            // delete
            invoke(mapper, "delete",
                    "x", 4L, DateUtil.addDay(new Date(), -4));
            Object cond2 = newInstance(liveRoomConditionClass);
            System.out.println("countBy after delete:\t" +
                    invoke(mapper, "countBy", cond2));
            
            // deleteBy
            invoke(mapper, "deleteBy", cond1);
            System.out.println("selectBy after deleteBy:\t" +
                    invoke(mapper, "selectBy", cond1));
        });
    }
}
