package com.ke.toolwindow.content;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ToolWindow;
import com.ke.utils.ComponentUtil;
import com.ke.utils.RuntimeEnvUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebViewProjectConfig;
import com.ke.webview.communication.handler.KeCopilotPanelHandler;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.communication.handler.ptw.InlineChatPTWHandler;
import com.ke.webview.communication.handler.ptw.SelectCodeContextPTWHandler;
import com.ke.webview.communication.handler.wtp.*;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeCopilotWindowPanel extends BaseH5Panel implements Disposable, ToolWindowContentPanel {

    private final Map<String, BaseWTPHandler> baseWTPHandlerMap = new HashMap<>();

    private static final Logger LOGGER = Logger.getInstance(KeCopilotWindowPanel.class);


    private final Map<String, BasePTWHandler> basePTWHandlerMap = new HashMap<>();

    public KeCopilotWindowPanel(Project project) {
        super(project);
        Map<String, BasePTWHandler> ptwHandlerMap = Map.ofEntries(
                Map.entry(BaseCommandEnums.SELECT_CODE_CONTEXT.getCommand(), new SelectCodeContextPTWHandler(this)),
                Map.entry(BaseCommandEnums.INLINE_CHAT.getCommand(), new InlineChatPTWHandler(this))
        );

        Map<String, BaseWTPHandler> wtpHandlerMap = Map.ofEntries(
                Map.entry(BaseCommandEnums.DE_SELECT_CODE_CONTEXT.getCommand(), new DeselectCodeContextWTPHandler(project)),
                Map.entry(BaseCommandEnums.CHAT_INIT_FINISH.getCommand(), new ChatInitFinishWTPHandler(project)),
                Map.entry(BaseCommandEnums.INSERT_CODE.getCommand(), new InsertCodeWTPHandler(project)),
                Map.entry(BaseCommandEnums.ZOOM_IN_CODE_BLOCK.getCommand(), new ZoomInCodeBlockWTPHandler(project)),
                Map.entry(BaseCommandEnums.PIC_PREVIEW.getCommand(), new PicPreviewHandler(project)),
                Map.entry(BaseCommandEnums.FILE_LIST_CONTENT.getCommand(), new GetFileListContentWTPHandler(project)),
                Map.entry(BaseCommandEnums.OPEN_FILE_AND_SELECT.getCommand(), new OpenFileAndSelectWTPHandler(project)),
                Map.entry(BaseCommandEnums.GET_FILE_CONTENT.getCommand(), new GetFileContentWTPHandler(project))
        );

        KeCopilotPanelHandler.EP_NAME.getExtensionList().forEach(handler -> {
            if (MapUtils.isNotEmpty(handler.getPTWHandler(this, project))) {
                basePTWHandlerMap.putAll(handler.getPTWHandler(this, project));
            }

            if (MapUtils.isNotEmpty(handler.getWTPHandler(this, project))) {
                baseWTPHandlerMap.putAll(handler.getWTPHandler(this, project));
            }
        });
        basePTWHandlerMap.putAll(ptwHandlerMap);
        baseWTPHandlerMap.putAll(wtpHandlerMap);
    }


    @Override
    @NotNull
    public String getPath() {
        return "";
    }

    @Override
    public Map<String, BaseWTPHandler> getChildWTPHandlerMap() {
        return baseWTPHandlerMap;
    }

    @Override
    public Map<String, BasePTWHandler> getChildPTWHandlerMap() {
        return basePTWHandlerMap;
    }

    @Override
    public String getErrorH5Path() {
        return "/html/error.html";
    }

    @Override
    protected boolean needBorder() {
        return SystemInfo.isMac;
    }

    @Override
    protected void resizePanel() {
        ToolWindow codeLinkToolWindow = ComponentUtil.getCodeLinkToolWindow(getProject());

        //parent instanceof InternalDecoratorImpl
        Container parent = codeLinkToolWindow.getComponent().getParent();
        if (Objects.nonNull(parent)) {
            parent.setSize(new Dimension(parent.getWidth() - 50, parent.getHeight()));
            parent.validate();
        }

    }

    @Override
    public void doAfterWebviewInitFinish() {
        WebViewProjectConfig webViewProjectConfig = project.getService(WebViewManager.class).getWebViewProjectConfig();
        webViewProjectConfig.setIsMavenProject(RuntimeEnvUtil.isMavenProject(project));
        webViewProjectConfig.setJdkVersion(RuntimeEnvUtil.getJDKVersion(project));
    }

}
