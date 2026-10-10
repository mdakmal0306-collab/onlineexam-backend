package com.example.examination.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${spring.datasource.url:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String username;

    @Value("${spring.datasource.password:#{null}}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = (databaseUrl != null && !databaseUrl.isBlank()) ? databaseUrl : springDatasourceUrl;

        HikariConfig config = new HikariConfig();

        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                // Convert Render's postgres://user:password@host:port/dbname to standard JDBC format
                URI uri = new URI(rawUrl);
                String userInfo = uri.getUserInfo();
                String dbUser = username;
                String dbPassword = password;

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    dbUser = parts[0];
                    dbPassword = parts[1];
                }

                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }

                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + path;

                logger.info("Configured PostgreSQL DataSource from Render URL for host: {}, database: {}", uri.getHost(), path);
                config.setJdbcUrl(jdbcUrl);
                if (dbUser != null) {
                    config.setUsername(dbUser);
                }
                if (dbPassword != null) {
                    config.setPassword(dbPassword);
                }
                config.setDriverClassName("org.postgresql.Driver");
            } catch (Exception e) {
                logger.error("Failed to parse PostgreSQL URL as URI, using as-is: {}", e.getMessage());
                config.setJdbcUrl(rawUrl);
                if (username != null) config.setUsername(username);
                if (password != null) config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
            }
        } else if (rawUrl != null && rawUrl.startsWith("jdbc:postgresql:")) {
            config.setJdbcUrl(rawUrl);
            if (username != null) config.setUsername(username);
            if (password != null) config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
        } else {
            // Fallback for local development (MySQL / H2)
            String localUrl = rawUrl != null ? rawUrl : "jdbc:mysql://localhost:3306/online_exam?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            config.setJdbcUrl(localUrl);
            config.setUsername(username != null ? username : "root");
            config.setPassword(password != null ? password : "root");
            if (localUrl.contains("mysql")) {
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            }
        }

        // Optimized connection pool settings for Render free/starter tiers (512MB RAM)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1200000);

        return new HikariDataSource(config);
    }
}
