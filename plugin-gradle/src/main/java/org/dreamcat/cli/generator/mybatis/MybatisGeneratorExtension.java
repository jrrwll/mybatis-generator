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

    abstract Property<Boolean> getEnableResultMapWithBLOBs();

    abstract Property<Boolean> getEnableExtendsMapper();

    abstract Property<Boolean> getAddMapperAnnotation();

    abstract Property<Boolean> getEnableLombok();

    abstract Property<Boolean> getAddComments();

    abstract Property<Character> getDelimitKeyword();

    abstract Property<String> getNamePrefix();

    abstract Property<String> getNameSuffix();

    abstract Property<String> getEntityName();

    abstract Property<String> getMapperName();

    abstract Property<String> getExtendsMapperName();

    abstract Property<String> getConditionName();

    abstract Property<String> getPropertyName();

    abstract ListProperty<String> getPrunedStatements();

    abstract ListProperty<String> getTableNames();

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
