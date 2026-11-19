package com.houdini.monitor.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainViewFormatDurationTest {

    @Test
    void formatsSecondsMinutesHours() {
        assertEquals("5s", MainView.formatDuration(5));
        assertEquals("2m 05s", MainView.formatDuration(125));
        assertEquals("1h 01m 01s", MainView.formatDuration(3661));
    }
}
