package ru.practicum.service;

import ru.practicum.stat.avro.UserActionAvro;
import ru.practicum.grpc.stats.request.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.request.RecommendedEventProto;
import ru.practicum.grpc.stats.request.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.request.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationService {

    List<RecommendedEventProto> generateRecommendationsForUser(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);

    void saveUserAction(UserActionAvro userActionAvro);
}
