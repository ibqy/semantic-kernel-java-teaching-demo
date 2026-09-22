package com.xb.semantickernel.config;

/**
 * SemanticKernelProperties - Semantic Kernel 配置属性绑定
 *
 * 使用 Java record 配合 {@code @ConfigurationProperties} 将 application.yml
 * 中 {@code semantickernel.*} 前缀的配置项映射为强类型对象，
 * 是教学中演示 Spring Boot 外部化配置与 record 结合的典型示例。
 *
 * @author ibqy
 */
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Semantic Kernel 所需的核心配置参数
 *
 * @param apiKey      Azure OpenAI 的 API 密钥
 * @param model       使用的模型标识（如 gpt-4o-mini）
 * @param endpoint    Azure OpenAI 的服务端点地址
 * @param temperature 生成文本的随机程度，值越高越随机
 */
@ConfigurationProperties(prefix = "semantickernel")
public record SemanticKernelProperties(
        String apiKey,
        String model,
        String endpoint,
        Double temperature) {
}