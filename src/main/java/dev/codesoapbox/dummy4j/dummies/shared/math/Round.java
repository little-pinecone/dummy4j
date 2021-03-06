package dev.codesoapbox.dummy4j.dummies.shared.math;

/**
 * Provides convenience methods for rounding
 *
 * @since 0.6.0
 */
public final class Round {

    private Round() {
    }

    /**
     * Rounds the given value to two decimal places
     */
    public static float toTwoDecimals(float value) {
        return (float) (Math.round(value * 100.0F) / 100.0);
    }
}
