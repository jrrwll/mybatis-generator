package org.dreamcat.code.generator.fastapi_curd;

import static org.dreamcat.code.generator.fastapi_curd.FastapiCurdTemplateOutput.getResourceAsString;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.code.generator.base.InternalUtil;
import org.dreamcat.code.generator.base.SqlBasedGenerator;
import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.fastapi_curd.template.CommonTemplate;
import org.dreamcat.code.generator.fastapi_curd.template.EntityTemplate;
import org.dreamcat.code.generator.fastapi_curd.template.ModelTemplate;
import org.dreamcat.code.generator.fastapi_curd.template.RouterTemplate;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.AssertUtil;
import org.dreamcat.common.util.FunctionUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ObjectUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Jerry Will
 * @version 2025-09-24
 */
@Slf4j
@Getter
public class FastapiCurdGenerator implements SqlBasedGenerator {

    private final FastapiCurdGeneratorConfig config;
    private final File outputDir;
    private final File packageDir;
    private final String packageName;

    public FastapiCurdGenerator(FastapiCurdGeneratorConfig config) {
        this.config = config;

        AssertUtil.requireNotNull(config.getOutputDir(), "config.outputDir");
        File outputDir = new File(config.getOutputDir());
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new RuntimeException("fail to create dir `" + outputDir + "` for output dir");
        }
        this.outputDir = FunctionUtil.invoke(outputDir::getCanonicalFile);

        this.packageName = InternalUtil.formatPackageName(outputDir.getPath());
        this.packageDir = new File(outputDir, "src/" + packageName);
    }

    @Override
    public void generate(TableDef table) throws IOException {
        String fileName = config.formatFileName(table.getTableName());

        boolean overwrite = config.isOverwrite();
        EntityTemplate entityTemplate = new EntityTemplate(table, config);
        entityTemplate.writeDefault(packageDir, "entities/" + fileName + ".py", overwrite);

        ModelTemplate modelTemplate = new ModelTemplate(table, config);
        modelTemplate.writeDefault(packageDir, "models/" + fileName + ".py", overwrite);

        RouterTemplate routerTemplate = new RouterTemplate(table, config);
        routerTemplate.writeDefault(packageDir, "router/" + fileName + ".py", overwrite);
    }

    @Override
    public boolean isSupportGenerateBoilerplate() {
        return true;
    }

    @Override
    public void generateBoilerplate() throws IOException {
        CommonTemplate commonTemplate = new CommonTemplate(outputDir, config);

        if (!config.isDisableScript() && !new File(outputDir, "pyproject.toml").exists()) {
            String script = getResourceAsString("common/uv.sh");
            Map<String, String> env = MapUtil.of(
                    "project_dir", outputDir.getPath(),
                    "package_name", packageName,
                    "database_type", config.parseDatabaseType()
            );

            commonTemplate.runScript("uv", script, env);
            // clean __init__.py
            FileUtil.write(new File(packageDir, "__init__.py"), "");
        }

        boolean overwrite = config.isOverwrite();

        commonTemplate.writeMain(packageDir, "__main__.py", overwrite);

        commonTemplate.writeApp(packageDir, "app.py", overwrite);

        commonTemplate.writePythonPackage(packageDir, "common", overwrite);

        commonTemplate.writeDao(packageDir, "common/dao.py", overwrite);

        commonTemplate.writeDeps(packageDir, "common/deps.py", overwrite);

        commonTemplate.writePythonPackage(packageDir, "entities", overwrite);

        commonTemplate.writePythonPackage(packageDir, "models", overwrite);

        // .env
        File envFile = new File(outputDir, ".env");
        if (ObjectUtil.isNotBlank(config.getSqlalchemyUri()) && !envFile.exists()) {
            String envStr = "SQLALCHEMY_DATABASE_URI=" + config.getSqlalchemyUri() + "\n";
            FileUtil.write(envFile, envStr);
        }
    }
}
