package com.xb.semantickernel.controller.demo01;

/**
 * Demo01BasicChatController - 基础单轮对话演示
 *
 * 这是最简单的 Semantic Kernel 对话示例，演示如何构建 ChatHistory、
 * 发送用户消息并获取 AI 回复。教学要点：理解 ChatCompletionService
 * 的同步调用模式（block）和 ChatHistory 的基本用法。
 *
 * @author ibqy
 */
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
@RequestMapping("/api/demo01")
public class Demo01BasicChatController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo01BasicChatController(ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 处理单轮聊天请求
     *
     * 每次请求都新建 ChatHistory，意味着不保留上下文（无状态）。
     *
     * @param request 包含 "message" 字段的请求体
     * @return 包含角色标识和 AI 回复的 Map
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        // 每次请求新建 ChatHistory，即无记忆的单轮对话
        ChatHistory history = new ChatHistory();
        history.addUserMessage(message);
        // 调用 AI 服务，block() 将异步结果转为同步等待
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
                .block();
        String reply = ChatHelper.lastAssistantText(results);
        return Map.of("role", "assistant", "reply", reply);
    }
}