package com.xb.semantickernel.rag;

import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用假 Embedding 服务 —— 词袋模型，确定性输出
 *
 * <p>真实 embedding 服务把文本映射到语义空间；测试里用"关键词计数"模拟：
 * 每个维度对应一个关键词，文本在该关键词上出现几次向量就是几。
 * 这样"配送费多少"的查询向量必然与配送文档最相似——<b>检索结果可预期</b>，
 * 测试才能断言"检索到了正确的文档"。</p>
 *
 * <p>注意：实现的是 {@link TextEmbeddingGenerationService}（String 特化接口，
 * 本身不带泛型参数），而非泛型的 {@code EmbeddingGenerationService<T>}。</p>
 *
 * @author ibqy
 */
public class FakeEmbeddingService implements TextEmbeddingGenerationService {

    /** 向量维度对应的关键词（顺序即维度） */
    public static final List<String> KEYWORDS = List.of("营业", "积分", "配送", "退", "包间");

    @Override
    public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> values) {
        List<Embedding> result = new ArrayList<>();
        for (String value : values) {
            result.add(new Embedding(toVector(value)));
        }
        return Mono.just(result);
    }

    @Override
    public Mono<Embedding> generateEmbeddingAsync(String value) {
        return Mono.just(new Embedding(toVector(value)));
    }

    /** 词袋向量：第 i 维 = 第 i 个关键词在文本中的出现次数 */
    public static List<Float> toVector(String text) {
        List<Float> vector = new ArrayList<>();
        for (String keyword : KEYWORDS) {
            int count = 0;
            int idx = 0;
            while ((idx = text.indexOf(keyword, idx)) >= 0) {
                count++;
                idx += keyword.length();
            }
            vector.add((float) count);
        }
        return vector;
    }

    // ===== AIService 接口要求的方法（假服务返回标识性常量即可）=====

    @Override
    public String getModelId() {
        return "fake-embedding-model";
    }

    @Override
    public String getServiceId() {
        return "fake-embedding-service";
    }
}
