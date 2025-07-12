package org.dreamcat.cli.generator.mybatis.java;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.common.sql.ColumnCommonDef;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.Date;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
@NoArgsConstructor
public class EntityColumnDef {

    private String name;
    private String sqlName;
    private String property;
    private String comment;
    private JDBCType type;
    private Class<?> javaType;
    private String javaName;
    private String javaSimpleName;

    public EntityColumnDef(ColumnCommonDef column, String tableName, MyBatisGeneratorConfig config) {
        this.sqlName = this.name = column.getName();
        if (config.getDelimitKeyword() != null) {
            this.sqlName = config.getDelimitKeyword() + this.name + config.getDelimitKeyword();
        }
        this.property = config.formatPropertyName(column.getName(), tableName);
        this.comment = column.getComment();

        mapType(column.getType(), config);
        if (javaType.equals(byte[].class)) {
            javaName = javaSimpleName = "byte[]";
        } else {
            javaName = javaType.getName();
            javaSimpleName = javaType.getSimpleName();
        }
    }

    public boolean isBlob() {
        return type == JDBCType.BLOB || type == JDBCType.CLOB ||
                type == JDBCType.LONGVARBINARY ||
                type == JDBCType.BINARY || type == JDBCType.VARBINARY;
    }

    private void mapType(String type, MyBatisGeneratorConfig config) {
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
