package org.dreamcat.code.generator.fastapi_curd;

import lombok.SneakyThrows;
import org.dreamcat.code.generator.base.ColumnDef;
import org.dreamcat.code.generator.base.TemplateOutput;
import org.dreamcat.common.util.ClassLoaderUtil;

import java.sql.JDBCType;
import java.util.Objects;

/**
 * @author Jerry Will
 * @version 2025-09-26
 */
public abstract class FastapiCurdTemplateOutput extends TemplateOutput {

    @Override
    public String getGeneratorName() {
        return "Fastapi CURD Generator";
    }

    @SneakyThrows
    protected static String getResourceAsString(String name) {
        String filename = "org/dreamcat/code/generator/fastapi_curd/" + name;
        return ClassLoaderUtil.getResourceAsString(filename);
    }

    protected String mappingPythonType(ColumnDef column) {
        boolean notNull = column.isNotNull();
        JDBCType type = column.getType();

        PythonType pythonType = PythonType.of(type);
        if (pythonType == null) {
            if (JDBCType.BIT.equals(type)) {
                if (Objects.equals(column.getTypeLength(), 1)) {
                    pythonType = PythonType.bool;
                } else {
                    pythonType = PythonType.str;
                }
            } else {
                pythonType = PythonType.str;
            }
        }
        if (notNull) {
            return pythonType.toString();
        } else {
            return "Optional[" + pythonType + "]";
        }
    }

    public enum PythonType {

        _int(JDBCType.INTEGER, JDBCType.BIGINT, JDBCType.TINYINT, JDBCType.SMALLINT),
        _float(JDBCType.DECIMAL, JDBCType.NUMERIC, JDBCType.DOUBLE,
                JDBCType.FLOAT, JDBCType.REAL),
        str(JDBCType.VARCHAR, JDBCType.CHAR,
                JDBCType.LONGNVARCHAR, JDBCType.LONGVARCHAR, JDBCType.NCHAR),
        bool(JDBCType.BOOLEAN),
        datetime(JDBCType.TIMESTAMP, JDBCType.DATE, JDBCType.TIMESTAMP_WITH_TIMEZONE),
        ;

        final JDBCType[] types;

        PythonType(JDBCType... types) {
            this.types = types;
        }

        @Override
        public String toString() {
            return super.toString().replace("_", "");
        }

        public static PythonType of(JDBCType type) {
            for (PythonType value : values()) {
                for (JDBCType valueType : value.types) {
                    if (valueType.equals(type)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }
}
