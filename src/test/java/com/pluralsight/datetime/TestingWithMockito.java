package com.pluralsight.datetime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.when;

public class TestingWithMockito {

    private Instant currentTime;
    private Clock clock = Mockito.mock(Clock.class);
    private TimePrintingObject testObject;

    @Before
    public void setup() {
        currentTime = Instant.EPOCH; // or other initialisation
        when(clock.instant()).thenAnswer(invocation -> currentTime);
        testObject = new TimePrintingObject();
    }

    @Test
    public void myTest() {
        testObject.methodThatDependsOnTime(clock.instant());
        currentTime = currentTime.plus(1, ChronoUnit.DAYS); // simulate passage of time
        testObject.methodThatDependsOnTime(clock.instant());
    }
}

class TimePrintingObject {
    void methodThatDependsOnTime(Instant instant) {
        System.out.println(instant);
    }
}
