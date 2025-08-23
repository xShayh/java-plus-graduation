package ru.practicum.action;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.action.mapper.UserActionMapper;
import ru.practicum.action.service.ActionService;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final ActionService actionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("ActionController call collectUserAction for request = {}", request);
        actionService.collectUserAction(UserActionMapper.map(request));

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
