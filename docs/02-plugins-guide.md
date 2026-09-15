# Demo06-07：插件开发与工具调用

## 知识点

- `@DefineKernelFunction` 注解定义插件函数
- `KernelPluginFactory.createFromObject()` 注册插件
- `kernel.invokeAsync()` 显式编排调用
- `ToolCallBehavior.allowAllKernelFunctions()` 模型自主工具调用

## 定义插件函数 — @DefineKernelFunction

Semantic Kernel 通过注解将普通 Java 方法暴露为 Kernel 可调用的函数。以 `MathPlugin` 为例：

```java
public class MathPlugin {

    @DefineKernelFunction(name = "add", description = "计算两个整数的和")
    public int add(
            @KernelFunctionParameter(name = "a", description = "第一个加数") int a,
            @KernelFunctionParameter(name = "b", description = "第二个加数") int b) {
        return a + b;
    }

    @DefineKernelFunction(name = "subtract", description = "计算两个整数的差")
    public int subtract(
            @KernelFunctionParameter(name = "a", description = "被减数") int a,
            @KernelFunctionParameter(name = "b", description = "减数") int b) {
        return a - b;
    }

    @DefineKernelFunction(name = "multiply", description = "计算两个整数的乘积")
    public int multiply(...) { return a * b; }

    @DefineKernelFunction(name = "divide", description = "计算两个整数的商")
    public int divide(...) { return a / b; }
}
```

项目内置 4 个插件：

| 插件 | 函数 | 功能 |
|------|------|------|
| `MathPlugin` | add / subtract / multiply / divide | 四则运算 |
| `TimePlugin` | getCurrentTime | 获取当前时间 |
| `TodoListPlugin` | addTask / listTasks | 待办管理 |
| `TimeTaskPlugin` | scheduleReminder / listReminders | 定时提醒 |

## 注册插件

两种注册方式：

### 方式一：全局 Bean（推荐）

在 `SemanticKernelConfig` 中注册为 Spring Bean，自动注入 Kernel：

```java
@Bean
public KernelPlugin mathPlugin() {
    return KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin");
}
```

Kernel 构建时自动收集所有 `KernelPlugin` Bean：

```java
@Bean
public Kernel kernel(ChatCompletionService cs, List<KernelPlugin> plugins) {
    Kernel.Builder builder = Kernel.builder()
        .withAIService(ChatCompletionService.class, cs);
    for (KernelPlugin plugin : plugins) {
        builder.withPlugin(plugin);
    }
    return builder.build();
}
```

### 方式二：运行时动态创建（Demo06）

```java
KernelPlugin plugin = KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin");
```

## 显式编排调用 — Demo06

Demo06 演示了**程序显式调用**插件函数（Orchestrated 模式）：

```java
@GetMapping("/calc")
public Map<String, Object> calc(
        @RequestParam int a, @RequestParam int b,
        @RequestParam(defaultValue = "add") String op) {

    KernelArguments args = KernelArguments.builder()
        .withVariable("a", a)
        .withVariable("b", b)
        .build();

    FunctionResult<Integer> result = kernel.invokeAsync("MathPlugin", op)
        .withArguments(args)
        .withResultType(Integer.class)
        .block();

    Integer value = result.getResult();
    return Map.of("op", op, "a", a, "b", b, "result", value);
}
```

调用链：`kernel.invokeAsync(pluginName, functionName)` → `.withArguments(args)` → `.withResultType()` → `.block()`

## 模型自主工具调用 — Demo07

Demo07 演示了**模型自主决定调用哪些工具**（Agentic 模式）：

```java
@PostMapping("/chat")
public Map<String, Object> chat(@RequestBody Map<String, String> request) {
    String question = request.getOrDefault("question", "");
    ChatHistory history = new ChatHistory();
    history.addUserMessage(question);

    InvocationContext invocationContext = InvocationContext.builder()
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .build();

    List<ChatMessageContent<?>> results = chatCompletionService
        .getChatMessageContentsAsync(history, kernel, invocationContext)
        .block();

    return Map.of("toolCalling", true,
        "reply", ChatHelper.lastAssistantText(results));
}
```

关键配置：

```java
ToolCallBehavior.allowAllKernelFunctions(true)
```

这让模型可以自动发现并调用 Kernel 中注册的所有插件函数，无需程序显式指定。

## 两种模式对比

| 维度 | Orchestrated（Demo06） | Agentic（Demo07） |
|------|------------------------|-------------------|
| 调用方式 | `kernel.invokeAsync()` 显式调用 | 模型自主选择 |
| 控制力 | 程序完全控制 | 模型自主决策 |
| 适用场景 | 确定性计算、固定流程 | 开放域对话、灵活任务 |
| 工具选择 | 程序指定插件和函数 | 模型根据描述自动匹配 |

## 小结

- `@DefineKernelFunction` + `@KernelFunctionParameter` 定义工具函数
- `KernelPluginFactory.createFromObject()` 注册插件
- `kernel.invokeAsync(plugin, function)` 显式编排
- `ToolCallBehavior.allowAllKernelFunctions(true)` 启用模型自主工具调用
- 全局 Bean 注册 > 运行时动态创建
