package org.dreamcat.code.generator.mybatis.template;

import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.code.generator.mybatis.MybatisTemplateOutput;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.MapUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2021-12-08
 */
public class JavaMapperTemplate extends MybatisTemplateOutput {

    public String entity_package;
    public String mapper_package;
    public String condition_type;

    public String entity_type;
    public String mapper_type;
    public String primary_key_declare_list;

    public String import_package = "";
    public String at_annotation = "";

    // blobs
    public String select_by_primary_key_with_blobs = "";
    public String select_with_blobs = "";

    // extends
    public String extends_mapper_package;
    public String extends_mapper_type;
    public String extends_import_package = "";
    public String extends_at_annotation = "";

    public JavaMapperTemplate(TableDef table, MyBatisGeneratorConfig config) {
        String tableName = table.getTableName();

        this.entity_package = config.getEntityPackageName();
        this.mapper_package = config.getMapperPackageName();
        this.condition_type = config.formatConditionName(tableName);
        this.entity_type = config.formatEntityName(tableName);
        this.mapper_type = config.formatMapperName(tableName);

        this.primary_key_declare_list = table.getPrimaryKeyColumns().stream().map(c -> {
            String javaName = c.getJavaName();
            if (javaName.startsWith("java.lang") || javaName.startsWith("java.util")) {
                javaName = c.getJavaSimpleName();
            }
            String property = config.formatPropertyName(c.getColumnName(), tableName);
            return formatPrimaryKeyDeclare(property, javaName);
        }).collect(Collectors.joining(", "));

        if (config.isEnableResultMapWithBLOBs() && table.hasBlobColumns()) {
            this.select_by_primary_key_with_blobs = InterpolationUtil.format(_select_by_primary_key_with_blobs,
                    "entity_type", entity_type, "primary_key_declare_list", primary_key_declare_list);
            this.select_with_blobs = InterpolationUtil.format(_select_with_blobs,
                    "entity_type", entity_type, "condition_type", condition_type);
        }

        if (config.isEnableExtendsMapper()) {
            this.extends_mapper_package = config.getExtendsMapperPackageName();
            this.extends_mapper_type = config.formatExtendsMapperName(tableName);
        }

        List<String> imports = new ArrayList<>();
        List<String> extends_imports = new ArrayList<>();
        if (!mapper_package.equals(config.getConditionPackageName())) {
            imports.add(config.getConditionPackageName() + "." + condition_type);
        }
        if (config.isEnableExtendsMapper()) {
            if (!mapper_package.equals(extends_mapper_package)) {
                String fullMapperType = mapper_package + "." + mapper_type;
                extends_imports.add(fullMapperType);
            }
        }
        if (config.isAddMapperAnnotation()) {
            if (config.isEnableExtendsMapper()) {
                this.extends_at_annotation = "\n@Mapper";
                extends_imports.add("org.apache.ibatis.annotations.Mapper");
            } else {
                this.at_annotation = "\n@Mapper";
                imports.add("org.apache.ibatis.annotations.Mapper");
            }
        }

        if (!imports.isEmpty()) {
            this.import_package = imports.stream()
                    .map(s -> "import " + s + ";\n")
                    .collect(Collectors.joining());
        }
        if (!extends_imports.isEmpty()) {
            this.extends_import_package = "\n" + extends_imports.stream()
                    .map(s -> "import " + s + ";\n")
                    .collect(Collectors.joining());
        }
    }

    @Override
    public String getTemplate() {
        return _all;
    }

    public void writeSub(File outputDir, String name, boolean overwrite) throws IOException {
        write(_all_sub, outputDir, name, overwrite);
    }

    private static String formatPrimaryKeyDeclare(String property, String type) {
        return InterpolationUtil.format(_primary_key_declare, MapUtil.of(
                "property", property, "type", type));
    }

    static final String _primary_key_declare = "@Param(\"$property\") $type $property";
    static final String _select_by_primary_key_with_blobs =
            "\n    $entity_type selectByPrimaryKeyWithBLOBs($primary_key_declare_list);\n";
    static final String _select_with_blobs =
            "\n    List<$entity_type> selectWithBLOBs(@Param(\"condition\") $condition_type condition);\n";

    static final String _all;
    static final String _all_sub;

    static {
        _all = getResourceAsString("Mapper.java");
        _all_sub = getResourceAsString("ExtendsMapper.java");
    }
}
