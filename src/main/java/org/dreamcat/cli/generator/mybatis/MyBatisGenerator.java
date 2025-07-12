package org.dreamcat.cli.generator.mybatis;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.cli.generator.mybatis.template.JavaConditionTemplate;
import org.dreamcat.cli.generator.mybatis.template.JavaEntityTemplate;
import org.dreamcat.cli.generator.mybatis.template.JavaMapperTemplate;
import org.dreamcat.cli.generator.mybatis.template.SqlMapperTemplate;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.util.AssertUtil;
import org.dreamcat.common.util.ObjectUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2021-11-29
 */
@Slf4j
@Getter
public class MyBatisGenerator {

    protected final MyBatisGeneratorConfig config;

    public MyBatisGenerator(MyBatisGeneratorConfig config) {
        this.config = config;

        AssertUtil.requireNotNull(config.getSrcDir(), "config.srcDir");
    }

    public void generate(String sql) throws IOException {
        List<TableCommonDef> tableDefs = InternalUtil.parseCreateTable(sql);
        generate(tableDefs);
    }

    public void generate(String jdbcUrl, String jdbcUser, String jdbcPassword) throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
            generate(connection);
        }
    }

    public void generate(Connection connection) throws SQLException, IOException {
        List<TableCommonDef> tableDefs = InternalUtil.fetchTableDefs(connection);
        generate(tableDefs);
    }

    public void generate(List<TableCommonDef> tableDefs) throws IOException {
        for (TableCommonDef tableDef : tableDefs) {
            generate(tableDef);
        }
    }

    public void generate(TableCommonDef tableDef) throws IOException {
        EntityDef entityDef = new EntityDef(tableDef, config);
        String srcDir = config.getSrcDir();
        boolean overwrite = config.isOverwrite();
        List<String> removedSqlMethods = null;
        if (ObjectUtil.isNotEmpty(config.getPrunedStatements())) {
            removedSqlMethods = config.getPrunedStatements().stream()
                    .map(Enum::name).collect(Collectors.toList());
        }

        // java-mapper
        File mapperDir = new File(srcDir, config.getMapperPackageName().replace('.', '/'));
        if (!mapperDir.exists() && !mapperDir.mkdirs()) {
            log.error("fail to create dir {} for java-mapper", mapperDir);
        } else {
            JavaMapperTemplate javaMapperTemplate = new JavaMapperTemplate(entityDef, config);

            String javaMapperName = entityDef.getMapperName() + ".java";
            File javaMapperFile = javaMapperTemplate.write(mapperDir, javaMapperName, overwrite);
            InternalUtil.pruneJavaIfNeed(javaMapperFile, removedSqlMethods);

            // java-extends-mapper
            if (config.isEnableExtendsMapper()) {
                File extendsMapperDir = new File(srcDir, config.getExtendsMapperPackageName().replace('.', '/'));
                if (!extendsMapperDir.exists() && !extendsMapperDir.mkdirs()) {
                    log.error("fail to create dir {} for java-extends-mapper", extendsMapperDir);
                } else {
                    String javaExtendsMapperName = entityDef.getExtendsMapperName() + ".java";
                    javaMapperTemplate.writeSub(extendsMapperDir, javaExtendsMapperName, overwrite);
                }
            }
        }

        // sql-mapper
        String sqlMapperName = entityDef.getMapperName() + ".xml";
        SqlMapperTemplate sqlMapperTemplate = new SqlMapperTemplate(entityDef, config);
        File sqlMapperDir;
        if (config.getSqlMapperDir() != null) {
            sqlMapperDir = new File(config.getSqlMapperDir());
        } else {
            // put mappers together
            sqlMapperDir = mapperDir;
        }
        File sqlMapperFile = sqlMapperTemplate.write(sqlMapperDir, sqlMapperName, overwrite);
        InternalUtil.pruneXmlIfNeed(sqlMapperFile, removedSqlMethods);
        // sql-extends-mapper
        if (config.isEnableExtendsMapper()) {
            File extendsSqlMapperDir;
            if (config.getExtendsSqlMapperDir() != null) {
                extendsSqlMapperDir = new File(config.getExtendsSqlMapperDir());
            } else {
                extendsSqlMapperDir = new File(srcDir, config.getExtendsMapperPackageName().replace('.', '/'));
            }
            if (!extendsSqlMapperDir.exists() && !extendsSqlMapperDir.mkdirs()) {
                log.error("fail to create dir {} for sql-extends-mapper", extendsSqlMapperDir);
            } else {
                String javaExtendsMapperName = entityDef.getExtendsMapperName() + ".xml";
                sqlMapperTemplate.writeSub(extendsSqlMapperDir, javaExtendsMapperName, overwrite);
            }
        }

        // java-entity
        File entityDir = new File(srcDir, config.getEntityPackageName().replace('.', '/'));
        if (!entityDir.exists() && !entityDir.mkdirs()) {
            log.info("fail to create dir {}", entityDir);
        } else {
            String javaEntityName = entityDef.getEntityName() + ".java";
            JavaEntityTemplate javaEntityTemplate = new JavaEntityTemplate(entityDef, config);
            javaEntityTemplate.write(entityDir, javaEntityName, overwrite);
        }

        // java-condition
        File conditionDir = new File(srcDir, config.getConditionPackageName().replace('.', '/'));
        if (!conditionDir.exists() && !conditionDir.mkdirs()) {
            log.info("fail to create dir {}", conditionDir);
        } else {
            String conditionName = entityDef.getConditionName() + ".java";
            JavaConditionTemplate javaConditionTemplate = new JavaConditionTemplate(entityDef, config);
            javaConditionTemplate.write(conditionDir, conditionName, overwrite);
        }
    }
}
