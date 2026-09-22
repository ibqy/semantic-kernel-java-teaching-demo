package com.xb.semantickernel.controller.demo10;

/**
 * Demo10TodoAssistantController - AI 待办助手演示
 *
 * 结合多轮对话记忆和工具调用，构建一个能管理待办事项的 AI 助手。
 * 教学要点：ChatHistory 按 sessionId 隔离 + 工具调用实现真实的 CRUD 操作。
 *
 * @author ibqy
 */
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
    // 按 sessionId 缓存 ChatHistory，同一会话的多轮对话共享上下文
    private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();

    public Demo10TodoAssistantController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 处理待办助手的聊天请求
     *
     * 首次对话时注入 System Message 设定助手行为，
     * 后续对话复用历史保持上下文连贯性。
     *
     * @param request 包含 "sessionId" 和 "message" 的请求体
     * @return 会话信息及助手回复
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.getOrDefault("sessionId", "default");
        String message = request.getOrDefault("message", "");
        ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());
        // 仅首次对话时设置 System Message，避免重复注入人设
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