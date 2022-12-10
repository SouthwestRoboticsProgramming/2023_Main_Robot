package com.swrobotics.lib.time;

import org.junit.Test;

import static org.junit.Assert.*;

public final class DurationTest {
    private static final double TEST_PRECISION = 0.001;

    @Test
    public void test_getCount() {
        assertEquals(new Duration(1, TimeUnit.HOURS).getCount(), 1, TEST_PRECISION);
        assertEquals(new Duration(12, TimeUnit.MINUTES).getCount(), 12, TEST_PRECISION);
        assertEquals(new Duration(114, TimeUnit.SECONDS).getCount(), 114, TEST_PRECISION);
        assertEquals(new Duration(0, TimeUnit.MILLISECONDS).getCount(), 0, TEST_PRECISION);
        assertEquals(new Duration(0.123, TimeUnit.MICROSECONDS).getCount(), 0.123, TEST_PRECISION);
        assertEquals(new Duration(12093, TimeUnit.NANOSECONDS).getCount(), 12093, TEST_PRECISION);
    }

    @Test
    public void test_getUnit() {
        assertEquals(new Duration(1, TimeUnit.HOURS).getUnit(), TimeUnit.HOURS);
        assertEquals(new Duration(12, TimeUnit.MINUTES).getUnit(), TimeUnit.MINUTES);
        assertEquals(new Duration(114, TimeUnit.SECONDS).getUnit(), TimeUnit.SECONDS);
        assertEquals(new Duration(0, TimeUnit.MILLISECONDS).getUnit(), TimeUnit.MILLISECONDS);
        assertEquals(new Duration(0.123, TimeUnit.MICROSECONDS).getUnit(), TimeUnit.MICROSECONDS);
        assertEquals(new Duration(12093, TimeUnit.NANOSECONDS).getUnit(), TimeUnit.NANOSECONDS);
    }

    @Test
    public void test_getCountInUnit() {
        Duration dur = new Duration(2, TimeUnit.HOURS);
        assertEquals(dur.getCountInUnit(TimeUnit.MINUTES), 120, TEST_PRECISION);
        assertEquals(dur.getCountInUnit(TimeUnit.SECONDS), 7200, TEST_PRECISION);
        assertEquals(dur.getCountInUnit(TimeUnit.MILLISECONDS), 7200000, TEST_PRECISION);
        assertEquals(dur.getCountInUnit(TimeUnit.MICROSECONDS), 7200000000.0, TEST_PRECISION);
        assertEquals(dur.getCountInUnit(TimeUnit.NANOSECONDS), 7200000000000.0, TEST_PRECISION);
    }

    @Test
    public void test_convertUnit() {
        Duration dur = new Duration(2, TimeUnit.HOURS);
        assertEquals(dur.convertUnit(TimeUnit.MINUTES).getCount(), 120, TEST_PRECISION);
        assertEquals(dur.convertUnit(TimeUnit.SECONDS).getCount(), 7200, TEST_PRECISION);
        assertEquals(dur.convertUnit(TimeUnit.MILLISECONDS).getCount(), 7200000, TEST_PRECISION);
        assertEquals(dur.convertUnit(TimeUnit.MICROSECONDS).getCount(), 7200000000.0, TEST_PRECISION);
        assertEquals(dur.convertUnit(TimeUnit.NANOSECONDS).getCount(), 7200000000000.0, TEST_PRECISION);
    }

    @Test
    public void test_getDurationNanos() {
        assertEquals(new Duration(2, TimeUnit.NANOSECONDS).getDurationNanos(), 2, TEST_PRECISION);
        assertEquals(new Duration(2, TimeUnit.MICROSECONDS).getDurationNanos(), 2000, TEST_PRECISION);
        assertEquals(new Duration(2, TimeUnit.MILLISECONDS).getDurationNanos(), 2000000, TEST_PRECISION);
        assertEquals(new Duration(2, TimeUnit.SECONDS).getDurationNanos(), 2000000000, TEST_PRECISION);
        assertEquals(new Duration(2, TimeUnit.MINUTES).getDurationNanos(), 120000000000.0, TEST_PRECISION);
        assertEquals(new Duration(2, TimeUnit.HOURS).getDurationNanos(), 7200000000000.0, TEST_PRECISION);
    }

    @Test
    public void test_equals() {
        assertEquals(new Duration(1, TimeUnit.SECONDS), new Duration(1, TimeUnit.SECONDS));
        assertNotEquals(new Duration(2, TimeUnit.SECONDS), new Duration(1, TimeUnit.SECONDS));
        assertNotEquals(new Duration(1, TimeUnit.MINUTES), new Duration(1, TimeUnit.SECONDS));
        assertNotEquals(new Duration(2, TimeUnit.MINUTES), new Duration(1, TimeUnit.SECONDS));
    }

    @Test
    public void test_hashCode() {
        assertEquals(new Duration(1, TimeUnit.SECONDS).hashCode(), new Duration(1, TimeUnit.SECONDS).hashCode());
    }
}
