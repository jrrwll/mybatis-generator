package org.dreamcat.cli.generator.mybatis.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.sql.ColumnDef;
import org.dreamcat.cli.generator.mybatis.sql.TableDef;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
public class EntityDef {

    private String tableName;
    private Map<String, Column> columns;
    private List<Column> primaryKeyColumns;
    private List<Column> notPrimaryKeyColumns;
    // extents
    private String mapperClass;
    private String entityClass;
    private Map<String, Column> selectColumns;
    private Map<String, Column> insertColumns;

    public EntityDef(TableDef table, MyBatisGeneratorConfig config) {
        this.columns = new HashMap<>();
        for (ColumnDef column : table.getColumns()) {
            this.columns.put(column.getName(), new Column(column));
        }

        this.primaryKeyColumns = table.getPrimaryKey().getColumns().stream()
                .map(it -> this.columns.get(it))
                .collect(Collectors.toList());

        Set<String> pkColumns = new HashSet<>(table.getPrimaryKey().getColumns());
        this.notPrimaryKeyColumns = new ArrayList<>(columns.size() - pkColumns.size());
        columns.forEach((name, column) -> {
            if (pkColumns.contains(name)) {
                return;
            }

            this.notPrimaryKeyColumns.add(column);
        });
    }

    @Data
    @NoArgsConstructor
    public static class Column {

        // wrapped
        private ColumnDef column;
        // extents
        private String property;

        public Column(ColumnDef column) {

        }
    }
}
