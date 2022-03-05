package org.dreamcat.cli.generator.mybatis.template;

import java.io.IOException;
import java.util.Map;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.text.DollarInterpolation;
import org.dreamcat.common.util.MapUtil;

/**
 * @author Jerry Will
 * @version 2021-12-08
 */
public class JavaMapperTemplate {

    @Override
    public String toString() {
        Map<String, String> context = MapUtil.of(
        );
        return DollarInterpolation.format(Holder.all, context);
    }

    private static class Holder {

        static final String all;

        static {
            try {
                String filename = "org/dreamcat/cli/generator/mybatis/Mapper.java";
                all = FileUtil.readAsString(filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
