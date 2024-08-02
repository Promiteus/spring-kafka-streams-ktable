package com.romnm87.kafkatable.topologies;

import com.romnm87.kafkatable.dtos.Purchase;
import com.romnm87.kafkatable.topologies.interfaces.IGroupPurchaseTopology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class GroupPurchaseTopology implements IGroupPurchaseTopology {
    public final static String PURCHASE_INPUT_TOPIC = "purchases";
    public final static String PURCHASES_GROUPS_STORE = "purchases_groups_store";

    @Override
    public void process(StreamsBuilder streamsBuilder) {
        KStream<String, Purchase> purchasesSteam = streamsBuilder.stream(PURCHASE_INPUT_TOPIC, Consumed.with(Serdes.String(), new JsonSerde<>(Purchase.class)).withOffsetResetPolicy(Topology.AutoOffsetReset.LATEST))
                .selectKey((k, v) -> v.getName());

        purchasesSteam.print(Printed.<String, Purchase>toSysOut().withLabel("purchases_topic"));

        KGroupedStream<String, Purchase> purchaseGroups = purchasesSteam.map((key, purchase) -> KeyValue.pair(purchase.getName(), purchase))
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Purchase.class)));

        KTable<String, Purchase> purchaseKTable = purchaseGroups.reduce((purchase, v1) -> Purchase.builder()
                .currency(purchase.getCurrency())
                .id(purchase.getId())
                .timestamp(v1.getTimestamp())
                .name(purchase.getName()).price(purchase.getPrice()+v1.getPrice()).build(), Materialized.as(PURCHASES_GROUPS_STORE));

        purchaseKTable.toStream().print(Printed.<String, Purchase>toSysOut().withLabel("purchases_groups"));
    }
}
