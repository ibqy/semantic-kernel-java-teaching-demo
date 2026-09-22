package com.xb.semantickernel.controller.demo09;

/**
 * Demo09JsonOutputController - 结构化 JSON 输出演示
 *
 * 演示如何让 AI 模型返回结构化 JSON 并解析为 Java 对象。
 * 教学要点：通过 System Message 约束输出格式，再用 Jackson
 * 反序列化。同时展示了处理模型"不守规矩"输出时的容错策略。
 *
 * @author ibqy
 */
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping("/api/demo09")
public class Demo09JsonOutputController {

    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;
    private final ObjectMapper objectMapper;

    public Demo09JsonOutputController(
            ChatCompletionService chatCompletionService, Kernel kernel, ObjectMapper objectMapper) {
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据国家名查询信息，返回结构化 JSON
     *
     * 通过 System Message 严格约束模型只输出 JSON，
     * 然后用 Jackson 解析为 CountryInfo 对象。
     *
     * @param request 包含 "country" 字段的请求体
     * @return 解析后的国家信息和模型原始输出
     */
    @PostMapping("/country")
    public Map<String, Object> country(@RequestBody Map<String, String> request) {
        String country = request.getOrDefault("country", "中国");
        ChatHistory history = new ChatHistory();
        history.addSystemMessage("""
                你是一个信息抽取助手。你会收到一个国家名，请返回该国的以下信息，
                并且【只能】输出如下 JSON，不要任何额外文字、解释或 Markdown 代码块：
                {"name":"国家名","capital":"首都","population":人口数(整数),"currency":"货币"}
                """);
        history.addUserMessage("国家：" + country);
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build()).block();
        String raw = ChatHelper.lastAssistantText(results);
        // 清理模型可能附带的 Markdown 代码块标记
        String json = raw.replaceAll("```json", "").replaceAll("```", "").trim();
        CountryInfo info = CountryInfo.empty();
        try {
            info = objectMapper.readValue(json, CountryInfo.class);
        } catch (Exception e) {
            // 解析失败时保留原始输出，方便排查模型"不守规矩"的情况
            info = new CountryInfo("解析失败", e.getMessage(), 0L, raw);
        }
        return Map.of("parsed", info, "rawModelOutput", raw);
    }
}