package org.dreamcat.cli.generator.react_admin;

import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Jerry Will
 * @version 2025-09-29
 */
@Slf4j
class ReactAdminGeneratorTest {

    @Test
    void test() throws IOException {
        File outputDir = new File("build/cli/generated_tests/react-admin-gen").getCanonicalFile();
        log.info("generate to " + outputDir);

        String sql = ClassLoaderUtil.getResourceAsString("ddl_classic.sql");

        ReactAdminGeneratorConfig config = new ReactAdminGeneratorConfig();
        config.setOutputDir(outputDir.getPath());
        config.setOverwrite(true);
        config.setAddComments(true);
        config.setPreferCamelCase(true);
        config.setEnableChinese(true);
        config.setViteApiUrl("http://localhost:8080/api/v1");

        ReactAdminGenerator gen = new ReactAdminGenerator(config);
        gen.generateBoilerplate();
        gen.generate(sql);
    }
}
