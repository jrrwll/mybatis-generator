package org.dreamcat.cli.generator.mybatis;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.IOException;
import java.util.List;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.sql.SqlUtil;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.x.json.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
class MyBatisGeneratorTest {

    MyBatisGeneratorConfig config = new MyBatisGeneratorConfig();

    @BeforeEach
    void init() {
        config.setOverwrite(true);
        config.setSrcDir("src/test/java");
        config.setPutMapperTogether(true);
        config.setAddMapperAnnotation(true);

        JsonUtil.configure(m -> m.setSerializationInclusion(Include.NON_ABSENT));
    }

    @Test
    void test() throws IOException {
        String sql = ClassPathUtil.getResourceAsString("ddl.sql");
        List<TableCommonDef> tableDefs = SqlUtil.fromCreateTable(sql);
        for (TableCommonDef tableDef : tableDefs) {
            System.out.println(JsonUtil.toJson(tableDef));
        }
        MyBatisGenerator gen = new MyBatisGenerator(config);
        gen.generate(sql);
    }

    @Test
    void testGenerate() throws IOException {
        config.setUseLombok(false);
        MyBatisGenerator gen = new MyBatisGenerator(config);

        TableCommonDef singleTableDef = JsonUtil.fromJson(
                ClassPathUtil.getResource("singleTableDef.json"), TableCommonDef.class);
        gen.generate(singleTableDef);

        config.setUseLombok(false);
        TableCommonDef multiTableDef = JsonUtil.fromJson(
                ClassPathUtil.getResource("multiTableDef.json"), TableCommonDef.class);
        gen.generate(multiTableDef);
    }
}
