package org.dreamcat.cli.generator.mybatis.template;

import java.io.IOException;
import java.util.stream.Collectors;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.java.EntityColumnDef;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.text.DollarInterpolation;
import org.dreamcat.common.util.MapUtil;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
public class SqlMapperTemplate extends TemplateOutput {

    public String entity_package;
    public String entity_name;
    public String mapper_package;
    public String mapper_name;
    public String result_map;

    /**
     * {@code <id column="$column" jdbcType="$type" property="$property" /> }
     */
    public String result_map_id_list;
    /**
     * {@code <result column="$column" jdbcType="$type" property="$property" /> }
     */
    public String result_map_result_list;
    public String table_name;
    /**
     * {@code id,created_at,updated_at }
     */
    public String column_list;
    /**
     * {@code #{item.$property,jdbcType=$type}, }
     */
    public String insert_column_value_list;
    public String batch_insert_column_value_list;
    /**
     * {@code <if test="$property != null"> $column, </if> }
     */
    public String if_test_column_list;
    /**
     * {@code <if test="$property != null"> #{$property,jdbcType=$type}, </if> }
     */
    public String if_test_column_value_list;
    /**
     * {@code $column = #{$property,jdbcType=$type} and  }
     */
    public String primary_key_eq_list;
    /**
     * {@code $column = #{$property,jdbcType=$type}, }
     */
    public String update_column_value_list;
    public String update_by_column_value_list;
    /**
     * {@code <if test="$property != null"> id = #{$property,jdbcType=$type}, </if> }
     */
    public String update_if_test_column_value_list;
    public String update_by_if_test_column_value_list;

    public SqlMapperTemplate(EntityDef entity, MyBatisGeneratorConfig config) {
        this.entity_package = config.getEntityPackageName();
        this.entity_name = entity.getEntityName();
        this.mapper_package = config.getMapperPackageName();
        this.mapper_name = entity.getMapperName();
        this.result_map = entity_package + "." + entity_name;

        this.result_map_id_list = entity.getPrimaryKeyColumns().stream()
                .map(it -> formatResultMapColumn(it, "id"))
                .collect(Collectors.joining("\n"));
        this.result_map_result_list = entity.getNotPrimaryKeyColumns().stream()
                .map(it -> formatResultMapColumn(it, "result"))
                .collect(Collectors.joining("\n"));

        this.table_name = entity.getTableName();
        this.column_list = String.join(", ", entity.getEntityColumns().keySet());

        this.insert_column_value_list = entity.getEntityColumns().values().stream()
                .map(SqlMapperTemplate::formatInsertColumnValue)
                .collect(Collectors.joining(", "));
        this.batch_insert_column_value_list = entity.getEntityColumns().values().stream()
                .map(SqlMapperTemplate::formatBatchInsertColumnValue)
                .collect(Collectors.joining(", "));

        this.if_test_column_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatIfTestColumn)
                .collect(Collectors.joining("\n"));
        this.if_test_column_value_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatIfTestColumnValue)
                .collect(Collectors.joining("\n"));

        this.primary_key_eq_list = entity.getPrimaryKeyColumns().stream()
                .map(SqlMapperTemplate::formatColumnEq)
                .collect(Collectors.joining("\n    and "));

        this.update_column_value_list = entity.getNotPrimaryKeyColumns().stream()
                .map(SqlMapperTemplate::formatColumnEq)
                .collect(Collectors.joining("\n    , "));
        this.update_by_column_value_list = entity.getColumns().values().stream()
                .map(SqlMapperTemplate::formatColumnEqBy)
                .collect(Collectors.joining("\n    , "));

        this.update_if_test_column_value_list = entity.getNotPrimaryKeyColumns().stream()
                .map(SqlMapperTemplate::formatUpdateIfTestColumnValue)
                .collect(Collectors.joining("\n      ,\n"));
        this.update_by_if_test_column_value_list = entity.getNotPrimaryKeyColumns().stream()
                .map(SqlMapperTemplate::formatUpdateByIfTestColumnValue)
                .collect(Collectors.joining("\n      ,\n"));
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    private static String formatResultMapColumn(EntityColumnDef column, String element) {
        return DollarInterpolation.format(_result_map_column, MapUtil.of(
                "element", element,
                "column", column.getName(),
                "type", column.getType(),
                "property", column.getProperty()));
    }

    private static String formatInsertColumnValue(EntityColumnDef column) {
        return DollarInterpolation.format(_insert_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatBatchInsertColumnValue(EntityColumnDef column) {
        return DollarInterpolation.format(_batch_insert_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatIfTestColumn(EntityColumnDef column) {
        return DollarInterpolation.format(_if_test_column, MapUtil.of(
                "property", column.getProperty(),
                "column", column.getName()));
    }

    private static String formatIfTestColumnValue(EntityColumnDef column) {
        return DollarInterpolation.format(_if_test_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatColumnEq(EntityColumnDef column) {
        return DollarInterpolation.format(_column_eq, MapUtil.of(
                "column", column.getName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatColumnEqBy(EntityColumnDef column) {
        return DollarInterpolation.format(_column_eq_by, MapUtil.of(
                "column", column.getName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatUpdateIfTestColumnValue(EntityColumnDef column) {
        return DollarInterpolation.format(_update_if_test_column_value, MapUtil.of(
                "column", column.getName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatUpdateByIfTestColumnValue(EntityColumnDef column) {
        return DollarInterpolation.format(_update_by_if_test_column_value, MapUtil.of(
                "column", column.getName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static final String _all;
    private static final String _result_map_column = "    <$element column=\"$column\" jdbcType=\"$type\" property=\"$property\"/>";
    private static final String _insert_column_value = "#{$property,jdbcType=$type}";
    private static final String _batch_insert_column_value = "#{item.$property,jdbcType=$type}";
    private static final String _if_test_column = "      <if test=\"$property != null\">\n"
            + "        $column,\n"
            + "      </if>";
    private static final String _if_test_column_value = "      <if test=\"$property != null\">\n"
            + "        #{$property,jdbcType=$type},\n"
            + "      </if>";
    private static final String _column_eq = "$column = #{$property,jdbcType=$type}";
    private static final String _column_eq_by = "$column = #{entity.$property,jdbcType=$type}";

    private static final String _update_if_test_column_value = "      <if test=\"$property != null\">\n"
            + "        $column = #{$property,jdbcType=$type}\n"
            + "      </if>";
    private static final String _update_by_if_test_column_value = "      <if test=\"entity.$property != null\">\n"
            + "        $column = #{entity.$property,jdbcType=$type}\n"
            + "      </if>";

    static {
        try {
            String filename = "org/dreamcat/cli/generator/mybatis/mapper.xml";
            _all = ClassPathUtil.getResourceAsString(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
