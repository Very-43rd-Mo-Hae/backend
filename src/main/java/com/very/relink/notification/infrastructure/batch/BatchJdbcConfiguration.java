package com.very.relink.notification.infrastructure.batch;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJdbcConfiguration extends JdbcDefaultBatchConfiguration {

    private final DataSource batchDataSource;
    private final PlatformTransactionManager batchTransactionManager;

    public BatchJdbcConfiguration(
            @Qualifier("batchDataSource") DataSource batchDataSource,
            @Qualifier("batchTransactionManager") PlatformTransactionManager batchTransactionManager
    ) {
        this.batchDataSource = batchDataSource;
        this.batchTransactionManager = batchTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return batchDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return batchTransactionManager;
    }

    @Override
    protected String getTablePrefix() {
        return "BATCH_";
    }
}
