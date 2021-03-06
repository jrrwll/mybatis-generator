package org.dreamcat.cli.generator.mybatis.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.common.sql.ColumnCommonDef;
import org.dreamcat.common.sql.TableCommonDef;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
public class EntityDef {

    private String tableName;
    private Map<String, EntityColumnDef> columns;
    private List<EntityColumnDef> primaryKeyColumns;
    private List<EntityColumnDef> notPrimaryKeyColumns;

    // extents
    private String entityName;
    private String mapperName;
    private String conditionName;

    private Map<String, EntityColumnDef> entityColumns = new HashMap<>();

    public EntityDef(TableCommonDef table, MyBatisGeneratorConfig config) {
        this.tableName = table.getName();

        this.entityName = config.getEntityNameConfig().format(tableName);
        this.mapperName = config.getMapperNameConfig().format(tableName);
        this.conditionName = config.getConditionNameConfig().format(tableName);

        this.columns = new HashMap<>();
        for (ColumnCommonDef column : table.getColumns()) {
            this.columns.put(column.getName(), new EntityColumnDef(column, config));
        }

        this.primaryKeyColumns = table.getPrimaryKey().getColumns().stream()
                .map(it -> this.columns.get(it))
                .collect(Collectors.toList());

        Set<String> pkColumns = new HashSet<>(table.getPrimaryKey().getColumns());
        this.notPrimaryKeyColumns = new ArrayList<>(columns.size() - pkColumns.size());
        columns.forEach((name, column) -> {
            if (!pkColumns.contains(name)) {
                this.notPrimaryKeyColumns.add(column);
            }
            if (!config.getIgnoreColumns().contains(name)) {
                entityColumns.put(name, column);
            }
        });
    }
}
