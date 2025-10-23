package com.ke.service.notify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.notification.impl.NotificationsToolWindowNotificationListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.BalloonLayoutData;
import com.ke.Bundle;
import com.ke.exception.BusinessException;
import com.ke.utils.ComponentUtil;
import com.ke.utils.FileUtil;
import com.ke.utils.WindowUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.List;
import java.util.Objects;


/**
 * @Author: zhangshaoxun001
 * @Date: 2022/12/6 17:48
 * @Version 1.0
 * @Description
 */
public class NotifyServiceImpl {

    private final Project project;


    private static final Logger LOGGER = Logger.getInstance(NotifyServiceImpl.class);

    public NotifyServiceImpl(Project project) {
        this.project = project;
    }


    /**
     * 通知
     */
    public void notify(NotificationType notificationType, String content) {
        notify(project, CodeLinkNotifications.createNotification(content, notificationType));
    }


    /**
     * 通知
     */
    public void notify(NotificationType notificationType, String title, String content) {
        notify(project, CodeLinkNotifications.createNotification(title, content, notificationType));
    }

    /**
     * 通知错误
     */
    public void error(String content) {
        notify(NotificationType.ERROR, content);
    }

    /**
     * 通知错误
     */
    public void error(String title, String content) {
        notify(NotificationType.ERROR, title, content);
    }


    /**
     * 通知businessException
     */
    public void notifyException(Exception exception) {
        if (exception instanceof BusinessException) {
            notify(NotificationType.ERROR, exception.getMessage());
        } else {
            notify(NotificationType.ERROR, ExceptionUtils.getStackTrace(exception));
        }
    }

    /**
     * 通知信息
     */
    public void info(String content) {
        notify(NotificationType.INFORMATION, content);
    }

    /**
     * 通知信息
     */
    public void info(String title, String content) {
        notify(NotificationType.INFORMATION, title, content);
    }

    /**
     * 通知警告信息
     */
    public void warn(String content) {
        notify(NotificationType.WARNING, content);
    }

    /**
     * 通知警告信息
     */
    public void warn(String title, String content) {
        notify(NotificationType.WARNING, title, content);
    }


    public void createLinkNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type) {
        notify(project, CodeLinkNotifications.createNotification(title, content, type));
    }


    public void createFileLinkNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type) {

        Notification notification = CodeLinkNotifications.createNotification(title, content, type)
                .setListener(new NotificationListener.Adapter() {
                    @Override
                    protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                        String path = e.getDescription();
                        FileUtil.openFile(project, path);
                    }
                });

        notify(project, notification);
    }


    public void createFullContentNotification(@NotNull String content, @NotNull NotificationType type, @Nullable AnAction action) {
        createFullContentNotification("", content, type, action);
    }


    /**
     * 创建带有完整内容的通知
     *
     * @param title   通知标题
     * @param content 通知内容
     * @param type    通知类型
     * @param action  通知动作
     */
    public void createFullContentNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type, @Nullable AnAction action) {


        Notification notification = CodeLinkNotifications.createFullContentNotification(title, content, type, false);

        if (Objects.nonNull(action)) {
            notification.addAction(action);
        }
        notify(project, notification);
    }


    /**
     * 创建带有完整内容的通知
     */
    public void createFullContentNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type, @Nullable List<AnAction> actions, boolean expireOnLinkClick) {

        Notification notification = CodeLinkNotifications.createFullContentNotification(title, content, type, expireOnLinkClick);

        if (CollectionUtils.isNotEmpty(actions)) {
            actions.forEach(notification::addAction);
        }
        notify(project, notification);
    }


    /**
     * 当不在默认显示器上时，如果webview不支持OSR，通知消息会被webview覆盖，所以特殊处理一下
     */
    public static void notify(@NotNull Project project, @NotNull Notification notification) {
        if (Objects.isNull(notification.getListener())) {
            notification.setListener(new NotificationListener.UrlOpeningListener(true));
        }
        ToolWindow toolWindow = ComponentUtil.getToolWindow(project, Bundle.get("toolWindow.CodeLink.id"));
        GraphicsDevice currentDevice = WindowUtil.getCurrentDevice(project);
        if (Objects.nonNull(toolWindow)
                && toolWindow.isVisible()
                && (toolWindow.getAnchor().equals(ToolWindowAnchor.RIGHT) || toolWindow.getAnchor().equals(ToolWindowAnchor.BOTTOM))
                && Objects.nonNull(currentDevice)
                && !currentDevice.equals(WindowUtil.getDefaultGraphicsDevice())) {

            try {
                Balloon balloon = NotificationsManagerImpl.createBalloon(WindowManager.getInstance().getIdeFrame(project), notification, true, false, BalloonLayoutData.fullContent(), project);
                if (balloon instanceof BalloonImpl) {
                    NotificationsManagerImpl.frameActivateBalloonListener(balloon, () -> {
                        if (!balloon.isDisposed()) {
                            int delay = 1000;
                            ((BalloonImpl) balloon).startSmartFadeoutTimer(delay);
                        }
                    });

                }

                balloon.showInCenterOf(WindowManager.getInstance().getFrame(project).getRootPane());
                new NotificationsToolWindowNotificationListener(project).notify(notification);
                return;
            } catch (Exception e) {
                LOGGER.warn("notify error", e);
            }
        }

        notification.notify(project);


    }
}
