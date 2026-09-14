package com.xb.semantickernel.util;

import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;

public final class ChatHelper {

    private ChatHelper() {}

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

    public static boolean containsRole(List<ChatMessageContent<?>> results, AuthorRole role) {
        if (results == null) return false;
        return results.stream().anyMatch(m -> m.getAuthorRole() == role);
    }
}