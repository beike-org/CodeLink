package com.ke.agentic;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import org.jetbrains.annotations.NotNull;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/19 17:40
 * @Description
 */
public class AgenticProjectFileListener implements VirtualFileListener {

    private final Project project;

    public AgenticProjectFileListener(Project project) {
        this.project = project;
    }

    /**
     * 当虚拟文件的内容发生更改时触发。
     *
     * @param event 包含更改信息的事件对象。
     */
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        VirtualFile file = event.getFile();
        Project project = getProjectForFile(file);
        if (project != null) {
            // 将变化的进行存储，用于sidecar user action监听
            // 在启动监听时，自动将文件列表清空
            project.getService(SideCarUserActionTrace.class).addChangedFile(file.getPath());
        }
    }

    private Project getProjectForFile(VirtualFile file) {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            if (project.getBasePath() != null && file.getPath().startsWith(project.getBasePath())) {
                return project;
            }
        }
        return null;
    }
}
