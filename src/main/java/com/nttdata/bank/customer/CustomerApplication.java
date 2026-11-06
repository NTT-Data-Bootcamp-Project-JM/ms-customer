package com.nttdata.bank.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Owns personal and business customers. No other service writes this database.
 */
@SpringBootApplication
public class CustomerApplication {

    /**
     * Starts the customer service.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
