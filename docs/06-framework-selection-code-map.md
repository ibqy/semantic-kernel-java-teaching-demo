# 框架选型与代码地图

> 结论先行：本项目用 Semantic Kernel Java 1.5.0 讲 AI 应用的**通用心智模型**。
> 学完这一章，你应该能回答两个问题：① SK 与 Spring AI / LangChain4j 差在哪；
> ② 12 个 demo 的代码分别在哪、对应哪一章。

## 1. 三个 Java AI 框架的定位

Java 生态在 2024-2026 年形成了三条并行的 AI 应用路线。它们解决的是同一类问题（把 LLM 接入 Java 应用），但出发点不同：

| 维度 | Semantic Kernel (SK) | Spring AI | LangChain4j |
|:---|:---|:---|:---|
| 出身 | 微软，.NET 优先、Java 跟进 | Spring 官方团队 | 社区驱动（原 LangChain4j 独立项目） |
| Spring 融合 | 需手写 `@Configuration` 装配 | 一等公民，starter + 自动配置 | 有 spring-boot-starter |
| 核心抽象 | `Kernel` + `KernelPlugin` | `ChatClient` + `Advisor` + `ToolCallback` | `AiServices` + `ChatModel` |
| 插件定义 | `@DefineKernelFunction` | `@Tool` | `@Tool` |
| 记忆 | `ChatHistory`（手动管理） | `ChatMemory` + 多种实现 | `ChatMemory` / `ChatMemoryProvider` |
| 向量库 | `VectorStore` 抽象（需自行接入） | 官方 starter 覆盖主流向量库 | 内置多种 embedding store |
| 强项 | 概念统一、跨语言心智模型 | 与 Spring 生态无缝、企业级配套 | 功能覆盖广、迭代快 |
| 弱项 | Java 版发布节奏慢于另两者 | 抽象层较厚，行为需读源码确认 | 版本演进快，API 有变动 |

**怎么选**（一句话版）：

- 团队主栈是 Spring Boot、要快速上线 → **Spring AI**（对应 [spring-ai-teaching-demo](https://ibqy.github.io/spring-ai-teaching-demo/)）
- 需要丰富的内置能力（RAG、Agent、Guardrail）、接受更快的 API 变动 → **LangChain4j**
- 想理解 AI 框架的**通用抽象**、或团队已有 .NET/Azure OpenAI 背景 → **SK**（本项目）

### 为什么教学路线仍要保留 SK

不是因为 SK 更"好"，而是因为它把 AI 应用拆成了最小的一组概念：Kernel（执行容器）、Plugin（能力）、ChatHistory（记忆）、ToolCallBehavior（调用策略）。这四个概念在三个框架里**一一对应**，只是名字不同。先学最朴素的版本，再看 Spring AI 的封装，会清楚每一层在替你做什么。

## 2. 心智模型映射表

这张表是"迁移成本"的度量。会了左列，换到右列只是换名字：

| SK 概念 | Spring AI 对应物 | 本项目落点 |
|:---|:---|:---|
| `Kernel` | `ChatClient` / Bean 装配 | [SemanticKernelConfig#kernel](../src/main/java/com/xb/semantickernel/config/SemanticKernelConfig.java) |
| `ChatCompletionService` | `ChatModel` | [SemanticKernelConfig#chatCompletionService](../src/main/java/com/xb/semantickernel/config/SemanticKernelConfig.java) |
| `KernelPlugin` | 含 `@Tool` 方法的 Bean | `mathPlugin()` / `timePlugin()` / `todoListPlugin()` / `timeTaskPlugin()` |
| `@DefineKernelFunction` | `@Tool` | [MathPlugin#add](../src/main/java/com/xb/semantickernel/plugin/MathPlugin.java) |
| `@KernelFunctionParameter` | `@ToolParam` | `MathPlugin#add(a, b)` |
| `ChatHistory` | `ChatMemory` + 会话窗口 | [Demo03](../src/main/java/com/xb/semantickernel/controller/demo03/Demo03ChatHistoryMemoryController.java) |
| `ToolCallBehavior.allowAllKernelFunctions(true)` | 默认启用工具自动调用 | [Demo11#handle](../src/main/java/com/xb/semantickernel/controller/demo11/Demo11TimeTaskAssistantController.java) |
| `InvocationContext` | `ChatOptions` | Demo05 / Demo07 |
| `TextEmbeddingGenerationService` | `EmbeddingModel` | [RagService](../src/main/java/com/xb/semantickernel/rag/RagService.java) |
| `KernelArguments` | 方法入参 | [Demo11#invoke](../src/main/java/com/xb/semantickernel/controller/demo11/Demo11TimeTaskAssistantController.java) |
| `kernel.invokeAsync(plugin, function)` | 直接方法调用（不经模型） | [Demo11#tick](../src/main/java/com/xb/semantickernel/controller/demo11/Demo11TimeTaskAssistantController.java) |

注意最后一行：`invokeAsync` **不经过模型**，是纯函数调用。这是理解"Agentic vs Orchestrated"的关键 —— 见第 5 节。

## 3. 代码地图：12 个 demo

### 3.1 基础对话层（demo01-05）

| Demo | 端点 | 关键类 | 核心 API | 文档 |
|:---|:---|:---|:---|:---|
| 01 基础对话 | `POST /api/demo01/chat` | `Demo01BasicChatController` | `getChatMessageContentsAsync` | [01](/01-kernel-basics) |
| 02 流式输出 | `GET /api/demo02/chat` | `Demo02StreamingChatController` | 流式消息收集 | [01](/01-kernel-basics) |
| 03 对话历史 | `POST /api/demo03/chat` | `Demo03ChatHistoryMemoryController` | `ChatHistory` + `ConcurrentHashMap` 会话 | [03](/03-chat-history) |
| 04 系统角色 | `POST /api/demo04/chat` | `Demo04SystemRoleController` | `addSystemMessage` | [03](/03-chat-history) |
| 05 提示词模板 | `POST /api/demo05/chat` | `Demo05PromptTemplateController` | 文本块 + `.formatted()` | [03](/03-chat-history) |

`Demo05` 值得单独看一眼：它没有引入任何模板引擎，直接用 Java 文本块做参数化：

```java
String systemPrompt = """
        你是一名资深技术讲师。请围绕给定主题写一段面向中文初学者的介绍。
        要求：
        - 精确地输出 %d 行要点；
        ...
        """.formatted(lines);
```

这提醒一件事：**Prompt 模板不需要框架**，需要的是把 Prompt 当代码管理（版本化、可评审、可测试）。

### 3.2 插件与工具调用层（demo06-11）

| Demo | 端点 | 关键类 | 教学点 | 文档 |
|:---|:---|:---|:---|:---|
| 06 自定义函数 | `GET /api/demo06/calc` | `Demo06DefineKernelFunctionController` | **程序调用函数**，模型不参与 | [02](/02-plugins-guide) |
| 07 工具调用 | `POST /api/demo07/chat` | `Demo07ToolCallingController` | **模型决定调哪个函数** | [02](/02-plugins-guide) |
| 08 角色 + 工具 | `POST /api/demo08/chat` | `Demo08SystemRolePlusToolController` | 角色约束与工具共存 | [02](/02-plugins-guide) |
| 09 结构化输出 | `POST /api/demo09/country` | `Demo09JsonOutputController` | System 约束 JSON + Jackson 反序列化 | [04](/04-agentic-patterns) |
| 10 待办助手 | `POST /api/demo10/chat` | `Demo10TodoAssistantController` | 多轮 + `TodoListPlugin` 状态 | [04](/04-agentic-patterns) |
| 11 定时任务助手 | `POST /api/demo11/handle` | `Demo11TimeTaskAssistantController` | Agentic 模式（模型自主） | [04](/04-agentic-patterns) |
| 11 显式编排 | `GET /api/demo11/invoke` | 同上 | Orchestrated 模式（程序指定） | [04](/04-agentic-patterns) |

插件本体在 `plugin/` 目录，四个类的能力边界：

| 插件 | 方法 | 有无状态 |
|:---|:---|:---|
| [MathPlugin](../src/main/java/com/xb/semantickernel/plugin/MathPlugin.java) | `add` / `subtract` / `multiply` / `divide` | 无（纯函数） |
| [TimePlugin](../src/main/java/com/xb/semantickernel/plugin/TimePlugin.java) | `getCurrentTime` | 无 |
| [TodoListPlugin](../src/main/java/com/xb/semantickernel/plugin/TodoListPlugin.java) | 增 / 删 / 查 | **有**（内部集合） |
| [TimeTaskPlugin](../src/main/java/com/xb/semantickernel/plugin/TimeTaskPlugin.java) | `scheduleReminder` | **有**（提醒列表） |

> 有状态插件是理解"Agent 会记住事情"的起点，也是坑的来源：`TodoListPlugin` 是单例 Bean，所有会话共享同一份待办。生产上应把状态外置到会话或数据库。

### 3.3 RAG 层（demo12）

| 端点 | 作用 |
|:---|:---|
| `POST /api/demo12/index/rebuild` | 重建索引（先清空再写入，幂等） |
| `POST /api/demo12/ask` | 检索 + 拼上下文 + 生成 |
| `GET /api/demo12/index/status` | 查看当前索引文档数 |

代码分两层：

- [RagService](../src/main/java/com/xb/semantickernel/rag/RagService.java) —— 编排层：`indexDocuments` → `retrieve` → `buildContext`
- [InMemoryVectorStore](../src/main/java/com/xb/semantickernel/rag/InMemoryVectorStore.java) —— 检索内核：余弦相似度 + TopK

`InMemoryVectorStore` 只有 100 行，但它把 RAG 最容易被神秘化的部分讲清楚了：

```java
// 维度不一致直接抛异常，而不是静默算出一个错数
if (a.size() != b.size()) {
    throw new IllegalArgumentException(
            "向量维度不一致: " + a.size() + " vs " + b.size()
                    + "（通常是索引与查询用了不同的 embedding 模型）");
}
```

异常信息里直接写出最常见的病因 —— 这是"可运维的错误信息"的写法示范。

### 3.4 支撑代码

| 类 | 职责 |
|:---|:---|
| [SemanticKernelConfig](../src/main/java/com/xb/semantickernel/config/SemanticKernelConfig.java) | 全部 Bean 装配：客户端 → 服务 → Kernel → 插件 |
| [SemanticKernelProperties](../src/main/java/com/xb/semantickernel/config/SemanticKernelProperties.java) | `endpoint` / `apiKey` / `model` / `embeddingModel` 配置绑定 |
| [ChatHelper](../src/main/java/com/xb/semantickernel/util/ChatHelper.java) | `lastAssistantText` 倒序取最后一条非空回复 |
| [GlobalExceptionHandler](../src/main/java/com/xb/semantickernel/common/GlobalExceptionHandler.java) | 统一异常响应 |

`ChatHelper.lastAssistantText` 看似琐碎，但它是必要的：SK 返回的是**消息列表**（可能包含 TOOL 角色的中间消息），不是单个字符串。直接取 `results.get(0)` 会拿到错误内容。

## 4. Kernel 装配的关键技巧

[SemanticKernelConfig#kernel](../src/main/java/com/xb/semantickernel/config/SemanticKernelConfig.java) 的方法签名值得单独讲：

```java
@Bean
public Kernel kernel(ChatCompletionService chatCompletionService, List<KernelPlugin> plugins) {
    Kernel.Builder builder = Kernel.builder()
            .withAIService(ChatCompletionService.class, chatCompletionService);
    for (KernelPlugin plugin : plugins) {   // Spring 注入容器中所有 KernelPlugin
        builder.withPlugin(plugin);
    }
    return builder.build();
}
```

`List<KernelPlugin> plugins` 是 Spring 的**集合注入**：容器中有几个 `KernelPlugin` Bean，这里就收到几个。

效果是：新增插件只需加一个 `@Bean` 方法，`kernel()` 一行不用改。这是本项目唯一一处"框架级"设计，也是把 SK 接入 Spring 时的标准做法。

反例（很多教程这么写）：

```java
// 每加一个插件都要改这里，且忘记改不会有任何编译错误
builder.withPlugin(KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin"));
```

## 5. Agentic 与 Orchestrated：两种模式的代码对照

[Demo11](../src/main/java/com/xb/semantickernel/controller/demo11/Demo11TimeTaskAssistantController.java) 把两种模式放在同一个类里，方便直接对比。

**Agentic（模型自主）**：

```java
InvocationContext ctx = InvocationContext.builder()
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build();
List<ChatMessageContent<?>> results = chatCompletionService
        .getChatMessageContentsAsync(history, kernel, ctx).block();
```

你只给一句话和一堆工具，**调哪个、调几次由模型决定**。

**Orchestrated（程序编排）**：

```java
Object result = kernel.invokeAsync(plugin, function)
        .withArguments(arguments).withResultType(resultClass).block().getResult();
```

`plugin` 和 `function` 都是请求参数里显式传进来的，**模型完全不参与**。

| 维度 | Agentic | Orchestrated |
|:---|:---|:---|
| 谁决定调用 | 模型 | 代码 |
| 可预测性 | 低（同样输入可能不同路径） | 高（确定性） |
| 灵活性 | 高（能处理未预见的组合） | 低（只走写好的路径） |
| 调试难度 | 高（要看中间消息） | 低（一个断点到底） |
| 适用 | 开放域助手、探索性任务 | 关键业务流程、计费/支付 |

> 生产建议：**主流程用 Orchestrated，边缘能力用 Agentic**。把不可预测的部分关进一个可控的盒子里。

`/invoke` 里还有一段值得注意的类型处理：

```java
Integer intVal = tryParseInt(value);
if (intVal != null) builder.withVariable(key, intVal);
else builder.withVariable(key, value);
```

HTTP 参数全是字符串，而 `MathPlugin.add(int, int)` 需要整数。这种"边界上的类型转换"是 Java + AI 集成最常见的脏活，框架不会替你做。

## 6. 教学边界（诚实说明）

这个项目是**教学项目**，以下能力是刻意简化的，不要直接照搬到生产：

| 简化点 | 现状 | 生产做法 |
|:---|:---|:---|
| 会话存储 | `ConcurrentHashMap`，重启丢失、多实例不共享 | Redis / 数据库 + TTL |
| 向量库 | `InMemoryVectorStore`，进程内、无持久化 | Milvus / pgvector / Elasticsearch |
| 阻塞调用 | MVC 里 `.block()` 等响应式结果 | WebFlux 链式组合，或虚拟线程 |
| 重试与超时 | 无 | Resilience4j + 超时预算 |
| 可观测 | 只有日志 | Micrometer + 调用链追踪（Prompt/Token 也要进指标） |
| 鉴权限流 | 无 | 网关层 + 用户级配额 |
| Prompt 管理 | 硬编码在 Controller | 外部化配置 / 版本化 Prompt 仓库 |

`.block()` 那条特别说明：SK 的 `ChatCompletionService` 是响应式 API（返回 `Mono`），在 Spring MVC 的同步 Controller 里必须 `block()`。教学项目图省事，但这会**占满 Tomcat 线程**。如果要做高并发，要么切 WebFlux，要么用虚拟线程（Java 21 已支持）。

## 7. 小结

| 问题 | 答案 |
|:---|:---|
| SK 的核心抽象是什么 | Kernel / Plugin / ChatHistory / ToolCallBehavior |
| 换 Spring AI 要重学吗 | 不用，概念一一对应，只是换名字 |
| 什么时候用 Agentic | 开放域、需要模型临场组合工具 |
| 什么时候用 Orchestrated | 关键流程、需要可预测与可测试 |
| 这个项目的定位 | 理解原理与通用抽象，不追求生产完备 |

## 延伸阅读

- 上一章：[05 · RAG 检索增强](/05-rag-guide)
- 对照组：[spring-ai-teaching-demo](https://ibqy.github.io/spring-ai-teaching-demo/) —— 同一批能力，Spring AI 的写法
- 下一站：[agentscope-java-teaching-demo](https://ibqy.github.io/agentscope-java-teaching-demo/) —— 多 Agent 协作框架
- 完整学习路线：[Java + AI 课程体系](https://ibqy.github.io/curriculum)
