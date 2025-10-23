package com.ke;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.ke.editor.action.DefaultPromptActionFactory;
import com.ke.webview.WebViewManager;
import com.ke.webview.WebViewProjectConfig;
import com.ke.utils.RuntimeEnvUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginStartupActivity implements StartupActivity {

    private final static Logger LOG = Logger.getInstance(PluginStartupActivity.class);

	@Override
	public void runActivity(@NotNull Project project) {

		DefaultPromptActionFactory.refreshActions();

        project.getService(WebViewManager.class)
                .setWebViewProjectConfig(WebViewProjectConfig.builder()
                        .jdkVersion(RuntimeEnvUtil.getJDKVersion(project))
                        .isMavenProject(RuntimeEnvUtil.isMavenProject(project))
                        .build());

        List<StartupActivityHandler> startupActivityHandlers = StartupActivityHandler.EP_NAME.getExtensionList();
		startupActivityHandlers.forEach(handler -> {
			try {
				handler.beforeWebViewInit(project);
			} catch (Exception e) {
				LOG.warn("before init startup activity handler error:" + e);
			}
		});

		//通知toolwindow可以加载了
		project.getService(WebViewManager.class).initWebViewManager();
		startupActivityHandlers.forEach(handler -> {
			try {
				handler.init(project);
			} catch (Exception e) {
				LOG.warn("init startup activity handler error:" + e);
			}
		});


	}

}
