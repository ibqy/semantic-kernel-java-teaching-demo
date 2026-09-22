package com.xb.semantickernel.config;

/**
 * SemanticKernelConfig - Semantic Kernel 核心 Bean 装配配置
 *
 * 本类是整个演示项目的"装配中心"，负责创建 OpenAI 客户端、聊天补全服务、
 * Kernel 实例以及所有自定义插件（Plugin）。教学中用于展示 Spring {@code @Configuration}
 * 与 Semantic Kernel SDK 的集成方式，以及依赖注入的层级关系。
 *
 * @author ibqy
 */
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.xb.semantickernel.plugin.MathPlugin;
import com.xb.semantickernel.plugin.TimePlugin;
import com.xb.semantickernel.plugin.TimeTaskPlugin;
import com.xb.semantickernel.plugin.TodoListPlugin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SemanticKernelProperties.class)
public class SemanticKernelConfig {

    /**
     * 创建 Azure OpenAI 异步客户端
     *
     * 使用配置中的 endpoint 和 apiKey 进行认证，
     * 异步客户端是后续所有 AI 调用的底层通道。
     *
     * @param props 从配置文件绑定的 Semantic Kernel 参数
     * @return 已认证的 OpenAI 异步客户端实例
     */
    @Bean
    public OpenAIAsyncClient openAIAsyncClient(SemanticKernelProperties props) {
        return new OpenAIClientBuilder()
                .endpoint(props.endpoint())
                .credential(new AzureKeyCredential(props.apiKey()))
                .buildAsyncClient();
    }

    /**
     * 创建聊天补全服务
     *
     * 将异步客户端与指定模型绑定，封装为 Semantic Kernel
     * 统一的 ChatCompletionService 接口供上层使用。
     *
     * @param client Azure OpenAI 异步客户端
     * @param props  配置属性，提供模型标识
     * @return 聊天补全服务实例
     */
    @Bean
    public ChatCompletionService chatCompletionService(
            OpenAIAsyncClient client, SemanticKernelProperties props) {
        return OpenAIChatCompletion.builder()
                .withModelId(props.model())
                .withOpenAIAsyncClient(client)
                .build();
    }

    /**
     * 创建 Semantic Kernel 核心实例
     *
     * 将聊天补全服务注册为默认 AI 服务，并遍历容器中所有 KernelPlugin
     * 逐一挂载到 Kernel 上，使模型能在对话中调用这些工具函数。
     *
     * @param chatCompletionService 聊天补全服务
     * @param plugins               Spring 容器中所有已注册的插件列表
     * @return 装配完成的 Kernel 实例
     */
    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService, List<KernelPlugin> plugins) {
        Kernel.Builder builder = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService);
        // 将 Spring 容器中所有 KernelPlugin Bean 逐一注册到 Kernel
        for (KernelPlugin plugin : plugins) {
            builder.withPlugin(plugin);
        }
        return builder.build();
    }

    /** 将 MathPlugin 包装为 KernelPlugin，提供加减乘除工具函数 */
    @Bean
    public KernelPlugin mathPlugin() {
        return KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin");
    }

    /** 将 TimePlugin 包装为 KernelPlugin，提供获取当前时间的工具函数 */
    @Bean
    public KernelPlugin timePlugin() {
        return KernelPluginFactory.createFromObject(new TimePlugin(), "TimePlugin");
    }

    /** 将 TodoListPlugin 包装为 KernelPlugin，提供待办事项增删查的工具函数 */
    @Bean
    public KernelPlugin todoListPlugin() {
        return KernelPluginFactory.createFromObject(new TodoListPlugin(), "TodoListPlugin");
    }

    /** 将 TimeTaskPlugin 包装为 KernelPlugin，提供定时提醒安排的工具函数 */
    @Bean
    public KernelPlugin timeTaskPlugin() {
        return KernelPluginFactory.createFromObject(new TimeTaskPlugin(), "TimeTaskPlugin");
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}