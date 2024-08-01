package com.romnm87.kafkatable.topologies.interfaces;

import org.apache.kafka.streams.StreamsBuilder;

public interface IGroupPurchaseTopology {
    void process(StreamsBuilder streamsBuilder);
}
