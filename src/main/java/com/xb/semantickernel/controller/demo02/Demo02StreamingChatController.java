package com.xb.semantickernel.controller.demo02;

/**
 * Demo02StreamingChatController - 流式对话演示
 *
 * 演示 Semantic Kernel 的流式聊天接口，模型逐 token 返回内容。
 * 教学要点：对比 Demo01 的同步模式，流式接口通过 Reactor Flux
 * 实现"打字机效果"，是生产环境提升用户体验的关键技术。
 *
 * @author ibqy
 */
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

    /**
     * 处理流式聊天请求
     *
     * 演示中将流式结果收集完整后再返回，实际生产中可直接
     * 将 Flux 作为 SSE 响应体推送给前端。
     *
     * @param message 用户消息
     * @return 包含流式模式标识和完整回复的 Map
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam(defaultValue = "你好") String message) {
        ChatHistory history = new ChatHistory();
        history.addUserMessage(message);
        // 流式接口返回 Flux，每个元素是一个内容片段
        Flux<StreamingChatContent<?>> flux = chatCompletionService
                .getStreamingChatMessageContentsAsync(history, kernel, InvocationContext.builder().build());
        // 演示中先收集全部片段再拼接，实际可用 SSE 逐片推送
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