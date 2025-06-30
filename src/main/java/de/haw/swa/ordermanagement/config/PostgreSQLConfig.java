package de.haw.swa.ordermanagement.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Configuration class for PostgreSQL-specific settings.
 * This configuration is only active when the 'postgres' profile is enabled.
 */
@Configuration
@Profile("postgres")
public class PostgreSQLConfig {

    /**
     * Database initializer for PostgreSQL.
     * Executes schema and data scripts when the application starts.
     */
    @Bean
    @ConditionalOnProperty(
        name = "spring.sql.init.mode", 
        havingValue = "always", 
        matchIfMissing = false
    )
    public DataSourceInitializer postgresDataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        // Add data script
        populator.addScript(new ClassPathResource("data-postgres.sql"));
        
        // Configure populator settings
        populator.setContinueOnError(true);
        populator.setIgnoreFailedDrops(true);
        
        initializer.setDatabasePopulator(populator);
        
        return initializer;
    }
}