package com.xb.semantickernel.plugin;

/**
 * TimeTaskPlugin 单元测试 - 验证定时提醒的安排、列表展示和计数递增
 */
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeTaskPlugin 定时提醒插件测试")
class TimeTaskPluginTest {

    private TimeTaskPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new TimeTaskPlugin();
    }

    @Test
    @DisplayName("空列表返回提示信息")
    void listReminders_empty_returnsEmptyMessage() {
        assertEquals("当前没有任何定时提醒", plugin.listReminders());
    }

    @Test
    @DisplayName("安排提醒后列表包含该提醒")
    void scheduleReminder_oneReminder_listContainsIt() {
        String result = plugin.scheduleReminder("开会", "09:00");
        assertTrue(result.contains("09:00 提醒：开会"));
        assertTrue(result.contains("共 1 条提醒"));

        String list = plugin.listReminders();
        assertTrue(list.contains("1. 09:00 提醒：开会"));
    }

    @Test
    @DisplayName("多条提醒按序展示")
    void scheduleReminder_multiple_listInOrder() {
        plugin.scheduleReminder("晨会", "09:00");
        plugin.scheduleReminder("复盘", "17:00");

        String list = plugin.listReminders();
        assertTrue(list.contains("1. 09:00 提醒：晨会"));
        assertTrue(list.contains("2. 17:00 提醒：复盘"));
    }

    @Test
    @DisplayName("每次安排后计数递增")
    void scheduleReminder_countIncrements() {
        assertTrue(plugin.scheduleReminder("A", "08:00").contains("共 1 条"));
        assertTrue(plugin.scheduleReminder("B", "09:00").contains("共 2 条"));
    }
}
