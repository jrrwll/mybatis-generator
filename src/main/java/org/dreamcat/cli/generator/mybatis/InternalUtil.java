package org.dreamcat.cli.generator.mybatis;

import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.sql.ColumnCommonDef;
import org.dreamcat.common.sql.IndexCommonDef;
import org.dreamcat.common.sql.JdbcColumnDef;
import org.dreamcat.common.sql.JdbcUtil;
import org.dreamcat.common.sql.TableCommonDef;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2024-12-22
 */
class InternalUtil {

    // sql parser
    @SneakyThrows
    public static List<TableCommonDef> parseCreateTable(String sql) {
        List<TableCommonDef> tableDefs = new ArrayList<>();
        Statements statements = CCJSqlParserUtil.parseStatements(sql);
        for (Statement statement : statements) {
            if (statement instanceof CreateTable) {
                CreateTable createTable = (CreateTable) statement;
                TableCommonDef tableDef = convert(createTable);
                tableDefs.add(tableDef);
            }
        }
        return tableDefs;
    }

    private static TableCommonDef convert(CreateTable createTable) {
        TableCommonDef tableCommonDef = new TableCommonDef();

        tableCommonDef.setName(unwrapText(createTable.getTable().getName()));
        // find comment
        List<String> tableOptionsStrings = createTable.getTableOptionsStrings();
        tableCommonDef.setComment(findComment(tableOptionsStrings));
        tableCommonDef.setIfNotExists(createTable.isIfNotExists());
        tableCommonDef.setOrReplace(createTable.isOrReplace());

        tableCommonDef.setColumns(new ArrayList<>());
        int position = 0;
        for (ColumnDefinition columnDefinition : createTable.getColumnDefinitions()) {
            ColumnCommonDef columnDef = new ColumnCommonDef();
            columnDef.setPosition(++position);
            columnDef.setName(unwrapText(columnDefinition.getColumnName()));

            String dataType = columnDefinition.getColDataType().getDataType();
            String[] typeAndParams = dataType.split(" *\\(| *\\)");
            columnDef.setType(typeAndParams[0]);

            List<String> columnSpecs = columnDefinition.getColumnSpecs();
            columnDef.setComment(findComment(columnSpecs));

            columnDef.setNotNull(findNotNull(columnSpecs));

            tableCommonDef.getColumns().add(columnDef);
        }

        List<Index> indexes = createTable.getIndexes();
        if (ObjectUtil.isNotEmpty(indexes)) {
            tableCommonDef.setUniqueIndexes(new ArrayList<>());
            tableCommonDef.setIndexes(new ArrayList<>());
            for (Index index : indexes) {
                List<String> columns = index.getColumns().stream()
                        .map(Index.ColumnParams::getColumnName)
                        .map(InternalUtil::unwrapText)
                        .collect(Collectors.toList());
                IndexCommonDef indexDef = new IndexCommonDef();
                indexDef.setName(index.getName());
                indexDef.setColumns(columns);

                if (index.getType().equalsIgnoreCase("primary key")) {
                    tableCommonDef.setPrimaryKey(indexDef);
                } else if (index.getType().equalsIgnoreCase("unique key")) {
                    tableCommonDef.getUniqueIndexes().add(indexDef);
                } else {
                    tableCommonDef.getIndexes().add(indexDef);
                }
            }
        }
        return tableCommonDef;
    }

    private static String findComment(List<String> optionsStrings) {
        if (ObjectUtil.isEmpty(optionsStrings)) {
            return null;
        }
        for (int i = 0, n = optionsStrings.size(); i < n; i++) {
            String option = optionsStrings.get(i);
            if ("comment".equalsIgnoreCase(option) && i < n - 1) {
                String comment = optionsStrings.get(i + 1);
                return unwrapText(comment);
            }
        }
        return null;
    }

    private static boolean findNotNull(List<String> optionsStrings) {
        if (ObjectUtil.isEmpty(optionsStrings)) {
            return false;
        }
        for (int i = 0, n = optionsStrings.size(); i < n; i++) {
            String option = optionsStrings.get(i);
            if ("not".equalsIgnoreCase(option) && i < n - 1) {
                String nextValue = optionsStrings.get(i + 1);
                if ("null".equalsIgnoreCase(nextValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String unwrapText(String name) {
        return StringUtil.trim(name, "\"`'");
    }

    // jdbc
    public static List<TableCommonDef> fetchTableDefs(Connection connection) throws SQLException {
        String databaseName = connection.getSchema();
        List<String> tableNames = JdbcUtil.getTables(connection, databaseName);

        List<TableCommonDef> tableDefs = new ArrayList<>();
        for (String tableName : tableNames) {
            TableCommonDef tableDef = new TableCommonDef();
            tableDefs.add(tableDef);
            tableDef.setName(tableName);

            // columns
            List<JdbcColumnDef> columns = JdbcUtil.getColumns(connection, databaseName, tableName);
            tableDef.setColumns(columns.stream()
                    .map(InternalUtil::convert).collect(Collectors.toList()));

            // primaryKey
            List<JdbcColumnDef> primaryKeys = JdbcUtil.getPrimaryKeys(connection, databaseName, tableName);
            IndexCommonDef primaryKey = new IndexCommonDef();
            primaryKey.setColumns(new ArrayList<>());
            tableDef.setPrimaryKey(primaryKey);
            if (!primaryKeys.isEmpty()) {
                primaryKey.getColumns().addAll(primaryKeys.stream()
                        .sorted(Comparator.comparingInt(JdbcColumnDef::getKeySeq))
                        .map(JdbcColumnDef::getName).collect(Collectors.toList()));
                primaryKey.setName(primaryKeys.get(0).getPkName());
            }

        }
        return tableDefs;
    }

    private static ColumnCommonDef convert(JdbcColumnDef column) {
        ColumnCommonDef columnDef = new ColumnCommonDef();
        columnDef.setName(column.getName());
        columnDef.setComment(column.getComment());
        columnDef.setType(column.getType());
        columnDef.setNotNull(Objects.equals(column.getNullable(), false));
        return columnDef;
    }

    // prune file
    static final Pattern stmt_id = Pattern.compile("<(select|delete|update|insert) id=\"(\\w+?)\" .*>");
    static final Pattern stmt_end = Pattern.compile("</(select|delete|update|insert)>");
    static final Pattern sql_method = Pattern.compile("\\w+ (\\w+)\\(");

    public static void pruneXmlIfNeed(File file, List<String> removedSqlMethods) throws IOException {
        if (ObjectUtil.isEmpty(removedSqlMethods)) return;
        List<String> lines = FileUtil.readLines(file);
        List<String> outputLines = new ArrayList<>();
        for (int i = 0, n = lines.size(); i < n; i++) {
            String line = lines.get(i);
            Matcher stmtIdMatcher = stmt_id.matcher(line);
            if (!stmtIdMatcher.find()) {
                outputLines.add(line);
                continue;
            }

            String sqlMethod = stmtIdMatcher.group(2);
            if (!removedSqlMethods.contains(sqlMethod)) {
                outputLines.add(line);
                continue;
            }

            while (i++ < n) {
                String endLine = lines.get(i);
                Matcher stmtEndMatcher = stmt_end.matcher(endLine);
                if (stmtEndMatcher.find()) {
                    break;
                }
            }
        }
        String content = String.join("\n", outputLines);
        FileUtil.write(file, content);
    }

    public static void pruneJavaIfNeed(File file, List<String> removedSqlMethods) throws IOException {
        if (ObjectUtil.isEmpty(removedSqlMethods)) return;
        List<String> lines = FileUtil.readLines(file);
        List<String> outputLines = new ArrayList<>();
        for (int i = 0, n = lines.size(); i < n; i++) {
            String line = lines.get(i);
            Matcher sqlMethodMatcher = sql_method.matcher(line);
            if (!sqlMethodMatcher.find()) {
                outputLines.add(line);
                continue;
            }

            String sqlMethod = sqlMethodMatcher.group(1);
            if (!removedSqlMethods.contains(sqlMethod)) {
                outputLines.add(line);
                continue;
            }
            if (i < n - 1) {
                String nextLine = lines.get(i + 1);
                if (ObjectUtil.isBlank(nextLine)) i++;
            }
        }
        String content = String.join("\n", outputLines);
        FileUtil.write(file, content);
    }
}
