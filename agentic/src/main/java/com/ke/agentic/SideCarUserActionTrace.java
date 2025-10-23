package com.ke.agentic;

import com.google.common.collect.Sets;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.Strings;
import lombok.Getter;

import java.util.Set;

@Service(Service.Level.PROJECT)
public class SideCarUserActionTrace {

    private final Project project;
    /**
     * 记录项目级别的用户操作的文件
     * key: projectId
     * value: 文件列表
     */
    @Getter
    private final Set<String> changedFiles = Sets.newHashSet();

    private String sessionId = "";

    public SideCarUserActionTrace(Project project) {
        this.project = project;
    }

    /**
     * 开启用户操作监控，清空所有之前用户已经修改的文件
     */
    public void startUserActionMonitor(String sessionId) {
        // 项目初始化时，一直到首次被agent使用，清空非agent阶段所有的文件修改
        if (Strings.isEmpty(sessionId)) {
            this.sessionId = sessionId;
            this.changedFiles.clear();
            return;
        }
        // 代表这次是在一个session内的监听
        if (this.sessionId.equals(sessionId)) {
            return;
        } else {
            // 已经请开启了新的session
            this.changedFiles.clear();
        }
        this.sessionId = sessionId;
    }

    public void addChangedFile(String filePath) {
        if (Strings.isEmpty(filePath)) {
            return;
        }
        this.changedFiles.add(filePath);
    }

}
