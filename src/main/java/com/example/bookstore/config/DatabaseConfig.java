package com.example.bookstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.bookstore.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuration is handled by application.properties
}