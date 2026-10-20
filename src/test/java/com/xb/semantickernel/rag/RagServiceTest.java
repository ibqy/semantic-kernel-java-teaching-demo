package com.xb.semantickernel.rag;

import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RagService 测试 —— 用假 embedding 服务离线跑通"索引→检索→上下文"全流程
 *
 * <p>教学价值：依赖 {@code TextEmbeddingGenerationService} <b>接口</b>而非 OpenAI 实现，
 * 使得整条 RAG 链路不需要 API Key 就能验证——这就是"面向接口编程"
 * 在测试上的直接回报。</p>
 *
 * @author ibqy
 */
@DisplayName("RagService 检索编排")
class RagServiceTest {

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(new FakeEmbeddingService());
    }

    @Test
    @DisplayName("索引后可检索，且返回语义最相关的文档")
    void indexThenRetrieve() {
        ragService.indexDocuments(List.of(
                "门店营业时间为每天 8:00-22:00",
                "会员积分规则：消费 1 元累计 1 积分",
                "配送范围覆盖门店 3 公里内，配送费 5 元",
                "支持 7 天无理由退换货"));

        List<InMemoryVectorStore.ScoredDocument> hits = ragService.retrieve("配送费多少钱", 2);

        assertThat(hits).hasSize(2);
        assertThat(hits.get(0).document().text()).contains("配送");
        assertThat(hits.get(0).score()).isGreaterThan(0);
    }

    @Test
    @DisplayName("context 组装带编号与相关度（可引用、可追溯）")
    void buildContextIsCitable() {
        ragService.indexDocuments(List.of(
                "配送范围覆盖门店 3 公里内，配送费 5 元",
                "会员积分规则：消费 1 元累计 1 积分"));
        String context = ragService.buildContext(ragService.retrieve("配送费", 2));

        assertThat(context).contains("文档1");
        assertThat(context).contains("相关度");
        assertThat(context).contains("配送");
    }

    @Test
    @DisplayName("未建索引时检索返回空（调用方应引导建索引而非硬答）")
    void retrieveWithoutIndexReturnsEmpty() {
        assertThat(ragService.retrieve("任意问题", 3)).isEmpty();
        assertThat(ragService.buildContext(List.of())).isEmpty();
        assertThat(ragService.indexedCount()).isZero();
    }

    @Test
    @DisplayName("空问题/空白问题返回空（不浪费 embedding 调用）")
    void blankQueryReturnsEmpty() {
        ragService.indexDocuments(List.of("文档一"));
        assertThat(ragService.retrieve("", 3)).isEmpty();
        assertThat(ragService.retrieve("   ", 3)).isEmpty();
        assertThat(ragService.retrieve(null, 3)).isEmpty();
    }

    @Test
    @DisplayName("重复索引幂等：文档数不翻倍")
    void reIndexIsIdempotent() {
        List<String> docs = List.of("文档一", "文档二");
        ragService.indexDocuments(docs);
        ragService.indexDocuments(docs);
        assertThat(ragService.indexedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("索引空列表等于清空")
    void indexEmptyClears() {
        ragService.indexDocuments(List.of("文档一"));
        ragService.indexDocuments(List.of());
        assertThat(ragService.indexedCount()).isZero();
    }

    @Test
    @DisplayName("embedding 服务异常向上传播（不静默返回空结果骗过调用方）")
    void embeddingFailurePropagates() {
        RagService broken = new RagService(new TextEmbeddingGenerationService() {
            @Override
            public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> values) {
                return Mono.error(new RuntimeException("embedding 服务不可用"));
            }

            @Override
            public Mono<Embedding> generateEmbeddingAsync(String value) {
                return Mono.error(new RuntimeException("embedding 服务不可用"));
            }

            @Override
            public String getModelId() {
                return "broken-model";
            }

            @Override
            public String getServiceId() {
                return "broken-service";
            }
        });

        // 用 indexDocuments 触发：retrieve 在空库时会直接返回空、根本不会调 embedding，
        // 测不到传播路径——选对触发方法是异常用例有效的前提
        assertThatThrownBy(() -> broken.indexDocuments(List.of("测试文档")))
                .hasMessageContaining("embedding 服务不可用");
    }
}
