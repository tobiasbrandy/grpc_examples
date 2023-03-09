package com.tobiasbrandy.grpc.examples.helloworld.server;

import io.grpc.InsecureServerCredentials;

import com.tobiasbrandy.grpc.examples.helloworld.GrpcServer;

public final class HelloWorldServer {

    public static void main(final String[] args) throws InterruptedException {
        GrpcServer.run(GrpcServer.parsePort(args), InsecureServerCredentials.create(),
            io.grpc.protobuf.services.ProtoReflectionService.newInstance(),
            com.tobiasbrandy.grpc.examples.helloworld.server.GreeterService.newInstance()
        );
    }

}
