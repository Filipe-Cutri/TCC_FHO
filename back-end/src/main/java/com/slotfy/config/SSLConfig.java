package com.slotfy.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * SSL Configuration for HTTPS support and HTTP to HTTPS redirect.
 * This configuration is enabled only in development and production profiles.
 */
@Configuration
@Profile({"dev", "prod"}) // Only enable in development and production
public class SSLConfig {

    /**
     * Configure Tomcat with both HTTP and HTTPS connectors.
     * HTTP connector redirects all traffic to HTTPS.
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // Security constraint to force HTTPS
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                
                context.addConstraint(securityConstraint);
            }
        };
        
        // Add HTTP connector that redirects to HTTPS
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    /**
     * Create HTTP connector that redirects to HTTPS.
     */
    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}