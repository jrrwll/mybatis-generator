package org.dreamcat.cli.generator.mybatis.sql;

import java.util.List;
import lombok.Data;

/**
 * @author Jerry Will
 * @version 2021-12-06
 */
@Data
public class IndexDef {

    private String name;
    private List<String> columns;

}
