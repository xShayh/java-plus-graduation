package ru.practicum.stat;

import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.request.RecommendedEventProto;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public interface StatClient {

    void registerUserAction(long eventId, long userId, ActionTypeProto actionType, Instant instant);

    Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults);

    Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults);

    Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds);
}
