package org.dreamcat.cli.generator.mybatis.template;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.text.DollarInterpolation;
import org.dreamcat.common.util.MapUtil;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
public class SqlMapperTemplate {

    public String namespace;
    public String result_map;
    public String result_map_id_list;
    public String result_map_result_list;
    public String table_name;
    public String select_column_list;
    public String insert_column_list;
    public String insert_column_value_list;
    public String if_test_column_list;
    public String if_test_column_value_list;
    public String primary_key_eq_list;
    public String update_column_value_list;
    public String update_if_test_column_value_list;

    public SqlMapperTemplate(EntityDef entity) {
        this.namespace = entity.getMapperClass();
        this.result_map = entity.getEntityClass();

        this.result_map_id_list = entity.getPrimaryKeyColumns().stream()
                .map(it -> formatResultMapColumn(it, "id"))
                .collect(Collectors.joining("\n        ")); // 8 spaces
        this.result_map_result_list = entity.getNotPrimaryKeyColumns().stream()
                .map(it -> formatResultMapColumn(it, "result"))
                .collect(Collectors.joining("\n        "));

        this.table_name = entity.getTableName();
        this.select_column_list = String.join(", ", entity.getSelectColumns().keySet());

        this.insert_column_list = String.join(", ", entity.getInsertColumns().keySet());
        this.insert_column_value_list = entity.getInsertColumns().values().stream()
                .map(SqlMapperTemplate::formatInsertColumnValue)
                .collect(Collectors.joining(" "));

        this.if_test_column_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatIfTestColumn)
                .collect(Collectors.joining("\n            ")); // 12 spaces
        this.if_test_column_value_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatIfTestColumnValue)
                .collect(Collectors.joining("\n            "));

        this.primary_key_eq_list = entity.getPrimaryKeyColumns().stream()
                .map(SqlMapperTemplate::formatColumnEq)
                .collect(Collectors.joining(" and "));

        this.update_column_value_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatColumnEq)
                .collect(Collectors.joining(", "));
        this.update_if_test_column_value_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatUpdateIfTestColumnValue)
                .collect(Collectors.joining(", "));

    }

    public String toString() {
        Map<String, String> context = MapUtil.of(
                "namespace", namespace,
                "result_map", result_map,
                "result_map_id_list", result_map_id_list,
                "result_map_result_list", result_map_result_list,
                "table_name", table_name,
                "select_column_list", select_column_list,
                "insert_column_list", insert_column_list,
                "insert_column_value_list", insert_column_value_list,
                "if_test_column_list", if_test_column_list,
                "if_test_column_value_list", if_test_column_value_list,
                "primary_key_eq_list", primary_key_eq_list,
                "update_column_value_list", update_column_value_list,
                "update_if_test_column_value_list", update_if_test_column_value_list);
        return DollarInterpolation.format(Holder.all, context);
    }

    private static String formatResultMapColumn(EntityDef.Column column, String element) {
        return DollarInterpolation.format(Holder.result_map_column, MapUtil.of(
                "element", element,
                "column", column.getColumn().getName(),
                "type", column.getColumn().getType(),
                "property", column.getProperty()));
    }

    private static String formatInsertColumnValue(EntityDef.Column column) {
        return DollarInterpolation.format(Holder.insert_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getColumn().getType()));
    }

    private static String formatIfTestColumn(EntityDef.Column column) {
        return DollarInterpolation.format(Holder.if_test_column, MapUtil.of(
                "property", column.getProperty(),
                "column", column.getColumn().getName()));
    }

    private static String formatIfTestColumnValue(EntityDef.Column column) {
        return DollarInterpolation.format(Holder.if_test_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getColumn().getType()));
    }

    private static String formatColumnEq(EntityDef.Column column) {
        return DollarInterpolation.format(Holder.column_eq, MapUtil.of(
                "column", column.getColumn().getName(),
                "property", column.getProperty(),
                "type", column.getColumn().getType()));
    }

    private static String formatUpdateIfTestColumnValue(EntityDef.Column column) {
        return DollarInterpolation.format(Holder.update_if_test_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getColumn().getType()));
    }

    private static class Holder {

        static final String all;
        static final String result_map_column = "<$element column=\"$column\" jdbcType=\"$type\" property=\"$property\" />";
        static final String insert_column_value = "#{item.$property,jdbcType=$type},";
        static final String if_test_column = "<if test=\"$property != null\">\n"
                + "                $column,\n"
                + "            </if>";
        static final String if_test_column_value = "<if test=\"$property != null\">\n"
                + "                #{$property,jdbcType=$type},\n"
                + "            </if>";
        static final String column_eq = "$column = #{$property,jdbcType=$type}";
        static final String update_if_test_column_value = "<if test=\"entity.$column != null\">\n"
                + "                id = #{entity.$property,jdbcType=$type},\n"
                + "            </if>";

        static {
            try {
                String filename = "org/dreamcat/cli/generator/mybatis/mapper.xml";
                all = FileUtil.readAsString(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
