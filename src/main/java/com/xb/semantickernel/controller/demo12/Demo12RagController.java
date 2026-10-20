package com.xb.semantickernel.controller.demo12;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.xb.semantickernel.rag.InMemoryVectorStore;
import com.xb.semantickernel.rag.RagService;
import com.xb.semantickernel.util.ChatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo12RagController - RAG 检索增强演示
 *
 * <p>教学要点：把 demo08（Spring AI 版 RAG）的思想用 Semantic Kernel 的 API 重演一遍——
 * "先检索、后生成"，并且回答必须带引用。与 Spring AI 版的差异只在 SDK 调用方式：
 * 思想（索引/检索/生成三段式）是框架无关的。</p>
 *
 * <p><b>防幻觉设计</b>：检索不到相关内容时，直接返回"知识库未提及"，
 * 不让模型自由发挥——这正是一个生产 RAG 系统最重要的产品决策。</p>
 *
 * @author ibqy
 */
@RestController
@RequestMapping("/api/demo12")
public class Demo12RagController {

    private static final Logger log = LoggerFactory.getLogger(Demo12RagController.class);

    /** 教学知识库：开业信息、会员政策、配送规则（生产上来自文档加载/ETL） */
    private static final List<String> KNOWLEDGE_BASE = List.of(
            "小北优选咖啡店营业时间为每天 8:00-22:00，周末不休。门店位于市中心广场一层。",
            "会员积分规则：消费 1 元累计 1 积分，100 积分可抵扣 5 元；积分有效期一年。",
            "配送范围覆盖门店 3 公里内，配送费 5 元，订单满 30 元免配送费；配送时长约 30-45 分钟。",
            "本店支持 7 天无理由退换货，生鲜商品除外；退款将在 3 个工作日内原路退回。",
            "包间可提前 24 小时预约，最低消费 200 元，最多容纳 12 人。");

    private final RagService ragService;
    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;

    public Demo12RagController(RagService ragService,
                               ChatCompletionService chatCompletionService,
                               Kernel kernel) {
        this.ragService = ragService;
        this.chatCompletionService = chatCompletionService;
        this.kernel = kernel;
    }

    /**
     * 重建索引：把内置知识库灌入向量库（幂等，可重复调用）
     */
    @PostMapping("/index/rebuild")
    public Map<String, Object> rebuildIndex() {
        int count = ragService.indexDocuments(KNOWLEDGE_BASE);
        return Map.of("status", "ok", "indexed", count);
    }

    /**
     * RAG 问答：检索 → 组装上下文 → 带引用生成
     *
     * @param question 用户问题
     * @param topK     检索条数（默认 3）
     * @return 答案 + 引用文档 + 相似度（完整链路对调用方透明可查）
     */
    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody Map<String, String> request) {
        String question = request.getOrDefault("question", "");
        if (question.isBlank()) {
            return Map.of("error", "question 不能为空");
        }

        // 1. 检索
        List<InMemoryVectorStore.ScoredDocument> hits = ragService.retrieve(question, 3);
        if (hits.isEmpty()) {
            // 索引为空或问题为空：引导先建索引，而不是硬答
            return Map.of("error", "向量库为空，请先调用 POST /api/demo12/index/rebuild 建立索引");
        }

        String context = ragService.buildContext(hits);

        // 2. 组装 Prompt：上下文 + 问题 + 防幻觉指令
        ChatHistory history = new ChatHistory();
        history.addSystemMessage("""
                你是门店客服助手。严格依据下方"知识库内容"回答问题：
                1. 只能使用知识库中的信息，禁止编造
                2. 知识库没有提及的内容，直接回答"知识库中未提及"
                3. 回答末尾用【依据文档N】标注引用了哪些文档
                """);
        history.addUserMessage("知识库内容:\n" + context + "\n问题: " + question);

        // 3. 生成（RAG 场景不需要工具调用，显式关闭——与 demo07/10 显式开启同理：
        //    调用配置应该是明确的意图，而不是依赖默认值）
        InvocationContext ctx = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(false)).build();
        String answer = ChatHelper.lastAssistantText(
                chatCompletionService.getChatMessageContentsAsync(history, kernel, ctx).block());

        // 4. 返回答案 + 引用明细（可解释性：用户能验证答案出处）
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("question", question);
        resp.put("answer", answer);
        resp.put("citations", hits.stream()
                .map(h -> Map.of("id", h.document().id(),
                        "score", String.format("%.3f", h.score()),
                        "text", h.document().text()))
                .toList());
        return resp;
    }

    /** 查看当前索引状态 */
    @GetMapping("/index/status")
    public Map<String, Object> indexStatus() {
        return Map.of("indexed", ragService.indexedCount());
    }
}
