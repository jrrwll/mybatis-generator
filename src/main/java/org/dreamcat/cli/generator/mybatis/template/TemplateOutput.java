package org.dreamcat.cli.generator.mybatis.template;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.ClassLoaderUtil;
import org.dreamcat.common.util.DateUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ReflectUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
@Slf4j
public abstract class TemplateOutput {

    public abstract String getTemplate();

    public String getExtendsTemplate() {
        throw new UnsupportedOperationException();
    }

    public File write(File outputDir, String name, boolean overwrite) throws IOException {
        return write(getTemplate(), outputDir, name, overwrite);
    }

    public void writeSub(File outputDir, String name, boolean overwrite) throws IOException {
        write(getExtendsTemplate(), outputDir, name, overwrite);
    }

    private File write(String template, File outputDir, String name, boolean overwrite) throws IOException {
        Map<String, String> context = createContext();
        String content = InterpolationUtil.format(template, context);

        File file = new File(outputDir, name);
        if (file.exists()) {
            if (overwrite) {
                log.warn("overwrite file {}", file);
            } else {
                log.warn("file {} already exists, skip", file);
                return null;
            }
        }
        log.info("writing to {}", file.getCanonicalPath());
        try (FileWriter w = new FileWriter(file)) {
            w.write(content);
        }
        return file;
    }

    private Map<String, String> createContext() {
        List<Field> fields = ReflectUtil.retrieveBeanFields(getClass());
        Map<String, String> context = MapUtil.of(
                "generator_name", "Mybatis-Generator",
                "username", System.getProperty("user.name"),
                "date", DateUtil.formatDate(new Date())
        );
        for (Field field : fields) {
            if (!field.getType().equals(String.class)) continue;
            context.put(field.getName(), (String) ReflectUtil.getFieldValue(this, field));
        }
        return context;
    }

    @SneakyThrows
    static String getResourceAsString(String name) {
        String filename = "org/dreamcat/cli/generator/mybatis/" + name;
        return ClassLoaderUtil.getResourceAsString(filename);
    }
}
