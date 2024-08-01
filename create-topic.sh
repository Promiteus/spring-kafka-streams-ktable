#!/bin/bash

docker-compose exec -T broker /bin/kafka-topics --create --topic purchases \
  --partitions 1 --replication-factor 1 --bootstrap-server broker:9092