package com.xb.semantickernel.controller.demo05;

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
@RequestMapping("/api/demo05")
public class Demo05PromptTemplateController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo05PromptTemplateController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String topic = request.getOrDefault("topic", "Semantic Kernel");
        int lines = Integer.parseInt(request.getOrDefault("lines", "2"));
        String systemPrompt = """
                你是一名资深技术讲师。请围绕给定主题写一段面向中文初学者的介绍。
                要求：
                - 精确地输出 %d 行要点；
                - 每行以"- "开头；
                - 只输出要点本身，不要任何客套话或 Markdown 代码块。
                """.formatted(lines);
        String userPrompt = "主题：" + topic;
        ChatHistory history = new ChatHistory();
        history.addSystemMessage(systemPrompt);
        history.addUserMessage(userPrompt);
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
                .block();
        return Map.of("topic", topic, "lines", lines, "reply", ChatHelper.lastAssistantText(results));
    }
}