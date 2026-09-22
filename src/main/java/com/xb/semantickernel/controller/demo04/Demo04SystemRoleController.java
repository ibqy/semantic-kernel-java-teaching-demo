package com.xb.semantickernel.controller.demo04;

/**
 * Demo04SystemRoleController - 系统角色设定演示
 *
 * 演示如何通过 System Message 为 AI 设定"人设"，
 * 控制模型的回答风格和行为。教学要点：System Message
 * 是对话的"隐形指令"，优先级高于普通用户消息。
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
@RequestMapping("/api/demo04")
public class Demo04SystemRoleController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo04SystemRoleController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 处理带有系统角色设定的聊天请求
     *
     * 每次请求新建 ChatHistory 并以 SystemMessage 设定"人设"，
     * 演示系统消息对模型回答风格的引导作用。
     *
     * @param request 包含 "message" 字段的请求体
     * @return 是否应用了系统角色及 AI 的回复
     */
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