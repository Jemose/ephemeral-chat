package com.underarmour.interview.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Registry Application
 *
 * @author david.moore
 */
@EnableEurekaServer
@SpringBootApplication
public class ServiceRegistryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
