package org.dreamcat.cli.generator.mybatis.template;

import java.io.IOException;
import java.util.stream.Collectors;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.text.DollarInterpolation;
import org.dreamcat.common.util.MapUtil;

/**
 * @author Jerry Will
 * @version 2021-12-08
 */
public class JavaMapperTemplate extends TemplateOutput {

    public String entity_package;
    public String mapper_package;
    public String condition_type;

    public String entity_type;
    public String mapper_type;
    public String primary_key_declare_list;

    public String import_mapper = "";
    public String at_mapper = "";

    public JavaMapperTemplate(EntityDef entity, MyBatisGeneratorConfig config) {
        this.entity_package = config.getEntityPackageName();
        this.mapper_package = config.getMapperPackageName();
        this.condition_type = entity.getConditionName();
        this.entity_type = entity.getEntityName();
        this.mapper_type = entity.getMapperName();

        this.primary_key_declare_list = entity.getPrimaryKeyColumns().stream().map(c -> {
            String javaName = c.getJavaName();
            if (javaName.startsWith("java.lang") || javaName.startsWith("java.util")) {
                javaName = c.getJavaSimpleName();
            }
            return formatPrimaryKeyDeclare(c.getProperty(), javaName);
        }).collect(Collectors.joining(", "));

        if (config.isAddMapperAnnotation()) {
            this.import_mapper = "\nimport org.apache.ibatis.annotations.Mapper;";
            this.at_mapper = "\n@Mapper";
        }
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    private static String formatPrimaryKeyDeclare(String property, String type) {
        return DollarInterpolation.format(_primary_key_declare, MapUtil.of(
                "property", property, "type", type));
    }

    static final String _primary_key_declare = "@Param(\"$property\") $type $property";
    static final String _all;

    static {
        try {
            String filename = "org/dreamcat/cli/generator/mybatis/Mapper.java";
            _all = ClassPathUtil.getResourceAsString(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
