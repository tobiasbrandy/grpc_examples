package com.tobiasbrandy.grpc.examples.helloworld;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class BlockingStreamObserver<V> implements StreamObserver<V> {
    private final Consumer<V>    onNext;
    private final CountDownLatch finished;
    private       Throwable      error;

    public BlockingStreamObserver(final Consumer<V> onNext) {
        this.onNext   = Objects.requireNonNull(onNext);
        this.finished = new CountDownLatch(1);
    }

    @Override
    public void onNext(final V value) {
        onNext.accept(value);
    }

    @Override
    public void onError(final Throwable t) {
        finished.countDown();
        error = t;
    }

    @Override
    public void onCompleted() {
        finished.countDown();
    }

    public boolean hasFinished() {
        return finished.getCount() == 0;
    }

    public void await(final Deadline deadline) throws StatusRuntimeException {
        try {
            if(deadline == null) {
                finished.await();
            } else if(!finished.await(deadline.timeRemaining(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS)) {
                throw Status.DEADLINE_EXCEEDED.asRuntimeException();
            }
        } catch(final InterruptedException e) {
            throw Status.CANCELLED.asRuntimeException();
        }

        if(error != null) {
            throw Status.fromThrowable(error).asRuntimeException();
        }
    }
}