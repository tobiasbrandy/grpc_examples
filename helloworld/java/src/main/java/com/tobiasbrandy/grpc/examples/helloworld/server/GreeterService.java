package com.tobiasbrandy.grpc.examples.helloworld.server;

import com.tobiasbrandy.grpc.examples.helloworld.GreeterGrpc;
import com.tobiasbrandy.grpc.examples.helloworld.HelloReply;
import com.tobiasbrandy.grpc.examples.helloworld.HelloRequest;

import io.grpc.stub.StreamObserver;

public final class GreeterService extends GreeterGrpc.GreeterImplBase {
    private GreeterService() {}

    public static GreeterService newInstance() {
        return new GreeterService();
    }

    @Override
    public void sayHello(final HelloRequest req, final StreamObserver<HelloReply> responseObserver) {
        final HelloReply reply = HelloReply.newBuilder()
            .setMessage("Hello " + req.getName())
            .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
