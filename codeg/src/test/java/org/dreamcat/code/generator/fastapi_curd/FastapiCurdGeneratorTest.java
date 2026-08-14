package org.dreamcat.code.generator.fastapi_curd;

import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Jerry Will
 * @version 2025-09-26
 */
@Slf4j
@Tag("integration")
class FastapiCurdGeneratorTest {

    @Test
    void test() throws IOException {
        FileUtil.loadDotEnvFile();
        File outputDir = new File("build/generated_tests/fastapi-curd").getCanonicalFile();
        log.info("generate to " + outputDir);

        String sql = ClassLoaderUtil.getResourceAsString("ddl_classic.sql");

        FastapiCurdGeneratorConfig config = new FastapiCurdGeneratorConfig();
        config.setOutputDir(outputDir.getPath());
        config.setOverwrite(true);
        config.setAddComments(true);
        config.setSqlalchemyUri(System.getProperty("SQLALCHEMY_DATABASE_URI"));
        config.setPreferCamelCase(true);

        FastapiCurdGenerator gen = new FastapiCurdGenerator(config);
        gen.generateBoilerplate();
        gen.generate(sql);
    }

}
