package com.xb.semantickernel.controller.demo07;

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

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String question = request.getOrDefault("question", "");
        ChatHistory history = new ChatHistory();
        history.addUserMessage(question);
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