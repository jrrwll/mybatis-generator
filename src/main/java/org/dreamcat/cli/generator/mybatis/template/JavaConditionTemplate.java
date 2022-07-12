package org.dreamcat.cli.generator.mybatis.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.java.EntityColumnDef;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.text.DollarInterpolation;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.StringUtil;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
public class JavaConditionTemplate extends TemplateOutput {

    public String mapper_package;
    public String condition_type;
    /**
     * $comment
     */
    // $column_upper("$column"),
    public String column_enum_list;
    public String criteria_method_list;

    public JavaConditionTemplate(EntityDef entity, MyBatisGeneratorConfig config) {
        this.mapper_package = config.getMapperPackageName();
        this.condition_type = entity.getConditionName();

        List<String> columnEnumList = new ArrayList<>();
        List<String> criteriaMethodList = new ArrayList<>();
        for (EntityColumnDef c : entity.getColumns().values()) {
            columnEnumList.add(DollarInterpolation.format(_column_enum, MapUtil.of(
                    "column", c.getName(),
                    "column_upper", c.getName().toUpperCase())));
            if (c.getJavaType().equals(String.class)) {
                criteriaMethodList.add(DollarInterpolation.format(_criteria_method_str, MapUtil.of(
                        "property", c.getProperty(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                        "column", c.getName())));
            } else {
                criteriaMethodList.add(DollarInterpolation.format(_criteria_method, MapUtil.of(
                        "property", c.getProperty(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                        "column", c.getName(),
                        "type", "String")));
                criteriaMethodList.add(DollarInterpolation.format(_criteria_method_list, MapUtil.of(
                        "property", c.getProperty(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                        "column", c.getName(),
                        "suffix", "Str",
                        "type", "String")));
            }

            Map<String, String> m = MapUtil.of(
                    "property", c.getProperty(),
                    "property_capital", StringUtil.toCapitalCase(c.getProperty()),
                    "column", c.getName(),
                    "suffix", "",
                    "type", c.getJavaSimpleName());
            criteriaMethodList.add(DollarInterpolation.format(_criteria_method, m));
            criteriaMethodList.add(DollarInterpolation.format(_criteria_method_list, m));
            criteriaMethodList.add(DollarInterpolation.format(_criteria_method_void, m));
        }
        this.column_enum_list = String.join("\n", columnEnumList);
        this.criteria_method_list = String.join("\n", criteriaMethodList);

    }

    @Override
    public String getTemplate() {
        return _all;
    }

    private static final String _all;
    private static final String _criteria_method;
    private static final String _criteria_method_list;
    private static final String _criteria_method_str;
    private static final String _criteria_method_void;
    private static final String _column_enum = "        $column_upper(\"$column\"),";

    static {
        try {
            _all = ClassPathUtil.getResourceAsString(
                    "org/dreamcat/cli/generator/mybatis/Condition.java");
            _criteria_method = ClassPathUtil.getResourceAsString(
                    "org/dreamcat/cli/generator/mybatis/criteria_method.txt");
            _criteria_method_list = ClassPathUtil.getResourceAsString(
                    "org/dreamcat/cli/generator/mybatis/criteria_method_list.txt");
            _criteria_method_str = ClassPathUtil.getResourceAsString(
                    "org/dreamcat/cli/generator/mybatis/criteria_method_str.txt");
            _criteria_method_void = ClassPathUtil.getResourceAsString(
                    "org/dreamcat/cli/generator/mybatis/criteria_method_void.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
