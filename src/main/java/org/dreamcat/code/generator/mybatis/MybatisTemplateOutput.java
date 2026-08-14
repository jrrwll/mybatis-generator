package org.dreamcat.code.generator.mybatis;

import lombok.SneakyThrows;
import org.dreamcat.code.generator.base.TemplateOutput;
import org.dreamcat.common.util.ClassLoaderUtil;

/**
 * @author Jerry Will
 * @version 2025-09-23
 */
public abstract class MybatisTemplateOutput extends TemplateOutput {

    @Override
    public String getGeneratorName() {
        return "Mybatis Generator";
    }

    @SneakyThrows
    protected static String getResourceAsString(String name) {
        String filename = "org/dreamcat/code/generator/mybatis/" + name;
        return ClassLoaderUtil.getResourceAsString(filename);
    }
}
