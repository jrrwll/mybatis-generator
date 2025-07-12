package org.dreamcat.cli.generator.mybatis;

import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig.StatementType;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig.TableConfig;
import org.dreamcat.cli.generator.mybatis.MybatisGeneratorExtension.Table;
import org.dreamcat.common.Pair;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.sql.DriverUtil;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.util.EnumUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.StringUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2024-12-22
 */
public class MybatisGeneratorTask extends DefaultTask {

    @TaskAction
    public void run() throws Exception {
        MybatisGeneratorExtension extension = getProject().getExtensions()
                .getByType(MybatisGeneratorExtension.class);
        SortedMap<String, Table> tables = extension.getTables().getAsMap();
        List<String> tableNames = extension.getTableNames().getOrNull();

        MyBatisGeneratorConfig config = buildConfig(extension, tables, tableNames);
        getLogger().info("mybatis generator config: \n{}", JsonUtil.toJsonWithPretty(config));
        MyBatisGenerator gen = new MyBatisGenerator(config);

        String sqlPath = extension.getSqlPath().getOrNull();
        String jdbcUrl = extension.getJdbcUrl().getOrNull();
        String jdbcUser = extension.getJdbcUser().getOrNull();
        String jdbcPassword = extension.getJdbcPassword().getOrNull();
        String jdbcDriverClassName = extension.getJdbcDriverClassName().getOrNull();

        if (ObjectUtil.isNotEmpty(sqlPath)) {
            runForSql(sqlPath, gen);
        } else {
            if (ObjectUtil.isEmpty(jdbcUrl)) {
                throw new IllegalArgumentException("require sqlPath or jdbcUrl, please setup it");
            }
            if (tableNames == null) tableNames = new ArrayList<>(tables.keySet());
            if (ObjectUtil.isEmpty(tableNames)) {
                throw new IllegalArgumentException("require tables or tableNames for jdbcUrl, please setup it");
            }
            runForJdbc(jdbcUrl, jdbcUser, jdbcPassword,
                    jdbcDriverClassName, tableNames, gen);
        }
    }

    private void runForSql(String sqlPath, MyBatisGenerator gen) throws IOException {
        getLogger().quiet("use sqlPath: {}", sqlPath);
        File sqlPathFile;
        if (sqlPath.startsWith("/")) {
            sqlPathFile = new File(sqlPath).getCanonicalFile();
        } else {
            sqlPathFile = new File(getProject().getRootDir(), sqlPath).getCanonicalFile();
        }
        getLogger().quiet("resolved sqlPath: {}", sqlPathFile);
        List<Pair<String, File>> sqls = getSql(sqlPathFile);
        if (ObjectUtil.isEmpty(sqls)) {
            throw new IllegalArgumentException("no sql found in " + sqlPath);
        }
        for (Pair<String, File> sqlAndFile : sqls) {
            String sql = sqlAndFile.getFirst();
            File file = sqlAndFile.getSecond();
            try {
                getLogger().info("parsing sql on {} ", file);
                List<TableCommonDef> tableDefs = InternalUtil.parseCreateTable(sql);
                getLogger().info("parsed tableDefs: {}", JsonUtil.toJson(tableDefs));
                gen.generate(tableDefs);
            } catch (Exception e) {
                getLogger().quiet("failed to generate sql on {} ", file, e);
            }
        }
    }

    private void runForJdbc(String jdbcUrl, String jdbcUser, String jdbcPassword,
            String jdbcDriverClassName,
            List<String> tableNames, MyBatisGenerator gen) throws Exception {
        getLogger().quiet("use jdbcUrl: {}", jdbcUrl);
        getLogger().quiet("use jdbcUser: {}", jdbcUser);
        getLogger().quiet("use jdbcPassword: {}", jdbcPassword == null ?
                "null" : StringUtil.repeat('*', jdbcPassword.length()));
        getLogger().quiet("use jdbcDriverClassName: {}", jdbcDriverClassName);
        getLogger().quiet("use tableNames: {}", JsonUtil.toJson(tableNames));

        URL[] urls = GradleUtil.getUserCodeClassPaths(getProject());
        getLogger().info("resolved classpath: {}", JsonUtil.toJson(urls));

        ClassLoader userCodeClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        DriverUtil.runIsolated(jdbcUrl, jdbcUser, jdbcPassword, userCodeClassLoader, jdbcDriverClassName,
                connection -> {
                    getLogger().info("fetching tables from JDBC connection");
                    try {
                        List<TableCommonDef> tableDefs = InternalUtil.fetchTableDefs(connection);
                        for (TableCommonDef tableDef : tableDefs) {
                            if (!tableNames.contains(tableDef.getName())) {
                                getLogger().info("skip table {} since not included by tables", tableDef.getName());
                                continue;
                            }
                            getLogger().info("generating sql for tableDef: {}", JsonUtil.toJson(tableDef));
                            gen.generate(tableDef);
                        }
                    } catch (Exception e) {
                        getLogger().quiet("failed to generate sql on jdbc {} for user {}", jdbcUrl, jdbcUser, e);
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

    private MyBatisGeneratorConfig buildConfig(MybatisGeneratorExtension extension,
            Map<String, Table> tables, List<String> tableNames) {
        MyBatisGeneratorConfig config = new MyBatisGeneratorConfig();
        setIfNotNull(config::setOverwrite, extension.getOverwrite());
        String srcDir = extension.getSrcDir().getOrNull();
        if (srcDir == null) {
            throw new IllegalArgumentException("srcDir is null, please setup it");
        }
        setFileIfNotNull(config::setSrcDir, extension.getSrcDir());
        setFileIfNotNull(config::setSqlMapperDir, extension.getSqlMapperDir());
        setFileIfNotNull(config::setExtendsSqlMapperDir, extension.getExtendsSqlMapperDir());

        setIfNotNull(config::setEntityPackageName, extension.getEntityPackageName());
        setIfNotNull(config::setMapperPackageName, extension.getMapperPackageName());
        setIfNotNull(config::setExtendsMapperPackageName, extension.getExtendsMapperPackageName());
        setIfNotNull(config::setConditionPackageName, extension.getConditionPackageName());

        List<String> ignoreColumns = extension.getIgnoreColumns().getOrNull();
        if (ignoreColumns != null) {
            config.setIgnoreColumns(new HashSet<>(ignoreColumns));
        }
        setIfNotNull(config::setForceInt, extension.getForceInt());
        setIfNotNull(config::setForceDecimal, extension.getForceDecimal());
        setIfNotNull(config::setEnableResultMapWithBLOBs, extension.getEnableResultMapWithBLOBs());
        setIfNotNull(config::setEnableExtendsMapper, extension.getEnableExtendsMapper());
        setIfNotNull(config::setAddMapperAnnotation, extension.getAddMapperAnnotation());
        setIfNotNull(config::setEnableLombok, extension.getEnableLombok());
        setIfNotNull(config::setAddComments, extension.getAddComments());
        setIfNotNull(config::setDelimitKeyword, extension.getDelimitKeyword());
        setIfNotNull(config::setNamePrefix, extension.getNamePrefix());
        setIfNotNull(config::setNameSuffix, extension.getNameSuffix());
        setIfNotNull(config::setEntityName, extension.getEntityName());
        setIfNotNull(config::setMapperName, extension.getMapperName());
        setIfNotNull(config::setExtendsMapperName, extension.getExtendsMapperName());
        setIfNotNull(config::setConditionName, extension.getConditionName());
        setIfNotNull(config::setPropertyName, extension.getPropertyName());

        List<String> prunedStatements = extension.getPrunedStatements().getOrElse(Collections.emptyList());
        if (ObjectUtil.isNotEmpty(prunedStatements)) {
            List<StatementType> statementTypes = prunedStatements.stream()
                    .map(it -> EnumUtil.get(it, StatementType.class))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            config.setPrunedStatements(statementTypes);
        }

        if (ObjectUtil.isNotEmpty(tables)) {
            for (Table table : tables.values()) {
                String tableName = table.getName();

                TableConfig tableConfig = new TableConfig();
                setIfNotNull(tableConfig::setEntityName, table.getEntityName());
                setIfNotNull(tableConfig::setMapperName, table.getMapperName());
                setIfNotNull(tableConfig::setExtendsMapperName, table.getExtendsMapperName());
                setIfNotNull(tableConfig::setConditionName, table.getConditionName());
                setIfNotNull(tableConfig::setPropertyNames, table.getPropertyNames());

                config.getTableConfigs().put(tableName, tableConfig);
            }
        }
        return config;
    }

    private void setFileIfNotNull(Consumer<String> setter, Provider<String> provider) {
        String filename = provider.getOrNull();
        if (filename == null) return;
        if (!filename.startsWith("/")) {
            filename = new File(getProject().getProjectDir(), filename).getAbsolutePath();
        }
        setter.accept(filename);
    }

    private static <T> void setIfNotNull(Consumer<T> setter, Provider<T> provider) {
        T val = provider.getOrNull();
        if (val != null && (!(val instanceof Collection) || !((Collection<?>) val).isEmpty())) {
            setter.accept(val);
        }
    }
}
