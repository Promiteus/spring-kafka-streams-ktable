package com.romnm87.kafkatable.topologies;

import com.romnm87.kafkatable.dtos.Purchase;
import com.romnm87.kafkatable.topologies.interfaces.ITopology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class GroupPurchaseTopology implements ITopology {
    public final static String PURCHASE_INPUT_TOPIC = "purchases";
    public final static String PURCHASES_GROUPS_STORE = "purchases_groups_store";

    @Override
    public void process(StreamsBuilder streamsBuilder) {
        //Получать только последние данные из топика PURCHASE_INPUT_TOPIC -- Topology.AutoOffsetReset.LATEST.
        //Модифицировать поток данных (selectKey()) добавление в качестве ключа поля name модели Purchase
        KStream<String, Purchase> purchasesSteam = streamsBuilder.stream(PURCHASE_INPUT_TOPIC, Consumed.with(Serdes.String(), new JsonSerde<>(Purchase.class)).withOffsetResetPolicy(Topology.AutoOffsetReset.LATEST))
                .selectKey((k, v) -> v.getName());

        //Вывести в консоль исходные данные топика с новым ключом на базе поля name. Метка в консоле [purchases_topic]
        purchasesSteam.print(Printed.<String, Purchase>toSysOut().withLabel("purchases_topic"));


        // Сгруппировать поток данных по ключу name
        // purchasesSteam.map() для ребалансировки топиков
        KGroupedStream<String, Purchase> purchaseGroups = purchasesSteam.map((key, purchase) -> KeyValue.pair(purchase.getName(), purchase))
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Purchase.class)));

        // Сложить цены сгруппированные данных из хранили со сгруппированными данными, поступающими из потока данных
        KTable<String, Purchase> purchaseKTable = purchaseGroups.reduce((purchase, v1) -> Purchase.builder()
                .currency(purchase.getCurrency())
                .id(purchase.getId())
                .timestamp(v1.getTimestamp())
                .name(purchase.getName())
                .price(purchase.getPrice()+v1.getPrice()).build(), Materialized.as(PURCHASES_GROUPS_STORE));

        purchaseKTable.toStream().print(Printed.<String, Purchase>toSysOut().withLabel("purchases_table"));
    }
}
