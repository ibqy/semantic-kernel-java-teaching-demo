package com.xb.semantickernel.controller.demo03;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
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
@RequestMapping("/api/demo03")
public class Demo03ChatHistoryMemoryController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;
    private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();

    public Demo03ChatHistoryMemoryController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        String message = request.getOrDefault("message", "");
        ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());
        history.addUserMessage(message);
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
                .block();
        String reply = ChatHelper.lastAssistantText(results);
        history.addAssistantMessage(reply);
        return Map.of("sessionId", sessionId, "historySize", history.getMessages().size(), "reply", reply);
    }
}