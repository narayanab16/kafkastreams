package com.narayana.kafkastreams.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ApplicationKStreamExample {

    public static void main(final String[] args) {
        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "application-kstream-demo");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "application-kstream-demo-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final boolean doReset = args.length > 1 && args[1].equals("--reset");
        final KafkaStreams streams = buildKafkaStreams(streamsConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        // Delete the application's local state on reset
        if (doReset) {
            streams.cleanUp();
        }
        startKafkaStreamsSynchronously(streams);
    }

    static KafkaStreams buildKafkaStreams(final Properties streamsConfiguration) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> input = builder.stream("dataIn4");
        System.out.println(" buildKafkaStreams --> " + input);
        KStream<String, Long> countStream = input.flatMapValues(value -> Arrays.asList(value.split(" ")))
                .groupBy((key, value) -> {
                    System.out.println(" key: " + key + ", value : " + value);
                    return value;
                })
                .count(Materialized.as("counts-store"))
                .toStream();
        // Print data to the console
        countStream.print(Printed.toSysOut());
        // Send data to the output topic
        countStream.to("dataOut0", Produced.with(Serdes.String(),Serdes.Long()));
        System.out.println(" buildKafkaStreams --> " + "out");

        Topology topology = builder.build();
        System.out.println("Kstream Topology : " + topology.describe());
        return new KafkaStreams(topology, streamsConfiguration);
    }

    static void startKafkaStreamsSynchronously(final KafkaStreams streams) {
        final CountDownLatch latch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            System.out.println("old : " + oldState.isRunning() + ", new : " + newState.isRunning());
            if (oldState == KafkaStreams.State.REBALANCING && newState == KafkaStreams.State.RUNNING) {
                latch.countDown();
            }
        });
        // start kstream service
        streams.start();
        try {
            latch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

