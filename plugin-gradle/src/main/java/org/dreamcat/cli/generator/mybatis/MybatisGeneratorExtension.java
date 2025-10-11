package org.dreamcat.cli.generator.mybatis;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

/**
 * @author Jerry Will
 * @version 2024-12-22
 */
public abstract class MybatisGeneratorExtension {

    abstract Property<String> getSqlPath();

    abstract Property<String> getJdbcUrl();

    abstract Property<String> getJdbcUser();

    abstract Property<String> getJdbcPassword();

    abstract Property<String> getJdbcDriverClassName();

    abstract Property<Boolean> getOverwrite();

    abstract Property<String> getSrcDir();

    abstract Property<String> getSqlMapperDir();

    abstract Property<String> getExtendsSqlMapperDir();

    abstract Property<String> getEntityPackageName();

    abstract Property<String> getMapperPackageName();

    abstract Property<String> getExtendsMapperPackageName();

    abstract Property<String> getConditionPackageName();

    abstract ListProperty<String> getIgnoreColumns();

    abstract Property<Boolean> getForceInt();

    abstract Property<Boolean> getForceDecimal();

    abstract Property<Boolean> getTinyint1AsBool();

    abstract Property<Boolean> getEnableResultMapWithBLOBs();

    abstract Property<Boolean> getEnableExtendsMapper();

    abstract Property<Boolean> getAddMapperAnnotation();

    abstract Property<Boolean> getEnableGeneratedKeys();

    abstract Property<Boolean> getEnableLombok();

    abstract Property<Boolean> getAddComments();

    abstract Property<Character> getDelimitKeyword();

    abstract Property<String> getNameRegex();

    abstract Property<String> getNameReplacement();

    abstract Property<String> getEntityNamePrefix();

    abstract Property<String> getEntityNameSuffix();

    abstract Property<String> getMapperNamePrefix();

    abstract Property<String> getMapperNameSuffix();

    abstract Property<String> getExtendsMapperNamePrefix();

    abstract Property<String> getExtendsMapperNameSuffix();

    abstract Property<String> getConditionNamePrefix();

    abstract Property<String> getConditionNameSuffix();

    abstract Property<String> getPropertyNameRegex();

    abstract Property<String> getPropertyNameReplacement();

    abstract ListProperty<String> getPrunedStatements();

    abstract ListProperty<String> getTableNames();

    public MybatisGeneratorExtension() {
        getEnableExtendsMapper().convention(true);
    }

    abstract NamedDomainObjectContainer<Table> getTables();

    public interface Table {

        String getName(); // for NamedDomainObjectContainer

        Property<String> getEntityName();

        Property<String> getMapperName();

        Property<String> getExtendsMapperName();

        Property<String> getConditionName();

        MapProperty<String, String> getPropertyNames();
    }
}
