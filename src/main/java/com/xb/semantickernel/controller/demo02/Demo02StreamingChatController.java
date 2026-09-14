package com.xb.semantickernel.controller.demo02;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo02")
public class Demo02StreamingChatController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo02StreamingChatController(ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam(defaultValue = "你好") String message) {
        ChatHistory history = new ChatHistory();
        history.addUserMessage(message);
        Flux<StreamingChatContent<?>> flux = chatCompletionService
                .getStreamingChatMessageContentsAsync(history, kernel, InvocationContext.builder().build());
        List<StreamingChatContent<?>> chunks = flux.collectList().block();
        StringBuilder sb = new StringBuilder();
        if (chunks != null) {
            for (StreamingChatContent<?> chunk : chunks) {
                if (chunk.getContent() != null) sb.append(chunk.getContent());
            }
        }
        return Map.of("mode", "streaming", "reply", sb.toString());
    }
}