package org.dreamcat.cli.generator.mybatis.java;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.common.sql.ColumnCommonDef;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
@NoArgsConstructor
public class EntityColumnDef {

    private String name;
    private String property;
    private String comment;
    private JDBCType type; //
    private Class<?> javaType;
    private String javaName;
    private String javaSimpleName;

    public EntityColumnDef(ColumnCommonDef column, MyBatisGeneratorConfig config) {
        this.name = column.getName();
        this.property = config.getPropertyNameConfig().format(column.getName());
        this.comment = column.getComment();

        mapType(column.getType(), config);
        if (javaType.equals(byte[].class)) {
            javaName = javaSimpleName = "byte[]";
        } else {
            javaName = javaType.getName();
            javaSimpleName = javaType.getSimpleName();
        }
    }

    private void mapType(String type, MyBatisGeneratorConfig config) {
        switch (type) {
            case "varchar":
            case "char":
            case "string":
            case "text":
            case "longvarchar":
            case "tinytext":
            case "mediumtext":
            case "longtext":
                this.type = JDBCType.VARCHAR;
                this.javaType = String.class;
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
                if (config.isUseIntForTinyAndSmall()) break;
            case "smallint":
            case "int16":
            case "uint16":
            case "i16":
            case "u16":
                this.type = JDBCType.SMALLINT;
                this.javaType = Short.class;
                if (config.isUseIntForTinyAndSmall()) break;
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
                break;
            case "double":
            case "f64":
                this.type = JDBCType.DOUBLE;
                this.javaType = Double.class;
                break;
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
