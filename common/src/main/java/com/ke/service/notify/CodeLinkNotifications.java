package com.ke.service.notify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/12/23 16:58
 * @Description
 */
public final class CodeLinkNotifications {

    public static final String GROUP_ID = "CodeLink Notification Group";


    public static Notification createNotification(String content, NotificationType notificationType) {
        return createNotification(content, notificationType, false);
    }

    public static Notification createNotification(String content, NotificationType notificationType, boolean fullContent) {
        if (fullContent) {
            return createFullContentNotification("", content, notificationType, false);
        }
        return NotificationGroupManager.getInstance()
                .getNotificationGroup(GROUP_ID)
                .createNotification(content, notificationType);
    }


    public static Notification createNotification(String title, String content, NotificationType notificationType) {
        return createNotification(title, content, notificationType, false);
    }

    public static Notification createNotification(String title, String content, NotificationType notificationType, boolean fullContent) {
        if (fullContent) {
            return createFullContentNotification(title, content, notificationType, false);
        }
        return NotificationGroupManager.getInstance()
                .getNotificationGroup(GROUP_ID)
                .createNotification(content, notificationType);
    }


    public static Notification createFullContentNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type, boolean expireOnLinkClick) {
        FullContent notification = new FullContent(GROUP_ID, title, content, type);
        notification.setListener(new NotificationListener.UrlOpeningListener(expireOnLinkClick));
        return notification;
    }

    private static class FullContent extends Notification implements NotificationFullContent {
        public FullContent(@NotNull String groupId, @NotNull @NlsContexts.NotificationTitle String title, @NotNull @NlsContexts.NotificationContent String content, @NotNull NotificationType type) {
            super(groupId, title, content, type);
        }
    }
}
