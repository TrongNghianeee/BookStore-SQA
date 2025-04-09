package com.example.bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookstoreApplication {

    public static void main(String[] args) {
        // Enable legacy SSL/TLS protocols
        System.setProperty("com.microsoft.sqlserver.jdbc.SSL_PROTOCOL", "TLS");
        System.setProperty("javax.net.ssl.trustStore", "NONE");

        // Disable SSL verification
        System.setProperty("javax.net.ssl.trustAllCertificates", "true");

        SpringApplication.run(BookstoreApplication.class, args);
    }
}
