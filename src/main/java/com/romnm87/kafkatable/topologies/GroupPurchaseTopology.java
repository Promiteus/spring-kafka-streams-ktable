package com.romnm87.kafkatable.topologies;

import com.romnm87.kafkatable.topologies.interfaces.IGroupPurchaseTopology;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.stereotype.Component;

@Component
public class GroupPurchaseTopology implements IGroupPurchaseTopology {
    @Override
    public void process(StreamsBuilder streamsBuilder) {

    }
}
