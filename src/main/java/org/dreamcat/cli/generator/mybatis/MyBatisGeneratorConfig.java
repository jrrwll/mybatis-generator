package org.dreamcat.cli.generator.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Jerry Will
 * @version 2021-12-07
 */
@Data
@JsonInclude(Include.NON_EMPTY)
public class MyBatisGeneratorConfig {

    private static final String default_mapper_package_name = "com.example.mapper.base";
    private static final String default_extends_mapper_package_name = "com.example.mapper";
    private static final String default_mapper_name = "${prefix}${name}${suffix}BaseMapper";
    private static final String default_extends_mapper_name = "${prefix}${name}${suffix}Mapper";

    private boolean overwrite;
    private String srcDir; // location of mapper.java and entity.java
    private String sqlMapperDir; // location of mapper.xml, null means mapperPackageName
    private String extendsSqlMapperDir; // location of extends mapper.xml, null means extendsMapperPackageName
    private String entityPackageName = "com.example.entity";
    @Getter(AccessLevel.NONE)
    private String mapperPackageName;
    @Getter(AccessLevel.NONE)
    private String extendsMapperPackageName;
    @Getter(AccessLevel.NONE)
    private String conditionPackageName; // default is entityPackageName + ".condition";

    private Set<String> ignoreColumns = new HashSet<>();
    private boolean forceInt = true; // force use int for tinyint and smallint
    private boolean forceDecimal; // force use BigDecimal for float numbers
    private boolean enableResultMapWithBLOBs;
    private boolean enableExtendsMapper; // gen Mapper extends BaseMapper
    private boolean addMapperAnnotation; // @Mapper
    private boolean enableLombok = true; // @Data
    private boolean addComments = false;
    private Character delimitKeyword; // ` or "

    @JsonIgnore
    private UnaryOperator<String> nameWrapper = StringUtil::toCapitalCamelCase;
    @JsonIgnore
    private UnaryOperator<String> propertyNameWrapper = StringUtil::toCamelCase;
    private String namePrefix = "";
    private String nameSuffix = "";
    private String entityName = "${prefix}${name}${suffix}";
    @Getter(AccessLevel.NONE)
    private String mapperName;
    private String extendsMapperName = default_extends_mapper_name;
    private String conditionName = "${prefix}${name}${suffix}Condition";
    private String propertyName = "${name}";
    private Map<String, TableConfig> tableConfigs = new HashMap<>();

    // statements which need be pruned
    private List<StatementType> prunedStatements = new ArrayList<>();

    public String getMapperPackageName() {
        if (mapperPackageName != null) return mapperPackageName;
        if (enableExtendsMapper) {
            return default_mapper_package_name;
        } else {
            return default_extends_mapper_package_name;
        }
    }

    public String getExtendsMapperPackageName() {
        if (extendsMapperPackageName != null) return extendsMapperPackageName;
        if (mapperPackageName != null) return mapperPackageName;
        return default_extends_mapper_package_name;
    }

    public String getConditionPackageName() {
        if (conditionPackageName != null) return conditionPackageName;
        return entityPackageName + ".condition";
    }

    public String getMapperName() {
        if (mapperName != null) return mapperName;
        if (enableExtendsMapper) {
            return default_mapper_name;
        } else {
            return default_extends_mapper_name;
        }
    }

    public String formatEntityName(String tableName) {
        return formatTableName(tableName, entityName, TableConfig::getEntityName);
    }

    public String formatMapperName(String tableName) {

        return formatTableName(tableName, getMapperName(), TableConfig::getMapperName);
    }

    public String formatExtendsMapperName(String tableName) {
        return formatTableName(tableName, extendsMapperName, TableConfig::getExtendsMapperName);
    }

    public String formatConditionName(String tableName) {
        return formatTableName(tableName, conditionName, TableConfig::getConditionName);
    }

    private String formatTableName(String tableName, String nameFormat, Function<TableConfig, String> nameGetter) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            String name = nameGetter.apply(tableConfig);
            if (name != null) {
                return name;
            }
        }
        String name = nameWrapper.apply(tableName);
        return InterpolationUtil.format(nameFormat, "name", name,
                "prefix", namePrefix,
                "suffix", nameSuffix);
    }

    public String formatPropertyName(String columnName, String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            String name = tableConfig.getPropertyNames().get(columnName);
            if (name != null) {
                return name;
            }
        }
        String name = propertyNameWrapper.apply(columnName);
        return InterpolationUtil.format(propertyName, "name", name);
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class TableConfig {

        private String entityName;
        private String mapperName;
        private String extendsMapperName;
        private String conditionName;

        private Map<String, String> propertyNames = new HashMap<>();
    }

    public enum StatementType {
        insert,
        insertSelective,
        batchInsert,
        deleteByPrimaryKey,
        delete,
        selectByPrimaryKey,
        select,
        count,
        updateByPrimaryKey,
        updateByPrimaryKeySelective,
        update,
        updateSelective,
    }
}
