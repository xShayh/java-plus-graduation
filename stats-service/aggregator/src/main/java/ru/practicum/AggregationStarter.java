package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.stat.avro.EventSimilarityAvro;
import ru.practicum.stat.avro.UserActionAvro;
import ru.practicum.service.SimilarityService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final SimilarityService similarityService;
    private final Consumer<Long, UserActionAvro> consumer;
    private final KafkaConfig kafkaConfig;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(kafkaConfig.getKafkaProperties().getUserActionTopic()));
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer
                        .poll(Duration.ofMillis(kafkaConfig.getKafkaProperties().getConsumerAttemptTimeout()));
                int count = 0;
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    log.info("UserActionAvro got from consumer: {}", record);
                    handleRecord(record);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignores) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                consumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                similarityService.close();
            }
        }
    }

    private void handleRecord(ConsumerRecord<Long, UserActionAvro> consumerRecord) throws InterruptedException {
        List<EventSimilarityAvro> eventSimilarityList = similarityService.updateSimilarity(consumerRecord.value());
        for (EventSimilarityAvro eventSimilarity : eventSimilarityList) {
            similarityService.collectEventSimilarity(eventSimilarity);
        }
    }

    private void manageOffsets(ConsumerRecord<Long, UserActionAvro> consumerRecord, int count, Consumer<Long, UserActionAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                new OffsetAndMetadata(consumerRecord.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}
