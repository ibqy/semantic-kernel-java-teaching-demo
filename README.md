# Semantic Kernel for Java 教学演示

基于 [Microsoft Semantic Kernel for Java](https://github.com/microsoft/semantic-kernel-java) 构建的教学演示项目，通过 11 个渐进式 Demo 帮助初学者掌握 Semantic Kernel 的核心概念，涵盖 Kernel 构建、插件开发、对话管理、工具调用和 Agentic 模式。

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.4.4 |
| Semantic Kernel for Java | 1.5.0 |
| Java | 21 |
| AI 服务 | OpenAI ChatCompletion（兼容 DashScope/本地模型） |

## Demo 一览

| 编号 | 名称 | 核心知识点 | API 端点 |
|------|------|-----------|----------|
| Demo01 | 基础对话 | Kernel 构建、ChatCompletionService、ChatHistory | `POST /api/demo01/chat` |
| Demo02 | 流式对话 | `getStreamingChatMessageContentsAsync`、Flux 事件流 | `GET /api/demo02/chat` |
| Demo03 | 对话历史记忆 | ChatHistory 多轮对话、session 会话管理 | `POST /api/demo03/chat` |
| Demo04 | 系统角色 | `addSystemMessage` 设定 Agent 人格 | `POST /api/demo04/chat` |
| Demo05 | Prompt 模板 | 动态 System Prompt、参数化构造 | `POST /api/demo05/chat` |
| Demo06 | 自定义插件 | `@DefineKernelFunction` 注解、`KernelPluginFactory`、`invokeAsync` | `GET /api/demo06/calc` |
| Demo07 | 工具调用 | `ToolCallBehavior.allowAllKernelFunctions`、模型自主选择工具 | `POST /api/demo07/chat` |
| Demo08 | 角色+工具 | 系统角色 + 工具调用联合使用 | `POST /api/demo08/chat` |
| Demo09 | JSON 结构化输出 | Prompt 约束 JSON、Jackson 反序列化 | `POST /api/demo09/country` |
| Demo10 | 待办助手 | TodoListPlugin + ChatHistory 多轮会话 | `POST /api/demo10/chat` |
| Demo11 | 定时任务助手 | Agentic / Orchestrated 两种模式对比、KernelArguments | `POST /api/demo11/handle` + `GET /api/demo11/invoke` |

## 快速启动

### 前置条件

- JDK 21+
- Maven 3.8+
- OpenAI 兼容 API Key 和 Endpoint

### 环境变量

```bash
# Windows PowerShell
$env:OPENAI_API_KEY="your-api-key-here"
$env:SEMANTIC_KERNEL_MODEL="qwen-plus"          # 模型名
$env:SEMANTIC_KERNEL_ENDPOINT="https://dashscope.aliyuncs.com/compatible-mode/v1"

# Linux / macOS
export OPENAI_API_KEY="your-api-key-here"
export SEMANTIC_KERNEL_MODEL="qwen-plus"
export SEMANTIC_KERNEL_ENDPOINT="https://dashscope.aliyuncs.com/compatible-mode/v1"
```

### 运行

```bash
git clone <repo-url>
cd semantic-kernel-java-teaching-demo
mvn spring-boot:run
```

启动后访问 `http://localhost:8080/api/demo01/chat`，POST body `{"message":"你好"}` 验证。

## 教学路径

建议按以下顺序学习：

```
Demo01 基础对话        →  Kernel / ChatCompletionService / ChatHistory
    ↓
Demo02 流式对话        →  Flux 流式事件
    ↓
Demo03 对话历史记忆     →  ChatHistory 多轮 + session 隔离
    ↓
Demo04 系统角色        →  addSystemMessage 人格注入
    ↓
Demo05 Prompt 模板     →  参数化动态构造 System Prompt
    ↓
Demo06 自定义插件       →  @DefineKernelFunction / invokeAsync
    ↓
Demo07 工具调用        →  ToolCallBehavior 模型自主调度
    ↓
Demo08 角色+工具       →  System Role + Tool 组合使用
    ↓
Demo09 JSON 输出       →  Prompt 约束 + Jackson 解析
    ↓
Demo10 待办助手        →  TodoListPlugin 多轮实战
    ↓
Demo11 定时任务助手     →  Agentic vs Orchestrated 双模式
```

## 项目结构

```
semantic-kernel-java-teaching-demo/
├── pom.xml                                          # Maven 配置，Semantic Kernel 1.5.0
├── README.md
├── docs/
│   ├── semantic-kernel-java-teaching-guide.html      # 交互式教学导航页
│   ├── 01-kernel-basics.md                           # Kernel基础：ChatCompletion基本对话
│   ├── 02-plugins-guide.md                           # 插件开发：@DefineKernelFunction
│   ├── 03-chat-history.md                            # 对话历史管理
│   └── 04-agentic-patterns.md                        # Agentic vs Orchestrated 模式
└── src/main/
    ├── java/com/xb/semantickernel/
    │   ├── SemanticKernelTeachingDemoApplication.java  # 启动类
    │   ├── config/
    │   │   ├── SemanticKernelConfig.java               # Kernel/ChatCompletion/Plugin Bean
    │   │   └── SemanticKernelProperties.java            # 配置属性绑定
    │   ├── plugin/
    │   │   ├── MathPlugin.java          # 四则运算插件
    │   │   ├── TimePlugin.java          # 当前时间插件
    │   │   ├── TodoListPlugin.java      # 待办管理插件
    │   │   └── TimeTaskPlugin.java      # 定时提醒插件
    │   ├── util/
    │   │   └── ChatHelper.java          # 对话工具类
    │   └── controller/
    │       ├── demo01/  Demo01BasicChatController.java
    │       ├── demo02/  Demo02StreamingChatController.java
    │       ├── demo03/  Demo03ChatHistoryMemoryController.java
    │       ├── demo04/  Demo04SystemRoleController.java
    │       ├── demo05/  Demo05PromptTemplateController.java
    │       ├── demo06/  Demo06DefineKernelFunctionController.java
    │       ├── demo07/  Demo07ToolCallingController.java
    │       ├── demo08/  Demo08SystemRolePlusToolController.java
    │       ├── demo09/  Demo09JsonOutputController.java / CountryInfo.java
    │       ├── demo10/  Demo10TodoAssistantController.java
    │       └── demo11/  Demo11TimeTaskAssistantController.java
    └── resources/
        └── application.yml              # 配置：端口/API Key/模型/Endpoint
```