package org.dreamcat.code.generator.mybatis.template;

import org.dreamcat.code.generator.base.ColumnDef;
import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.code.generator.mybatis.MyBatisGeneratorConfig.TableConfig;
import org.dreamcat.code.generator.mybatis.MybatisTemplateOutput;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.MapUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
public class SqlMapperTemplate extends MybatisTemplateOutput {

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
    public String insert_column_list;
    public String base_column_list; // column_list without blob columns

    public String insert_generated_key = "";
    /**
     * {@code #{item.$property,jdbcType=$type}, }
     */
    public String insert_column_value_list;
    public String batch_insert_column_value_list;
    public String on_duplicate_key_update;
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

    // blob
    public String result_map_with_blobs = "";
    public String blob_column_list = "";
    public String select_with_blobs = "";

    // extends
    public String extends_mapper_package;
    public String extends_mapper_name;
    public String extends_result_map_with_blobs = "";
    public String extends_blob_column_list = "";

    // functions
    private final UnaryOperator<String> propertyFormatter;
    private final UnaryOperator<String> sqlNameFormatter;

    public SqlMapperTemplate(TableDef table, MyBatisGeneratorConfig config) {
        String tableName = table.getTableName();
        this.propertyFormatter = c -> config.formatPropertyName(c, tableName);
        this.sqlNameFormatter = config::formatSqlName;

        this.entity_package = config.getEntityPackageName();
        this.entity_name = config.formatEntityName(tableName);
        this.mapper_package = config.getMapperPackageName();
        this.mapper_name = config.formatMapperName(tableName);
        this.result_map = entity_package + "." + entity_name;

        this.result_map_id_list = table.getPrimaryKeyColumns().stream()
                .map(it -> formatResultMapColumn(it, "id"))
                .collect(Collectors.joining("\n"));
        this.result_map_result_list = table.getNotPrimaryKeyColumns().stream()
                .map(it -> formatResultMapColumn(it, "result"))
                .collect(Collectors.joining("\n"));

        this.table_name = config.formatSqlName(tableName);
        this.base_column_list = table.getAllColumns().values().stream()
                .map(c -> config.formatSqlName(c.getColumnName()))
                .collect(Collectors.joining(", "));

        // generated key is only supported for one primary key
        if (config.isEnableGeneratedKeys() && table.getPrimaryKeyColumns().size() == 1) {
            ColumnDef columnDef = table.getPrimaryKeyColumns().get(0);
            boolean generatedKey = config.isTableGeneratedKeys(tableName);
            if (generatedKey || columnDef.isAutoIncrement()) {
                String column = columnDef.getColumnName();
                String property = this.propertyFormatter.apply(column);
                this.insert_generated_key = InterpolationUtil.format(_insert_generated_key,
                        "property", property,
                        "column", column);
            }
        }

        this.insert_column_list = getAllInsertColumns(table, config)
                .map(c -> config.formatSqlName(c.getColumnName()))
                .collect(Collectors.joining(", "));
        this.insert_column_value_list = getAllInsertColumns(table, config)
                .map(this::formatInsertColumnValue)
                .collect(Collectors.joining(", "));
        this.batch_insert_column_value_list = getAllInsertColumns(table, config)
                .map(this::formatBatchInsertColumnValue)
                .collect(Collectors.joining(", "));
        List<String> uniqueKeyColumns = config.getUniqueKeyColumns(tableName);
        if (uniqueKeyColumns != null) {
            String duplicate_key_update_valus = getAllInsertColumns(table, config)
                    .filter(column -> !uniqueKeyColumns.contains(column.getColumnName()))
                    .map(column -> InterpolationUtil.format(
                            _duplicate_key_update, "column", column.getColumnName()))
                    .collect(Collectors.joining(",\n"));
            this.on_duplicate_key_update = InterpolationUtil.format(_on_duplicate_key_update,
                    "table_name", table_name,
                    "insert_column_list", insert_column_list,
                    "insert_column_value_list", insert_column_value_list,
                    "duplicate_key_update_valus", duplicate_key_update_valus);
        }

        this.if_test_column_list = table.getColumns().values().stream()
                .map(this::formatIfTestColumn)
                .collect(Collectors.joining("\n"));
        this.if_test_column_value_list = table.getColumns().values().stream()
                .map(this::formatIfTestColumnValue)
                .collect(Collectors.joining("\n"));

        this.primary_key_eq_list = table.getPrimaryKeyColumns().stream()
                .map(this::formatColumnEq)
                .collect(Collectors.joining("\n    and "));

        this.update_column_value_list = table.getNotPrimaryKeyColumns().stream()
                .map(this::formatColumnEq)
                .collect(Collectors.joining("\n    , "));
        this.update_by_column_value_list = table.getNotPrimaryKeyColumns().stream()
                .map(this::formatColumnEqBy)
                .collect(Collectors.joining("\n    , "));

        this.update_if_test_column_value_list = table.getNotPrimaryKeyColumns().stream()
                .map(this::formatUpdateIfTestColumnValue)
                .collect(Collectors.joining("\n"));
        this.update_by_if_test_column_value_list = table.getNotPrimaryKeyColumns().stream()
                .map(this::formatUpdateByIfTestColumnValue)
                .collect(Collectors.joining("\n"));

        boolean needBlob = config.isEnableResultMapWithBLOBs() && table.hasBlobColumns();
        if (needBlob) {
            String result_map_blob_list = table.getBlobColumns().stream()
                    .map(it -> formatResultMapColumn(it, "result"))
                    .collect(Collectors.joining("\n"));
            this.result_map_with_blobs = InterpolationUtil.format(_result_map_with_blobs,
                    "result_map", result_map, "result_map_blob_list", result_map_blob_list);

            this.result_map_result_list = table.getNotPrimaryKeyColumns().stream()
                    .filter(c -> !c.isBlob())
                    .map(it -> formatResultMapColumn(it, "result"))
                    .collect(Collectors.joining("\n"));

            this.base_column_list = table.getBaseColumns().stream()
                    .map(c -> config.formatSqlName(c.getColumnName()))
                    .collect(Collectors.joining(", "));

            this.blob_column_list = InterpolationUtil.format(_blob_column_list, "blob_column_list",
                    table.getBlobColumns().stream()
                            .map(c -> config.formatSqlName(c.getColumnName()))
                            .collect(Collectors.joining(", ")));

            this.select_with_blobs = InterpolationUtil.format(
                    _select_with_blobs, "primary_key_eq_list", primary_key_eq_list, "table_name", table_name);
        }

        if (config.isEnableExtendsMapper()) {
            this.extends_mapper_package = config.getExtendsMapperPackageName();
            this.extends_mapper_name = config.formatExtendsMapperName(tableName);
            if (needBlob) {
                this.extends_result_map_with_blobs = InterpolationUtil.format(_extends_result_map_with_blobs,
                        "result_map", result_map,
                        "mapper_package", mapper_package, "mapper_name", mapper_name);
                this.extends_blob_column_list = InterpolationUtil.format(_extends_blob_column_list,
                        "mapper_package", mapper_package, "mapper_name", mapper_name);
            }
        }
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    public void writeSub(File outputDir, String name, boolean overwrite) throws IOException {
        write(_all_sub, outputDir, name, overwrite);
    }

    private String formatResultMapColumn(ColumnDef column, String element) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_result_map_column, MapUtil.of(
                "element", element,
                "column", column.getColumnName(),
                "type", column.getType(),
                "property", property));
    }

    private String formatInsertColumnValue(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_insert_column_value, MapUtil.of(
                "property", property,
                "type", column.getType()));
    }

    private String formatBatchInsertColumnValue(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_batch_insert_column_value, MapUtil.of(
                "property", property,
                "type", column.getType()));
    }

    private String formatIfTestColumn(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        String columnSqlName = this.sqlNameFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_if_test_column, MapUtil.of(
                "property", property,
                "column", columnSqlName));
    }

    private String formatIfTestColumnValue(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_if_test_column_value, MapUtil.of(
                "property", property,
                "type", column.getType()));
    }

    private String formatColumnEq(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        String columnSqlName = this.sqlNameFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_column_eq, MapUtil.of(
                "column", columnSqlName,
                "property", property,
                "type", column.getType()));
    }

    private String formatColumnEqBy(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        String columnSqlName = this.sqlNameFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_column_eq_by, MapUtil.of(
                "column", columnSqlName,
                "property", property,
                "type", column.getType()));
    }

    private String formatUpdateIfTestColumnValue(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        String columnSqlName = this.sqlNameFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_update_if_test_column_value, MapUtil.of(
                "column", columnSqlName,
                "property", property,
                "type", column.getType()));
    }

    private String formatUpdateByIfTestColumnValue(ColumnDef column) {
        String property = this.propertyFormatter.apply(column.getColumnName());
        String columnSqlName = this.sqlNameFormatter.apply(column.getColumnName());
        return InterpolationUtil.format(_update_by_if_test_column_value, MapUtil.of(
                "column", columnSqlName,
                "property", property,
                "type", column.getType()));
    }

    private static Stream<ColumnDef> getAllInsertColumns(TableDef table, MyBatisGeneratorConfig config) {
        String tableName = table.getTableName();
        return table.getAllColumns().values().stream()
                .filter(column -> !config.isNoInsertColumn(column.getColumnName(), tableName));
    }

    static final String _result_map_column = "    <$element column=\"$column\" jdbcType=\"$type\" "
            + "property=\"$property\"/>";
    static final String _insert_column_value = "#{$property,jdbcType=$type}";
    static final String _batch_insert_column_value = "#{item.$property,jdbcType=$type}";
    static final String _insert_generated_key = "\n    useGeneratedKeys=\"true\" keyProperty=\"$property\" keyColumn=\"$column\"";
    static final String _duplicate_key_update = "      $column = values($column)";

    static final String _if_test_column = "      <if test=\"$property != null\">\n"
            + "        $column,\n"
            + "      </if>";
    static final String _if_test_column_value = "      <if test=\"$property != null\">\n"
            + "        #{$property,jdbcType=$type},\n"
            + "      </if>";
    static final String _column_eq = "$column = #{$property,jdbcType=$type}";
    static final String _column_eq_by = "$column = #{entity.$property,jdbcType=$type}";

    static final String _update_if_test_column_value = "      <if test=\"$property != null\">\n"
            + "        $column = #{$property,jdbcType=$type},\n"
            + "      </if>";
    static final String _update_by_if_test_column_value = "      <if test=\"entity.$property != null\">\n"
            + "        $column = #{entity.$property,jdbcType=$type},\n"
            + "      </if>";

    static final String _result_map_with_blobs =
            "\n  <resultMap id=\"ResultMapWithBLOBs\" type=\"$result_map\" extends=\"BaseResultMap\">\n"
                    + "$result_map_blob_list\n"
                    + "  </resultMap>\n";

    static final String _blob_column_list = "\n  <sql id=\"blob_column_list\">\n"
            + "    $blob_column_list\n"
            + "  </sql>\n";

    static final String _extends_result_map_with_blobs = "\n  <resultMap id=\"ResultMapWithBLOBs\" "
            + "type=\"$result_map\" extends=\"$mapper_package.$mapper_name.ResultMapWithBLOBs\"/>\n";

    static final String _extends_blob_column_list = "\n  <sql id=\"blob_column_list\">\n"
            + "    <include refid=\"$mapper_package.$mapper_name.blob_column_list\"/>\n"
            + "  </sql>\n";

    static final String _all;
    static final String _all_sub;
    static final String _on_duplicate_key_update;
    static final String _select_by_primary_key_with_blobs;
    static final String _select_with_blobs;

    static {
        _all = getResourceAsString("mapper.xml");
        _all_sub = getResourceAsString("extends_mapper.xml");
        _on_duplicate_key_update = getResourceAsString("onDuplicateKeyUpdate.txt");
        _select_by_primary_key_with_blobs = getResourceAsString("selectByPrimaryKeyWithBLOBs.txt");
        _select_with_blobs = getResourceAsString("selectWithBLOBs.txt");
    }
}
