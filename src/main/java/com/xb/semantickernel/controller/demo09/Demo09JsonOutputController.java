package com.xb.semantickernel.controller.demo09;

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
        String json = raw.replaceAll("```json", "").replaceAll("```", "").trim();
        CountryInfo info = CountryInfo.empty();
        try {
            info = objectMapper.readValue(json, CountryInfo.class);
        } catch (Exception e) {
            info = new CountryInfo("解析失败", e.getMessage(), 0L, raw);
        }
        return Map.of("parsed", info, "rawModelOutput", raw);
    }
}