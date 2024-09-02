package com.incrementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the Increment Service application.
 */
@SpringBootApplication
public class IncrementServiceApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        SpringApplication.run(IncrementServiceApplication.class, args);
    }

}
