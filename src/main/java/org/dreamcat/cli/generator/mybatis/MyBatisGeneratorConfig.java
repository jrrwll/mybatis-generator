package org.dreamcat.cli.generator.mybatis;

import java.util.Set;
import lombok.Data;

/**
 * @author Jerry Will
 * @version 2021-12-07
 */
@Data
public class MyBatisGeneratorConfig {

    private Set<String> ignoreSelectColumns;
    private Set<String> ignoreInsertColumns;

}
