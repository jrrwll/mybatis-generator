package org.dreamcat.code.generator.base;

import lombok.Data;
import org.dreamcat.common.sql.ColumnCommonDef;
import org.dreamcat.common.sql.TableCommonDef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
public class TableDef {

    private String tableName;
    private String tableComment;
    private Map<String, ColumnDef> columns;
    private List<ColumnDef> primaryKeyColumns;
    private List<ColumnDef> notPrimaryKeyColumns;
    private List<ColumnDef> baseColumns;
    private List<ColumnDef> blobColumns;

    private Map<String, ColumnDef> allColumns = new LinkedHashMap<>();

    public TableDef(TableCommonDef table, SqlBasedGeneratorConfig config) {
        this.tableName = table.getName();
        this.tableComment = table.getComment();

        this.columns = new LinkedHashMap<>();
        for (ColumnCommonDef column : table.getColumns()) {
            this.columns.put(column.getName(), new ColumnDef(column, config));
        }

        this.primaryKeyColumns = table.getPrimaryKey().getColumns().stream()
                .map(it -> this.columns.get(it))
                .collect(Collectors.toList());

        Set<String> pkColumns = new HashSet<>(table.getPrimaryKey().getColumns());
        this.notPrimaryKeyColumns = new ArrayList<>();
        this.baseColumns = new ArrayList<>();
        this.blobColumns = new ArrayList<>();
        columns.forEach((name, column) -> {
            if (config.getIgnoreColumns().contains(name)) return;
            // all columns
            allColumns.put(name, column);

            // not primary key columns
            if (!pkColumns.contains(name)) {
                this.notPrimaryKeyColumns.add(column);
            }

            // base columns and blob columns
            if (!column.isBlob()) {
                this.baseColumns.add(column);
            } else {
                this.blobColumns.add(column);
            }
        });
    }

    public boolean hasBlobColumns() {
        return !blobColumns.isEmpty();
    }
}