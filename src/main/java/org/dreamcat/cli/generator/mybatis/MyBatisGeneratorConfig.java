package org.dreamcat.cli.generator.mybatis;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dreamcat.common.util.StringUtil;

/**
 * @author Jerry Will
 * @version 2021-12-07
 */
@Data
public class MyBatisGeneratorConfig {

    private String entityPackageName = "com.example.entity";
    private String mapperPackageName = "com.example.mapper";

    private Set<String> ignoreSelectColumns = new HashSet<>();
    private Set<String> ignoreInsertColumns = new HashSet<>();

    private boolean overwrite;
    private String srcDir;
    private boolean putMapperTogether;
    private String sqlMapperDir;

    private boolean addMapperAnnotation;
    private boolean useLombok = true;

    private NameConfig entityNameConfig = NameConfig.builder()
            .nameWrapper(StringUtil::toCapitalCamelCase)
            .build();

    private NameConfig mapperNameConfig = NameConfig.builder()
            .suffix("Mapper")
            .nameWrapper(StringUtil::toCapitalCamelCase)
            .build();

    private NameConfig conditionNameConfig = NameConfig.builder()
            .suffix("Condition")
            .nameWrapper(StringUtil::toCapitalCamelCase)
            .build();

    private NameConfig criteriaNameConfig = NameConfig.builder()
            .suffix("Criteria")
            .nameWrapper(StringUtil::toCapitalCamelCase)
            .build();

    private NameConfig propertyNameConfig = NameConfig.builder()
            .nameWrapper(StringUtil::toCamelCase)
            .build();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameConfig {

        @Builder.Default
        private String prefix = "";
        @Builder.Default
        private String suffix = "";
        private UnaryOperator<String> nameWrapper;

        public String format(String name) {
            return prefix + nameWrapper.apply(name) + suffix;
        }
    }
}
