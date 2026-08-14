package org.dreamcat.code.generator.fastapi_curd.template;

import org.dreamcat.code.generator.base.ColumnDef;
import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.fastapi_curd.FastapiCurdGeneratorConfig;
import org.dreamcat.code.generator.fastapi_curd.FastapiCurdTemplateOutput;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jerry Will
 * @version 2025-09-26
 */
public class EntityTemplate extends FastapiCurdTemplateOutput {

    public String table_name;
    public String entity_name;
    public String filed_declare_list;

    public EntityTemplate(TableDef table, FastapiCurdGeneratorConfig config) {
        this.table_name = table.getTableName();
        this.entity_name = config.formatClassName(table.getTableName());

        List<String> fieldDeclareList = new ArrayList<>();
        for (ColumnDef c : table.getColumns().values()) {
            String _field_declare_tmp = _field_declare;
            String comment = c.getComment();
            if (ObjectUtil.isNotBlank(comment) && config.isAddComments()) {
                _field_declare_tmp = _field_declare_with_comment;
            }

            boolean isStrColumn = PythonType.str.equals(PythonType.of(c.getType()));
            List<String> field_define = new ArrayList<>();
            if (!c.isNotNull()) {
                field_define.add("default=None");
            } else if (config.getIgnoreColumns().contains(c.getColumnName()) ||
                    config.getNotCreateFields().contains(c.getColumnName())) {
                field_define.add("sa_column_kwargs={\"server_default\": \"\"}");
            }

            if (table.getPrimaryKeyColumns().contains(c)) {
                field_define.add("primary_key=True");
            }
            if (isStrColumn && c.getTypeLength() != null) {
                field_define.add("max_length=" + c.getTypeLength());
            }
            fieldDeclareList.add(InterpolationUtil.format(_field_declare_tmp, MapUtil.of(
                    "comment", formatComment(comment),
                    "type", mappingPythonType(c),
                    "field", c.getColumnName(),
                    "field_define", String.join(", ", field_define)
            )));
        }

        this.filed_declare_list = "\n" + String.join("\n\n", fieldDeclareList);
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    static final String _all = getResourceAsString("entity.py.txt");
    static final String _field_declare = "    $field: $type = Field($field_define)";
    static final String _field_declare_with_comment = "    # $comment\n" + _field_declare;
}
