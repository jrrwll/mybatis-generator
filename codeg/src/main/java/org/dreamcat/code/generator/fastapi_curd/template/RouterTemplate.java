package org.dreamcat.code.generator.fastapi_curd.template;

import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.fastapi_curd.FastapiCurdGeneratorConfig;
import org.dreamcat.code.generator.fastapi_curd.FastapiCurdTemplateOutput;

/**
 * @author Jerry Will
 * @version 2025-09-28
 */
public class RouterTemplate extends FastapiCurdTemplateOutput {

    public String file_name;
    public String class_name;
    public String router_name;

    public String page_no_alias = "";
    public String page_size_alias = "";

    public RouterTemplate(TableDef table, FastapiCurdGeneratorConfig config) {
        String tableName = table.getTableName();

        this.file_name = config.formatFileName(tableName);
        this.class_name = config.formatClassName(tableName);
        this.router_name = config.formatRouterName(tableName);

        if (config.isPreferCamelCase()) {
            this.page_no_alias = ", alias=\"pageNo\"";
            this.page_size_alias = ", alias=\"pageSize\"";
        }
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    static final String _all = getResourceAsString("router.py.txt");
}
