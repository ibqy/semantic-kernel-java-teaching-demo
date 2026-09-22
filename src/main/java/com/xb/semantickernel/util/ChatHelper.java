package com.xb.semantickernel.util;

/**
 * ChatHelper - 聊天消息工具类
 *
 * 封装了从 Semantic Kernel 返回的消息列表中提取助手回复、
 * 判断角色等常用操作。各 Demo Controller 复用它来
 * 避免重复的"取最后一条助手消息"逻辑。
 *
 * @author ibqy
 */
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;

public final class ChatHelper {

    private ChatHelper() {}

    /**
     * 从消息列表中倒序提取最后一条非空的助手回复文本
     *
     * 倒序遍历是因为最新回复通常位于列表末尾。
     *
     * @param results 模型返回的消息内容列表
     * @return 最后一条助手回复文本，若为空则返回提示信息
     */
    public static String lastAssistantText(List<ChatMessageContent<?>> results) {
        if (results == null || results.isEmpty()) {
            return "（无返回内容）";
        }
        for (int i = results.size() - 1; i >= 0; i--) {
            String content = results.get(i).getContent();
            if (content != null && !content.isBlank()) {
                return content;
            }
        }
        return "（返回内容为空）";
    }

    /**
     * 判断消息列表中是否包含指定角色的消息
     *
     * @param results 消息内容列表
     * @param role    要匹配的角色（如 ASSISTANT、TOOL）
     * @return 包含则返回 true
     */
    public static boolean containsRole(List<ChatMessageContent<?>> results, AuthorRole role) {
        if (results == null) return false;
        return results.stream().anyMatch(m -> m.getAuthorRole() == role);
    }
}