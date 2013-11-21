package com.doomonafireball.timeismoney.android;

import com.doomonafireball.timeismoney.android.activity.MainActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UtilTest {

    @Before
    public void setup() {
    }

    @Test
    public void timeElapsedFormatTest() {
        // Test many hours
        long time = 2l * 60l * 60l * 1000l;
        String timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "2h 00m 00s 000ms");

        // Test many minutes
        time = 30l * 60l * 1000l;
        timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "30m 00s 000ms");

        // Test many seconds
        time = 30l * 1000l;
        timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "30s 000ms");

        // Test many millis
        time = 500l;
        timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "500ms");
    }
}
