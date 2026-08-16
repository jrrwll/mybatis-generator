package org.dreamcat.code.generator;

import org.junit.jupiter.api.Test;

/**
 * @author Jerry Will
 * @version 2025-10-11
 */
class MainTest {

    @Test
    void test() {
        Main.main(new String[]{"-h"});
        Main.main(new String[]{"mb", "-c", "./src/test/resources/mb.json",
                "-s", "../src/test/resources/ddl.sql"});
        Main.main(new String[]{"ra", "-c", "./src/test/resources/ra.json",
                "-s", "./src/test/resources/ddl_classic.sql"});
        Main.main(new String[]{"fc", "-c", "./src/test/resources/fc.json",
                "-s", "./src/test/resources/ddl_classic.sql"});
    }
}
