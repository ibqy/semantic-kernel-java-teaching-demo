package com.xb.semantickernel.controller.demo05;

/**
 * Demo05PromptTemplateController - 提示词模板演示
 *
 * 演示如何动态构建 System Prompt 模板，通过参数控制输出格式。
 * 教学要点：好的提示词工程是 AI 应用的核心技能，
 * 本例展示用 Java 文本块 + String.formatted 实现参数化提示词。
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
@RequestMapping("/api/demo05")
public class Demo05PromptTemplateController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo05PromptTemplateController(
            ChatCompletionService chatCompletionService, Kernel kernel) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 处理提示词模板聊天请求
     *
     * 根据用户指定的主题和行数，动态生成结构化的系统提示词。
     *
     * @param request 包含 "topic"（主题）和 "lines"（行数）的请求体
     * @return 主题、行数和 AI 生成的要点
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String topic = request.getOrDefault("topic", "Semantic Kernel");
        int lines = Integer.parseInt(request.getOrDefault("lines", "2"));
        // 使用 Java 文本块定义提示词模板，%d 由参数动态填充
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