package org.dreamcat.code.generator.mybatis;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.code.generator.base.InternalUtil;
import org.dreamcat.code.generator.base.SqlBasedGenerator;
import org.dreamcat.code.generator.base.TableDef;
import org.dreamcat.code.generator.mybatis.MyBatisGeneratorConfig.StatementType;
import org.dreamcat.code.generator.mybatis.template.JavaConditionTemplate;
import org.dreamcat.code.generator.mybatis.template.JavaEntityTemplate;
import org.dreamcat.code.generator.mybatis.template.JavaMapperTemplate;
import org.dreamcat.code.generator.mybatis.template.SqlMapperTemplate;
import org.dreamcat.common.util.AssertUtil;
import org.dreamcat.common.util.ObjectUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2021-11-29
 */
@Slf4j
@Getter
public class MyBatisGenerator implements SqlBasedGenerator {

    protected final MyBatisGeneratorConfig config;

    public MyBatisGenerator(MyBatisGeneratorConfig config) {
        this.config = config;

        AssertUtil.requireNotNull(config.getSrcDir(), "config.srcDir");
    }

    @Override
    public void generate(TableDef table) throws IOException {
        String tableName = table.getTableName();

        String srcDir = config.getSrcDir();
        boolean overwrite = config.isOverwrite();
        List<String> removedSqlMethods = null;
        if (ObjectUtil.isNotEmpty(config.getPrunedStatements())) {
            removedSqlMethods = config.getPrunedStatements().stream()
                    .map(Enum::name).collect(Collectors.toList());
        }
        boolean noUk = config.getUniqueKeyColumns(tableName) == null;
        boolean noBlobs = !config.isEnableResultMapWithBLOBs() || !table.hasBlobColumns();

        // java-mapper
        File mapperDir = new File(srcDir, config.getMapperPackageName().replace('.', '/'));
        if (!mapperDir.exists() && !mapperDir.mkdirs()) {
            log.error("fail to create dir {} for java-mapper", mapperDir);
        } else {
            JavaMapperTemplate javaMapperTemplate = new JavaMapperTemplate(table, config);

            String javaMapperName = config.formatMapperName(tableName) + ".java";
            File javaMapperFile = javaMapperTemplate.writeDefault(mapperDir, javaMapperName, overwrite);
            InternalUtil.pruneJavaIfNeed(javaMapperFile, removedSqlMethods);
            if (noUk) {
                InternalUtil.pruneJavaIfNeed(javaMapperFile,
                        StatementType.insertOnDuplicateKeyUpdate,
                        StatementType.batchInsertOnDuplicateKeyUpdate);
            }
            if (noBlobs) {
                InternalUtil.pruneJavaIfNeed(javaMapperFile, StatementType.allWithBLOBs());
            }
            // java-extends-mapper
            if (config.isEnableExtendsMapper()) {
                File extendsMapperDir = new File(srcDir, config.getExtendsMapperPackageName().replace('.', '/'));
                if (!extendsMapperDir.exists() && !extendsMapperDir.mkdirs()) {
                    log.error("fail to create dir {} for java-extends-mapper", extendsMapperDir);
                } else {
                    String javaExtendsMapperName = config.formatExtendsMapperName(tableName) + ".java";
                    javaMapperTemplate.writeSub(extendsMapperDir, javaExtendsMapperName, overwrite);
                }
            }
        }

        // sql-mapper
        String sqlMapperName = config.formatMapperName(tableName) + ".xml";
        SqlMapperTemplate sqlMapperTemplate = new SqlMapperTemplate(table, config);
        File sqlMapperDir;
        if (config.getSqlMapperDir() != null) {
            sqlMapperDir = new File(config.getSqlMapperDir());
        } else {
            // put mappers together
            sqlMapperDir = mapperDir;
        }
        File sqlMapperFile = sqlMapperTemplate.writeDefault(sqlMapperDir, sqlMapperName, overwrite);
        InternalUtil.pruneXmlIfNeed(sqlMapperFile, removedSqlMethods);
        if (noUk) {
            InternalUtil.pruneXmlIfNeed(sqlMapperFile,
                    StatementType.insertOnDuplicateKeyUpdate,
                    StatementType.batchInsertOnDuplicateKeyUpdate);
        }
        if (noBlobs) {
            InternalUtil.pruneXmlIfNeed(sqlMapperFile, StatementType.allWithBLOBs());
        }
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
                String javaExtendsMapperName = config.formatExtendsMapperName(tableName) + ".xml";
                sqlMapperTemplate.writeSub(extendsSqlMapperDir, javaExtendsMapperName, overwrite);
            }
        }

        // java-entity
        File entityDir = new File(srcDir, config.getEntityPackageName().replace('.', '/'));
        if (!entityDir.exists() && !entityDir.mkdirs()) {
            log.info("fail to create dir {}", entityDir);
        } else {
            String javaEntityName = config.formatEntityName(tableName) + ".java";
            JavaEntityTemplate javaEntityTemplate = new JavaEntityTemplate(table, config);
            javaEntityTemplate.writeDefault(entityDir, javaEntityName, overwrite);
        }

        // java-condition
        File conditionDir = new File(srcDir, config.getConditionPackageName().replace('.', '/'));
        if (!conditionDir.exists() && !conditionDir.mkdirs()) {
            log.info("fail to create dir {}", conditionDir);
        } else {
            String conditionName = config.formatConditionName(tableName) + ".java";
            JavaConditionTemplate javaConditionTemplate = new JavaConditionTemplate(table, config);
            javaConditionTemplate.writeDefault(conditionDir, conditionName, overwrite);
        }
    }
}
