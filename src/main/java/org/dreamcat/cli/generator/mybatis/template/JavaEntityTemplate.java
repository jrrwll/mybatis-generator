package org.dreamcat.cli.generator.mybatis.template;

import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.java.EntityColumnDef;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
public class JavaEntityTemplate extends TemplateOutput {

    public String entity_package;
    public String entity_type;
    public String import_package = "";
    public String import_java_package;
    public String at_annotation = "";
    /**
     * $comment
     */
    // private $type $property;
    public String property_declare_list;
    /*
    public $type get${property_capital}() {
        return $property;
    }

    public void set${property_capital}(int $property) {
        this.$property = $property;
    }
    */
    public String property_get_set_list = "";
    // + "$property=" + $property
    public String append_property_list;
    public String to_string = "";

    public JavaEntityTemplate(EntityDef entity, MyBatisGeneratorConfig config) {
        this.entity_package = config.getEntityPackageName();
        this.entity_type = entity.getEntityName();

        Set<String> java_imports = new HashSet<>();
        java_imports.add("import java.io.Serializable;");
        List<String> propertyDeclareList = new ArrayList<>();
        List<String> appendPropertyList = new ArrayList<>();
        for (EntityColumnDef c : entity.getColumns().values()) {
            String _property_declare_tmp = _property_declare;
            if (ObjectUtil.isNotBlank(c.getComment()) && config.isAddComments()) {
                _property_declare_tmp = _property_declare_with_comment;
            }
            propertyDeclareList.add(InterpolationUtil.format(_property_declare_tmp, MapUtil.of(
                    "comment", c.getComment(),
                    "type", c.getJavaSimpleName(),
                    "property", c.getProperty()), ""));
            appendPropertyList.add(InterpolationUtil.format(_append_property, MapUtil.of(
                    "property", c.getProperty())));
            if (Date.class.equals(c.getJavaType())) {
                java_imports.add("import java.util.Date;");
            } else if (BigDecimal.class.equals(c.getJavaType())) {
                java_imports.add("import java.math.BigDecimal;");
            }
        }
        this.property_declare_list = String.join("\n", propertyDeclareList);
        this.import_java_package = java_imports.stream()
                .sorted(Comparator.naturalOrder()).collect(Collectors.joining("\n"));

        if (config.isEnableLombok()) {
            this.import_package = "\nimport lombok.Getter;\n"
                    + "import lombok.Setter;\n"
                    + "import lombok.ToString;\n";
            at_annotation = "\n@Getter\n@Setter\n@ToString";
        } else {
            List<String> getSetList = new ArrayList<>();
            for (EntityColumnDef c : entity.getEntityColumns().values()) {
                getSetList.add(InterpolationUtil.format(_property_get_set, MapUtil.of(
                        "property", c.getProperty(),
                        "type", c.getJavaSimpleName(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty())
                )));
            }
            this.property_get_set_list = "\n" + String.join("\n\n", getSetList);

            this.append_property_list = "                + \"" +
                    String.join("\n                + \", ", appendPropertyList);
            this.to_string = "\n" + InterpolationUtil.format(_to_string, MapUtil.of(
                    "append_property_list", append_property_list
            ));
        }
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    static final String _property_declare = "    private $type $property;";
    static final String _property_declare_with_comment = "    /**\n"
            + "     * $comment\n"
            + "     */\n"
            + "    private $type $property;";
    static final String _property_get_set = "    public $type get${property_capital}() {\n"
            + "        return $property;\n"
            + "    }\n"
            + "\n    public void set${property_capital}($type $property) {\n"
            + "        this.$property = $property;\n"
            + "    }";
    static final String _append_property = "$property=\" + $property";

    static final String _all;
    static final String _to_string;

    static {
        _all = getResourceAsString("Entity.java");
        _to_string = getResourceAsString("entity_to_string.txt");
    }
}
