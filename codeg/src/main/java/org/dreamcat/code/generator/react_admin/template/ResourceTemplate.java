package org.dreamcat.code.generator.react_admin.template;

import org.dreamcat.code.generator.base.ColumnDef;
import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.react_admin.ReactAdminGeneratorConfig;
import org.dreamcat.code.generator.react_admin.ReactAdminGeneratorConfig.FilterConfig;
import org.dreamcat.code.generator.react_admin.ReactAdminGeneratorConfig.TableConfig;
import org.dreamcat.code.generator.react_admin.ReactAdminTemplateOutput;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.FunctionUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ObjectUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Jerry Will
 * @version 2025-09-23
 */
public class ResourceTemplate extends ReactAdminTemplateOutput {

    public String display_name;
    public String resource_name;
    public String class_name;
    public String title_field;

    public String filter_list;
    public String list_fields;
    public String edit_fields;
    public String create_fields;

    public String import_resource;
    public String use_resource;

    public ResourceTemplate(TableDef table, ReactAdminGeneratorConfig config) {
        String tableName = table.getTableName();
        TableConfig tableConfig = config.getTableConfigs().getOrDefault(tableName, new TableConfig());

        this.display_name = config.formatDisplayName(tableName, table.getTableComment());
        this.resource_name = config.formatResourceName(tableName);
        this.class_name = config.formatClassName(tableName);
        this.title_field = FunctionUtil.firstNotBlank(tableConfig.getTitleField(), config.getTitleField());

        Function<ColumnDef, String> labelGetter = c -> config.formatFieldDisplayName(c.getColumnName(), c.getComment(), tableName);

        // filter
        List<String> filter_list = new ArrayList<>();
        Map<String, FilterConfig> filterFields = tableConfig.getFilterFields();
        for (ColumnDef c : table.getNotIgnoredColumns().values()) {
            String field_name = config.formatFieldName(c.getColumnName(), tableName);
            String field_label = labelGetter.apply(c);

            if (ObjectUtil.isEmpty(config.getFixFilterFields()) && ObjectUtil.isEmpty(filterFields)) break;

            if (ObjectUtil.isEmpty(filterFields)) {
                if (!config.getFixFilterFields().contains(c.getColumnName())) continue;

                String filter_input = InterpolationUtil.format(_input, MapUtil.of(
                        "input_type", mappingRaInputType(c),
                        "field_name", field_name,
                        "label", field_label,
                        "field_options", "alwaysOn"
                ));
                filter_list.add(filter_input);
                continue;
            }

            FilterConfig filterConfig = filterFields.get(c.getColumnName());
            if (filterConfig == null) continue;
            boolean alwaysOn = filterConfig.getFix() != null ?
                    filterConfig.getFix() : config.getFixFilterFields().contains(c.getColumnName());

            if (ObjectUtil.isNotEmpty(filterConfig.getChoices())) {
                String field_options = "choices={" + JsonUtil.toJson(filterConfig.getChoices()) + "}";
                if (alwaysOn) {
                    field_options += " alwaysOn";
                }

                String filter_input = InterpolationUtil.format(_input, MapUtil.of(
                        "input_type", "SelectArrayInput",
                        "field_name", field_name,
                        "label", field_label,
                        "field_options", field_options
                ));
                filter_list.add(filter_input);
            } else if (ObjectUtil.isNotBlank(filterConfig.getReference())) {
                String option_text = FunctionUtil.firstNotBlank(filterConfig.getReferenceOptionText(),
                        tableConfig.getTitleField(), config.getTitleField());
                String filter_reference_input = InterpolationUtil.format(_filter_reference_input, MapUtil.of(
                        "field_name", field_name,
                        "label", field_label,
                        "reference", filterConfig.getReference(),
                        "option_text", option_text,
                        "field_options", alwaysOn ? "alwaysOn" : ""
                ));
                filter_list.add(filter_reference_input);
            } else {
                String filter_input = InterpolationUtil.format(_input, MapUtil.of(
                        "input_type", mappingRaInputType(c),
                        "field_name", field_name,
                        "label", field_label,
                        "field_options", alwaysOn ? "alwaysOn" : ""
                ));
                filter_list.add(filter_input);
            }
        }
        this.filter_list = String.join("\n", filter_list);

        // list
        List<String> list_fields = new ArrayList<>();
        for (ColumnDef c : table.getNotIgnoredColumns().values()) {
            if (ObjectUtil.isEmpty(config.getNotGetFields(tableName))
                    || config.getNotGetFields(tableName).contains(c.getColumnName())) continue;

            String field_name = config.formatFieldName(c.getColumnName(), tableName);
            String field_label = labelGetter.apply(c);

            String field_type = mappingRaFieldType(c);
            String field_options = field_type.equals("DateField") ? "showTime" : "";

            String list_field = InterpolationUtil.format(_list_field, MapUtil.of(
                    "field_type", field_type,
                    "field_name", field_name,
                    "label", field_label,
                    "field_options", field_options
            ));
            list_fields.add(list_field);
        }
        this.list_fields = String.join("\n", list_fields);

        // create and edit
        List<String> create_fields = new ArrayList<>();
        List<String> edit_fields = new ArrayList<>();
        for (ColumnDef c : table.getNotIgnoredColumns().values()) {
            boolean isPrimaryKey = table.getPrimaryKeyColumns().contains(c);

            String field_name = config.formatFieldName(c.getColumnName(), tableName);
            String field_label = labelGetter.apply(c);

            String input_type = mappingRaInputType(c);
            String field_options = "";
            if (isPrimaryKey) {
                field_options = " disabled";
            }

            String input_field = InterpolationUtil.format(_input, MapUtil.of(
                    "input_type", input_type,
                    "field_name", field_name,
                    "label", field_label,
                    "field_options", field_options
            ));
            if (!isPrimaryKey) {
                if (!config.getNotCreateFields(tableName).contains(c.getColumnName())) {
                    create_fields.add(input_field);
                }
            }
            if (!config.getNotUpdateFields(tableName).contains(c.getColumnName())) {
                edit_fields.add(input_field);
            }
        }
        this.create_fields = String.join("\n", create_fields);
        this.edit_fields = String.join("\n", edit_fields);

        // index
        String file_name = config.formatFileName(tableName);
        String className = config.formatClassName(tableName);
        this.import_resource = InterpolationUtil.format(_import_resource,
                "class_name", className, "file_name", file_name);
        this.use_resource = InterpolationUtil.format(_use_resource,
                "class_name", className);
    }

    public void writeIndex(File indexFile) throws IOException {
        if(!indexFile.exists()) {
            String content = InterpolationUtil.format(_index_all,
                    "import_resource_list", import_resource,
                    "use_resource_list", use_resource);
            FileUtil.write(indexFile, content);
            return;
        }
        // append to the existing index.tsx
        List<String> lines = FileUtil.readLines(indexFile);
        int blankIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                blankIndex = i;
                break;
            }
        }

        lines.add(blankIndex, import_resource);
        lines.remove(lines.size() - 1);
        lines.add(use_resource);
        lines.add("]");
        FileUtil.write(indexFile, String.join("\n", lines));
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    static final String _all = getResourceAsString("resource/resource.tsx");
    static final String _input = "        <${input_type} label=\"${label}\" source=\"${field_name}\" ${field_options}/>";
    static final String _filter_reference_input = "        <ReferenceArrayInput source=\"${field_name}\" reference=\"${reference}\" label=\"${label}\" ${field_options}><SelectArrayInput optionText=\"${option_text}\" /></ReferenceArrayInput>";
    static final String _list_field = "            <${field_type} label=\"${label}\" source=\"${field_name}\" ${field_options}/>";

    static final String _index_all = getResourceAsString("resource/index.tsx");
    static final String _import_resource = "import ${class_name}Resource from './${file_name}';";
    static final String _use_resource = "    ${class_name}Resource(),";
}
