package com.xb.semantickernel.controller.demo11;

/**
 * Demo11TimeTaskAssistantController - 综合 Agent 与显式编排演示
 *
 * 这是整个教学项目的"毕业 demo"，展示了三种调用模式：
 * 1. /handle — Agentic 模式，AI 自主调用工具（多工具协作）
 * 2. /invoke — Orchestrated 模式，程序显式编排函数调用
 * 3. /tick   — 简化版编排模式，演示计数器 + 函数调用
 * 教学要点：帮助学习者理解"Agent 自主"与"程序编排"的本质区别。
 *
 * @author ibqy
 */
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
    // 按 sessionId 缓存对话历史，与 Demo03/Demo10 相同模式
    private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();
    // 线程安全的计数器，用于 /tick 接口演示编排模式
    private final AtomicInteger counter = new AtomicInteger(0);

    public Demo11TimeTaskAssistantController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * Agentic 模式：AI 自主决策调用哪些工具
     *
     * 与 Demo10 类似但工具集更丰富，模型可组合使用
     * 数学计算、时间查询、待办管理等多种能力。
     *
     * @param request 包含 "sessionId" 和 "message" 的请求体
     * @return 模式标识、会话信息及助手回复
     */
    @PostMapping("/handle")
    public Map<String, Object> handle(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        String message = request.getOrDefault("message", "");
        ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());
        // 首次对话注入 System Message，定义管家角色和可用工具清单
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

    /**
     * 显式编排模式：由程序指定要调用的插件和函数
     *
     * 动态构建参数，自动识别整数类型，支持自定义返回类型。
     *
     * @param plugin     插件名称
     * @param function   函数名称
     * @param resultType 期望的返回类型
     * @param allParams  所有请求参数（包含函数入参）
     * @return 调用结果
     */
    @GetMapping("/invoke")
    public Map<String, Object> invoke(
            @RequestParam String plugin,
            @RequestParam String function,
            @RequestParam(defaultValue = "String") String resultType,
            @RequestParam Map<String, String> allParams) {
        KernelArguments.Builder<?> builder = KernelArguments.builder();
        // 过滤掉路由参数，只保留函数实际需要的入参
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            String key = e.getKey();
            if ("plugin".equals(key) || "function".equals(key) || "resultType".equals(key)) continue;
            String value = e.getValue();
            // 尝试将参数解析为整数，数学函数需要 int 类型入参
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

    /**
     * 简化的编排演示：每次调用计数器自增，并调用 MathPlugin 计算 counter + 10
     *
     * @return 当前计数值及计算结果
     */
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