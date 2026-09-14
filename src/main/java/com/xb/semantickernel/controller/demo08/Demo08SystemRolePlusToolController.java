package com.xb.semantickernel.controller.demo08;

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

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        ChatHistory history = new ChatHistory();
        history.addSystemMessage(
                "你是一名严谨的数学助教。回答必须：先给结论，再给一步一句的推演过程；"
                + "遇到需要实时时间或计算结果时，一定要使用可用工具，而不是凭空编造数字。全程使用中文。");
        history.addUserMessage(message);
        InvocationContext ctx = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build();
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, ctx).block();
        return Map.of("systemRole", "严谨的数学助教", "toolCalling", true,
                "reply", ChatHelper.lastAssistantText(results));
    }
}