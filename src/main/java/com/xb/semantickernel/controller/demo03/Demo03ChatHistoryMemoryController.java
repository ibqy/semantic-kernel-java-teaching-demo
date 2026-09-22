package com.xb.semantickernel.controller.demo03;

/**
 * Demo03ChatHistoryMemoryController - 多轮对话与历史记忆演示
 *
 * 通过 sessionId 维护每个会话的 ChatHistory，实现多轮对话。
 * 教学要点：ChatHistory 本身就是对话记忆的载体，配合 ConcurrentHashMap
 * 可以按会话粒度隔离上下文，避免不同用户之间串话。
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

    /**
     * 处理多轮聊天请求
     *
     * 按 sessionId 复用 ChatHistory，使模型能"记住"之前的对话内容。
     *
     * @param request 包含 "sessionId" 和 "message" 的请求体
     * @return 会话 ID、历史记录条数和 AI 回复
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        String message = request.getOrDefault("message", "");
        // computeIfAbsent 保证同一 sessionId 只初始化一次 ChatHistory
        ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());
        history.addUserMessage(message);
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
                .block();
        String reply = ChatHelper.lastAssistantText(results);
        // 将助手回复也追加到历史中，下一轮模型就能看到完整上下文
        history.addAssistantMessage(reply);
        return Map.of("sessionId", sessionId, "historySize", history.getMessages().size(), "reply", reply);
    }
}