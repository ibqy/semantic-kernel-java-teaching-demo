---
layout: home

hero:
  name: Semantic Kernel Java 教学
  text: 微软 SK Java 版插件式 AI 开发
  tagline: 基于 Semantic Kernel Java 的渐进示例：Kernel 构建 → 插件开发 @DefineKernelFunction → 对话历史记忆 → Agentic 模式
  actions:
    - theme: brand
      text: 开始学习 →
      link: /01-kernel-basics
    - theme: alt
      text: GitHub 源码
      link: https://github.com/ibqy/semantic-kernel-java-teaching-demo

features:
  - icon: ⚙️
    title: Kernel 基础
    details: Kernel 构建、AI 服务注册、首次 Prompt 调用
  - icon: 🧩
    title: 插件开发
    details: "@DefineKernelFunction 注解定义插件，把业务代码交给模型调度"
  - icon: 🔍
    title: 自动函数调用
    details: ToolCallBehavior 配置，模型自主决定调用哪个插件
  - icon: 💬
    title: 对话历史记忆
    details: ChatHistory 管理多轮上下文，控制 Token 消耗
  - icon: 🤖
    title: Agentic 模式
    details: Agentic vs Orchestrated 两种模式对比，TodoListPlugin 实战
  - icon: 📖
    title: 完整 HTML 教程
    details: 图文版教学指南（GitHub 链接），配合文档站对照阅读
---

## 课程结构

| 章节 | 主题 | 关键代码 |
|:---|:---|:---|
| [01 · Kernel 基础](/01-kernel-basics) | Kernel/服务装配、首次对话、流式输出 | `SemanticKernelConfig`、`Demo01`、`Demo02` |
| [02 · 插件开发](/02-plugins-guide) | `@DefineKernelFunction`、ToolCallBehavior | `MathPlugin`、`Demo06`、`Demo07` |
| [03 · 对话历史记忆](/03-chat-history) | ChatHistory、System/User/Assistant 角色、Prompt 模板 | `Demo03`、`Demo04`、`Demo05` |
| [04 · Agentic 模式](/04-agentic-patterns) | 工具自主调用、结构化输出、Agentic vs Orchestrated | `Demo09`、`Demo10`、`Demo11` |
| [05 · RAG 检索增强](/05-rag-guide) | 索引/检索/生成三段式、余弦相似度 TopK | `RagService`、`InMemoryVectorStore`、`Demo12` |
| [06 · 框架选型与代码地图](/06-framework-selection-code-map) | SK vs Spring AI vs LangChain4j、12 demo 全量索引 | 全项目 |

## 代码地图

| 目录 | 关键类 | 职责 |
|:---|:---|:---|
| `config/` | [SemanticKernelConfig](../src/main/java/com/xb/semantickernel/config/SemanticKernelConfig.java) | OpenAI 客户端 → ChatCompletionService → Kernel → 四个插件，全部 Bean 装配 |
| `plugin/` | `MathPlugin`、`TimePlugin`、`TodoListPlugin`、`TimeTaskPlugin` | 被模型调度的工具函数（后两个有状态） |
| `controller/demo01~demo11` | 各 `DemoXXController` | 12 个渐进示例，每个 demo 一个包 |
| `controller/demo12` | `Demo12RagController` | RAG 三端点：索引重建 / 提问 / 索引状态 |
| `rag/` | [RagService](../src/main/java/com/xb/semantickernel/rag/RagService.java)、[InMemoryVectorStore](../src/main/java/com/xb/semantickernel/rag/InMemoryVectorStore.java) | 编排层 + 检索内核（余弦相似度，100 行） |
| `util/` | [ChatHelper](../src/main/java/com/xb/semantickernel/util/ChatHelper.java) | `lastAssistantText` 倒序取最后一条非空回复 |

## 在路线中的位置

**阶段 2 · AI 应用入门 —— 第 3 站**

| 上一站 | 本站 | 下一站 |
|:---|:---|:---|
| [spring-ai-alibaba-teaching-demo](https://ibqy.github.io/spring-ai-alibaba-teaching-demo/) | **Semantic Kernel Java** | [agentscope-java-teaching-demo](https://ibqy.github.io/agentscope-java-teaching-demo/) |

本站与 [spring-ai-teaching-demo](https://ibqy.github.io/spring-ai-teaching-demo/) 是**同題异构对照**：同一批能力（对话、工具调用、记忆、RAG），两种框架的写法。建议连着读，第 6 章给出逐项映射表。

完整学习路线见 [Java + AI 课程体系](https://ibqy.github.io/curriculum)。

## 运行前准备

```bash
# 1. 配置 Azure OpenAI 或兼容端点（见 application.yml）
#    endpoint / api-key / model / embedding-model 四项必填
# 2. 启动
mvn spring-boot:run
# 3. 访问
curl -X POST http://localhost:8080/api/demo01/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"你好"}'
```

> 项目基于 Semantic Kernel Java **1.5.0** + Spring Boot **3.5.16** + Java 21。
> 没有 API Key 也能跑 `InMemoryVectorStore` 的单测（不依赖网络）。


