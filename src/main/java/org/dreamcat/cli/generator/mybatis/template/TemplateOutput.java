package org.dreamcat.cli.generator.mybatis.template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.text.DollarInterpolation;
import org.dreamcat.common.util.DateUtil;
import org.dreamcat.common.util.MapUtil;
import org.dreamcat.common.util.ReflectUtil;

/**
 * @author Jerry Will
 * @version 2022-07-12
 */
@Slf4j
public abstract class TemplateOutput {

    public abstract String getTemplate();

    public void write(String outputDir, String name, boolean overwrite) throws IOException {
        write(new File(outputDir), name, overwrite);
    }

    public void write(File outputDir, String name, boolean overwrite) throws IOException {
        File file = new File(outputDir, name);
        if (file.exists()) {
            if (overwrite) {
                log.warn("overwrite file {}", file);
            } else {
                log.warn("file {} already exists, skip", file);
                return;
            }
        }
        log.info("writing to {}", file.getCanonicalPath());
        try (FileWriter w = new FileWriter(file)) {
            w.write(format());
        }
    }

    public String format() {
        List<Field> fields = ReflectUtil.retrieveNoStaticFields(getClass());
        Map<String, String> context = MapUtil.of(
                "generator_name", "Mybatis-Generator",
                "username", System.getProperty("user.name"),
                "date", DateUtil.format(new Date(), "yyyy-MM-dd")
        );
        for (Field field : fields) {
            if (!field.getType().equals(String.class)) continue;
            context.put(field.getName(), (String) ReflectUtil.getValue(this, field));
        }
        return DollarInterpolation.format(getTemplate(), context);
    }
}
