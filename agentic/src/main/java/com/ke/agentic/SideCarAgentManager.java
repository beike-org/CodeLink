package com.ke.agentic;


import com.intellij.openapi.Disposable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.ke.RetryTemplate;
import com.ke.agentic.socket.SideCarHttpSocketManager;
import com.ke.agentic.socket.SideCarHttpSocketServer;
import com.ke.agentic.pts.PTSHandler;
import com.ke.agentic.topic.SideCarNotifier;
import com.ke.utils.LocalUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.SideCarIDEInfo;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebViewProjectConfig;
import com.ke.webview.topic.RefreshNotifier;
import com.ke.webview.util.PTWUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Duration;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/8/27 15:18
 * @Description
 */
public class SideCarAgentManager implements Disposable {


    private final static Logger LOG = Logger.getInstance(SideCarAgentManager.class);


    public static final Integer DEFAULT_AGENT_PORT = 42424;

    public static final Integer DEFAULT_SOCKET_PORT = 41100;

    //健康检查最大重试次数
    private static final int HEALTH_CHECK_MAX_RETRIES = 50;

    // 健康检查轮询间隔（毫秒）
    private static final long HEALTH_CHECK_INTERVAL = 200;

    @Getter
    public Integer agentPort;

    @Getter
    public Integer socketPort;

    private SideCarHttpSocketServer httpSocketServer;

    @Getter
    private SideCarAgentProcessService sideCarAgentProcessService;

    @Getter
    @Setter
    private Boolean agentStarted = false;

    @Getter
    @Setter
    private Boolean agentReady = false;

    private final AtomicBoolean inited = new AtomicBoolean(false);

    public SideCarAgentManager() {
        Disposer.register(ApplicationManager.getApplication(), this);

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(RefreshNotifier.REFRESH_ACTION_TOPIC, new RefreshNotifier() {
            @Override
            public void refresh(Project project) {
                WebViewProjectConfig webViewProjectConfig = project.getService(WebViewManager.class).getWebViewProjectConfig();
                if (updateSideCarIDEInfo(webViewProjectConfig.getSideCarIDEInfo())) {
                    PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewProjectConfig, project);
                }
            }
        });
        init();
    }

    public static SideCarAgentManager getInstance() {
        return ApplicationManager.getApplication().getService(SideCarAgentManager.class);
    }

    public void init() {
        startBackGroundProcess();
    }

    private void startBackGroundProcess() {
        if (inited.compareAndSet(false, true)) {
            LOG.info("SideCarAgentManager init");
            SideCarHttpSocketManager.getInstance().init();
            startSocket();
            startAgent();
        }
    }


    public void startSocket() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (isSocketRunning()) {
                return true;
            }
            for (int i = 0; i < 1000; i++) {
                if (LocalUtil.isPortAvailable(DEFAULT_SOCKET_PORT + i)) {
                    socketPort = DEFAULT_SOCKET_PORT + i;
                    LOG.info("Socket port is available: " + socketPort);
                    try {
                        httpSocketServer = new SideCarHttpSocketServer(socketPort);
                        ApplicationManager.getApplication().getMessageBus().syncPublisher(SideCarNotifier.SIDE_CAR_NOTIFIER_TOPIC).startSocket(socketPort);
                        return true;
                    } catch (Exception e) {
                        LOG.error("Start socket server error", e);
                        return false;
                    }
                }
            }

            LOG.warn("Socket port is not available");
            return false;
        });

    }


    public void startAgent() {
        if (isAgentRunning()) {
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            for (int i = 0; i < 1000; i++) {
                if (LocalUtil.isPortAvailable(DEFAULT_AGENT_PORT + i)) {
                    agentPort = DEFAULT_AGENT_PORT + i;
                    LOG.info("SideCarAgent port is available: " + agentPort);

                    try {
                        sideCarAgentProcessService = new SideCarAgentProcessService(agentPort);
                        sideCarAgentProcessService.startNotify();
                        return true;
                    } catch (Exception e) {
                        LOG.error("Start sidecar agent error", e);
                    }

                }
            }

            LOG.warn("sidecar agent is not available");
            return false;
        });

    }


    public boolean updateSideCarIDEInfo(SideCarIDEInfo sideCarIDEInfo) {
        if (Objects.isNull(sideCarIDEInfo)) {
            return false;
        }
        if (Objects.equals(sideCarIDEInfo.getSideCarPort(), agentPort) && Objects.equals(sideCarIDEInfo.getSocketPort(), socketPort)) {
            return false;
        }
        sideCarIDEInfo.setSocketPort(socketPort);
        sideCarIDEInfo.setSideCarPort(agentPort);
        return true;
    }

    public boolean isSocketRunning() {
        return httpSocketServer != null && httpSocketServer.isAlive();
    }

    public boolean isAgentRunning() {
        return sideCarAgentProcessService != null && sideCarAgentProcessService.isRunning() && agentStarted;
    }

    public boolean isAgentReady() {
        return isAgentRunning() && agentReady;
    }

    public boolean isAgentShutdown() {
        return sideCarAgentProcessService != null && sideCarAgentProcessService.isShutdown();
    }

    public void shutdownAgent() {
        if (sideCarAgentProcessService != null) {
            sideCarAgentProcessService.shutdown();
        }
    }

    public void shutdownSocket() {
        if (httpSocketServer != null) {
            httpSocketServer.stop();
        }
    }


    @Override
    public void dispose() {
        inited.compareAndSet(true, false);
        if (isAgentRunning()) {
            shutdownAgent();
        }
        if (isSocketRunning()) {
            shutdownSocket();
        }
    }


    /**
     * 检查 SideCarAgent 的健康状态
     * 使用CompletableFuture异步执行健康检查
     */
    public void healthCheck() {
        RetryTemplate.builder()
                .maxAttempts(HEALTH_CHECK_MAX_RETRIES)
                .waitDuration(Duration.ofMillis(HEALTH_CHECK_INTERVAL))
                .retryOn(Exception.class)
                .onRetry(retryContext -> LOG.debug("Health check attempt " + retryContext.getRetryCount() + " failed"))
                .build()
                .execute(context -> {
                    Boolean isHealthy = PTSHandler.getInstance().healthCheckPort(agentPort);
                    if (!Boolean.TRUE.equals(isHealthy)) {
                        throw new RuntimeException("Health check failed");
                    }
                    return true;
                })
                .thenAccept(success -> {
                    if (success) {
                        agentReady = true;
                        ApplicationManager.getApplication().invokeLater(() -> {
                            ApplicationManager.getApplication().getMessageBus()
                                    .syncPublisher(SideCarNotifier.SIDE_CAR_NOTIFIER_TOPIC)
                                    .agentReady();
                            LOG.info("SideCarAgent health check success on port: " + agentPort);
                        });
                    }
                })
                .exceptionally(throwable -> {
                    LOG.error("SideCarAgent health check failed after " + HEALTH_CHECK_MAX_RETRIES + " attempts", throwable);
                    return null;
                });
    }


}
