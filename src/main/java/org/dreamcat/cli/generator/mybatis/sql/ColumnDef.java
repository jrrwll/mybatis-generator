package org.dreamcat.cli.generator.mybatis.sql;

import lombok.Data;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
public class ColumnDef {

    private String name;
    private String type;
    private int[] typeParams; // decimal(16, 6), varchar(255)
    private boolean notNull;
    private String defaultValue;
    private String onUpdate;
    private String comment;
}
