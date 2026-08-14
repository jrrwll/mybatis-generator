package org.dreamcat.code.generator.fastapi_curd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.code.generator.base.SqlBasedGeneratorConfig;
import org.dreamcat.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author Jerry Will
 * @version 2025-09-24
 */
@Getter
@Setter
public class FastapiCurdGeneratorConfig extends SqlBasedGeneratorConfig {

    private String projectName;
    private String outputDir;
    // mysql+pymysql://user:password@host:port/db
    // postgresql+psycopg2://user:password@host:port/db
    // mssql+pyodbc://user:password@host:port/db
    private String sqlalchemyUri;
    private boolean preferCamelCase = false; // default snake case in json fields
    private boolean disableScript = false; // disable run script

    private String deleteColumn;
    private String orderColumn;

    @JsonIgnore
    protected UnaryOperator<String> nameWrapper = StringUtil::toSnakeCase;

    private List<String> simpleFields = new ArrayList<>();
    // columnName -> support multiple choose
    private Map<String, Boolean> listFields = new HashMap<>();
    // don't need to get or list
    private List<String> notGetFields = Collections.singletonList("deleted");
    // don't need to create or update
    private List<String> notCreateFields = Arrays.asList(
            "created_at", "updated_at", "deleted");
    private List<String> notUpdateFields = new ArrayList<>();

    private Map<String, TableConfig> tableConfigs = new HashMap<>();

    public String parseDatabaseType() {
        if (sqlalchemyUri == null) {
            return "mysql";
        }
        return sqlalchemyUri.split("\\+")[0];
    }

    public String formatFileName(String tableName) {
        return formatName(tableName, tableConfigs::get,
                TableConfig::getName);
    }

    public String formatClassName(String tableName) {
        return formatName(tableName, tableConfigs::get,
                TableConfig::getClassName, StringUtil::toCapitalCamelCase);
    }

    public String formatRouterName(String tableName) {
        UnaryOperator<String> wrapper;
        if (preferCamelCase) {
            wrapper = StringUtil::toCamelCase;
        } else {
            wrapper = StringUtil::toSnakeCase;
        }
        return formatName(tableName, tableConfigs::get,
                TableConfig::getRouterName, wrapper);
    }

    public List<String> getSimpleFields(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            return tableConfig.getSimpleFields();
        }
        return simpleFields;
    }

    public Map<String, Boolean> getListFields(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            return tableConfig.getListFields();
        }
        return listFields;
    }

    public List<String> getNotGetFields(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            return tableConfig.getNotGetFields();
        }
        return notGetFields;
    }

    public List<String> getNotCreateFields(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            return tableConfig.getNotCreateFields();
        }
        return notCreateFields;
    }

    public List<String> getNotUpdateFields(String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            return tableConfig.getNotUpdateFields();
        }
        return notUpdateFields;
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class TableConfig {

        private String name;
        private String className;
        private String routerName;

        private List<String> simpleFields;
        // columnName -> support multiple choose
        private Map<String, Boolean> listFields;
        private List<String> notGetFields;
        private List<String> notCreateFields; // don't need to create or update
        private List<String> notUpdateFields;

        private Map<String, String> fieldNames = new HashMap<>();
    }
}
