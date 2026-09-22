package com.xb.semantickernel.plugin;

/**
 * TodoListPlugin - 待办事项插件
 *
 * 为 AI 模型提供"添加待办"和"查看待办"的能力，
 * 是教学 Demo10 中 AI 待办助手的核心后端支撑。
 * 教学要点：插件本质上就是普通 Java 类，只需注解即可被 AI 调用。
 *
 * @author ibqy
 */
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TodoListPlugin {

    // 线程安全列表，保证并发场景下的数据一致性
    private final List<String> tasks = new CopyOnWriteArrayList<>();

    @DefineKernelFunction(name = "addTask", description = "往待办清单中新增一条待办事项")
    public String addTask(
            @KernelFunctionParameter(name = "task", description = "待办事项的内容") String task) {
        tasks.add(task);
        return "已成功添加待办：" + task + "（当前共 " + tasks.size() + " 条）";
    }

    @DefineKernelFunction(name = "listTasks", description = "列出当前所有的待办事项")
    public String listTasks() {
        if (tasks.isEmpty()) return "当前没有任何待办事项";
        StringBuilder sb = new StringBuilder("当前待办事项：\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(i + 1).append(". ").append(tasks.get(i)).append('\n');
        }
        return sb.toString().trim();
    }
}