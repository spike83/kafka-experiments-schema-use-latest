package io.spoud.example;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

public class App
{

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String topic = "offer";
    public static void main( String[] args ) throws InterruptedException {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", "http://localhost:8081");

        try (KafkaProducer<String, io.spoud.example.Offer> producer = new KafkaProducer<>(props)) {
            while (true) {
                ProducerRecord<String, io.spoud.example.Offer> record = createRandomRecord();
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        LOGGER.error("failed to commit", exception);
                        System.exit(1);
                    } else {
                        LOGGER.info("Committed message offset={}, partition={}, value={}", metadata.offset(), metadata.partition(), record.value());
                    }
                });
                Thread.sleep(500);
            }
        }
    }

    private static ProducerRecord<String,io.spoud.example.Offer> createRandomRecord() {
        return new ProducerRecord<>(topic, UUID.randomUUID().toString(), io.spoud.example.Offer.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setContent("content " + UUID.randomUUID().toString())
                .build());
    }
}
