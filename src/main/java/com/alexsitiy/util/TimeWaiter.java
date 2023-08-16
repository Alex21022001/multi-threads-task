package com.alexsitiy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class TimeWaiter {

    private TimeWaiter(){}

    private static final Logger LOG = LoggerFactory.getLogger(TimeWaiter.class);
    private static final Clock clock = Clock.systemUTC();

    public static void waitTime(long millis) {
        Instant start = Instant.now(clock);
        Instant now = Instant.now(clock);

        LOG.debug("Start to wait: [{}]ms",millis);
        while (Duration.between(start, now).toMillis() < millis) {
            now = Instant.now(clock);
        }

    }
}
