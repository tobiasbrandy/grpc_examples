package com.tobiasbrandy.grpc.examples.helloworld.client;

import io.grpc.Channel;
import io.grpc.Deadline;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tobiasbrandy.grpc.examples.helloworld.BlockingStreamObserver;
import com.tobiasbrandy.grpc.examples.helloworld.ChannelManager;
import com.tobiasbrandy.grpc.examples.helloworld.GreeterGrpc;
import com.tobiasbrandy.grpc.examples.helloworld.HelloReply;
import com.tobiasbrandy.grpc.examples.helloworld.HelloRequest;
import com.tobiasbrandy.grpc.examples.helloworld.NTo1StreamObserver;

public final class HelloWorldClient {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldClient.class);

    private final GreeterGrpc.GreeterBlockingStub greeterBlockingStub;
    private final GreeterGrpc.GreeterStub         greeterAsyncStub;

    public HelloWorldClient(final Channel greeterChannel) {
        Objects.requireNonNull(greeterChannel);
        this.greeterBlockingStub = GreeterGrpc.newBlockingStub(greeterChannel);
        this.greeterAsyncStub    = GreeterGrpc.newStub(greeterChannel);
    }

    private void sayHello(final String name) {
        final HelloReply response;
        try {
            response = greeterBlockingStub.sayHello(HelloRequest.newBuilder()
                .setName(name)
                .build()
            );
        } catch(final StatusRuntimeException e) {
            logger.error("SeyHello() RPC failed: {}", e.getStatus());
            return;
        }

        logger.info(response.getMessage());
    }

    private void sayHelloRepeated(final String name) {
        final Iterator<HelloReply> response;
        try {
            response = greeterBlockingStub.sayHelloRepeated(HelloRequest.newBuilder()
                .setName(name)
                .build()
            );

            while(response.hasNext()) {
                // Iterator may fail
                logger.info(response.next().getMessage());
            }
        } catch(final StatusRuntimeException e) {
            logger.error("sayHelloRepeated() RPC failed: {}", e.getStatus());
        }
    }

    private static void sleepUnsafe(final long millis) {
        try {
            Thread.sleep(millis);
        } catch(final InterruptedException e) {
            // ignore
        }
    }

    private void sayHelloMany(final String[] names) {
        final var responseObserver = new NTo1StreamObserver<HelloReply>();
        final StreamObserver<HelloRequest> requestObserver = greeterAsyncStub.sayHelloMany(responseObserver);

        try {
            for(final var name : names) {
                requestObserver.onNext(HelloRequest.newBuilder()
                    .setName(name)
                    .build()
                );

                sleepUnsafe(1000); // Sleep 2 seconds to simulate latency

                if(responseObserver.hasFinished()) {
                    // Server cancelled before we were finished
                    return;
                }
            }
        } catch(final RuntimeException e) {
            // Unexpected exception
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        try {
            final Deadline deadline = greeterAsyncStub.getCallOptions().getDeadline();
            logger.info(responseObserver.get(deadline).getMessage());
        } catch(final StatusRuntimeException e) {
            logger.error("sayHelloMany() RPC failed: {}", e.getStatus());
        }
    }

    private void sayHelloManyMany(final String[] names) {
        final var responseObserver = new BlockingStreamObserver<HelloReply>(v -> logger.info(v.getMessage()));
        final StreamObserver<HelloRequest> requestObserver = greeterAsyncStub.sayHelloManyMany(responseObserver);

        try {
            for(final var name : names) {
                requestObserver.onNext(HelloRequest.newBuilder()
                    .setName(name)
                    .build()
                );

                sleepUnsafe(1000); // Sleep 2 seconds to simulate latency

                if(responseObserver.hasFinished()) {
                    // Server cancelled before we were finished
                    return;
                }
            }
        } catch(final RuntimeException e) {
            // Unexpected exception
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        try {
            responseObserver.await(greeterAsyncStub.getCallOptions().getDeadline());
        } catch(final StatusRuntimeException e) {
            logger.error("sayHelloMany() RPC failed: {}", e.getStatus());
        }
    }

    private static String parseTarget(final String[] args) {
        if(args.length < 2) {
            return DEFAULT_TARGET;
        }
        return args[1];
    }

    private static final String   DEFAULT_TARGET   = "localhost:50051";
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(5);

    public static void main(final String[] args) {
        final String[] names = {"World", "Steve", "Tobias", "Martin", "Sarah",};

        final String target = parseTarget(args);

        try(final ChannelManager channels = new ChannelManager(SHUTDOWN_TIMEOUT, new HashMap<>())) {
            final ManagedChannel greeterChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
            channels.registerChannel("GREETER", greeterChannel);

            final HelloWorldClient client = new HelloWorldClient(greeterChannel);

            logger.info("------- SayHello -------");
            client.sayHello(names[0]);

            logger.info("------- SayHelloRepeated -------");
            client.sayHelloRepeated(names[2]);

            logger.info("------- SayHelloMany -------");
            client.sayHelloMany(names);

            logger.info("------- SayHelloManyMany -------");
            client.sayHelloManyMany(names);
        }
    }
}

