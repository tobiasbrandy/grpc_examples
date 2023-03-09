package com.tobiasbrandy.grpc.examples.helloworld.client;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.time.Duration;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tobiasbrandy.grpc.examples.helloworld.ChannelManager;
import com.tobiasbrandy.grpc.examples.helloworld.GreeterGrpc;
import com.tobiasbrandy.grpc.examples.helloworld.HelloReply;
import com.tobiasbrandy.grpc.examples.helloworld.HelloRequest;

public final class HelloWorldClient {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldClient.class);

    private HelloWorldClient() {}

    private static final String DEFAULT_NAME = "World";
    private static String parseName(final String[] args) {
        if(args.length < 1) {
            return DEFAULT_NAME;
        }
        return args[0];
    }

    private static final String DEFAULT_TARGET = "localhost:50051";
    private static String parseTarget(final String[] args) {
        if(args.length < 2) {
            return DEFAULT_TARGET;
        }
        return args[1];
    }


    public static void main(final String[] args) {
        final String name = parseName(args);
        final String target = parseTarget(args);

        try(final ChannelManager channels = new ChannelManager(Duration.ofSeconds(5), new HashMap<>())) {
            final ManagedChannel greeterChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
            channels.registerChannel("GREETER", greeterChannel);

            final var greeterClient = GreeterGrpc.newBlockingStub(greeterChannel);

            final HelloReply response;
            try {
                response = greeterClient.sayHello(HelloRequest.newBuilder()
                    .setName(name)
                    .build()
                );
            } catch(final StatusRuntimeException e) {
                logger.error("SeyHello() RPC failed: {}", e.getStatus());
                return;
            }

            logger.info(response.getMessage());
        }
    }
}

