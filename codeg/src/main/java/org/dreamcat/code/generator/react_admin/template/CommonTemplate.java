package org.dreamcat.code.generator.react_admin.template;

import org.dreamcat.code.generator.base.InternalUtil;
import org.dreamcat.code.generator.react_admin.ReactAdminGeneratorConfig;
import org.dreamcat.code.generator.react_admin.ReactAdminTemplateOutput;

import java.io.File;
import java.io.IOException;

/**
 * @author Jerry Will
 * @version 2025-09-29
 */
public class CommonTemplate extends ReactAdminTemplateOutput {

    public String project_name;
    public String package_name;
    public boolean enableChinese;

    public CommonTemplate(File outputDir, ReactAdminGeneratorConfig config) {
        this.project_name = outputDir.getName();
        this.package_name = InternalUtil.formatPackageName(outputDir.getPath());
        this.enableChinese = config.isEnableChinese();
    }

    public void writeMain(File outputDir, String name, boolean overwrite) throws IOException {
        write(_main, outputDir, name, overwrite);
    }

    public void writeIndex(File outputDir, String name, boolean overwrite) throws IOException {
        write(enableChinese ? _index_zh : _index, outputDir, name, overwrite);
    }

    public void writeRest(File outputDir, String name, boolean overwrite) throws IOException {
        write(_rest, outputDir, name, overwrite);
    }

    public void writeComponents(File outputDir, String name, boolean overwrite) throws IOException {
        String template = getResourceAsString("components/" + name);
        write(template, outputDir, "components/" + name, overwrite);
    }

    static final String _main = getResourceAsString("common/main.tsx");
    static final String _index = getResourceAsString("common/index.tsx");
    static final String _index_zh = getResourceAsString("common/index_zh.tsx");
    static final String _rest = getResourceAsString("common/rest.tsx");
}
