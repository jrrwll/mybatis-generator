package org.dreamcat.cli.generator.mybatis.template;

import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.java.EntityColumnDef;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
public class JavaConditionTemplate extends TemplateOutput {

    public String condition_package;
    public String condition_type;
    /**
     * $comment
     */
    // $column_upper("$column"),
    public String column_enum_list;
    public String criteria_method_list;
    public String import_package = "";

    public JavaConditionTemplate(EntityDef entity, MyBatisGeneratorConfig config) {
        this.condition_package = config.getConditionPackageName();
        this.condition_type = entity.getConditionName();

        List<String> columnEnumList = new ArrayList<>();
        List<String> criteriaMethodList = new ArrayList<>();
        for (EntityColumnDef c : entity.getColumns().values()) {
            columnEnumList.add(InterpolationUtil.format(_column_enum, MapUtil.of(
                    "column", c.getName(),
                    "column_upper", c.getName().toUpperCase())));
            if (c.getJavaType().equals(String.class)) {
                criteriaMethodList.add(InterpolationUtil.format(_criteria_method_str, MapUtil.of(
                        "property", c.getProperty(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                        "column", c.getSqlName())));
            } else {
                criteriaMethodList.add(InterpolationUtil.format(_criteria_method, MapUtil.of(
                        "property", c.getProperty(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                        "column", c.getSqlName(),
                        "type", "String")));
                criteriaMethodList.add(InterpolationUtil.format(_criteria_method_list, MapUtil.of(
                        "property", c.getProperty(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                        "column", c.getSqlName(),
                        "suffix", "Str",
                        "type", "String")));
            }

            Map<String, String> m = MapUtil.of(
                    "property", c.getProperty(),
                    "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                    "column", c.getSqlName(),
                    "suffix", "",
                    "type", c.getJavaSimpleName());
            criteriaMethodList.add(InterpolationUtil.format(_criteria_method, m));
            criteriaMethodList.add(InterpolationUtil.format(_criteria_method_list, m));
            criteriaMethodList.add(InterpolationUtil.format(_criteria_method_void, m));

            if (import_package.isEmpty() && BigDecimal.class.equals(c.getJavaType())) {
                import_package = "\nimport java.math.BigDecimal;";
            }
        }
        this.column_enum_list = String.join("\n", columnEnumList);
        this.criteria_method_list = String.join("\n", criteriaMethodList);

    }

    @Override
    public String getTemplate() {
        return _all;
    }

    private static final String _column_enum = "        $column_upper(\"$column\"),";

    private static final String _all;
    private static final String _criteria_method;
    private static final String _criteria_method_list;
    private static final String _criteria_method_str;
    private static final String _criteria_method_void;

    static {
        _all = getResourceAsString("Condition.java");
        _criteria_method = getResourceAsString("criteria_method.txt");
        _criteria_method_list = getResourceAsString("criteria_method_list.txt");
        _criteria_method_str = getResourceAsString("criteria_method_str.txt");
        _criteria_method_void = getResourceAsString("criteria_method_void.txt");
    }
}
