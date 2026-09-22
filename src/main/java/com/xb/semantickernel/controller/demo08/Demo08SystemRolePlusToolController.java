package com.xb.semantickernel.controller.demo08;

/**
 * Demo08SystemRolePlusToolController - 系统角色 + 工具调用组合演示
 *
 * 将 Demo04 的 System Message 人设与 Demo07 的 Tool Calling 结合，
 * 展示 AI 在特定人设约束下如何决策调用工具。
 * 教学要点：实际应用中"人设 + 工具"是最常见的 Agent 模式。
 *
 * @author ibqy
 */
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.xb.semantickernel.util.ChatHelper;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo08")
public class Demo08SystemRolePlusToolController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo08SystemRolePlusToolController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 处理带有"数学助教"角色设定和工具调用的聊天请求
     *
     * @param request 包含 "message" 字段的请求体
     * @return 角色名、工具调用标识和 AI 的回复
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        ChatHistory history = new ChatHistory();
        history.addSystemMessage(
                "你是一名严谨的数学助教。回答必须：先给结论，再给一步一句的推演过程；"
                + "遇到需要实时时间或计算结果时，一定要使用可用工具，而不是凭空编造数字。全程使用中文。");
        history.addUserMessage(message);
        // 同时开启工具调用，让"人设"指导模型何时使用工具
        InvocationContext ctx = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build();
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, ctx).block();
        return Map.of("systemRole", "严谨的数学助教", "toolCalling", true,
                "reply", ChatHelper.lastAssistantText(results));
    }
}