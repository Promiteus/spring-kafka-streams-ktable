package com.romnm87.kafkatable;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaTableApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaTableApplication.class, args);
    }
}
