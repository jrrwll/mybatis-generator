package org.dreamcat.cli.generator.mybatis.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class JavaEntityTemplate extends TemplateOutput {

    public String entity_package;
    public String entity_type;
    public String import_lombok = "";
    public String at_lombok = "";
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

    public JavaEntityTemplate(EntityDef entity, MyBatisGeneratorConfig config) {
        this.entity_package = config.getEntityPackageName();
        this.entity_type = entity.getEntityName();

        if (config.isUseLombok()) {
            import_lombok = "\nimport lombok.Data;";
            at_lombok = "\n@Data";
        } else {
            List<String> getSetList = new ArrayList<>();
            for (EntityColumnDef c : entity.getColumns().values()) {
                getSetList.add(DollarInterpolation.format(_property_get_set, MapUtil.of(
                        "property", c.getProperty(),
                        "type", c.getJavaSimpleName(),
                        "property_capital", StringUtil.toCapitalCase(c.getProperty())
                )));
            }
            this.property_get_set_list = "\n\n" + String.join("\n\n", getSetList);
        }

        List<String> propertyDeclareList = new ArrayList<>();
        List<String> appendPropertyList = new ArrayList<>();
        for (EntityColumnDef c : entity.getColumns().values()) {
            propertyDeclareList.add(DollarInterpolation.format(_property_declare, MapUtil.of(
                    "comment", c.getComment(),
                    "type", c.getJavaSimpleName(),
                    "property", c.getProperty())));
            appendPropertyList.add(DollarInterpolation.format(_append_property, MapUtil.of(
                    "property", c.getProperty())));

        }
        this.property_declare_list = String.join("\n", propertyDeclareList);
        this.append_property_list = "                + \"" +
                String.join("\n                + \", ", appendPropertyList);
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    static final String _all;
    static final String _property_declare = "    /**\n"
            + "     *  $comment\n"
            + "     */\n"
            + "    private $type $property;";
    static final String _property_get_set = "    public $type get${property_capital}() {\n"
            + "        return $property;\n"
            + "    }\n"
            + "\n    public void set${property_capital}($type $property) {\n"
            + "        this.$property = $property;\n"
            + "    }";
    static final String _append_property = "$property=\" + $property";

    static {
        try {
            String filename = "org/dreamcat/cli/generator/mybatis/Entity.java";
            _all = ClassPathUtil.getResourceAsString(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
