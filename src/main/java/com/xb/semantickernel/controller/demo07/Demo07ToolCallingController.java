package com.xb.semantickernel.controller.demo07;

/**
 * Demo07ToolCallingController - 自动工具调用演示
 *
 * 演示 AI 模型自主决定是否调用工具函数（Tool Calling）。
 * 教学要点：通过 ToolCallBehavior.allowAllKernelFunctions(true)
 * 开放所有已注册函数，模型会根据用户问题自动选择合适的工具。
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
@RequestMapping("/api/demo07")
public class Demo07ToolCallingController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo07ToolCallingController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 处理带有工具调用能力的聊天请求
     *
     * 模型在回答过程中可能自动调用 MathPlugin、TimePlugin 等工具，
     * 整个过程对调用方透明。
     *
     * @param request 包含 "question" 字段的请求体
     * @return 可用函数列表和 AI 的最终回复
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String question = request.getOrDefault("question", "");
        ChatHistory history = new ChatHistory();
        history.addUserMessage(question);
        // allowAllKernelFunctions(true) 让模型有权调用 Kernel 中所有函数
        InvocationContext invocationContext = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build();
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, invocationContext).block();
        List<String> availableFunctions = new java.util.ArrayList<>();
        for (var plugin : kernel.getPlugins()) {
            for (var fn : plugin) availableFunctions.add(fn.getName());
        }
        return Map.of("toolCalling", true, "availableFunctions", availableFunctions,
                "reply", ChatHelper.lastAssistantText(results));
    }
}