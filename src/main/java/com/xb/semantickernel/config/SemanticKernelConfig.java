package com.xb.semantickernel.config;

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

    @Bean
    public OpenAIAsyncClient openAIAsyncClient(SemanticKernelProperties props) {
        return new OpenAIClientBuilder()
                .endpoint(props.endpoint())
                .credential(new AzureKeyCredential(props.apiKey()))
                .buildAsyncClient();
    }

    @Bean
    public ChatCompletionService chatCompletionService(
            OpenAIAsyncClient client, SemanticKernelProperties props) {
        return OpenAIChatCompletion.builder()
                .withModelId(props.model())
                .withOpenAIAsyncClient(client)
                .build();
    }

    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService, List<KernelPlugin> plugins) {
        Kernel.Builder builder = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService);
        for (KernelPlugin plugin : plugins) {
            builder.withPlugin(plugin);
        }
        return builder.build();
    }

    @Bean
    public KernelPlugin mathPlugin() {
        return KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin");
    }

    @Bean
    public KernelPlugin timePlugin() {
        return KernelPluginFactory.createFromObject(new TimePlugin(), "TimePlugin");
    }

    @Bean
    public KernelPlugin todoListPlugin() {
        return KernelPluginFactory.createFromObject(new TodoListPlugin(), "TodoListPlugin");
    }

    @Bean
    public KernelPlugin timeTaskPlugin() {
        return KernelPluginFactory.createFromObject(new TimeTaskPlugin(), "TimeTaskPlugin");
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}