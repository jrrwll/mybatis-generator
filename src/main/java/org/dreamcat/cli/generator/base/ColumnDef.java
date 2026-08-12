package org.dreamcat.cli.generator.base;

import lombok.Data;
import org.dreamcat.common.sql.ColumnCommonDef;
import org.dreamcat.common.util.ObjectUtil;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
public class ColumnDef {

    private String columnName;
    private String comment;

    private JDBCType type;
    private Class<?> javaType;
    private String javaName;
    private String javaSimpleName;

    private Integer typeLength;
    private boolean notNull;
    private boolean autoIncrement;

    public ColumnDef(ColumnCommonDef column, SqlBasedGeneratorConfig config) {
        this.columnName = column.getName();
        this.comment = column.getComment();

        mapType(column.getType(), column.getTypeParams(), config);
        if (javaType.equals(byte[].class)) {
            javaName = javaSimpleName = "byte[]";
        } else {
            javaName = javaType.getName();
            javaSimpleName = javaType.getSimpleName();
        }

        this.notNull = column.isNotNull();
        if (ObjectUtil.isNotEmpty(column.getTypeParams())) {
            this.typeLength = column.getTypeParams().get(0);
        }

        this.autoIncrement = Objects.equals(column.getAutoIncrement(), true);
    }

    public boolean isBlob() {
        return type == JDBCType.BLOB || type == JDBCType.CLOB ||
                type == JDBCType.LONGVARBINARY ||
                type == JDBCType.BINARY || type == JDBCType.VARBINARY;
    }

    private void mapType(String type, List<Integer> typeParams, SqlBasedGeneratorConfig config) {
        if (type == null) return;
        switch (type.toLowerCase()) {
            case "varchar":
            case "char":
            case "string":
                this.type = JDBCType.VARCHAR;
                this.javaType = String.class;
                break;
            case "longvarchar":
                this.type = JDBCType.LONGVARCHAR;
                this.javaType = String.class;
                break;
            case "text":
            case "tinytext":
            case "mediumtext":
            case "longtext":
            case "clob":
            case "mediumclob":
            case "longclob":
                this.type = JDBCType.CLOB;
                this.javaType = String.class; // maybe overflow since max size of string is 2GB
                break;
            case "bool":
            case "boolean":
                this.type = JDBCType.BOOLEAN;
                this.javaType = Boolean.class;
                break;
            case "tinyint":
                if (ObjectUtil.isNotEmpty(typeParams) && typeParams.get(0) == 1) {
                    if (config.isTinyint1AsBool()) {
                        this.type = JDBCType.BOOLEAN;
                        this.javaType = Boolean.class;
                        break;
                    }
                }
            case "int8":
            case "uint8":
            case "i8":
            case "u8":
                this.type = JDBCType.TINYINT;
                this.javaType = Byte.class;
                if (!config.isForceInt()) break;
            case "smallint":
            case "int16":
            case "uint16":
            case "i16":
            case "u16":
                this.type = JDBCType.SMALLINT;
                this.javaType = Short.class;
                if (!config.isForceInt()) break;
            case "int":
            case "integer":
            case "int32":
            case "uint32":
            case "i32":
            case "u32":
                this.type = JDBCType.INTEGER;
                this.javaType = Integer.class;
                break;
            case "bigint":
            case "long":
            case "int64":
            case "uint64":
            case "i64":
            case "u64":
                this.type = JDBCType.BIGINT;
                this.javaType = Long.class;
                break;
            case "float":
            case "f32":
                this.type = JDBCType.FLOAT;
                this.javaType = Float.class;
                if (!config.isForceDecimal()) break;
            case "double":
            case "f64":
                this.type = JDBCType.DOUBLE;
                this.javaType = Double.class;
                if (!config.isForceDecimal()) break;
            case "decimal":
                this.type = JDBCType.DECIMAL;
                this.javaType = BigDecimal.class;
                break;
            case "date":
                this.type = JDBCType.DATE;
                this.javaType = Date.class;
                break;
            case "time":
                this.type = JDBCType.TIME;
                // refuse to use LocalTime
                this.javaType = Date.class;
                break;
            case "datetime":
            case "timestamp":
                this.type = JDBCType.TIMESTAMP;
                this.javaType = Date.class;
                break;
            case "bit":
                this.type = JDBCType.BIT;
                this.javaType = byte[].class;
                break;
            case "binary":
                this.type = JDBCType.BINARY;
                this.javaType = byte[].class;
                break;
            case "varbinary":
                this.type = JDBCType.VARBINARY;
                this.javaType = byte[].class;
                break;
            case "blob":
            case "mediumblob":
            case "longblob":
                this.type = JDBCType.BLOB;
                this.javaType = byte[].class;
                break;
            default:
                this.type = JDBCType.NULL;
                this.javaType = Void.class;
        }
    }
}