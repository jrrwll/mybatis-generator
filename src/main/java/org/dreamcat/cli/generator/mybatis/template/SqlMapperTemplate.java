package org.dreamcat.cli.generator.mybatis.template;

import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.java.EntityColumnDef;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.MapUtil;

import java.util.stream.Collectors;

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
    public String base_column_list; // column_list without blob columns

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

    // blob
    public String result_map_with_blobs = "";
    public String blob_column_list = "";
    public String select_by_primary_key_with_blobs = "";
    public String select_with_blobs = "";

    // extends
    public String extends_mapper_package;
    public String extends_mapper_name;
    public String extends_result_map_with_blobs = "";
    public String extends_blob_column_list = "";

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

        this.table_name = entity.getTableSqlName();
        this.base_column_list = this.column_list = entity.getEntityColumns().values().stream()
                .map(EntityColumnDef::getSqlName).collect(Collectors.joining(", "));

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

        boolean needBlob = config.isEnableResultMapWithBLOBs() && entity.hasBlobColumns();
        if (needBlob) {
            String result_map_blob_list = entity.getBlobColumns().stream()
                    .map(it -> formatResultMapColumn(it, "result"))
                    .collect(Collectors.joining("\n"));
            this.result_map_with_blobs = InterpolationUtil.format(_result_map_with_blobs,
                    "result_map", result_map, "result_map_blob_list", result_map_blob_list);
            this.base_column_list = entity.getBaseColumns().stream()
                    .map(EntityColumnDef::getSqlName).collect(Collectors.joining(", "));

            this.blob_column_list = InterpolationUtil.format(_blob_column_list, "blob_column_list",
                    entity.getBlobColumns().stream()
                            .map(EntityColumnDef::getSqlName).collect(Collectors.joining(", ")));

            this.select_by_primary_key_with_blobs = InterpolationUtil.format(_select_by_primary_key_with_blobs,
                    "primary_key_eq_list", primary_key_eq_list);
            this.select_with_blobs = _select_with_blobs;
        }

        if (config.isEnableExtendsMapper()) {
            this.extends_mapper_package = config.getExtendsMapperPackageName();
            this.extends_mapper_name = entity.getExtendsMapperName();
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

    @Override
    public String getExtendsTemplate() {
        return _all_sub;
    }

    private static String formatResultMapColumn(EntityColumnDef column, String element) {
        return InterpolationUtil.format(_result_map_column, MapUtil.of(
                "element", element,
                "column", column.getName(),
                "type", column.getType(),
                "property", column.getProperty()));
    }

    private static String formatInsertColumnValue(EntityColumnDef column) {
        return InterpolationUtil.format(_insert_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatBatchInsertColumnValue(EntityColumnDef column) {
        return InterpolationUtil.format(_batch_insert_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatIfTestColumn(EntityColumnDef column) {
        return InterpolationUtil.format(_if_test_column, MapUtil.of(
                "property", column.getProperty(),
                "column", column.getSqlName()));
    }

    private static String formatIfTestColumnValue(EntityColumnDef column) {
        return InterpolationUtil.format(_if_test_column_value, MapUtil.of(
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatColumnEq(EntityColumnDef column) {
        return InterpolationUtil.format(_column_eq, MapUtil.of(
                "column", column.getSqlName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatColumnEqBy(EntityColumnDef column) {
        return InterpolationUtil.format(_column_eq_by, MapUtil.of(
                "column", column.getSqlName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatUpdateIfTestColumnValue(EntityColumnDef column) {
        return InterpolationUtil.format(_update_if_test_column_value, MapUtil.of(
                "column", column.getSqlName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    private static String formatUpdateByIfTestColumnValue(EntityColumnDef column) {
        return InterpolationUtil.format(_update_by_if_test_column_value, MapUtil.of(
                "column", column.getSqlName(),
                "property", column.getProperty(),
                "type", column.getType()));
    }

    static final String _result_map_column = "    <$element column=\"$column\" jdbcType=\"$type\" "
            + "property=\"$property\"/>";
    static final String _insert_column_value = "#{$property,jdbcType=$type}";
    static final String _batch_insert_column_value = "#{item.$property,jdbcType=$type}";
    static final String _if_test_column = "      <if test=\"$property != null\">\n"
            + "        $column,\n"
            + "      </if>";
    static final String _if_test_column_value = "      <if test=\"$property != null\">\n"
            + "        #{$property,jdbcType=$type},\n"
            + "      </if>";
    static final String _column_eq = "$column = #{$property,jdbcType=$type}";
    static final String _column_eq_by = "$column = #{entity.$property,jdbcType=$type}";

    static final String _update_if_test_column_value = "      <if test=\"$property != null\">\n"
            + "        $column = #{$property,jdbcType=$type}\n"
            + "      </if>";
    static final String _update_by_if_test_column_value = "      <if test=\"entity.$property != null\">\n"
            + "        $column = #{entity.$property,jdbcType=$type}\n"
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
    static final String _select_by_primary_key_with_blobs;
    static final String _select_with_blobs;

    static {
        _all = getResourceAsString("mapper.xml");
        _all_sub = getResourceAsString("extends_mapper.xml");
        _select_by_primary_key_with_blobs = getResourceAsString("selectByPrimaryKeyWithBLOBs.txt");
        _select_with_blobs = getResourceAsString("selectWithBLOBs.txt");
    }
}
