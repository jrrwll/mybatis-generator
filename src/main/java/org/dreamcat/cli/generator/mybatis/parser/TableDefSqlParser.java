package org.dreamcat.cli.generator.mybatis.parser;

import java.util.List;
import org.dreamcat.cli.generator.mybatis.sql.TableDef;

/**
 * @author Jerry Will
 * @version 2021-12-08
 */
public interface TableDefSqlParser {

    List<TableDef> parse(String sql);
}
