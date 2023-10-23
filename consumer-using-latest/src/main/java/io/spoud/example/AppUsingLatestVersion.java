package io.spoud.example;

import io.spoud.example.Offer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

public class AppUsingLatestVersion
{

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUsingLatestVersion.class);
    private static final String topic = "offer";
    public static void main( String[] args ) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "group1-using-latest");
        props.put("use.latest.version", "true");
        props.put("auto.register.schemas", "false");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true); // without this line the value will be generic record!!
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", "http://localhost:8081");

        KafkaConsumer<String, Offer> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(java.util.Collections.singletonList(topic));

        while (true) {
            consumer.poll(java.time.Duration.ofMillis(100)).forEach(record -> {
                Offer value = record.value();
                LOGGER.info("Received message offset={}, partition={}, value={}", record.offset(), record.partition(), value);
            });
        }
    }

}
