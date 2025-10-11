package org.dreamcat.cli.generator.base;

import org.dreamcat.common.sql.TableCommonDef;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Jerry Will
 * @version 2025-09-24
 */
public interface SqlBasedGenerator {

    SqlBasedGeneratorConfig getConfig();

    default boolean isSupportGenerateBoilerplate() {
        return false;
    }

    default void generateBoilerplate() throws IOException {
        throw new UnsupportedOperationException();
    }

    void generate(TableDef table) throws IOException;

    default void generate(String sql) throws IOException {
        List<TableCommonDef> tableDefs = InternalUtil.parseCreateTable(sql);
        generate(tableDefs);
    }

    default void generate(String jdbcUrl, String jdbcUser, String jdbcPassword) throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
            generate(connection);
        }
    }

    default void generate(Connection connection) throws SQLException, IOException {
        List<TableCommonDef> tableDefs = InternalUtil.fetchTableDefs(connection);
        generate(tableDefs);
    }

    default void generate(List<TableCommonDef> tableDefs) throws IOException {
        for (TableCommonDef tableDef : tableDefs) {
            generate(tableDef);
        }
    }

    default void generate(TableCommonDef tableDef) throws IOException {
        TableDef table = new TableDef(tableDef, getConfig());
        generate(table);
    }
}
