# Demo01-02：Kernel 基础

## 知识点

- Kernel 构建与 Bean 注入
- `ChatCompletionService` 核心服务
- `ChatHistory` 消息容器
- `InvocationContext` 调用上下文
- 流式对话：`getStreamingChatMessageContentsAsync()`

## 架构概览

Semantic Kernel 的核心架构：

```
OpenAIAsyncClient → ChatCompletionService → Kernel
                                            ├── MathPlugin
                                            ├── TimePlugin
                                            ├── TodoListPlugin
                                            └── TimeTaskPlugin
```

## Kernel 配置

全局配置在 `SemanticKernelConfig` 中完成，启动时自动装配：

```java
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
```

配置属性通过 `application.yml` 注入：

```yaml
semantickernel:
  api-key: ${OPENAI_API_KEY:demo-placeholder}
  model: ${SEMANTIC_KERNEL_MODEL:qwen+-placeholder}
  endpoint: ${SEMANTIC_KERNEL_ENDPOINT:http://localhost:11434/v1}
  temperature: 0.7
```

## 基本对话 — Demo01

```java
@PostMapping("/chat")
public Map<String, Object> chat(@RequestBody Map<String, String> request) {
    String message = request.getOrDefault("message", "");

    ChatHistory history = new ChatHistory();
    history.addUserMessage(message);

    List<ChatMessageContent<?>> results = chatCompletionService
        .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
        .block();

    String reply = ChatHelper.lastAssistantText(results);
    return Map.of("role", "assistant", "reply", reply);
}
```

四步基本对话：
1. **创建 ChatHistory** — 消息容器
2. **添加用户消息** — `addUserMessage()`
3. **调用服务** — `getChatMessageContentsAsync(history, kernel, context)`
4. **提取回复** — `ChatHelper.lastAssistantText()` 从结果中提取最后一条 assistant 文本

## 流式对话 — Demo02

Demo02 使用 `getStreamingChatMessageContentsAsync()` 返回 `Flux<StreamingChatContent<?>>`，实现逐块接收：

```java
@GetMapping("/chat")
public Map<String, Object> chat(@RequestParam(defaultValue = "你好") String message) {
    ChatHistory history = new ChatHistory();
    history.addUserMessage(message);

    Flux<StreamingChatContent<?>> flux = chatCompletionService
        .getStreamingChatMessageContentsAsync(history, kernel, InvocationContext.builder().build());

    List<StreamingChatContent<?>> chunks = flux.collectList().block();
    StringBuilder sb = new StringBuilder();
    if (chunks != null) {
        for (StreamingChatContent<?> chunk : chunks) {
            if (chunk.getContent() != null) sb.append(chunk.getContent());
        }
    }
    return Map.of("mode", "streaming", "reply", sb.toString());
}
```

关键对比：

| 方法 | 返回类型 | 适用场景 |
|------|---------|---------|
| `getChatMessageContentsAsync` | `List<ChatMessageContent<?>>` | 一次性完整回复 |
| `getStreamingChatMessageContentsAsync` | `Flux<StreamingChatContent<?>>` | 实时逐块显示 |

## 小结

- `Kernel` 是 SK 的核心容器，持有 AI 服务和插件
- `ChatCompletionService` 是对话的核心接口
- `ChatHistory` 管理消息列表，支持 user/system/assistant 角色
- `InvocationContext` 控制调用行为（如工具调用策略）
- 流式输出用 `getStreamingChatMessageContentsAsync`，返回 `Flux`
