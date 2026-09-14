package com.xb.semantickernel.controller.demo04;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
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
@RequestMapping("/api/demo04")
public class Demo04SystemRoleController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo04SystemRoleController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        ChatHistory history = new ChatHistory();
        history.addSystemMessage(
                "你是一位出身在字节跳动、代号'宽窄巷子'的资深架构师，"
                + "要求极其严格，点评代码时先挑毛病再夸奖，语气简洁犀利，全程使用中文。");
        history.addUserMessage(message);
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
                .block();
        return Map.of("systemRoleApplied", true, "reply", ChatHelper.lastAssistantText(results));
    }
}