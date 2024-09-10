package com.romnm87.kafkatable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class KafkaTableApplication {
    @Autowired
    private KafkaStreams kafkaStreams;
    private ExecutorService executorService;

    public static void main(String[] args) {
        SpringApplication.run(KafkaTableApplication.class, args);
    }

    private Runnable startStream() {
        return () -> {
            log.warn("Kafka Streams starting...");
            this.kafkaStreams.start();
        };
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            log.warn("Starting service single pool...");
            this.executorService = Executors.newSingleThreadExecutor();
            this.executorService.submit(this.startStream());
        };
    }

    @PreDestroy
    public void destroy() {
        log.warn("Stopping service single pool...");
        this.executorService.shutdownNow();
    }

    @PostConstruct
    public void cleanUpStates() {
        this.kafkaStreams.cleanUp();
    }
}
