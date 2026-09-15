import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'zh-CN',
  title: 'Semantic Kernel Java 教学',
  description: 'Microsoft Semantic Kernel for Java 渐进式教学：Kernel 构建、插件开发、对话管理与 Agentic 模式',
  base: '/semantic-kernel-java-teaching-demo/',
  lastUpdated: true,
  markdown: {
    config(md) {
      const defaultLink =
        md.renderer.rules.link_open ||
        ((tokens, idx, options, _env, self) => self.renderToken(tokens, idx, options))
      md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
        const href = tokens[idx].attrGet('href')
        if (href && href.startsWith('../')) {
          const rel = href.replace(/^(\.\.\/)+/, '')
          const kind = /\.[A-Za-z]+$/.test(rel) ? 'blob' : 'tree'
          tokens[idx].attrSet('href', `https://github.com/ibqy/semantic-kernel-java-teaching-demo/${kind}/main/${rel}`)
        }
        return defaultLink(tokens, idx, options, env, self)
      }
    }
  },
  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: 'GitHub', link: 'https://github.com/ibqy/semantic-kernel-java-teaching-demo' }
    ],
    sidebar: [
      {
        text: '教学文档',
        items: [
          { text: '01 · Kernel 基础', link: '/01-kernel-basics' },
          { text: '02 · 插件开发', link: '/02-plugins-guide' },
          { text: '03 · 对话历史记忆', link: '/03-chat-history' },
          { text: '04 · Agentic 模式', link: '/04-agentic-patterns' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/ibqy/semantic-kernel-java-teaching-demo' }
    ],
    search: { provider: 'local' },
    outline: { level: [2, 3], label: '本页目录' },
    docFooter: { prev: '上一篇', next: '下一篇' },
    lastUpdated: { text: '最后更新于' },
    darkModeSwitchLabel: '外观',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式',
    sidebarMenuLabel: '文档',
    returnToTopLabel: '回到顶部',
    footer: {
      message: '个人教学项目 · 代码可跑 · 注释记录设计取舍',
      copyright: 'Copyright © 2026 ibqy'
    }
  }
})
