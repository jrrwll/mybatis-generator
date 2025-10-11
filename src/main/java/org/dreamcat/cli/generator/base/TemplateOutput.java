package org.dreamcat.cli.generator.base;

import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.ShellUtil;
import org.dreamcat.common.text.InterpolationUtil;
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

    public abstract String getGeneratorName();

    public String getTemplate() {
        throw new IllegalArgumentException("no default template defined");
    }

    public File writeDefault(File outputDir, String name, boolean overwrite) throws IOException {
        return write(getTemplate(), outputDir, name, overwrite);
    }

    protected File write(String template, File outputDir, String name, boolean overwrite) throws IOException {
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
        } else {
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new RuntimeException("fail to create dir `" + parentFile + "` for file " + name);
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
                "generator_name", getGeneratorName(),
                "username", System.getProperty("user.name"),
                "date", DateUtil.formatDate(new Date())
        );
        for (Field field : fields) {
            if (!field.getType().equals(String.class)) continue;
            context.put(field.getName(), (String) ReflectUtil.getFieldValue(this, field));
        }
        return context;
    }

    protected String formatComment(String comment) {
        if (comment == null) return null;
        return comment.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    // ==== ==== ==== ====    ==== ==== ==== ====    ==== ==== ==== ====

    public void runScript(String scriptName, String script, Map<String, String> env) {
        int exitCode;
        try {
            exitCode = ShellUtil.exec(env, script);
        } catch (Exception e) {
            log.error("failed to run {} script", scriptName, e);
            System.exit(1);
            return;
        }
        if (exitCode != 0) {
            throw new RuntimeException("failed to run " + scriptName + " script, exitCode=" + exitCode);
        }

        log.info("success to run {} script", scriptName);
    }
}
