package com.tobiasbrandy.grpc.examples.helloworld;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.ManagedChannel;

public final class ChannelManager implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);

    private final long timeoutMillis;
    private final Map<String, ManagedChannel> channels;

    public ChannelManager(final Duration timeout, final Map<String, ManagedChannel> channels) {
        Objects.requireNonNull(timeout);
        Objects.requireNonNull(channels);

        this.timeoutMillis = timeout.toMillis();
        this.channels = channels;
    }

    public void registerChannel(final String key, final ManagedChannel channel) {
        channels.put(key, channel);
    }

    public Channel getChannel(final String key) {
        return channels.get(key);
    }

    @Override
    public void close() {
        LOGGER.info("Closing Channel Manager");
        for (final var channel : channels.values()) {
            channel.shutdown();
        }

        long timeoutLeft = timeoutMillis;
        boolean interrupted = false;
        for (final var channelEntry : channels.entrySet()) {
            final var channel = channelEntry.getValue();

            if(timeoutLeft <= 0) {
                channel.shutdownNow();
                continue;
            }

            final Instant start = Instant.now();

            try {
                channel.awaitTermination(timeoutLeft, TimeUnit.MILLISECONDS);
            } catch(final InterruptedException e) {
                channel.shutdownNow();
                LOGGER.warn("Channel {} interrupted", channelEntry.getKey(), e);
                interrupted = true; // We interrupt after processing all channels
            }

            timeoutLeft -= Duration.between(start, Instant.now()).toMillis();
        }

        if(interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
