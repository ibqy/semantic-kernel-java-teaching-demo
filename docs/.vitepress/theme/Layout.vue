<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #home-features-before>
      <section class="sig">
        <div class="sig-card">
          <span class="sig-label">KERNEL · PLUGIN ARCHITECTURE</span>
          <h3 class="sig-title">Kernel 是中枢，插件是能力</h3>
          <div class="sk-orbit">
            <div class="sk-ring"></div>
            <div class="sk-core">Kernel<em>hub</em></div>
            <span class="sk-sat sk-s1">AI 服务</span>
            <span class="sk-sat sk-s2">@DefineKernelFunction 插件</span>
            <span class="sk-sat sk-s3">ChatHistory 记忆</span>
            <span class="sk-sat sk-s4">自动函数调用</span>
          </div>
          <div class="sg-chips">
            <span class="sg-chip">Agentic 模式</span>
            <span class="sg-chip">TodoListPlugin 实战</span>
            <span class="sg-chip">微软 SK · Java 版</span>
          </div>
          <p class="sig-note">SK 的思路：把业务能力封装成插件挂到 Kernel 上，模型自己决定调用哪个——从 Kernel 基础到 Agentic 模式，四篇文档把这条链路走完。</p>
        </div>
      </section>
    </template>
    <template #home-features-after>
      <div class="sig-more"><a href="https://ibqy.github.io/" target="_blank" rel="noopener">← ibqy.github.io · 探索全部项目</a></div>
    </template>
  </Layout>
</template>
