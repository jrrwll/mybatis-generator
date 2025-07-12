package org.dreamcat.cli.generator.mybatis;

import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig.StatementType;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig.TableConfig;
import org.dreamcat.cli.generator.mybatis.MybatisGeneratorMojo.Table;
import org.dreamcat.common.Pair;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.sql.DriverUtil;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.util.EnumUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2024-12-26
 */
public class MybatisGeneratorAction implements Runnable {

    private final MybatisGeneratorMojo mojo;
    private final MavenProject project;
    private final Log log;

    public MybatisGeneratorAction(MybatisGeneratorMojo mojo) {
        this.mojo = mojo;
        this.project = mojo.getProject();
        this.log = mojo.getLog();
    }

    @SneakyThrows
    public void run() {
        List<Table> tables = mojo.getTables();
        List<String> tableNames = mojo.getTableNames();
        MyBatisGeneratorConfig config = buildConfig(tables);
        logInfo("mybatis generator config: \n{}", JsonUtil.toJsonWithPretty(config));
        MyBatisGenerator gen = new MyBatisGenerator(config);

        String sqlPath = mojo.getSqlPath();
        String jdbcUrl = mojo.getJdbcUrl();
        String jdbcUser = mojo.getJdbcUser();
        String jdbcPassword = mojo.getJdbcPassword();
        String jdbcDriverClassName = mojo.getJdbcDriverClassName();

        if (ObjectUtil.isNotEmpty(sqlPath)) {
            runForSql(sqlPath, gen);
        } else {
            if (ObjectUtil.isEmpty(jdbcUrl)) {
                throw new IllegalArgumentException("require sqlPath or jdbcUrl, please setup it");
            }
            if (ObjectUtil.isEmpty(tableNames) && ObjectUtil.isNotEmpty(tables)){
                tableNames = tables.stream().map(Table::getName).collect(Collectors.toList());
            }
            if (ObjectUtil.isEmpty(tableNames)) {
                throw new IllegalArgumentException("require tables for jdbcUrl, please setup it");
            }
            runForJdbc(jdbcUrl, jdbcUser, jdbcPassword,
                    jdbcDriverClassName, tableNames, gen);
        }
    }

    private void runForSql(String sqlPath, MyBatisGenerator gen) throws IOException {
        logInfo("use sqlPath: {}", sqlPath);
        File sqlPathFile;
        if (sqlPath.startsWith("/")) {
            sqlPathFile = new File(sqlPath).getCanonicalFile();
        } else {
            sqlPathFile = new File(project.getBasedir(), sqlPath).getCanonicalFile();
        }
        logInfo("resolved sqlPath: {}", sqlPathFile);
        List<Pair<String, File>> sqls = getSql(sqlPathFile);
        if (ObjectUtil.isEmpty(sqls)) {
            throw new IllegalArgumentException("no sql found in " + sqlPath);
        }
        for (Pair<String, File> sqlAndFile : sqls) {
            String sql = sqlAndFile.getFirst();
            File file = sqlAndFile.getSecond();
            try {
                logInfo("parsing sql on {} ", file);
                List<TableCommonDef> tableDefs = InternalUtil.parseCreateTable(sql);
                logInfo("parsed tableDefs: {}", JsonUtil.toJson(tableDefs));
                gen.generate(tableDefs);
            } catch (Exception e) {
                log.error("failed to generate sql on {} " + file, e);
            }
        }
    }

    private void runForJdbc(String jdbcUrl, String jdbcUser, String jdbcPassword, String jdbcDriverClassName,
            List<String> tableNames, MyBatisGenerator gen) throws Exception {
        logInfo("use jdbcUrl: {}", jdbcUrl);
        logInfo("use jdbcUser: {}", jdbcUser);
        logInfo("use jdbcPassword: {}", jdbcPassword == null ? "null" : StringUtil.repeat('*', jdbcPassword.length()));
        logInfo("use jdbcDriverClassName: {}", jdbcDriverClassName);
        logInfo("use tableNames: {}", JsonUtil.toJson(tableNames));

        URLClassLoader userCodeClassLoader = MavenUtil.buildUserCodeClassLoader(project, mojo.getLocalRepository(), log);
        log.info("resolved classLoader urls: " + JsonUtil.toJson(userCodeClassLoader.getURLs()));

        DriverUtil.runIsolated(jdbcUrl, jdbcUser, jdbcPassword, userCodeClassLoader, jdbcDriverClassName, connection -> {
            logInfo("fetching tables from JDBC connection");
            try {
                List<TableCommonDef> tableDefs = InternalUtil.fetchTableDefs(connection);
                for (TableCommonDef tableDef : tableDefs) {
                    if (!tableNames.contains(tableDef.getName())) {
                        logInfo("skip table {} since not included by tables", tableDef.getName());
                        continue;
                    }
                    logInfo("generating sql for tableDef: {}", JsonUtil.toJson(tableDef));
                    gen.generate(tableDef);
                }
            } catch (Exception e) {
                log.error(StringUtil.formatMessage("failed to generate sql on jdbc {} for user {}",
                        jdbcUrl, jdbcUser), e);
            }
            return null;
        });
    }

    // sql content + file
    private List<Pair<String, File>> getSql(File sqlPathFile) throws IOException {
        List<Pair<String, File>> sqls = new ArrayList<>();
        if (sqlPathFile.isFile()) {
            sqls.add(Pair.of(FileUtil.readAsString(sqlPathFile), sqlPathFile));
            return sqls;
        }
        if (sqlPathFile.isDirectory()) {
            File[] files = sqlPathFile.listFiles();
            if (files == null) return null;
            for (File f : files) {
                sqls.add(Pair.of(FileUtil.readAsString(f), sqlPathFile));
            }
            return sqls;
        }
        return null;
    }

    private MyBatisGeneratorConfig buildConfig(List<Table> tables) {
        MyBatisGeneratorConfig config = new MyBatisGeneratorConfig();
        config.setOverwrite(mojo.getOverwrite());
        config.setSrcDir(mojo.getSrcDir());
        config.setSqlMapperDir(mojo.getSqlMapperDir());
        config.setExtendsSqlMapperDir(mojo.getExtendsSqlMapperDir());
        if (mojo.getEntityPackageName() != null) {
            config.setEntityPackageName(mojo.getEntityPackageName());
        }
        config.setMapperPackageName(mojo.getMapperPackageName());
        if (mojo.getExtendsMapperPackageName() != null) {
            config.setExtendsMapperPackageName(mojo.getExtendsMapperPackageName());
        }
        config.setConditionPackageName(mojo.getConditionPackageName());

        List<String> ignoreColumns = mojo.getIgnoreColumns();
        if (ignoreColumns != null) {
            config.setIgnoreColumns(new HashSet<>(ignoreColumns));
        }

        config.setForceInt(mojo.getForceInt());
        config.setForceDecimal(mojo.getForceDecimal());
        config.setEnableResultMapWithBLOBs(mojo.getEnableResultMapWithBLOBs());
        config.setEnableExtendsMapper(mojo.getEnableExtendsMapper());
        config.setAddMapperAnnotation(mojo.getAddMapperAnnotation());
        config.setEnableLombok(mojo.getEnableLombok());
        config.setAddComments(mojo.getAddComments());
        config.setDelimitKeyword(mojo.getDelimitKeyword());

        if (mojo.getNamePrefix() != null) {
            config.setNamePrefix(mojo.getNamePrefix());
        }
        if (mojo.getNameSuffix() != null) {
            config.setNameSuffix(mojo.getNameSuffix());
        }
        if (mojo.getEntityName() != null ) {
            config.setEntityName(mojo.getEntityName());
        }
        config.setMapperName(mojo.getMapperName());
        if (mojo.getExtendsMapperName() != null) {
            config.setExtendsMapperName(mojo.getExtendsMapperName());
        }
        if (mojo.getConditionName() != null) {
            config.setConditionName(mojo.getConditionName());
        }
        if (mojo.getPropertyName() != null) {
            config.setPropertyName(mojo.getPropertyName());
        }

        List<String> prunedStatements = mojo.getPrunedStatements();
        if (ObjectUtil.isNotEmpty(prunedStatements)) {
            List<StatementType> statementTypes = prunedStatements.stream().map(it -> EnumUtil.get(it,
                            StatementType.class))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            config.setPrunedStatements(statementTypes);
        }

        if (ObjectUtil.isNotEmpty(tables)) {
            for (Table table : tables) {
                String tableName = table.getName();
                TableConfig tableConfig = new TableConfig();

                tableConfig.setEntityName(table.getEntityName());
                tableConfig.setMapperName(table.getMapperName());
                tableConfig.setExtendsMapperName(table.getExtendsMapperName());
                tableConfig.setConditionName(table.getConditionName());
                tableConfig.setPropertyNames(table.getPropertyNames());

                config.getTableConfigs().put(tableName, tableConfig);
            }
        }
        return config;
    }

    private void logInfo(String msg, Object... args) {
        log.info(StringUtil.formatMessage(msg, args));
    }
}
