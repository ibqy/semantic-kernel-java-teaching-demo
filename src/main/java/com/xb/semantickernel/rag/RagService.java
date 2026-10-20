package com.xb.semantickernel.rag;

import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RagService - 检索增强生成服务（demo12 的编排层）
 *
 * <p><b>RAG 三段式</b>（与 Spring AI 版 demo08 是同一思想，此处用 SK 的 API 落地）：</p>
 * <ol>
 *   <li><b>索引 Indexing</b>：文档 → embedding → 向量库（离线完成，可重复）</li>
 *   <li><b>检索 Retrieval</b>：问题 → embedding → 余弦相似度 TopK</li>
 *   <li><b>生成 Generation</b>：检索结果拼进上下文 → 交给 ChatCompletionService</li>
 * </ol>
 *
 * <p><b>教学要点</b>：依赖的是 {@link TextEmbeddingGenerationService} 接口而非
 * OpenAI 实现类——接口依赖让单测可以用假 embedding 服务离线跑通全流程
 * （见 RagServiceTest），不需要真实 API Key。</p>
 *
 * @author ibqy
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final TextEmbeddingGenerationService embeddingService;
    private final InMemoryVectorStore vectorStore = new InMemoryVectorStore();

    public RagService(TextEmbeddingGenerationService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * 索引一批文档（重建式：先清空再写入，重复调用结果幂等）
     *
     * @param texts 文档原文列表（生产上应先切分，这里用整篇文档做教学简化）
     * @return 实际索引的文档数
     */
    public int indexDocuments(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            vectorStore.clear();
            return 0;
        }
        // 批量 embedding：一次调用处理全部文档，比逐条调用省往返
        List<List<Float>> vectors = embedBatch(texts);
        vectorStore.clear();
        for (int i = 0; i < texts.size(); i++) {
            vectorStore.add("doc-" + i, texts.get(i), vectors.get(i));
        }
        log.info("[RAG] 索引完成: {} 个文档", vectorStore.size());
        return vectorStore.size();
    }

    /**
     * 检索：把问题转成向量，返回最相似的 topK 条文档
     *
     * @param query 用户问题
     * @param topK  返回条数
     * @return 按相似度降序的结果（库为空返回空列表）
     */
    public List<InMemoryVectorStore.ScoredDocument> retrieve(String query, int topK) {
        if (query == null || query.isBlank() || vectorStore.size() == 0) {
            return List.of();
        }
        List<Float> queryVector = embedBatch(List.of(query)).get(0);
        List<InMemoryVectorStore.ScoredDocument> results = vectorStore.search(queryVector, topK);
        log.info("[RAG] 检索 '{}' → {} 条结果", query, results.size());
        return results;
    }

    /**
     * 把检索结果组装成可注入 Prompt 的上下文体
     *
     * <p>格式带编号，是为了让模型回答时能带引用（"根据文档1…"）——
     * 可追溯的答案比"听起来对"的答案更有价值（呼应幻觉抑制主题）。</p>
     *
     * @param results 检索结果
     * @return 上下文体；结果为空时返回空字符串（调用方应走"无依据"分支）
     */
    public String buildContext(List<InMemoryVectorStore.ScoredDocument> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append("文档").append(i + 1).append("（相关度 ")
                    .append(String.format("%.2f", results.get(i).score())).append("）: ")
                    .append(results.get(i).document().text()).append("\n");
        }
        return sb.toString();
    }

    /** 当前索引文档数 */
    public int indexedCount() {
        return vectorStore.size();
    }

    /**
     * 批量文本转向量
     *
     * <p>SK 的 embedding 服务是响应式 API（返回 {@code Mono}），
     * 本项目 Controller 是同步 MVC，这里 block() 等待结果——
     * 教学简化；WebFlux 项目应直接链式组合 Mono 不阻塞线程。</p>
     */
    private List<List<Float>> embedBatch(List<String> texts) {
        List<Embedding> embeddings = embeddingService.generateEmbeddingsAsync(texts).block();
        if (embeddings == null) {
            throw new IllegalStateException("embedding 服务无返回（网络异常或服务不可用）");
        }
        if (embeddings.size() != texts.size()) {
            throw new IllegalStateException(
                    "embedding 数量与输入不一致: " + embeddings.size() + " vs " + texts.size());
        }
        List<List<Float>> vectors = new ArrayList<>(embeddings.size());
        for (Embedding embedding : embeddings) {
            List<Float> vector = embedding.getVector();
            if (vector == null || vector.isEmpty()) {
                throw new IllegalStateException("embedding 服务返回了空向量");
            }
            vectors.add(vector);
        }
        return vectors;
    }
}
