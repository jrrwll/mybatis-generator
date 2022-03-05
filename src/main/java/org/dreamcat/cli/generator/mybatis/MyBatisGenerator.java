package org.dreamcat.cli.generator.mybatis;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.dreamcat.cli.generator.mybatis.parser.TableDefSqlParser;
import org.dreamcat.cli.generator.mybatis.sql.TableDef;

/**
 * @author Jerry Will
 * @version 2021-11-29
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBatisGenerator {

    private MyBatisGeneratorConfig config;
    private TableDefSqlParser parser;

    public void generate(String sql) {
        List<TableDef> tableDefs = parser.parse(sql);
        for (TableDef tableDef : tableDefs) {

        }
    }

}
