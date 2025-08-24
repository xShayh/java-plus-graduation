package ru.practicum.service.similarity;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.KafkaConfig;
import ru.practicum.configuration.KafkaTopic;
import ru.practicum.model.similarity.Similarity;
import ru.practicum.model.similarity.SimilarityCompositeKey;
import ru.practicum.repository.SimilarityRepository;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class SimilarityProcessor implements Runnable {
    private final SimilarityRepository similarityRepository;
    private final EnumMap<KafkaTopic, String> topics;
    private final KafkaConsumer<Long, EventSimilarityAvro> consumer;

    public SimilarityProcessor(KafkaConfig kafkaConfig, SimilarityRepository repository) {
        this.similarityRepository = repository;
        topics = kafkaConfig.getTopics();
        consumer = new KafkaConsumer<>(kafkaConfig.getSimilarityConsumerProps());
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.EVENTS_SIMILARITY)));

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(Duration.ofMillis(5000));
                if (records.isEmpty()) continue;

                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    Similarity similarity = new Similarity();
                    similarity.setKey(new SimilarityCompositeKey(record.value().getEventA(), record.value().getEventB()));
                    similarity.setScore(record.value().getScore());
                    similarityRepository.save(similarity);
                }
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Error when fixing offsets: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
            // Остановка потребителя
        } catch (Exception error) {
            log.error("Error when processing user actions", error);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    public void stop() {
        consumer.wakeup();
    }
}
