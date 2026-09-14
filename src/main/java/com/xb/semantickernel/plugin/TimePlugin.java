package com.xb.semantickernel.plugin;

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