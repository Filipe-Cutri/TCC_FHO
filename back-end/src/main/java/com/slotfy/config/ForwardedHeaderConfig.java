package com.slotfy.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Configuration for handling forwarded headers from reverse proxies.
 * 
 * This is essential for production deployment on Railway where:
 * - Railway's proxy terminates TLS/HTTPS
 * - Requests are forwarded to the app as HTTP
 * - X-Forwarded-Proto, X-Forwarded-Host, etc. headers indicate the original request scheme
 * 
 * Without this filter, Spring Security's HTTPS redirect creates an infinite loop
 * because the app sees HTTP and keeps redirecting to HTTPS.
 */
@Configuration
@Profile("prod")
public class ForwardedHeaderConfig {

    /**
     * Register ForwardedHeaderFilter with highest precedence.
     * This ensures the filter processes headers before any security checks.
     */
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
