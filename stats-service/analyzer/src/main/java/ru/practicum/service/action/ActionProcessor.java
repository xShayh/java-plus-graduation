package ru.practicum.service.action;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.KafkaConfig;
import ru.practicum.configuration.KafkaTopic;
import ru.practicum.configuration.UserActionWeightConfig;
import ru.practicum.model.action.ActionType;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class ActionProcessor implements Runnable {
    private final ActionService actionService;
    private final EnumMap<KafkaTopic, String> topics;
    private final KafkaConsumer<Long, UserActionAvro> consumer;

    public ActionProcessor(UserActionWeightConfig userActionWeightConfig,
                           KafkaConfig kafkaConfig,
                           ActionService actionService) {
        this.actionService = actionService;
        topics = kafkaConfig.getTopics();
        consumer = new KafkaConsumer<>(kafkaConfig.getActionConsumerProps());
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(topics.get(KafkaTopic.USER_ACTIONS)));

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(Duration.ofMillis(5000));
                if (records.isEmpty()) continue;

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    actionService.addUserAction(record.value().getUserId(), record.value().getEventId(),
                            ActionType.valueOf(record.value().getActionType().name()),
                            record.value().getTimestamp());
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
