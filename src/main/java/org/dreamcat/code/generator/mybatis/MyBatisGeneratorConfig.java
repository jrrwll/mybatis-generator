package org.dreamcat.code.generator.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.code.generator.base.SqlBasedGeneratorConfig;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author Jerry Will
 * @version 2021-12-07
 */
@Getter
@Setter
@JsonInclude(Include.NON_EMPTY)
public class MyBatisGeneratorConfig extends SqlBasedGeneratorConfig {

    private static final String default_mapper_package_name = "com.example.mapper.base";
    private static final String default_extends_mapper_package_name = "com.example.mapper";

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

    private boolean enableResultMapWithBLOBs;
    private boolean enableExtendsMapper; // gen Mapper extends BaseMapper
    private boolean addMapperAnnotation; // @Mapper
    private boolean enableGeneratedKeys = true; // use generatedKeys
    private boolean enableLombok = true; // @Data
    private Character delimitKeyword; // ` or "

    @JsonIgnore
    private UnaryOperator<String> nameWrapper = StringUtil::toCapitalCamelCase;

    private String entityNamePrefix = "";
    private String entityNameSuffix = "";
    private String mapperNamePrefix;
    private String mapperNameSuffix; // BaseMapper
    private String extendsMapperNamePrefix = "";
    private String extendsMapperNameSuffix = "Mapper";
    private String conditionNamePrefix = "";
    private String conditionNameSuffix = "Condition";

    private Map<String, TableConfig> tableConfigs = new HashMap<>();

    // statements which need be pruned
    private List<StatementType> prunedStatements = new ArrayList<>();
    // only works on all `insert` methods without `selective`
    private List<String> noInsertColumns;

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

    public String formatEntityName(String tableName) {
        String name = formatName(tableName, tableConfigs::get, TableConfig::getEntityName);
        return entityNamePrefix + name + entityNameSuffix;
    }

    public String formatMapperName(String tableName) {
        String name = formatName(tableName, tableConfigs::get, TableConfig::getMapperName);
        if (ObjectUtil.isNotBlank(mapperNamePrefix) || ObjectUtil.isNotBlank(mapperNameSuffix)) {
            return mapperNamePrefix + name + mapperNameSuffix;
        } else if (enableExtendsMapper) {
            return name + "BaseMapper";
        } else {
            return name + "Mapper";
        }
    }

    public String formatExtendsMapperName(String tableName) {
        String name = formatName(tableName, tableConfigs::get, TableConfig::getExtendsMapperName);
        return extendsMapperNamePrefix + name + extendsMapperNameSuffix;
    }

    public String formatConditionName(String tableName) {
        String name = formatName(tableName, tableConfigs::get, TableConfig::getConditionName);
        return conditionNamePrefix + name + conditionNameSuffix;
    }

    public String formatSqlName(String tableOrColumnName) {
        if (delimitKeyword == null) {
            return tableOrColumnName;
        }
        return delimitKeyword + tableOrColumnName + delimitKeyword;
    }

    public String formatPropertyName(String columnName, String tableName) {
        return formatPropertyName(columnName, tableName, tableConfigs::get,
                c -> c.getPropertyNames().get(columnName),
                StringUtil::toCamelCase);
    }

    public boolean isNoInsertColumn(String columnName, String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig == null || ObjectUtil.isEmpty(tableConfig.getNoInsertColumns())) return false;
        return tableConfig.getNoInsertColumns().contains(columnName);
    }

    public List<String> getUniqueKeyColumns(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig == null) return null;
        List<String> uniqueKeyColumns = tableConfig.getUniqueKeyColumns();
        if (ObjectUtil.isEmpty(uniqueKeyColumns)) return null;
        return uniqueKeyColumns;
    }

    public boolean isTableGeneratedKeys(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        return tableConfig != null && tableConfig.isGeneratedKeys();
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class TableConfig {

        private String entityName;
        private String mapperName;
        private String extendsMapperName;
        private String conditionName;

        private boolean generatedKeys;
        private List<String> uniqueKeyColumns;
        private List<String> noInsertColumns;

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
