# Demo03-05 & 09：对话历史管理与 Prompt 工程

## 知识点

- `ChatHistory` 多轮对话管理
- Session 会话隔离
- `addSystemMessage()` 角色注入
- 动态 System Prompt 参数化构造
- 结构化 JSON 输出与反序列化

## ChatHistory 多轮对话 — Demo03

`ChatHistory` 是 Semantic Kernel 的核心消息容器。Demo03 展示了如何利用它实现多轮对话记忆：

```java
private final Map<String, ChatHistory> sessions = new ConcurrentHashMap<>();

@PostMapping("/chat")
public Map<String, Object> chat(@RequestBody Map<String, String> request) {
    String sessionId = request.getOrDefault("sessionId", "default");
    String message = request.getOrDefault("message", "");

    // 获取或创建会话的 ChatHistory
    ChatHistory history = sessions.computeIfAbsent(sessionId, k -> new ChatHistory());

    // 添加用户消息
    history.addUserMessage(message);

    // 调用服务（携带完整历史）
    List<ChatMessageContent<?>> results = chatCompletionService
        .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
        .block();

    // 提取并保存回复
    String reply = ChatHelper.lastAssistantText(results);
    history.addAssistantMessage(reply);

    return Map.of("sessionId", sessionId,
        "historySize", history.getMessages().size(),
        "reply", reply);
}
```

关键点：
- `sessions` Map 按 sessionId 隔离对话，不同用户互不干扰
- `addAssistantMessage()` 将模型回复也写入历史，保证上下文完整
- 每次请求携带完整 `ChatHistory`，模型能"记住"之前所有对话

### 消息角色

SK 支持三种消息角色：

| 方法 | 角色 | 用途 |
|------|------|------|
| `addSystemMessage()` | System | 设定 AI 行为规范 |
| `addUserMessage()` | User | 用户输入 |
| `addAssistantMessage()` | Assistant | 模型回复 |

## 系统角色注入 — Demo04

通过 `addSystemMessage()` 为 AI 注入特定人格和行为准则：

```java
ChatHistory history = new ChatHistory();
history.addSystemMessage(
    "你是一位出身在字节跳动、代号'宽窄巷子'的资深架构师，"
    + "要求极其严格，点评代码时先挑毛病再夸奖，语气简洁犀利，全程使用中文。");
history.addUserMessage(message);
```

效果：后续所有回复都会以"严格架构师"的口吻给出。

## 动态 Prompt 模板 — Demo05

Demo05 展示了如何参数化构造 System Prompt，实现动态输出控制：

```java
@PostMapping("/chat")
public Map<String, Object> chat(@RequestBody Map<String, String> request) {
    String topic = request.getOrDefault("topic", "Semantic Kernel");
    int lines = Integer.parseInt(request.getOrDefault("lines", "2"));

    String systemPrompt = """
        你是一名资深技术讲师。请围绕给定主题写一段面向中文初学者的介绍。
        要求：
        - 精确地输出 %d 行要点；
        - 每行以"- "开头；
        - 只输出要点本身，不要任何客套话或 Markdown 代码块。
        """.formatted(lines);

    String userPrompt = "主题：" + topic;

    ChatHistory history = new ChatHistory();
    history.addSystemMessage(systemPrompt);
    history.addUserMessage(userPrompt);
    // ... 调用服务
}
```

核心技巧：
- Java 文本块 `"""` 编写可读的 Prompt 模板
- `.formatted()` 动态插入参数
- System Prompt 约束输出格式，User Prompt 提供具体内容

## 结构化 JSON 输出 — Demo09

Demo05 用 System Prompt 控制输出行数和格式，Demo09 将这个技巧推向极致：**要求模型只能输出合法 JSON**，然后用 Jackson 反序列化为 Java 对象。

```java
@PostMapping("/country")
public Map<String, Object> country(@RequestBody Map<String, String> request) {
    String country = request.getOrDefault("country", "中国");

    ChatHistory history = new ChatHistory();
    history.addSystemMessage("""
        你是一个信息抽取助手。你会收到一个国家名，请返回该国的以下信息，
        并且【只能】输出如下 JSON，不要任何额外文字、解释或 Markdown 代码块：
        {"name":"国家名","capital":"首都","population":人口数(整数),"currency":"货币"}
        """);
    history.addUserMessage("国家：" + country);

    List<ChatMessageContent<?>> results = chatCompletionService
        .getChatMessageContentsAsync(history, kernel, InvocationContext.builder().build())
        .block();

    String raw = ChatHelper.lastAssistantText(results);
    // 防御性清洗：去除模型可能附加的 Markdown 代码块标记
    String json = raw.replaceAll("```json", "").replaceAll("```", "").trim();

    CountryInfo info = CountryInfo.empty();
    try {
        info = objectMapper.readValue(json, CountryInfo.class);
    } catch (Exception e) {
        info = new CountryInfo("解析失败", e.getMessage(), 0L, raw);
    }
    return Map.of("parsed", info, "rawModelOutput", raw);
}
```

目标数据结构使用 Java `record`：

```java
public record CountryInfo(
    String name,
    String capital,
    long population,
    String currency) {

    public static CountryInfo empty() {
        return new CountryInfo("", "", 0L, "");
    }
}
```

### 关键技巧

1. **System Prompt 强制格式** — "只能输出 JSON，不要任何额外文字"
2. **防御性清洗** — `replaceAll("```json", "")` 去除模型偶尔附加的 Markdown 标记
3. **异常降级** — 解析失败时返回 `CountryInfo("解析失败", ...)` 而非抛异常，保证 API 可用性

### 输出对比

| 维度 | 自由文本（Demo01-05） | 结构化 JSON（Demo09） |
|------|----------------------|----------------------|
| 输出格式 | 自然语言，灵活但不确定 | 严格 JSON，可机器解析 |
| 下游处理 | 直接展示给用户 | 反序列化为 Java 对象，参与业务逻辑 |
| 可靠性 | 高（格式约束少） | 中（需防御性清洗 + 异常处理） |
| 适用场景 | 对话、问答、创作 | 信息抽取、数据转换、API 编排 |

::: warning 注意
不同模型对 JSON 格式指令的遵循程度不同。生产环境中建议：
- 选择支持 JSON Mode 的模型（如 GPT-4o 的 `response_format: json_object`）
- 始终做防御性清洗和异常处理
- 在 System Prompt 中给出明确的 JSON Schema 示例
:::

## 小结

- `ChatHistory` 是实现多轮对话的关键，每次请求携带完整历史
- 用 `Map<String, ChatHistory>` 按 sessionId 实现会话隔离
- `addSystemMessage()` 注入角色人格、行为约束和输出格式
- 文本块 + `.formatted()` 实现参数化 Prompt 模板
- System Prompt 可强制模型输出结构化 JSON，配合 Jackson 实现 LLM → Java 对象的端到端管道
