package com.xb.semantickernel.controller.demo11;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.xb.semantickernel.util.ChatHelper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo11")
public class Demo11TimeTaskAssistantController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;
    private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public Demo11TimeTaskAssistantController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    @PostMapping("/handle")
    public Map<String, Object> handle(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        String message = request.getOrDefault("message", "");
        ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());
        if (history.getMessages().isEmpty()) {
            history.addSystemMessage(
                    "你是一位专业的定时待办管家。可使用的工具："
                    + "scheduleReminder（安排/查看定时提醒）、addTask（新增待办）、"
                    + "listTasks（查看待办）、getCurrentTime（获取当前时间）、"
                    + "add/subtract/multiply/divide（数学计算）。"
                    + "根据用户需要主动调用工具，不凭空编造待办内容；回答简短友好。");
        }
        history.addUserMessage(message);
        InvocationContext ctx = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build();
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, ctx).block();
        String reply = ChatHelper.lastAssistantText(results);
        history.addAssistantMessage(reply);
        return Map.of("mode", "agentic（模型自主调用工具）", "sessionId", sessionId,
                "historySize", history.getMessages().size(), "assistant", reply);
    }

    @GetMapping("/invoke")
    public Map<String, Object> invoke(
            @RequestParam String plugin,
            @RequestParam String function,
            @RequestParam(defaultValue = "String") String resultType,
            @RequestParam Map<String, String> allParams) {
        KernelArguments.Builder<?> builder = KernelArguments.builder();
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            String key = e.getKey();
            if ("plugin".equals(key) || "function".equals(key) || "resultType".equals(key)) continue;
            String value = e.getValue();
            Integer intVal = tryParseInt(value);
            if (intVal != null) builder.withVariable(key, intVal);
            else builder.withVariable(key, value);
        }
        KernelArguments arguments = builder.build();
        Class<?> resultClass = resolveResultType(resultType);
        Object result = kernel.invokeAsync(plugin, function)
                .withArguments(arguments).withResultType(resultClass).block().getResult();
        return Map.of("mode", "orchestrated（程序显式编排函数）",
                "path", "/api/demo11/invoke", "plugin", plugin,
                "function", function, "result", result);
    }

    @GetMapping("/tick")
    public Map<String, Object> tick() {
        int next = counter.incrementAndGet();
        Integer added = kernel.invokeAsync("MathPlugin", "add")
                .withArguments(KernelArguments.builder()
                        .withVariable("a", next).withVariable("b", 10).build())
                .withResultType(Integer.class).block().getResult();
        return Map.of("mode", "orchestrated（程序显式编排函数）", "counter", next, "counterPlusOne", added);
    }

    private Class<?> resolveResultType(String resultType) {
        return switch (resultType.toLowerCase()) {
            case "integer", "int" -> Integer.class;
            case "long" -> Long.class;
            case "boolean" -> Boolean.class;
            case "double" -> Double.class;
            default -> String.class;
        };
    }

    private Integer tryParseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}