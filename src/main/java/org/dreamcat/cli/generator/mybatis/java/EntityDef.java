package org.dreamcat.cli.generator.mybatis.java;

import lombok.Data;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
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
public class EntityDef {

    private String tableName;
    private String tableSqlName;
    private Map<String, EntityColumnDef> columns;
    private List<EntityColumnDef> primaryKeyColumns;
    private List<EntityColumnDef> notPrimaryKeyColumns;
    private List<EntityColumnDef> baseColumns;
    private List<EntityColumnDef> blobColumns;

    // extents
    private String entityName;
    private String mapperName;
    private String extendsMapperName;
    private String conditionName;

    private Map<String, EntityColumnDef> entityColumns = new LinkedHashMap<>();

    public EntityDef(TableCommonDef table, MyBatisGeneratorConfig config) {
        this.tableSqlName = this.tableName = table.getName();
        if (config.getDelimitKeyword() != null) {
            this.tableSqlName = config.getDelimitKeyword() + this.tableName + config.getDelimitKeyword();
        }

        this.entityName = config.formatEntityName(tableName);
        this.mapperName = config.formatMapperName(tableName);
        this.extendsMapperName = config.formatExtendsMapperName(tableName);
        this.conditionName = config.formatConditionName(tableName);

        this.columns = new LinkedHashMap<>();
        for (ColumnCommonDef column : table.getColumns()) {
            this.columns.put(column.getName(), new EntityColumnDef(column, tableName, config));
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
            entityColumns.put(name, column);

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
