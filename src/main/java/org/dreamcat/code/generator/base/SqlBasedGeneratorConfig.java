package org.dreamcat.code.generator.base;

import lombok.Getter;
import lombok.Setter;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.StringUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Jerry Will
 * @version 2025-09-26
 */
@Getter
@Setter
public class SqlBasedGeneratorConfig {

    private boolean overwrite;

    private boolean addComments = false;
    private Set<String> ignoreColumns = new HashSet<>();

    private boolean forceInt = true; // force use int for tinyint and smallint
    private boolean forceDecimal; // force use BigDecimal for float numbers
    private boolean tinyint1AsBool = true; // treat tinyint(1) as boolean

    private String nameRegex = "(.*)";
    private String nameReplacement = "$1";

    private String propertyNameRegex = "(.*)";
    private String propertyNameReplacement = "$1";

    protected UnaryOperator<String> getNameWrapper() {
        return StringUtil::toCapitalCamelCase;
    }

    protected <C> String formatName(String tableName,
            Function<String, C> configGetter,
            Function<C, String> nameGetter) {
        return formatName(tableName, configGetter, nameGetter, getNameWrapper());
    }

    protected <C> String formatName(
            String tableName, Function<String, C> configGetter,
            Function<C, String> nameGetter, UnaryOperator<String> wrapper) {
        C config = configGetter.apply(tableName);
        if (config != null) {
            String name = nameGetter.apply(config);
            if (ObjectUtil.isNotBlank(name)) {
                return name;
            }
        }

        String name = tableName.replaceAll(nameRegex, nameReplacement);
        return wrapper.apply(name);
    }

    protected <C> String formatPropertyName(
            String columnName, String tableName, Function<String, C> configGetter,
            Function<C, String> nameGetter, UnaryOperator<String> wrapper) {
        C config = configGetter.apply(tableName);
        if (config != null) {
            String name = nameGetter.apply(config);
            if (ObjectUtil.isNotBlank(name)) {
                return name;
            }
        }

        String name = columnName.replaceAll(propertyNameRegex, propertyNameReplacement);
        return wrapper.apply(name);
    }
}
