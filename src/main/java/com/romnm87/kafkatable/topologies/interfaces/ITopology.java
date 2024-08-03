package com.romnm87.kafkatable.topologies.interfaces;

import org.apache.kafka.streams.StreamsBuilder;

public interface ITopology {
    void process(StreamsBuilder streamsBuilder);
}
