package ru.practicum.stat;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatClientImpl implements StatClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub userClient;

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    @Override
    public void registerUserAction(long eventId, long userId, ActionTypeProto actionType, Instant instant) {
        log.info("statsClientImpl registerUserAction for eventId = {}, userId = {}, actionType = {}, time = {}",
                eventId, userId, actionType, instant);

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
        UserActionProto request = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setActionType(actionType)
                .setTimestamp(timestamp)
                .build();
        log.info("statsClientImpl registerUserAction request = {}", request);
        userClient.collectUserAction(request);
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("statsClientImpl getSimilarEvents for eventId = {}, userId = {}, maxResults = {}",
                eventId, userId, maxResults);
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = analyzerClient.getSimilarEvents(request);

        return asStream(iterator);
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        log.info("statsClientImpl getInteractionsCount for event list = {}", eventIds);

        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        Iterator<RecommendedEventProto> iterator = analyzerClient.getInteractionsCount(request);

        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}