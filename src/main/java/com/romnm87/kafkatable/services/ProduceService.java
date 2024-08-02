package com.romnm87.kafkatable.services;

import com.google.gson.Gson;
import com.romnm87.kafkatable.dtos.Purchase;
import com.romnm87.kafkatable.topologies.GroupPurchaseTopology;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ProduceService {
    private final KafkaProducer<String, String> kafkaProducer;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public ProduceService(KafkaProducer<String, String> kafkaProducer, StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.kafkaProducer = kafkaProducer;
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
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

    /**
     * Извлеч результаты группировка Apache Kafka из хранилища состояний GroupPurchaseTopology.PURCHASES_GROUPS_STORE
     * @return List<Purchase>
     */
    public List<Purchase> getPurchaseGroups() {
        ReadOnlyKeyValueStore<String, Purchase> purchasesStoreData = streamsBuilderFactoryBean.getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        GroupPurchaseTopology.PURCHASES_GROUPS_STORE,
                        QueryableStoreTypes.keyValueStore()
                ));

        var purchases = purchasesStoreData.all();

        var spliterator = Spliterators.spliteratorUnknownSize(purchases, 0);

        return StreamSupport.stream(spliterator, false)
                .map(data -> data.value)
                .collect(Collectors.toList());
    }
}
