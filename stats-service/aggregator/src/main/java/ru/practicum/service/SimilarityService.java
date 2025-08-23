package ru.practicum.service;

import ru.practicum.stat.avro.EventSimilarityAvro;
import ru.practicum.stat.avro.UserActionAvro;

import java.util.List;

public interface SimilarityService {

    List<EventSimilarityAvro> updateSimilarity(UserActionAvro userAction);

    void collectEventSimilarity(EventSimilarityAvro eventSimilarityAvro);

    default void close() {

    }
}
