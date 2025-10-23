package com.ke.agentic;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.SideCarIDEInfo;
import com.ke.webview.WebViewManager;
import com.ke.webview.util.PTWUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 16:32
 * @Version 1.0
 * @Description IDE最新选中的文件同步至chat
 */
public class AgenticEditorFocusFileListener implements FileEditorManagerListener {

    private final Project project;

    private final WebViewManager webViewManager;

    public AgenticEditorFocusFileListener(Project project) {
        this.project = project;
        webViewManager = project.getService(WebViewManager.class);
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if (Objects.nonNull(event.getNewFile())) {
            updateSideCarIDEInfo();
        }
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        updateSideCarIDEInfo();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        updateSideCarIDEInfo();
    }


    private void updateSideCarIDEInfo() {
        SideCarIDEInfo sideCarIDEInfo = webViewManager.getWebViewProjectConfig().getSideCarIDEInfo();
        if (Objects.nonNull(sideCarIDEInfo)) {
            SideCarAgentUtil.updateSideCarIDEInfo(project, sideCarIDEInfo);
            PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewManager.getWebViewProjectConfig(), project);
        }
    }
}
