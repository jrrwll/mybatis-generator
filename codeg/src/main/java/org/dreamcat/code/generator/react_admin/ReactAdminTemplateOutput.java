package org.dreamcat.code.generator.react_admin;

import lombok.SneakyThrows;
import org.dreamcat.code.generator.base.ColumnDef;
import org.dreamcat.code.generator.base.TemplateOutput;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.ReflectUtil;

import java.util.Date;

/**
 * @author Jerry Will
 * @version 2025-09-23
 */
public abstract class ReactAdminTemplateOutput extends TemplateOutput {

    @Override
    public String getGeneratorName() {
        return "React Admin Generator";
    }

    @SneakyThrows
    protected static String getResourceAsString(String name) {
        String filename = "org/dreamcat/code/generator/react_admin/" + name;
        return ClassLoaderUtil.getResourceAsString(filename);
    }

    protected String mappingRaInputType(ColumnDef column) {
        Class<?> type = column.getJavaType();
        if (ReflectUtil.isAssignable(Number.class, type)) {
            return "NumberInput";
        } else if (Boolean.class.equals(type)) {
            return "BooleanInput";
        } else if (Date.class.equals(type)) {
            return "DateTimeInput";
        } else {
            return "TextInput";
        }
    }

    protected String mappingRaFieldType(ColumnDef column) {
        Class<?> type = column.getJavaType();
        if (ReflectUtil.isAssignable(Number.class, type)) {
            return "NumberField";
        } else if (Boolean.class.equals(type)) {
            return "BooleanField";
        } else if (Date.class.equals(type)) {
            return "DateField";
        } else {
            return "TextField";
        }
    }
}
