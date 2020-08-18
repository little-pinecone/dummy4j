package dev.codesoapbox.dummy4j.dummies;

import dev.codesoapbox.dummy4j.Dummy4j;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Provides methods for generating dates and times
 *
 * @since 0.4.0
 */
public class DateAndTimeDummy {

    private static final int DEFAULT_MIN_AGE = 18;
    private static final int DEFAULT_MAX_AGE = 80;
    private static final String END_MUST_NOT_PRECEDE_START = "End date must not precede start date";
    private static final String MAX_AGE_MUST_BE_GREATER_OR_EQUAL_MIN_AGE = "Max age must be greater or equal min age";
    private static final String AGE_MUST_BE_POSITIVE = "Given age must be a positive number";
    private static final String AT_MOST_MUST_BE_POSITIVE = "At most must be a positive number";


    private final Dummy4j dummy4j;
    private final Clock clock;

    public DateAndTimeDummy(Dummy4j dummy4j, Clock clock) {
        this.dummy4j = dummy4j;
        this.clock = clock;
    }

    /**
     * Generates a random date and time
     */
    public LocalDateTime any() {
        return LocalDateTime.ofEpochSecond(dummy4j.number().nextLong(), 0, ZoneOffset.UTC);
    }

    /**
     * Generates a random birthday date that is suitable for a person from {@code DEFAULT_MIN_AGE} to
     * {@code DEFAULT_MAX_AGE} years old
     */
    public LocalDate birthday() {
        return birthday(DEFAULT_MIN_AGE, DEFAULT_MAX_AGE);
    }

    /**
     * Generates a random birthday date that is suitable for a person in the given age
     */
    public LocalDate birthday(int age) {
        return birthday(age, age);
    }

    /**
     * Generates a random birthday date that is suitable for a person from {@code minAge} to {@code maxAge} years old
     */
    public LocalDate birthday(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < 0) {
            throw new IllegalArgumentException(AGE_MUST_BE_POSITIVE);
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException(MAX_AGE_MUST_BE_GREATER_OR_EQUAL_MIN_AGE);
        }
        LocalDate minDate = LocalDate.now(clock).minus((long) maxAge + 1, ChronoUnit.YEARS)
                .plus(1, ChronoUnit.DAYS);
        LocalDate maxDate = LocalDate.now(clock).minus(minAge, ChronoUnit.YEARS);
        return between(minDate, maxDate);
    }

    /**
     * Generates a random date and time within the given range
     */
    public LocalDateTime between(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(END_MUST_NOT_PRECEDE_START);
        }
        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(ZoneOffset.UTC);
        long randomEpochSecond = nextLongWithNegativeBounds(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, ZoneOffset.UTC);
    }

    /**
     * Generates a random date within the given range
     */
    public LocalDate between(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(END_MUST_NOT_PRECEDE_START);
        }
        long randomDay = nextLongWithNegativeBounds(start.toEpochDay(), end.toEpochDay());

        return LocalDate.ofEpochDay(randomDay);
    }

    private long nextLongWithNegativeBounds(long lowerBound, long upperBound) {
        long diff = Math.abs(lowerBound - upperBound);
        return dummy4j.number().nextLong(0, diff) + lowerBound;
    }

    /**
     * Generates a random date and time from the past that fits in the time range between {@code atMost units} and now
     */
    public LocalDateTime past(long atMost, ChronoUnit unit) {
        if (atMost < 0) {
            throw new IllegalArgumentException(AT_MOST_MUST_BE_POSITIVE);
        }
        return LocalDateTime.now(clock).minus(dummy4j.number().nextLong(0, atMost), unit);
    }

    /**
     * Generates a random date and time before the {@code referenceDate} that fits in the time range between
     * {@code atMost units} and {@code referenceDate}
     */
    public LocalDateTime before(LocalDateTime referenceDate, long atMost, ChronoUnit unit) {
        if (atMost < 0) {
            throw new IllegalArgumentException(AT_MOST_MUST_BE_POSITIVE);
        }
        return referenceDate.minus(dummy4j.number().nextLong(0, atMost), unit);
    }

    /**
     * Generates a random date and time from the future that fits in the time range between {@code atMost units} and now
     */
    public LocalDateTime future(long atMost, ChronoUnit unit) {
        if (atMost < 0) {
            throw new IllegalArgumentException(AT_MOST_MUST_BE_POSITIVE);
        }
        return LocalDateTime.now(clock).plus(dummy4j.number().nextLong(0, atMost), unit);
    }

    /**
     * Generates a random date and time after the {@code referenceDate} that fits in the time range between
     * {@code atMost units} and {@code referenceDate}
     */
    public LocalDateTime after(LocalDateTime referenceDate, long atMost, ChronoUnit unit) {
        if (atMost < 0) {
            throw new IllegalArgumentException(AT_MOST_MUST_BE_POSITIVE);
        }
        return referenceDate.plus(dummy4j.number().nextLong(0, atMost), unit);
    }
}
