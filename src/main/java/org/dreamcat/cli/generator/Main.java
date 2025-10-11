package org.dreamcat.cli.generator;

import lombok.Data;
import lombok.SneakyThrows;
import org.dreamcat.cli.generator.Main.FastapiCurd;
import org.dreamcat.cli.generator.Main.Mybatis;
import org.dreamcat.cli.generator.Main.ReactAdmin;
import org.dreamcat.cli.generator.base.SqlBasedGenerator;
import org.dreamcat.cli.generator.fastapi_curd.FastapiCurdGenerator;
import org.dreamcat.cli.generator.fastapi_curd.FastapiCurdGeneratorConfig;
import org.dreamcat.cli.generator.mybatis.MyBatisGenerator;
import org.dreamcat.cli.generator.mybatis.MyBatisGeneratorConfig;
import org.dreamcat.cli.generator.react_admin.ReactAdminGenerator;
import org.dreamcat.cli.generator.react_admin.ReactAdminGeneratorConfig;
import org.dreamcat.common.Pair;
import org.dreamcat.common.argparse.ArgParserContext;
import org.dreamcat.common.argparse.ArgParserEntrypoint;
import org.dreamcat.common.argparse.ArgParserField;
import org.dreamcat.common.argparse.ArgParserType;
import org.dreamcat.common.argparse.SubcommandArgParser;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.reflect.ObjectType;
import org.dreamcat.common.util.ObjectUtil;

import java.io.File;

/**
 * @author Jerry Will
 * @version 2025-09-23
 */
@SuppressWarnings({"unchecked"})
@ArgParserType(subcommands = {
        Mybatis.class,
        ReactAdmin.class,
        FastapiCurd.class
}, allProperties = true)
public class Main implements ArgParserEntrypoint {

    public static void main(String[] args) {
        SubcommandArgParser argParser = new SubcommandArgParser(Main.class);
        argParser.run(args);
    }

    @ArgParserField(firstChar = true, helpDesc = "print the help info")
    private boolean help;

    @Override
    public void run(ArgParserContext context) {
        System.err.println("Need a subcommand to run");
        System.out.println(context.getHelp());
    }

    @Data
    public static abstract class Action<C, G extends SqlBasedGenerator> implements ArgParserEntrypoint {

        @ArgParserField(firstChar = true, helpDesc = "print the help info")
        private boolean help;

        @ArgParserField(firstChar = true, helpDesc = "verbose mode")
        private boolean verbose;

        @ArgParserField(value = {"c", "conf", "config"}, helpDesc = "config json file")
        private File config;

        @ArgParserField(value = {"cc"}, helpDesc = "config json content")
        private String configContent;

        @ArgParserField(value = {"s"}, helpDesc = "ddl sql file")
        protected File sql;

        @ArgParserField(value = {"sc"}, helpDesc = "ddl sql content")
        private String sqlContent;

        private Pair<Class<C>, Class<G>> getConfigAndGeneratorType() {
            ObjectType objectType = ObjectType.fromType(getClass().getGenericSuperclass());
            return Pair.of(
                    objectType.getParameterType(0).getType(),
                    objectType.getParameterType(1).getType()
            );
        }

        @Override
        @SneakyThrows
        public void run(ArgParserContext context) {
            if (help) {
                System.out.println(context.getHelp());
                return;
            }
            if (verbose) {
                System.out.println("generate with config:");
                System.out.println(JsonUtil.toJsonWithPretty(this));
            }
            if (config == null && ObjectUtil.isBlank(configContent)) {
                System.err.println("Need a config file or content, pass by --config-file <file> | --config-content <content>");
                System.exit(1);
                return;
            }
            if (sql == null && ObjectUtil.isBlank(sqlContent)) {
                System.err.println("Need sql file or content, pass by --sql <ddl_sqls_file> | --sql-content <ddl_sqls>");
                System.exit(1);
                return;
            }

            Pair<Class<C>, Class<G>> pair = getConfigAndGeneratorType();
            Class<C> configType = pair.first();
            C configObj;
            if (config != null) {
                configObj = JsonUtil.fromJson(this.config, configType);
            } else {
                configObj = JsonUtil.fromJson(this.configContent, configType);
            }
            String sqlStr;
            if (sql != null) {
                sqlStr = FileUtil.readAsString(sql);
            } else {
                sqlStr = sqlContent;
            }

            Class<G> generatorType = pair.second();
            G generator = generatorType.getConstructor(configType).newInstance(configObj);
            if (generator.isSupportGenerateBoilerplate()) {
                generator.generateBoilerplate();
            }
            generator.generate(sqlStr);
        }
    }

    @ArgParserType(command = {"mybatis", "mb"},
            commandDesc = "generate mybatis code",
            allProperties = true)
    public static class Mybatis extends Action<
            MyBatisGeneratorConfig, MyBatisGenerator> {
    }

    @ArgParserType(command = {"react-admin", "ra"},
            commandDesc = "generate the whole react admin project",
            allProperties = true)
    public static class ReactAdmin extends Action<
            ReactAdminGeneratorConfig, ReactAdminGenerator> {
    }

    @ArgParserType(command = {"fastapi-curd", "fc"},
            commandDesc = "generate the whole fastapi curd project",
            allProperties = true)
    public static class FastapiCurd extends Action<
            FastapiCurdGeneratorConfig, FastapiCurdGenerator> {
    }
}
