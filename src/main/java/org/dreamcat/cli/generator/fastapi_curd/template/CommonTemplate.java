package org.dreamcat.cli.generator.fastapi_curd.template;

import org.dreamcat.cli.generator.base.InternalUtil;
import org.dreamcat.cli.generator.fastapi_curd.FastapiCurdGeneratorConfig;
import org.dreamcat.cli.generator.fastapi_curd.FastapiCurdTemplateOutput;

import java.io.File;
import java.io.IOException;

/**
 * @author Jerry Will
 * @version 2025-09-28
 */
public class CommonTemplate extends FastapiCurdTemplateOutput {

    public String project_name;
    public String package_name;

    public String delete_column = "deleted";
    public String order_column = "updated_by";

    private transient boolean dao_override;

    public CommonTemplate(File outputDir, FastapiCurdGeneratorConfig config) {
        this.project_name = outputDir.getName();
        this.package_name = InternalUtil.formatPackageName(outputDir.getPath());

        if (config.getDeleteColumn() != null) {
            this.delete_column = config.getDeleteColumn();
            this.dao_override = true;
        }
        if (config.getOrderColumn() != null) {
            this.order_column = config.getOrderColumn();
            this.dao_override = true;
        }
    }

    public void writePythonPackage(File outputDir, String name, boolean overwrite) throws IOException {
        write("", outputDir, name + "/__init__.py", overwrite);
    }

    public void writeMain(File outputDir, String name, boolean overwrite) throws IOException {
        write(_main, outputDir, name, overwrite);
    }

    public void writeApp(File outputDir, String name, boolean overwrite) throws IOException {
        write(_app, outputDir, name, overwrite);
    }

    public void writeDao(File outputDir, String name, boolean overwrite) throws IOException {
        if (dao_override) {
            write(_dao_override, outputDir, name, overwrite);
        } else {
            write(_dao, outputDir, name, overwrite);
        }
    }

    public void writeDeps(File outputDir, String name, boolean overwrite) throws IOException {
        write(_deps, outputDir, name, overwrite);
    }

    static final String _main = getResourceAsString("common/__main__.py.txt");
    static final String _app = getResourceAsString("common/app.py.txt");
    static final String _dao = getResourceAsString("common/dao.py.txt");
    static final String _dao_override = getResourceAsString("common/dao_override.py.txt");
    static final String _deps = getResourceAsString("common/deps.py.txt");
}
