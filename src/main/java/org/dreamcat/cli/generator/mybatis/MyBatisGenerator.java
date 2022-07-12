package org.dreamcat.cli.generator.mybatis;

import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.cli.generator.mybatis.java.EntityDef;
import org.dreamcat.cli.generator.mybatis.parser.TableDefSqlParser;
import org.dreamcat.cli.generator.mybatis.sql.TableDef;
import org.dreamcat.cli.generator.mybatis.template.JavaConditionTemplate;
import org.dreamcat.cli.generator.mybatis.template.JavaEntityTemplate;
import org.dreamcat.cli.generator.mybatis.template.JavaMapperTemplate;
import org.dreamcat.cli.generator.mybatis.template.SqlMapperTemplate;

/**
 * @author Jerry Will
 * @version 2021-11-29
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBatisGenerator {

    private MyBatisGeneratorConfig config;
    private TableDefSqlParser parser;

    public void generate(String sql) throws IOException {
        List<TableDef> tableDefs = parser.parse(sql);
        for (TableDef tableDef : tableDefs) {
            generate(tableDef);
        }
    }

    public void generate(TableDef tableDef) throws IOException {
        EntityDef entityDef = new EntityDef(tableDef, config);
        String srcDir = config.getSrcDir();
        boolean overwrite = config.isOverwrite();

        // java-mapper
        File mapperDir = new File(srcDir, config.getMapperPackageName().replace('.', '/'));
        if (!mapperDir.exists() && !mapperDir.mkdirs()) {
            log.info("fail to create dir {}", mapperDir);
        } else {
            String javaMapperName = entityDef.getMapperName() + ".java";
            JavaMapperTemplate javaMapperTemplate = new JavaMapperTemplate(entityDef, config);
            javaMapperTemplate.write(mapperDir, javaMapperName, overwrite);
        }

        // sql-mapper
        String sqlMapperName = entityDef.getMapperName() + ".xml";
        SqlMapperTemplate sqlMapperTemplate = new SqlMapperTemplate(entityDef, config);
        if (config.isPutMapperTogether()) {
            sqlMapperTemplate.write(mapperDir, sqlMapperName, overwrite);
        } else {
            sqlMapperTemplate.write(config.getSqlMapperDir(), sqlMapperName, overwrite);
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
        File conditionDir = new File(srcDir, config.getMapperPackageName().replace('.', '/'));
        if (!conditionDir.exists() && !conditionDir.mkdirs()) {
            log.info("fail to create dir {}", conditionDir);
        } else {
            String conditionName = entityDef.getConditionName() + ".java";
            JavaConditionTemplate javaConditionTemplate = new JavaConditionTemplate(entityDef, config);
            javaConditionTemplate.write(conditionDir, conditionName, overwrite);
        }
    }

}
