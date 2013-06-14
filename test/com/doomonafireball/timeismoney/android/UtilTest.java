package com.doomonafireball.timeismoney.android;

import com.doomonafireball.timeismoney.android.activity.MainActivity;
import com.xtremelabs.robolectric.RobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        Assert.assertEquals(timeElapsedString, "2h 0m 0s 0ms");

        // Test many minutes
        time = 30l * 60l * 1000l;
        timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "30m 0s 0ms");

        // Test many seconds
        time = 30l * 1000l;
        timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "30s 0ms");

        // Test many millis
        time = 500l;
        timeElapsedString = MainActivity.getTimeElapsedText(time);
        Assert.assertEquals(timeElapsedString, "500ms");
    }
}
