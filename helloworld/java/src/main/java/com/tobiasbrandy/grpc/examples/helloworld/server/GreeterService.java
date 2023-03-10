package com.tobiasbrandy.grpc.examples.helloworld.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tobiasbrandy.grpc.examples.helloworld.GreeterGrpc;
import com.tobiasbrandy.grpc.examples.helloworld.HelloReply;
import com.tobiasbrandy.grpc.examples.helloworld.HelloRequest;

import io.grpc.stub.StreamObserver;

public final class GreeterService extends GreeterGrpc.GreeterImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterService.class);

    private GreeterService() {}

    public static GreeterService newInstance() {
        return new GreeterService();
    }

    @Override
    public void sayHello(final HelloRequest request, final StreamObserver<HelloReply> responseObserver) {
        final HelloReply reply = HelloReply.newBuilder()
            .setMessage("Hello " + request.getName())
            .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private static final int REPETITIONS = 5;
    @Override
    public void sayHelloRepeated(final HelloRequest request, final StreamObserver<HelloReply> responseObserver) {
        final HelloReply reply = HelloReply.newBuilder()
            .setMessage("Hello " + request.getName())
            .build();

        for(int i = 0; i < REPETITIONS; i++) {
            responseObserver.onNext(reply);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloMany(final StreamObserver<HelloReply> responseObserver) {
        final StringBuilder reply = new StringBuilder("Hello");
        return new StreamObserver<>() {

            @Override
            public void onNext(final HelloRequest value) {
                reply.append(' ').append(value.getName()).append(',');
            }

            @Override
            public void onError(final Throwable t) {
                LOGGER.warn("sayHelloMany() RPC cancelled", t);
            }

            @Override
            public void onCompleted() {
                reply.deleteCharAt(reply.length() - 1); // Delete trailing comma

                responseObserver.onNext(HelloReply.newBuilder()
                    .setMessage(reply.toString())
                    .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloManyMany(final StreamObserver<HelloReply> responseObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(final HelloRequest value) {
                responseObserver.onNext(HelloReply.newBuilder()
                    .setMessage("Hello " + value.getName())
                    .build()
                );
            }

            @Override
            public void onError(final Throwable t) {
                LOGGER.warn("sayHelloManyMany() RPC cancelled", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
