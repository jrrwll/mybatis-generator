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
public class ModelTemplate extends FastapiCurdTemplateOutput {

    private static final String default_fields = "    pass";

    public String model_name;
    public String simple_get_field_declare_list = default_fields;
    public String get_field_declare_list = default_fields;
    public String list_param_field_declare_list = default_fields;
    public String create_field_declare_list = default_fields;
    public String update_field_declare_list = default_fields;

    public ModelTemplate(TableDef table, FastapiCurdGeneratorConfig config) {
        String tableName = table.getTableName();

        this.model_name = config.formatClassName(tableName);

        List<String> simple_get_field_declare_list = new ArrayList<>();
        List<String> get_field_declare_list = new ArrayList<>();
        List<String> list_param_field_declare_list = new ArrayList<>();
        List<String> create_field_declare_list = new ArrayList<>();
        List<String> update_field_declare_list = new ArrayList<>();

        for (ColumnDef c : table.getNotIgnoredColumns().values()) {
            boolean primaryKey = table.getPrimaryKeyColumns().contains(c);

            String _field_declare_tmp = _field_declare;
            String comment = c.getComment();
            if (ObjectUtil.isNotBlank(comment) && config.isAddComments()) {
                _field_declare_tmp = _field_declare_with_comment;
            }

            boolean isStrColumn = PythonType.str.equals(PythonType.of(c.getType()));
            List<String> field_define = new ArrayList<>();
            if (!c.isNotNull()) {
                field_define.add("default=None");
            }
            if (isStrColumn && c.getTypeLength() != null) {
                field_define.add("max_length=" + c.getTypeLength());
            }
            String field_declare_str = InterpolationUtil.format(_field_declare_tmp, MapUtil.of(
                    "comment", formatComment(comment),
                    "type", mappingPythonType(c),
                    "field", c.getColumnName(),
                    "field_define", String.join(", ", field_define)
            ));

            // simple
            if (ObjectUtil.isNotEmpty(config.getSimpleFields(tableName))) {
                if (config.getSimpleFields(tableName).contains(c.getColumnName())) {
                    simple_get_field_declare_list.add(field_declare_str);
                }
            } else {
                if (!c.isBlob()) {
                    simple_get_field_declare_list.add(field_declare_str);
                }
            }

            // get
            if (ObjectUtil.isEmpty(config.getNotGetFields(tableName))
                    || config.getNotGetFields(tableName).contains(c.getColumnName())) {
                get_field_declare_list.add(field_declare_str);
            }

            // list
            if (ObjectUtil.isNotEmpty(config.getListFields(tableName))) {
                Boolean enableList = config.getListFields(tableName).get(c.getColumnName());
                if (enableList != null) {
                    list_param_field_declare_list.add(field_declare_str);
                    if (enableList) {
                        String list_field_declare_str = InterpolationUtil.format(_field_declare, MapUtil.of(
                                "type", "list[" + mappingPythonType(c) + "]",
                                "field", c.getColumnName(),
                                "field_define", "default=[]"
                        ));
                        list_param_field_declare_list.add(list_field_declare_str);
                    }
                }
            }

            // create, update
            if (!primaryKey) {
                if (config.getNotCreateFields(tableName).contains(c.getColumnName())) {
                    continue;
                }

                create_field_declare_list.add(field_declare_str);

                if (config.getNotUpdateFields(tableName).contains(c.getColumnName())) {
                    continue;
                }

                update_field_declare_list.add(field_declare_str);
            } else {
                update_field_declare_list.add(field_declare_str);
            }
        }

        if (!simple_get_field_declare_list.isEmpty()) {
            this.simple_get_field_declare_list = String.join("\n\n", simple_get_field_declare_list);
        }

        get_field_declare_list.removeAll(simple_get_field_declare_list);
        if (!get_field_declare_list.isEmpty()) {
            this.get_field_declare_list = String.join("\n\n", get_field_declare_list);
        }
        if (!list_param_field_declare_list.isEmpty()) {
            this.list_param_field_declare_list = String.join("\n\n", list_param_field_declare_list);
        }
        if (!create_field_declare_list.isEmpty()) {
            this.create_field_declare_list = String.join("\n\n", create_field_declare_list);
        }

        update_field_declare_list.removeAll(create_field_declare_list);
        if (!update_field_declare_list.isEmpty()) {
            this.update_field_declare_list = String.join("\n\n", update_field_declare_list);
        }
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    static final String _all = getResourceAsString("model.py.txt");
    static final String _field_declare = "    $field: $type = Field($field_define)";
    static final String _field_declare_with_comment = "    # $comment\n" + _field_declare;
}
