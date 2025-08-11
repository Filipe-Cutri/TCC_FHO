package com.slotify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.slotify.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration will be added here as needed
}