package org.dreamcat.cli.generator.mybatis;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.IOException;
import java.util.List;
import org.dreamcat.common.json.DataMapper;
import org.dreamcat.common.sql.SqlUtil;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
class MyBatisGeneratorTest {

    MyBatisGeneratorConfig config = new MyBatisGeneratorConfig();
    DataMapper jsonMapper = DataMapper.build()
            .setSerializationInclusion(Include.NON_ABSENT)
            .build();

    @BeforeEach
    void init() {
        config.setOverwrite(true);
        config.setSrcDir("src/test/java");
        config.setAddMapperAnnotation(true);
        config.setEnableResultMapWithBLOBs(true);
        config.setEnableExtendsMapper(true);
    }

    @Test
    void test() throws IOException {
        String sql = ClassLoaderUtil.getResourceAsString("ddl.sql");
        List<TableCommonDef> tableDefs = SqlUtil.parseCreateTable(sql);
        for (TableCommonDef tableDef : tableDefs) {
            System.out.println("tableDef: " + tableDef.getName());
            System.out.println(jsonMapper.toJson(tableDef));
        }
        MyBatisGenerator gen = new MyBatisGenerator(config);
        gen.generate(sql);
    }

    @Test
    void testGenerate() throws IOException {
        config.setEnableLombok(false);
        config.setAddComments(false);
        config.setDelimitKeyword('`');
        MyBatisGenerator gen = new MyBatisGenerator(config);

        TableCommonDef singleTableDef = jsonMapper.fromJson(
                ClassLoaderUtil.getResource("singleTableDef.json"), TableCommonDef.class);
        gen.generate(singleTableDef);

        TableCommonDef multiTableDef = jsonMapper.fromJson(
                ClassLoaderUtil.getResource("multiTableDef.json"), TableCommonDef.class);
        gen.generate(multiTableDef);
    }
}
