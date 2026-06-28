package com.very.relink.notification.infrastructure.batch;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("batch.datasource")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource batchDataSource(
            @Qualifier("batchDataSourceProperties") DataSourceProperties batchDataSourceProperties
    ) {
        return batchDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public PlatformTransactionManager batchTransactionManager(
            @Qualifier("batchDataSource") DataSource batchDataSource
    ) {
        return new DataSourceTransactionManager(batchDataSource);
    }

    @Bean
    public DataSourceScriptDatabaseInitializer batchDataSourceScriptDatabaseInitializer(
            @Qualifier("batchDataSource") DataSource batchDataSource
    ) {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(java.util.List.of("classpath:db/batch-schema-mysql.sql"));
        settings.setContinueOnError(true);
        return new DataSourceScriptDatabaseInitializer(batchDataSource, settings);
    }
}
