package com.tobiasbrandy.grpc.examples.helloworld;

import io.grpc.Deadline;
import io.grpc.StatusRuntimeException;

public class NTo1StreamObserver<V> extends BlockingStreamObserver<V> {

    private V result;

    public NTo1StreamObserver() {
        super(v -> {});
    }

    @Override
    public void onNext(final V value) {
        super.onNext(value);
        result = value;
    }

    public V get(final Deadline deadline) throws StatusRuntimeException {
        super.await(deadline);
        return result;
    }
}