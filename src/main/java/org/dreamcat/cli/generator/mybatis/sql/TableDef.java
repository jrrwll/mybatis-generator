package org.dreamcat.cli.generator.mybatis.sql;

import java.util.List;
import lombok.Data;

/**
 * @author Jerry Will
 * @version 2021-11-29
 */
@Data
public class TableDef {

    private String name;
    private String comment;
    private List<ColumnDef> columns;
    private IndexDef primaryKey;
    private List<IndexDef> uniqueIndexes;
    private List<IndexDef> indexes;
    private String engine;
    private String defaultCharset;

}
