package com.ke.webview.topic;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/6/8 16:11
 * @Version 1.0
 * @Description
 */
public interface RefreshNotifier {

    Topic<RefreshNotifier> REFRESH_ACTION_TOPIC =
            Topic.create("CodeLink toolWindow refresh", RefreshNotifier.class);

    void refresh(Project project);

}
