package com.xb.semantickernel.plugin;

/**
 * TimePlugin - 时间感知插件
 *
 * 为 AI 模型提供"获取当前时间"的能力。
 * 教学要点：模型本身不知道真实时间，通过此工具函数
 * 可以让它在对话中按需获取系统时间，而非依赖训练数据。
 *
 * @author ibqy
 */
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimePlugin {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @DefineKernelFunction(name = "getCurrentTime", description = "获取当前的日期和时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(FORMATTER);
    }
}