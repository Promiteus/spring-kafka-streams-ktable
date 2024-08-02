package com.romnm87.kafkatable.configs;

import com.romnm87.kafkatable.topologies.GroupPurchaseTopology;
import com.romnm87.kafkatable.topologies.interfaces.IGroupPurchaseTopology;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class StreamsConfig {
    @Autowired
    private IGroupPurchaseTopology groupPurchaseTopology;
    private KafkaStreams kafkaStreams;
    private ExecutorService singleService;

    /*@Bean
    public NewTopic topicBuilder() {
        return TopicBuilder.name(GroupPurchaseTopology.PURCHASE_INPUT_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }*/

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

    /**
     * Топология исполняется в отдельном потоке
     * @param streamsBuilder StreamsBuilder
     * @param kafkaStreamsProps Properties
     * @return Runnable
     */
    private Runnable executeTopology(StreamsBuilder streamsBuilder, Properties kafkaStreamsProps) {
        return () -> {
            this.groupPurchaseTopology.process(streamsBuilder);
            this.kafkaStreams = new KafkaStreams(streamsBuilder.build(), kafkaStreamsProps);
            this.kafkaStreams.start();
        };
    }

    @Bean
    public CommandLineRunner exec() {
        return args -> {
            singleService = Executors.newSingleThreadExecutor();
            singleService.submit(this.executeTopology(streamsBuilder(), kafkaStreamsProps()));
        };
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        this.kafkaStreams.close();
        //this.singleService.awaitTermination(30, TimeUnit.SECONDS);
        this.singleService.shutdownNow();
    }
}
