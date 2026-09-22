package com.xb.semantickernel.plugin;

/**
 * TimeTaskPlugin - 定时提醒插件
 *
 * 演示有状态的插件：使用内存列表保存提醒事项，
 * 支持"安排提醒"和"列出提醒"两种操作。
 * 教学要点：插件可以持有状态，多次工具调用之间共享数据。
 *
 * @author ibqy
 */
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimeTaskPlugin {

    // 使用线程安全列表，避免并发调用工具时出现数据竞争
    private final List<String> reminders = new CopyOnWriteArrayList<>();

    @DefineKernelFunction(
            name = "scheduleReminder",
            description = "安排一条定时提醒：记录在某个时间点提醒某件事")
    public String scheduleReminder(
            @KernelFunctionParameter(name = "task", description = "提醒事项的内容") String task,
            @KernelFunctionParameter(name = "time", description = "提醒的时间点，例如 09:00") String time) {
        String entry = time + " 提醒：" + task;
        reminders.add(entry);
        return "已安排定时提醒：" + entry + "（当前共 " + reminders.size() + " 条提醒）";
    }

    @DefineKernelFunction(name = "listReminders", description = "列出当前所有的定时提醒")
    public String listReminders() {
        if (reminders.isEmpty()) return "当前没有任何定时提醒";
        StringBuilder sb = new StringBuilder("当前定时提醒：\n");
        for (int i = 0; i < reminders.size(); i++) {
            sb.append(i + 1).append(". ").append(reminders.get(i)).append('\n');
        }
        return sb.toString().trim();
    }
}