package productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import productservice.event.ProductEvent;
import productservice.kafka.ProductEventProducer;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class KafkaIntegrationTest {
    @Autowired
    private ProductEventProducer productEventProducer;

    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldPublishProductEvent() {

        ProductEvent event = new ProductEvent(
                "PRODUCT_CREATED",
                UUID.randomUUID(),
                "MacBook Pro",
                "Apple laptop",
                "apple",
                "laptops",
                BigDecimal.valueOf(249999),
                true,
                4.9
        );

        productEventProducer.sendProductEvent(event);
    }

    @Test
    void shouldPublishAndConsumeProductEvent() {

        Properties props = new Properties();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-group"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );

        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*"
        );

        KafkaConsumer<String, ProductEvent> consumer =
                new KafkaConsumer<>(
                        props,
                        new StringDeserializer(),
                        new JsonDeserializer<>(ProductEvent.class)
                );

        consumer.subscribe(Collections.singletonList("product-events"));

        ProductEvent event = new ProductEvent(
                "PRODUCT_CREATED",
                UUID.randomUUID(),
                "MacBook Pro",
                "Apple laptop",
                "apple",
                "laptops",
                BigDecimal.valueOf(249999),
                true,
                4.9
        );

        productEventProducer.sendProductEvent(event);

        ConsumerRecords<String, ProductEvent> records =
                consumer.poll(Duration.ofSeconds(10));

        boolean found = false;

        for (ConsumerRecord<String, ProductEvent> record : records) {

            ProductEvent received = record.value();

            if (received.getName().equals("MacBook Pro")) {
                found = true;
                break;
            }
        }

        assertThat(found).isTrue();

        consumer.close();
    }
}