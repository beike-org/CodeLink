package com.ke.toolwindow.content;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.util.ui.UIUtil;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.webview.WebviewCommand;
import com.ke.webview.communication.handler.ptw.SyncPluginConfigPTWHandler;
import com.ke.webview.communication.handler.ptw.SyncProjectConfigPTWHandler;
import com.ke.service.notify.NotifyServiceImpl;
import com.ke.setting.configuration.genral.user.bean.PluginConfigVO;
import com.ke.utils.*;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.WebViewManager;
import com.ke.webview.communication.handler.ptw.BasePTWHandler;
import com.ke.webview.communication.handler.wtp.*;
import com.ke.webview.dto.WPCommunicateDTO;
import com.ke.webview.topic.PTWCommandNotifier;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 嵌入H5页面的基础面板
 */
public abstract class BaseH5Panel extends SimpleToolWindowPanel implements Disposable, ToolWindowContentPanel {

    private final static Logger logger = Logger.getInstance(BaseH5Panel.class);

    //project
    @Getter
    protected final Project project;

    //ide内置浏览器
    @Getter
    protected JBCefBrowser jbCefBrowser;

    //webview发送给插件的消息处理器
    private final Map<String, BaseWTPHandler> baseWTPHandlerMap;

    //插件发送给webview的消息处理器
    private final Map<String, BasePTWHandler> basePTWHandlerMap;


    //不在主屏幕上打开，是否重置大小了
    private final AtomicBoolean resized = new AtomicBoolean(false);

    //h5能接受插件消息的信号
    private final Semaphore ptwSemaphore = new Semaphore(0);

    //等待加载完成后需要发送的消息
    private final List<WPCommunicateDTO> waitSendMsg = Collections.synchronizedList(new ArrayList<>());

    //webview的入口页
    private static final Path ENTRY_PAGE = PluginUtil.getPluginBasePath().resolve("webview/dist/index.html");

    //默认的webview地址
    private static final String DEFAULT_URL = ENTRY_PAGE.toUri().toString();

    public BaseH5Panel(Project project) {
        super(true);
        this.project = project;
        this.baseWTPHandlerMap = Map.of(
                BaseCommandEnums.SET_PLUGIN_CONFIG.getCommand(), new SetPluginConfigWTPHandler(),
                BaseCommandEnums.SET_PROJECT_CONFIG.getCommand(), new SetProjectConfigWTPHandler(project),
                BaseCommandEnums.OPEN_BROWSER.getCommand(), new OpenBrowserWTPHandler(),
                BaseCommandEnums.INIT_FINISH.getCommand(), new InitFinishWTPHandler(this, project),
                BaseCommandEnums.REFRESH.getCommand(), new RefreshWTPHandler(this, project),
                BaseCommandEnums.ALERT.getCommand(), new AlertWTPHandler(project)
        );

        basePTWHandlerMap = Map.of(
                BaseCommandEnums.SYNC_PLUGIN_CONFIG.getCommand(), new SyncPluginConfigPTWHandler(this),
                BaseCommandEnums.SYNC_PROJECT_CONFIG.getCommand(), new SyncProjectConfigPTWHandler(this)
        );

        initialize(getUrl());

        //订阅插件发送到webview的topic
        project.getMessageBus().connect(this).subscribe(PTWCommandNotifier.PTW_COMMAND_TOPIC, (PTWCommandNotifier) this::handlePTWMessage);

    }


    private void initialize(String url) {

        //支持右键点击弹出菜单的模式
        jbCefBrowser = JBCefBrowser.createBuilder()
                .setOffScreenRendering(useOffScreenRendering())
                .setClient(JBCefApp.getInstance().createClient())
                .build();


        JBCefJSQuery jsQuery = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);

        //添加通信处理器
        jsQuery.addHandler(s -> {

            try {
                WPCommunicateDTO object = JSONObject.parseObject(s, WPCommunicateDTO.class);

                //先从公用handler里面找处理器
                BaseWTPHandler handler = baseWTPHandlerMap.get(object.getCommand());

                //没有找到，从当前页面专属处理器里面找
                if (Objects.isNull(handler)) {
                    Map<String, BaseWTPHandler> handlerMap = getChildWTPHandlerMap();
                    if (Objects.nonNull(handlerMap)) {
                        handler = handlerMap.get(object.getCommand());
                    } else {
                        logger.warn(String.format("%s child handler map is null", this.getClass()));
                    }
                }

                if (Objects.nonNull(handler)) {
                    return handler.execute(object.getData());
                }
            } catch (Exception e) {
                Objects.requireNonNull(ApplicationUtil.findCurrentProject()).getService(NotifyServiceImpl.class).notifyException(e);
                logger.error("execute jsQuery error", e);
            }

            return null;
        });


        //页面正常加载校验
        JBCefJSQuery healthCheckQuery = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);
        healthCheckQuery.addHandler(s -> {
            //服务端返回502，页面加载失败并且未被LoadHandler捕捉到
            if (StringUtils.isEmpty(s) || s.equals("<head></head><body></body>")) {
                jbCefBrowser.loadHTML(getH5Content(getErrorH5Path()));
                logger.error("load CodeLink page is null");
            }
            return null;
        });


        //jbCefBrowser生命周期管理
        jbCefBrowser.getJBCefClient().addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public void onAfterCreated(CefBrowser browser) {
                //jbCefBrowser成功创建，还未开始加载webview
                logger.info("init webview onAfterCreated");
                initWebView(jsQuery);
            }

            @Override
            public void onBeforeClose(CefBrowser browser) {
                //猜测是IDEA的bug，频繁切换屏幕时会导致jbCefBrowser关闭，通过这里来监听到关闭事件
                logger.info(this.getClass() + " onBeforeClose");
            }
        }, jbCefBrowser.getCefBrowser());


        //jbCefBrowser加载webview生命周期管理
        jbCefBrowser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {

            final AtomicBoolean needResize = new AtomicBoolean(false);

            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {

                //如果不在默认屏幕打开，重置一下大小，防止分辨率问题
                if (resizeOnOpen()) {
                    GraphicsDevice defaultGraphicsDevice = WindowUtil.getDefaultGraphicsDevice();
                    if (resized.compareAndSet(false, true) &&
                            Objects.nonNull(defaultGraphicsDevice) &&
                            Objects.nonNull(WindowManager.getInstance().getFrame(project)) &&
                            !Objects.requireNonNull(WindowManager.getInstance().getFrame(project)).getGraphicsConfiguration().getDevice().equals(defaultGraphicsDevice)) {

                        needResize.set(true);
                    }
                }

                logger.info("init webview onLoadStart");
                //防止刷新时丢掉注入的js
                initWebView(jsQuery);

            }

            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {

                if (needResize.compareAndSet(true, false)) {
                    resizePanel();
                }

                //确认页面是否成功加载，因为onLoadError捕捉不到webview502的异常
                jbCefBrowser.getCefBrowser().executeJavaScript(healthCheckQuery.inject("document.documentElement.innerHTML"),
                                                               jbCefBrowser.getCefBrowser().getURL(), 0);
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
                jbCefBrowser.loadHTML(getH5Content(getErrorH5Path()));
                logger.warn("load CodeLink page error: " + errorCode + " " + errorText + " " + failedUrl);
                if (needResize.compareAndSet(true, false)) {
                    resizePanel();
                }
            }
        }, jbCefBrowser.getCefBrowser());


        jbCefBrowser.loadURL(url);

        //使用用户默认浏览器打开链接
        jbCefBrowser.setOpenLinksInExternalBrowser(true);

        //将浏览器加入到toolwindow
        setContent(jbCefBrowser.getComponent());

        if (needBorder()) {
            //增加边框解决不好拖动的问题
            jbCefBrowser.getComponent().setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, PluginUtil.isLightTheme() ? new JBColor(new Color(0xf5f5f5), new Color(0xf5f5f5)) : new JBColor(new Color(0x181a1b), new Color(0x181a1b))));
        }
    }

    private void handlePTWMessage(WebviewCommand command, Object data, List<BaseH5Panel> receivers) {
        BasePTWHandler ptwHandler = basePTWHandlerMap.get(command.getCommand());

        if (Objects.isNull(ptwHandler) && Objects.nonNull(getChildPTWHandlerMap())) {
            //如果加了receivers,则只有指定的receivers才能处理消息
            if (CollectionUtils.isEmpty(receivers) || receivers.contains(this)) {
                ptwHandler = getChildPTWHandlerMap().get(command.getCommand());
            }
        }

        if (Objects.nonNull(ptwHandler)) {
            ptwHandler.execute(data);
        }
    }


    protected String getUrl() {
        return DEFAULT_URL + getPath() + getDefaultQueryString();
    }

    protected String getUrl(String queryString) {
        return DEFAULT_URL + getPath() + queryString;
    }

    @Override
    public void dispose() {
        JBCefBrowser oldJbCefBrowser = getJbCefBrowser();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (!SystemInfo.isWindows || Integer.parseInt(ApplicationInfoImpl.getInstance().getMajorVersion()) > 2022) {
                //释放资源
                if (!oldJbCefBrowser.getJBCefClient().isDisposed()) {
                    oldJbCefBrowser.getJBCefClient().dispose();
                }
                if (!oldJbCefBrowser.isDisposed()) {
                    oldJbCefBrowser.dispose();
                }
            }

        });
    }

    public void executeJS(WPCommunicateDTO wpCommunicateDTO) {

        //判断页面是否初始化完成，或者是否要发送初始化信息
        if (wpCommunicateDTO.getCommand().equals(BaseCommandEnums.SYNC_PLUGIN_CONFIG.getCommand())) {
            jbCefBrowser.getCefBrowser().executeJavaScript("window.postMessage(" + JSONObject.parseObject(JSONObject.toJSONString(wpCommunicateDTO)) + ", '*');", jbCefBrowser.getCefBrowser().getURL(), 0);
        } else if (ptwSemaphore.availablePermits() > 0) {
            jbCefBrowser.getCefBrowser().executeJavaScript("window.postMessage(" + JSONObject.parseObject(JSONObject.toJSONString(wpCommunicateDTO)) + ", '*');", jbCefBrowser.getCefBrowser().getURL(), 0);
        } else {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    //页面初始化完成前，最多等待10秒,否则加入等待列表，等待页面初始化完成后再发送
                    if (!ptwSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                        waitSendMsg.add(wpCommunicateDTO);
                        logger.warn(String.format("%s wait semaphore timeout", wpCommunicateDTO.getCommand()));
                    } else {
                        ptwSemaphore.release(1);
                        jbCefBrowser.getCefBrowser().executeJavaScript("window.postMessage(" + JSONObject.parseObject(JSONObject.toJSONString(wpCommunicateDTO)) + ", '*');", jbCefBrowser.getCefBrowser().getURL(), 0);
                    }
                } catch (Exception e) {
                    Objects.requireNonNull(ApplicationUtil.findCurrentProject()).getService(NotifyServiceImpl.class).notifyException(e);
                }

            });
        }
    }

    @Override
    public void refreshContent(Project project) {

        //重置信号量
        ptwSemaphore.drainPermits();

        reopen(getUrl(project.getService(WebViewManager.class).getWebViewProjectConfig().getQueryString()));

    }

    private void initWebView(JBCefJSQuery jsQuery) {

        PluginConfigVO pluginConfig = UserConfigState.getInstance().getState().getPluginConfigVO();

        jbCefBrowser.getCefBrowser().executeJavaScript(
                "window.pluginConfig = " +
                        JSONObject.toJSONString(pluginConfig)
                        + ";",
                jbCefBrowser.getCefBrowser().getURL(), 0
        );

        jbCefBrowser.getCefBrowser().executeJavaScript(
                "window.projectConfig = " +
                        JSONObject.toJSONString(project.getService(WebViewManager.class).getWebViewProjectConfig())
                        + ";",
                jbCefBrowser.getCefBrowser().getURL(), 0
        );

        //注入webview发消息到插件的js函数,webview通过sendMessage()这个方法发消息给插件
        //NOTE: 如果在这个对象注入之前,webview发送了initFinish,就可能导致ptwSemaphore一直无法释放
        jbCefBrowser.getCefBrowser().executeJavaScript(

                //inject 支持成功回调和失败回调
                "window.JavaPanelBridge = {" +
                        "  sendMessage: function(data) {" +
                        "	" + jsQuery.inject("data") +
                        "  }," +

                        "  sendMessageWithCallback: function(data,successCallback,failureCallback) {" +
                        "	" + jsQuery.inject("data", "successCallback", "failureCallback") +
                        "  }" +
                        "};",
                jbCefBrowser.getCefBrowser().getURL(), 0
        );


    }

    public void notifyPanelLoaded() {
        if (ptwSemaphore.availablePermits() == 0) {
            //处理等待发送的消息
            while (!waitSendMsg.isEmpty()) {
                WPCommunicateDTO msg = waitSendMsg.remove(0);
                jbCefBrowser.getCefBrowser().executeJavaScript("window.postMessage(" + JSONObject.parseObject(JSONObject.toJSONString(msg)) + ", '*');", jbCefBrowser.getCefBrowser().getURL(), 0);
            }
            ptwSemaphore.release(1);
        }
    }

    private void reopen(String url) {
        resized.set(false);
        waitSendMsg.clear();
        dispose();
        initialize(url);
    }

    private String getH5Content(String path) {
        return FileUtil.getResource(path)
                .replace("[prism-theme]", UIUtil.isUnderDarcula() ? "prism-darcula" : "prism-vs")
                .replace("[bg]", ThemeUtils.getBackgroundColorRGB())
                .replace("[font-color]", ThemeUtils.getFontColorRGB())
                .replace("[font-size]", String.valueOf(ThemeUtils.getFontSize()))
                .replace("[separator-color]", ThemeUtils.getSeparatorColorRGB())
                .replace("[disabled-color]", ThemeUtils.getDisabledTextColorRGB())
                .replace("[scrollbar-color]", ThemeUtils.getScrollBarForegroundColorRGB())
                .replace("[scrollbar-radius]", String.valueOf(ThemeUtils.getScrollBarRadius()))
                .replace("[panel-background-color]", ThemeUtils.getPanelBackgroundColorRGB())
                .replace("[button-background-color]", ThemeUtils.getButtonBackgroundColorRGB())
                .replace("[button-disabled-background-color]", ThemeUtils.getDisabledButtonBackgroundColorRGB());
    }


    /**
     * 仅限开发时调用,打开webview的开发者工具
     */
    public void openDevTools() {
        JDialog myDevtoolsFrame = new JDialog(WindowManager.getInstance().getFrame(project));
        Rectangle bounds = Objects.requireNonNull(WindowManager.getInstance().getFrame(project)).getBounds();
        myDevtoolsFrame.setTitle("JCEF DevTools");
        myDevtoolsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        myDevtoolsFrame.setBounds(bounds.width / 4 + 100, bounds.height / 4 + 100, bounds.width / 2, bounds.height / 2);
        myDevtoolsFrame.setLayout(new BorderLayout());
        JBCefBrowser devTools = JBCefBrowser.createBuilder().setCefBrowser(jbCefBrowser.getCefBrowser().getDevTools()).setClient(jbCefBrowser.getJBCefClient()).build();
        myDevtoolsFrame.add(devTools.getComponent(), BorderLayout.CENTER);
        myDevtoolsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                myDevtoolsFrame.dispose();
                Disposer.dispose(devTools);
            }
        });
        myDevtoolsFrame.setVisible(true);
    }


    /**
     * windows不使用offScreen渲染时，不在默认屏幕打开，页面位置会飘，重置一下大小
     */
    protected boolean resizeOnOpen() {
        return SystemInfo.isWindows && !useOffScreenRendering();
    }

    protected void resizePanel() {

    }

    public void doAfterWebviewInitFinish() {

    }

    protected boolean useOffScreenRendering() {
        return UserConfigState.getInstance().getState().getWebviewOSR();
    }

    protected boolean needBorder() {
        return false;
    }

    protected String getDefaultQueryString() {
        return PluginUtil.isLightTheme() ? "" : "?theme=dark";
    }

    @NotNull
    public abstract String getPath();

    public abstract Map<String, BaseWTPHandler> getChildWTPHandlerMap();

    public abstract Map<String, BasePTWHandler> getChildPTWHandlerMap();

    public abstract String getErrorH5Path();

}
