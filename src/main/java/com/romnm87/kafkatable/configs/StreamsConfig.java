package com.romnm87.kafkatable.configs;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class StreamsConfig {
    @Bean
    public Properties kafkaStreamsProps() {
        Properties properties = new Properties();
        properties.put(org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG, "my-streams-app");
        properties.put(org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return properties;
    }

    @Bean
    public Properties kafkaProducerProps() {
        Properties properties = new Properties();
        properties.put(org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG, "my-streams-app-2");
        properties.put(org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return properties;
    }

    @Bean
    public KafkaProducer<String,String> kvKafkaProducer(Properties kafkaProducerProps) {
        return new KafkaProducer<String, String>(kafkaProducerProps);
    }

    @Bean
    public StreamsBuilder streamsBuilder() {
        return new StreamsBuilder();
    }
}
