package com.xb.semantickernel.controller.demo10;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.xb.semantickernel.util.ChatHelper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo10")
public class Demo10TodoAssistantController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;
    private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();

    public Demo10TodoAssistantController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        String message = request.getOrDefault("message", "");
        ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());
        if (history.getMessages().isEmpty()) {
            history.addSystemMessage(
                    "你是一位高效的中文待办助手。你会使用 addTask 新增待办、用 listTasks 查看待办。"
                    + "回答简短友好；在需要操作待办时务必调用工具，不要虚构待办清单内容。");
        }
        history.addUserMessage(message);
        InvocationContext ctx = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build();
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, ctx).block();
        String reply = ChatHelper.lastAssistantText(results);
        history.addAssistantMessage(reply);
        return Map.of("sessionId", sessionId, "historySize", history.getMessages().size(), "assistant", reply);
    }
}