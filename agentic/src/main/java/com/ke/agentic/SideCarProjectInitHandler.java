package com.ke.agentic;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.jcef.JBCefApp;
import com.ke.StartupActivityHandler;
import com.ke.agentic.topic.SideCarNotifier;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.SideCarIDEInfo;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebViewProjectConfig;
import com.ke.webview.topic.LoadNotifier;
import com.ke.webview.util.PTWUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 16:04
 * @Version 1.0
 * @Description
 */
public class SideCarProjectInitHandler implements StartupActivityHandler {

    @Override
    public void init(Project project) {
        // 支持webview
        if (JBCefApp.isSupported()) {

            if (SideCarAgentManager.getInstance().isAgentReady()){
                notifyPanelLoaded(project);
            }
        }

    }

    @Override
    public void beforeWebViewInit(Project project) {
        VirtualFileManager.getInstance().addAsyncFileListener(new AsyncFileListener() {

            private final AgenticProjectFileListener agenticProjectFileListener = new AgenticProjectFileListener(project);

            @NotNull
            @Override
            public ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> list) {
                return new ChangeApplier() {
                    @Override
                    public void afterVfsChange() {
                        list.forEach(event -> {
                            if (event instanceof VFileContentChangeEvent vFileContentChangeEvent) {
                                VirtualFile file = vFileContentChangeEvent.getFile();
                                agenticProjectFileListener.contentsChanged(new VirtualFileEvent(
                                        event.getRequestor(),
                                        file,
                                        file.getParent(),
                                        vFileContentChangeEvent.getOldModificationStamp(),
                                        vFileContentChangeEvent.getModificationStamp()
                                ));
                            }
                        });
                    }
                };
            }
        }, project);
        WebViewManager webViewManager = project.getService(WebViewManager.class);
        WebViewProjectConfig webViewProjectConfig = webViewManager.getWebViewProjectConfig();
        webViewProjectConfig.setSideCarIDEInfo(SideCarAgentUtil.generateSideCarIDEInfo(project));
        project.getMessageBus().connect(project).subscribe(SideCarNotifier.SIDE_CAR_NOTIFIER_TOPIC, new SideCarNotifier() {
            @Override
            public void startSocket(Integer socketPort) {
                SideCarIDEInfo sideCarIDEInfo = webViewProjectConfig.getSideCarIDEInfo();
                if (Objects.isNull(sideCarIDEInfo)) {
                    sideCarIDEInfo = SideCarAgentUtil.generateSideCarIDEInfo(project);
                }

                if (!socketPort.equals(sideCarIDEInfo.getSocketPort())) {
                    // 端口不一致，重新设置
                    sideCarIDEInfo.setSocketPort(socketPort);
                    sideCarIDEInfo.setEditorUrl("http://127.0.0.1:" + socketPort + "/");
                    webViewProjectConfig.setSideCarIDEInfo(sideCarIDEInfo);
                    PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewProjectConfig, project);
                }
            }


            /**
             * Agent通过健康检查
             */
            @Override
            public void agentReady() {
                notifyPanelLoaded(project);
            }
        });
    }


    public void notifyPanelLoaded(Project project) {
        WebViewProjectConfig webViewProjectConfig = project.getService(WebViewManager.class).getWebViewProjectConfig();
        if (SideCarAgentManager.getInstance().updateSideCarIDEInfo(webViewProjectConfig.getSideCarIDEInfo())) {
            PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewProjectConfig, project);
        }

        project.getMessageBus().syncPublisher(LoadNotifier.LOAD_TOPIC).startLoad(project);
        project.getService(WebViewManager.class).setCanLoaded(true);
    }
}
