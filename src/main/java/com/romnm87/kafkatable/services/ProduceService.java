package com.romnm87.kafkatable.services;

import com.google.gson.Gson;
import com.romnm87.kafkatable.dtos.Purchase;
import com.romnm87.kafkatable.topologies.GroupPurchaseTopology;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProduceService {
    private final KafkaProducer<String, String> kafkaProducer;

    public ProduceService(KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Отправить сообщения в топик Apache Kafka
     * @param key String
     * @param purchases List<Purchase>
     */
    public void sendMessage(String key, List<Purchase> purchases) {
       if ((purchases != null) && (purchases.size() > 0)) {
           purchases.forEach(purchase -> {
               Gson gson = new Gson();
               String value = gson.toJson(purchase);
               kafkaProducer.send(new ProducerRecord<>(GroupPurchaseTopology.PURCHASE_INPUT_TOPIC, key, value), new Callback() {
                   @Override
                   public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                       if (e == null) {
                           System.out.println("Message sent successfully. Offset: " + recordMetadata.offset());
                       } else {
                           System.err.println("Error sending message: " + e.getMessage());
                       }
                   }
               });
           });
       }
    }
}
