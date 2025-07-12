package org.dreamcat.cli.generator.mybatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

/**
 * @author Jerry Will
 * @version 2024-12-26
 */
@Getter
@Setter
// @Execute()
@Mojo(name = "mybatisGenerate")
public class MybatisGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;
    @Component
    protected RepositorySystem repositorySystem;

    @Parameter
    private String sqlPath;
    @Parameter
    private String jdbcUrl;
    @Parameter
    private String jdbcUser;
    @Parameter
    private String jdbcPassword;
    @Parameter
    private String jdbcDriverClassName;

    @Parameter(defaultValue = "false")
    private Boolean overwrite;
    @Parameter(required = true)
    private String srcDir;
    @Parameter
    private String sqlMapperDir;
    @Parameter
    private String extendsSqlMapperDir;
    @Parameter
    private String entityPackageName;
    @Parameter
    private String mapperPackageName;
    @Parameter
    private String extendsMapperPackageName;
    @Parameter
    private String conditionPackageName;

    @Parameter
    private List<String> ignoreColumns;
    @Parameter(defaultValue = "true")
    private Boolean forceInt;
    @Parameter(defaultValue = "false")
    private Boolean forceDecimal;
    @Parameter(defaultValue = "false")
    private Boolean enableResultMapWithBLOBs;
    @Parameter(defaultValue = "true")
    private Boolean enableExtendsMapper;
    @Parameter(defaultValue = "false")
    private Boolean addMapperAnnotation;
    @Parameter(defaultValue = "true")
    private Boolean enableLombok;
    @Parameter(defaultValue = "true")
    private Boolean addComments;
    @Parameter
    private Character delimitKeyword;

    @Parameter
    private String namePrefix;
    @Parameter
    private String nameSuffix;
    @Parameter
    private String entityName;
    @Parameter
    private String mapperName;
    @Parameter
    private String extendsMapperName;
    @Parameter
    private String conditionName;
    @Parameter
    private String propertyName;

    @Parameter
    private List<String> prunedStatements;
    @Parameter
    private List<String> tableNames;
    @Parameter
    private List<Table> tables;

    public void execute() throws MojoExecutionException {
        new MybatisGeneratorAction(this).run();
    }

    @Data
    public static class Table {

        @Parameter
        private String name;
        @Parameter
        private String entityName;
        @Parameter
        private String mapperName;
        @Parameter
        private String extendsMapperName;
        @Parameter
        private String conditionName;
        @Parameter
        private Map<String, String> propertyNames = new HashMap<>();
    }

}
