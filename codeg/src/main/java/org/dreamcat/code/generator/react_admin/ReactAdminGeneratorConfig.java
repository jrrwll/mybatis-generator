package org.dreamcat.code.generator.react_admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.code.generator.base.SqlBasedGeneratorConfig;
import org.dreamcat.common.util.ObjectUtil;
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
 * @version 2025-09-23
 */
@Getter
@Setter
public class ReactAdminGeneratorConfig extends SqlBasedGeneratorConfig {

    private String projectName;
    private String viteApiUrl;
    private String outputDir;
    private boolean preferCamelCase = false; // default snake case
    private boolean enableChinese;
    private boolean disableScript = false; // disable run script

    @JsonIgnore
    private UnaryOperator<String> nameWrapper = StringUtil::toSnakeCase;

    private List<String> fixFilterFields = Collections.emptyList();
    private String titleField = "name";
    // don't need to get or list
    private List<String> notGetFields = Collections.singletonList("deleted");
    // don't need to create or update
    private List<String> notCreateFields = Arrays.asList(
            "created_at", "updated_at", "deleted");
    private List<String> notUpdateFields = new ArrayList<>();

    private Map<String, TableConfig> tableConfigs = new HashMap<>();

    public String formatFileName(String tableName) {
        return formatName(tableName, tableConfigs::get, TableConfig::getName);
    }

    public String formatResourceName(String tableName) {
        UnaryOperator<String> wrapper;
        if (preferCamelCase) {
            wrapper = StringUtil::toCamelCase;
        } else {
            wrapper = StringUtil::toSnakeCase;
        }
        return formatName(tableName, tableConfigs::get,
                TableConfig::getResourceName, wrapper);
    }

    public String formatClassName(String tableName) {
        return formatName(tableName, tableConfigs::get,
                TableConfig::getClassName, StringUtil::toCapitalCamelCase);
    }

    public String formatDisplayName(String tableName, String tableComment) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            return tableConfig.getDisplayName();
        }
        if (ObjectUtil.isNotBlank(tableComment)) {
            return tableComment;
        }
        // use class name as default
        return formatClassName(tableName);
    }

    public String formatFieldName(String columnName, String tableName) {
        UnaryOperator<String> wrapper;
        if (preferCamelCase) {
            wrapper = StringUtil::toCamelCase;
        } else {
            wrapper = StringUtil::toSnakeCase;
        }

        return formatPropertyName(columnName, tableName, tableConfigs::get, c -> {
            FieldConfig fieldConfig = c.getFields().get(columnName);
            if (fieldConfig != null) return fieldConfig.getName();
            else return null;
        }, wrapper);
    }

    public String formatFieldDisplayName(String columnName, String columnComment, String tableName) {
        TableConfig tableConfig = tableConfigs.get(tableName);
        if (tableConfig != null) {
            FieldConfig fieldConfig = tableConfig.getFields().get(columnName);
            if (fieldConfig != null && ObjectUtil.isNotBlank(fieldConfig.getDisplayName())) {
                return fieldConfig.getDisplayName();
            }
        }
        if (ObjectUtil.isNotBlank(columnComment)) {
            return columnComment;
        }
        if (preferCamelCase) {
            return StringUtil.toCamelCase(columnName);
        } else {
            return StringUtil.toSnakeCase(columnName);
        }
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
        private String resourceName;
        private String className;
        private String displayName;
        private String titleField;

        private Map<String, FieldConfig> fields = Collections.emptyMap();

        private Map<String, FilterConfig> filterFields = Collections.emptyMap();
        private List<String> fixFilterFields = Collections.emptyList();
        private List<String> notGetFields;
        private List<String> notCreateFields; // don't need to create or update
        private List<String> notUpdateFields;

        private List<Action> actions = Collections.singletonList(Action.All);
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class FieldConfig {

        private String name;
        private String displayName;
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class FilterConfig {

        private Boolean fix;
        // SelectArrayInput
        private Map<String, String> choices = new HashMap<>();
        // ReferenceArrayInput
        private String reference;
        private String referenceOptionText;
    }

    public enum Action {
        All,
        Create,
        Edit,
        List,
    }
}
