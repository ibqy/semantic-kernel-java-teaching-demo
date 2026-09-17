package com.xb.semantickernel.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimePlugin 时间插件测试")
class TimePluginTest {

    private final TimePlugin plugin = new TimePlugin();

    @Test
    @DisplayName("返回格式为 yyyy-MM-dd HH:mm:ss")
    void getCurrentTime_shouldMatchFormat() {
        String result = plugin.getCurrentTime();
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "时间格式应为 yyyy-MM-dd HH:mm:ss，实际: " + result);
    }

    @Test
    @DisplayName("返回非空字符串")
    void getCurrentTime_shouldNotBeBlank() {
        assertFalse(plugin.getCurrentTime().isBlank());
    }
}
