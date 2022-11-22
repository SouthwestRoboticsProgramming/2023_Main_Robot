package com.swrobotics.lib.time;

import org.junit.Test;

import static org.junit.Assert.*;

public final class TimestampTest {
    @Test
    public void test_difference() {
        assertEquals(new Timestamp(100).difference(new Timestamp(50)).getDurationNanos(), 50, 0.0001);
        assertEquals(new Timestamp(150).difference(new Timestamp(50)).getDurationNanos(), 100, 0.0001);
        assertEquals(new Timestamp(50).difference(new Timestamp(-100)).getDurationNanos(), 150, 0.0001);
        assertEquals(new Timestamp(-100).difference(new Timestamp(50)).getDurationNanos(), -150, 0.0001);
    }

    @Test
    public void test_after() {
        assertEquals(new Timestamp(50).after(new Duration(50, TimeUnit.NANOSECONDS)).getNanoTime(), 100, 0.0001);
        assertEquals(new Timestamp(50).after(new Duration(100, TimeUnit.NANOSECONDS)).getNanoTime(), 150, 0.0001);
        assertEquals(new Timestamp(-100).after(new Duration(150, TimeUnit.NANOSECONDS)).getNanoTime(), 50, 0.0001);
        assertEquals(new Timestamp(50).after(new Duration(-150, TimeUnit.NANOSECONDS)).getNanoTime(), -100, 0.0001);
    }

    @Test
    public void test_isAtOrAfter() {
        assertFalse(new Timestamp(50).isAtOrAfter(new Timestamp(100)));
        assertTrue(new Timestamp(50).isAtOrAfter(new Timestamp(50)));
        assertTrue(new Timestamp(50).isAtOrAfter(new Timestamp(25)));
    }

    @Test
    public void test_addNanos() {
        assertEquals(new Timestamp(50).addNanos(50).getNanoTime(), 100, 0.0001);
        assertEquals(new Timestamp(100).addNanos(143).getNanoTime(), 243, 0.0001);
        assertEquals(new Timestamp(74).addNanos(-1000).getNanoTime(), -926, 0.0001);
        assertEquals(new Timestamp(82).addNanos(0).getNanoTime(), 82, 0.0001);
    }

    @Test
    public void test_getNanoTime() {
        assertEquals(new Timestamp(347).getNanoTime(), 347, 0.0001);
        assertEquals(new Timestamp(172849).getNanoTime(), 172849, 0.0001);
        assertEquals(new Timestamp(0).getNanoTime(), 0, 0.0001);
        assertEquals(new Timestamp(-103).getNanoTime(), -103, 0.0001);
    }

    @Test
    public void test_equals() {
        assertEquals(new Timestamp(283), new Timestamp(283));
        assertEquals(new Timestamp(71), new Timestamp(71));
        assertNotEquals(new Timestamp(341), new Timestamp(91923));
        assertNotEquals(new Timestamp(28130), new Timestamp(891207346918273.03578));
    }
}
