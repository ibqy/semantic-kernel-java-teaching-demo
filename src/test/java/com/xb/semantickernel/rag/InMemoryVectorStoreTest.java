package com.xb.semantickernel.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * 迷你向量库测试 —— RAG 检索内核的数学正确性
 *
 * <p>余弦相似度是 RAG 的"第一性原理"，算错一步，检索全盘皆错。
 * 这些用例全部离线可跑（不需要 embedding 服务）。</p>
 *
 * @author ibqy
 */
@DisplayName("InMemoryVectorStore 迷你向量库")
class InMemoryVectorStoreTest {

    private InMemoryVectorStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryVectorStore();
    }

    @Test
    @DisplayName("余弦相似度：同向量=1，正交=0，反向=-1")
    void cosineSimilarityMath() {
        // 浮点断言必须带偏移量：sqrt 运算后 -5.0/5.0 可能得到 -0.9999999999999998
        // （IEEE754 双精度固有误差），用 isEqualTo 比较 double 是常见脆测试
        assertThat(InMemoryVectorStore.cosineSimilarity(List.of(1f, 0f, 0f), List.of(1f, 0f, 0f)))
                .isCloseTo(1.0, within(1e-9));
        assertThat(InMemoryVectorStore.cosineSimilarity(List.of(1f, 0f), List.of(0f, 1f)))
                .isCloseTo(0.0, within(1e-9));
        assertThat(InMemoryVectorStore.cosineSimilarity(List.of(1f, 2f), List.of(-1f, -2f)))
                .isCloseTo(-1.0, within(1e-9));
        // 方向一致即可，模长无关： (2,0) 与 (5,0) 相似度仍为 1
        assertThat(InMemoryVectorStore.cosineSimilarity(List.of(2f, 0f), List.of(5f, 0f)))
                .isCloseTo(1.0, within(1e-9));
    }

    @Test
    @DisplayName("零向量返回 0（数学上未定义，工程上按不相似处理）")
    void zeroVectorIsNotSimilar() {
        assertThat(InMemoryVectorStore.cosineSimilarity(List.of(0f, 0f), List.of(1f, 1f)))
                .isEqualTo(0.0);
    }

    @Test
    @DisplayName("维度不一致抛异常（索引与查询用了不同模型是真实事故）")
    void dimensionMismatchThrows() {
        assertThatThrownBy(() -> InMemoryVectorStore.cosineSimilarity(
                List.of(1f, 2f, 3f), List.of(1f, 2f)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("维度不一致");
    }

    @Test
    @DisplayName("检索按相似度降序返回 TopK")
    void searchReturnsTopKByScore() {
        store.add("doc-1", "配送规则文档", List.of(0f, 0f, 3f, 0f, 0f));
        store.add("doc-2", "积分规则文档", List.of(0f, 2f, 0f, 0f, 0f));
        store.add("doc-3", "营业时间文档", List.of(1f, 0f, 0f, 0f, 0f));

        // 查询偏向"配送"
        List<InMemoryVectorStore.ScoredDocument> hits =
                store.search(List.of(0f, 0f, 5f, 0f, 0f), 2);

        assertThat(hits).hasSize(2);
        assertThat(hits.get(0).document().id()).isEqualTo("doc-1");
        assertThat(hits.get(0).score()).isCloseTo(1.0, within(1e-9));
        assertThat(hits.get(1).document().id()).isEqualTo("doc-2");
        assertThat(hits.get(1).score()).isCloseTo(0.0, within(1e-9));
    }

    @Test
    @DisplayName("TopK 大于库容量时返回全部，不报错")
    void topKLargerThanStore() {
        store.add("doc-1", "A", List.of(1f));
        store.add("doc-2", "B", List.of(1f));
        assertThat(store.search(List.of(1f), 10)).hasSize(2);
    }

    @Test
    @DisplayName("空库/topK<=0 返回空列表（不抛异常）")
    void emptyAndInvalidInput() {
        assertThat(store.search(List.of(1f), 5)).isEmpty();
        store.add("doc-1", "A", List.of(1f));
        assertThat(store.search(List.of(1f), 0)).isEmpty();
        assertThat(store.search(List.of(1f), -1)).isEmpty();
    }

    @Test
    @DisplayName("同 id 重复写入覆盖旧值（索引幂等）")
    void sameIdOverwrites() {
        store.add("doc-1", "旧内容", List.of(1f, 0f));
        store.add("doc-1", "新内容", List.of(0f, 1f));
        assertThat(store.size()).isEqualTo(1);

        List<InMemoryVectorStore.ScoredDocument> hits = store.search(List.of(0f, 1f), 1);
        assertThat(hits.get(0).document().text()).isEqualTo("新内容");
        assertThat(hits.get(0).score()).isCloseTo(1.0, within(1e-9));
    }
}
