package com.tobiasbrandy.grpc.examples.helloworld;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.Server;

public final class GrpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

    private GrpcServer() {}

    public static final int DEFAULT_PORT = 50051;
    public static int parsePort(final String[] args) {
        return  parsePort(args, DEFAULT_PORT);
    }
    public static int parsePort(final String[] args, final int defaultPort) {
        if(args.length == 0) {
            return defaultPort;
        }

        return parsePort(args[0], defaultPort);
    }
    public static int parsePort(final String portStr) {
        return parsePort(portStr, DEFAULT_PORT);
    }
    public static int parsePort(final String portStr, final int defaultPort) {
        try {
            return Integer.parseInt(portStr);
        } catch(final NumberFormatException e) {
            return defaultPort;
        }
    }

    public static void run(final int port, final io.grpc.ServerCredentials creds, final BindableService... services) throws InterruptedException {
        final var serverBuilder = Grpc.newServerBuilderForPort(port, creds);
        for(var service : services) {
            serverBuilder.addService(service);
        }
        final Server server = serverBuilder.build();

        try {
            server.start();
        } catch(final Exception e) {
            LOGGER.error("Unable to bind server", e);
            return;
        }

        LOGGER.info("Server started, listening on {}", port);

        // We use System.err instead of logger because JVM may have already cleaned it up.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch(final InterruptedException e) {
                server.shutdownNow();
                System.err.println("Server could not shutdown properly");
                e.printStackTrace(System.err);
                Thread.currentThread().interrupt();
            }


        }));

        server.awaitTermination();
    }
}