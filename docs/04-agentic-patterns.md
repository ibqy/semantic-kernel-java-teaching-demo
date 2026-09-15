# Demo10-11：Agentic 模式实战

## 知识点

- Agentic 模式：模型自主选择工具完成对话
- Orchestrated 模式：程序显式编排函数调用
- `KernelArguments` 动态参数传递
- 两种模式的适用场景对比

## Agentic 模式 — Demo10 待办助手

Agentic 模式的核心是**模型自主决策**何时调用哪个工具。Demo10 结合 TodoListPlugin 实现了完整的多轮待办助手：

```java
private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();

@PostMapping("/chat")
public Map<String, Object> chat(@RequestBody Map<String, String> request) {
    String sessionId = request.getOrDefault("sessionId", "default");
    String message = request.getOrDefault("message", "");

    ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());

    // 首次对话注入系统提示
    if (history.getMessages().isEmpty()) {
        history.addSystemMessage(
            "你是一位高效的中文待办助手。你会使用 addTask 新增待办、用 listTasks 查看待办。"
            + "回答简短友好；在需要操作待办时务必调用工具，不要虚构待办清单内容。");
    }

    history.addUserMessage(message);

    // 启用模型自主工具调用
    InvocationContext ctx = InvocationContext.builder()
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .build();

    List<ChatMessageContent<?>> results = chatCompletionService
        .getChatMessageContentsAsync(history, kernel, ctx).block();

    String reply = ChatHelper.lastAssistantText(results);
    history.addAssistantMessage(reply);  // 保存回复到历史

    return Map.of("sessionId", sessionId,
        "historySize", history.getMessages().size(), "assistant", reply);
}
```

运行示例：
```
用户："帮我添加一个待办：明天下午3点开会"
助手：（自动调用 addTask）"已添加待办：明天下午3点开会"

用户："我现在有哪些待办？"
助手：（自动调用 listTasks）"当前待办：1. 明天下午3点开会"
```

## Agentic vs Orchestrated 双模式 — Demo11

Demo11 是项目的综合实战，同时展示了两种模式，并支持更多工具组合（定时提醒 + 待办 + 数学 + 时间）。

### Agentic 模式 (`/api/demo11/handle`)

与 Demo10 类似，模型自主调度所有注册的工具：

```java
history.addSystemMessage(
    "你是一位专业的定时待办管家。可使用的工具："
    + "scheduleReminder（安排/查看定时提醒）、addTask（新增待办）、"
    + "listTasks（查看待办）、getCurrentTime（获取当前时间）、"
    + "add/subtract/multiply/divide（数学计算）。"
    + "根据用户需要主动调用工具，不凭空编造待办内容；回答简短友好。");

InvocationContext ctx = InvocationContext.builder()
    .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
    .build();
```

### Orchestrated 模式 (`/api/demo11/invoke`)

程序显式调用指定插件的指定函数，适合确定性计算场景：

```java
@GetMapping("/invoke")
public Map<String, Object> invoke(
        @RequestParam String plugin,
        @RequestParam String function,
        @RequestParam(defaultValue = "String") String resultType,
        @RequestParam Map<String, String> allParams) {

    // 动态构建参数
    KernelArguments.Builder<?> builder = KernelArguments.builder();
    for (Map.Entry<String, String> e : allParams.entrySet()) {
        // ... 类型判断与参数注入
        builder.withVariable(key, value);
    }

    Object result = kernel.invokeAsync(plugin, function)
        .withArguments(builder.build())
        .withResultType(resultClass)
        .block().getResult();

    return Map.of("mode", "orchestrated（程序显式编排函数）", "result", result);
}
```

Demo11 还提供了 `/api/demo11/tick` 端点展示最简单的显式编排：

```java
@GetMapping("/tick")
public Map<String, Object> tick() {
    int next = counter.incrementAndGet();
    Integer added = kernel.invokeAsync("MathPlugin", "add")
        .withArguments(KernelArguments.builder()
            .withVariable("a", next).withVariable("b", 10).build())
        .withResultType(Integer.class).block().getResult();
    return Map.of("counter", next, "counterPlusOne", added);
}
```

## 模式选择指南

```
需要模型自主决策？
    ├── 是 → Agentic 模式
    │        └── ToolCallBehavior.allowAllKernelFunctions(true)
    │        └── 适用：开放域对话、任务编排、用户意图不明确
    │
    └── 否 → Orchestrated 模式
             └── kernel.invokeAsync(plugin, function)
             └── 适用：确定性计算、固定流程、性能敏感场景
```

## 小结

- **Agentic**：模型自主选择工具，适合开放域任务
- **Orchestrated**：程序显式编排，适合确定性和高性能场景
- Demo11 同时展示两种模式，可在同一项目中按需选用
- `ToolCallBehavior.allowAllKernelFunctions(true)` 是 Agentic 模式的关键开关
- `KernelArguments` 支持动态参数传递，类型自动适配
