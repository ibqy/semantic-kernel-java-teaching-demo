package com.xb.semantickernel.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * InMemoryVectorStore - 迷你内存向量库（RAG 的检索内核）
 *
 * <p><b>教学定位</b>：RAG 听起来神秘，检索内核其实就是"算余弦相似度 + 取 TopK"。
 * Semantic Kernel 的 {@code VectorStore} 抽象是同一件事的工程化封装
 * （异步集合、记录映射、多种后端），但对"理解原理"来说是过度设计。
 * 这个类用 60 行把内核讲清楚，且<b>不依赖任何外部服务，可以离线单测</b>。</p>
 *
 * <p><b>算法</b>：余弦相似度 = (A·B) / (|A|×|B|)，取值 [-1, 1]，1 表示方向完全一致。
 * 选余弦而不是欧氏距离的原因：文本向量的"长度"携带文档长度信息，
 * 而我们关心的是语义方向（主题是否相近），与篇幅无关。</p>
 *
 * @author ibqy
 */
public class InMemoryVectorStore {

    /** 库中的一条文档（含向量） */
    public record Document(String id, String text, List<Float> vector) {}

    /** 检索结果：文档 + 相似度得分 */
    public record ScoredDocument(Document document, double score) {}

    private final List<Document> documents = new CopyOnWriteArrayList<>();

    /** 写入一条文档（重复 id 会覆盖旧值，保证索引幂等） */
    public void add(String id, String text, List<Float> vector) {
        documents.removeIf(d -> d.id().equals(id));
        documents.add(new Document(id, text, vector));
    }

    /** 清空索引（重建索调用） */
    public void clear() {
        documents.clear();
    }

    /** 当前文档数 */
    public int size() {
        return documents.size();
    }

    /**
     * 相似度检索：返回与 queryVector 最相似的 topK 条文档（按得分降序）
     *
     * @param queryVector 查询向量
     * @param topK        返回条数（<=0 时返回空列表）
     * @return 按相似度降序排列的结果；库为空时返回空列表
     */
    public List<ScoredDocument> search(List<Float> queryVector, int topK) {
        if (topK <= 0 || documents.isEmpty()) {
            return List.of();
        }
        List<ScoredDocument> scored = new ArrayList<>();
        for (Document doc : documents) {
            scored.add(new ScoredDocument(doc, cosineSimilarity(queryVector, doc.vector())));
        }
        // 得分降序；同分时按 id 升序，保证结果稳定（测试可复现）
        scored.sort(Comparator.comparingDouble(ScoredDocument::score).reversed()
                .thenComparing(s -> s.document().id()));
        return scored.subList(0, Math.min(topK, scored.size()));
    }

    /**
     * 计算两个向量的余弦相似度
     *
     * <p>边界处理：维度不一致抛 IllegalArgumentException（上游数据错误，
     * 不应该静默算出一个错数）；零向量返回 0.0（数学上未定义，工程上按"不相似"处理）。</p>
     */
    public static double cosineSimilarity(List<Float> a, List<Float> b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("向量不能为 null");
        }
        if (a.size() != b.size()) {
            throw new IllegalArgumentException(
                    "向量维度不一致: " + a.size() + " vs " + b.size()
                            + "（通常是索引与查询用了不同的 embedding 模型）");
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double x = a.get(i);
            double y = b.get(i);
            dot += x * y;
            normA += x * x;
            normB += y * y;
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
