package com.ke.utils;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import com.intellij.openapi.updateSettings.impl.PluginUpdates;
import com.intellij.openapi.updateSettings.impl.UpdateChecker;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/6/9 18:03
 * @Version 1.0
 * @Description
 */
public class PluginUtil {

    private static final Logger LOG = Logger.getInstance(PluginUtil.class);

    // 插件id
    public static final PluginId PLUGIN_ID = PluginId.getId("com.ke.codelink");

    // 未找到插件版本号时的默认版本号
    public static final String DEFAULT_NOT_FOUND_VERSION = "unknown";


    /**
     * 获取plugin.xml中写的版本号
     */
    public static String getVersion() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PLUGIN_ID);
        return plugin == null ? DEFAULT_NOT_FOUND_VERSION : plugin.getVersion();
    }

    /**
     * 获取插件运行时环境
     */
    public static String getEnv(){
        return ApplicationManager.getApplication().isInternal() ? "dev" : (PluginUtil.getVersion().contains("alpha") ? "preprod" : "prod");
    }

    /**
     * 获取plugin.xml中写的changeLog
     */
    public static String getChangeLog() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PLUGIN_ID);
        return plugin == null ? "" : plugin.getChangeNotes();
    }



    /**
     * 获取操作系统
     */
    public static String getOS() {
        return System.getProperty("os.name");
    }

    /**
     * 判断用户ide是亮色主题还是暗色主题
     */
    public static Boolean isLightTheme() {
        return JBColor.isBright();
    }


    /**
     * 获取插件的安装路径
     */
    @NotNull
    public static Path getPluginBasePath() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PLUGIN_ID);
        assert plugin != null;
        return plugin.getPluginPath();
    }


    /**
     * 获取插件的下载器类
     */
    public static PluginDownloader getDownloader() {
        PluginUpdates pluginUpdates = UpdateChecker.getInternalPluginUpdates().getPluginUpdates();
        Collection<PluginDownloader> allEnabled = pluginUpdates.getAllEnabled();
        for (PluginDownloader downloader : allEnabled) {
            if (downloader.getPluginId().equals(PLUGIN_ID.getIdString())) {
                return downloader;
            }
        }
        return null;
    }


    public static boolean isCodeLinkPlugin(@NotNull PluginDescriptor pluginDescriptor) {
        return pluginDescriptor.getPluginId().equals(PLUGIN_ID);
    }
}
