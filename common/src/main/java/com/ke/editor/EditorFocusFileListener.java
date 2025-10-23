package com.ke.editor;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ke.webview.BaseCommandEnums;
import com.ke.utils.FileUtil;
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
public class EditorFocusFileListener implements FileEditorManagerListener {

    private final Project project;

    private final WebViewManager webViewManager;

    public EditorFocusFileListener(Project project) {
        this.project = project;
        webViewManager = project.getService(WebViewManager.class);
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if (Objects.nonNull(event.getNewFile())) {
            webViewManager.getWebViewProjectConfig().setWorkspaceFileType(FileUtil.findBestLanguage(event.getNewFile()));
            PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewManager.getWebViewProjectConfig(), project);
        }
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewManager.getWebViewProjectConfig(), project);
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewManager.getWebViewProjectConfig(), project);
    }

}
