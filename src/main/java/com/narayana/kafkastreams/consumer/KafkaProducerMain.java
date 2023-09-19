package com.narayana.kafkastreams.consumer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaProducerMain {
    public static void main(String[] args) {
        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put("key.serializer", StringSerializer.class);
        streamsConfiguration.put("value.serializer", StringSerializer.class);
        KafkaProducer kafkaProducer = new KafkaProducer<String, String>(streamsConfiguration);
        String topicName = "dataIn4";
        Integer partition = 0;
        String key = "key";
        String values[] = {"hello! nice to meet you", "test is successful", "welcome to the new home", "dummy assets", "source started", "target recahed", "how are you"};
        int counter = 0;
        for(String value: values) {
            ProducerRecord producerRecord = new ProducerRecord(topicName, partition, key+counter, value);
            kafkaProducer.send(producerRecord);
            counter++;
        }
        kafkaProducer.flush();
        kafkaProducer.close();
        System.out.println(" Total " + counter + " sent successfully");
        System.out.println(" Producer shutdown");
    }
}
