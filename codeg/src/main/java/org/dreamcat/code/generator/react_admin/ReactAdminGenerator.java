package org.dreamcat.code.generator.react_admin;

import static org.dreamcat.code.generator.react_admin.ReactAdminTemplateOutput.getResourceAsString;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.code.generator.base.InternalUtil;
import org.dreamcat.code.generator.base.SqlBasedGenerator;
import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.react_admin.template.CommonTemplate;
import org.dreamcat.code.generator.react_admin.template.ResourceTemplate;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.util.AssertUtil;
import org.dreamcat.common.util.FunctionUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ObjectUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Will
 * @version 2025-09-23
 */
@Slf4j
@Getter
public class ReactAdminGenerator implements SqlBasedGenerator {

    private final ReactAdminGeneratorConfig config;
    private final File outputDir;
    private final String packageName;
    private final File srcDir;
    private final File resourceIndexFile;

    public ReactAdminGenerator(ReactAdminGeneratorConfig config) {
        this.config = config;

        AssertUtil.requireNotNull(config.getOutputDir(), "config.outputDir");
        File outputDir = new File(config.getOutputDir());
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new RuntimeException("fail to create dir `" + outputDir + "` for output dir");
        }
        this.outputDir = FunctionUtil.invoke(outputDir::getCanonicalFile);

        this.packageName = InternalUtil.formatPackageName(outputDir.getPath());
        this.srcDir = new File(outputDir, "src");
        this.resourceIndexFile = new File(srcDir, "resource/index.tsx");
    }

    @Override
    public void generate(List<TableCommonDef> tableDefs) throws IOException {
        if (resourceIndexFile.exists()) {
            if (!resourceIndexFile.delete() && resourceIndexFile.exists()) {
                throw new IOException("failed to delete file " + resourceIndexFile);
            }
        }
        SqlBasedGenerator.super.generate(tableDefs);
    }

    @Override
    public void generate(TableDef table) throws IOException {
        String fileName = config.formatFileName(table.getTableName());
        boolean overwrite = config.isOverwrite();

        ResourceTemplate resourceTemplate = new ResourceTemplate(table, config);
        resourceTemplate.writeDefault(srcDir, "resource/" + fileName + ".tsx", overwrite);
        resourceTemplate.writeIndex(resourceIndexFile);
    }

    @Override
    public boolean isSupportGenerateBoilerplate() {
        return true;
    }

    @Override
    public void generateBoilerplate() throws IOException {
        CommonTemplate commonTemplate = new CommonTemplate(outputDir, config);

        if (!config.isDisableScript() && !new File(outputDir, "pnpm-lock.yaml").exists()) {
            String script = getResourceAsString("common/pnpm.sh");
            Map<String, String> env = MapUtil.of(
                    "project_dir", outputDir.getPath(),
                    "enable_chinese", String.valueOf(config.isEnableChinese())
            );

            commonTemplate.runScript("pnpm", script, env);
            // remove src dir
            FileUtil.deleteRecursively(new File(outputDir, "src"));
        }

        boolean overwrite = config.isOverwrite();

        commonTemplate.writeMain(srcDir, "main.tsx", overwrite);

        commonTemplate.writeIndex(srcDir, "index.tsx", overwrite);

        commonTemplate.writeRest(srcDir, "provider/rest.tsx", overwrite);

        commonTemplate.writeComponents(srcDir, "button.tsx", overwrite);

        // .env
        File envFile = new File(outputDir, ".env");
        if (ObjectUtil.isNotBlank(config.getViteApiUrl()) && !envFile.exists()) {
            String envStr = "VITE_API_URL=" + config.getViteApiUrl() + "\n";
            FileUtil.write(envFile, envStr);
        }
    }
}
