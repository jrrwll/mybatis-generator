package org.dreamcat.cli.generator.mybatis;

import java.io.IOException;
import org.dreamcat.cli.generator.mybatis.sql.TableDef;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.x.json.JsonUtil;
import org.junit.jupiter.api.Test;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
class MyBatisGeneratorTest {

    @Test
    void testGenerate() throws IOException {
        MyBatisGeneratorConfig config = new MyBatisGeneratorConfig();
        config.setOverwrite(true);
        config.setSrcDir("src/test/java");
        config.setPutMapperTogether(true);
        config.setAddMapperAnnotation(true);

        MyBatisGenerator gen = new MyBatisGenerator(config, null);

        TableDef singleTableDef = JsonUtil.fromJson(
                ClassPathUtil.getResource("singleTableDef.json"), TableDef.class);
        gen.generate(singleTableDef);

        config.setUseLombok(false);
        TableDef multiTableDef = JsonUtil.fromJson(
                ClassPathUtil.getResource("multiTableDef.json"), TableDef.class);
        gen.generate(multiTableDef);
    }
}
