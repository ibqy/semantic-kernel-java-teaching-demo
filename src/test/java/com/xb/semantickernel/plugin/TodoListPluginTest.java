package com.xb.semantickernel.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TodoListPlugin 待办插件测试")
class TodoListPluginTest {

    private TodoListPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new TodoListPlugin();
    }

    @Test
    @DisplayName("空列表返回提示信息")
    void listTasks_empty_returnsEmptyMessage() {
        String result = plugin.listTasks();
        assertEquals("当前没有任何待办事项", result);
    }

    @Test
    @DisplayName("添加一条待办后列表非空")
    void addTask_oneTask_listContainsIt() {
        String addResult = plugin.addTask("写单元测试");
        assertTrue(addResult.contains("写单元测试"));
        assertTrue(addResult.contains("共 1 条"));

        String listResult = plugin.listTasks();
        assertTrue(listResult.contains("1. 写单元测试"));
    }

    @Test
    @DisplayName("添加多条待办后按序展示")
    void addTask_multipleTasks_listInOrder() {
        plugin.addTask("任务A");
        plugin.addTask("任务B");
        plugin.addTask("任务C");

        String list = plugin.listTasks();
        assertTrue(list.contains("1. 任务A"));
        assertTrue(list.contains("2. 任务B"));
        assertTrue(list.contains("3. 任务C"));
    }

    @Test
    @DisplayName("每条 addTask 返回的计数递增")
    void addTask_countIncrements() {
        assertTrue(plugin.addTask("A").contains("共 1 条"));
        assertTrue(plugin.addTask("B").contains("共 2 条"));
        assertTrue(plugin.addTask("C").contains("共 3 条"));
    }
}
