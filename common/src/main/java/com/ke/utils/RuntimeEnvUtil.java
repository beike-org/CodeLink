package com.ke.utils;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spring.model.utils.SpringCommonUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/12/11 11:28
 * @Version 1.0
 * @Description
 */
public class RuntimeEnvUtil {

    private final static Logger LOGGER = Logger.getInstance(RuntimeEnvUtil.class);

    /**
     * 获取当前项目的JDK版本
     */
    public static String getJDKVersion(Project project) {
        try {
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (Objects.nonNull(projectSdk) && projectSdk.getSdkType() instanceof JavaSdk) {
                JavaSdkVersion version = JavaSdk.getInstance().getVersion(projectSdk);
                return Objects.nonNull(version) ? version.getDescription() : "";
            }
        } catch (NoClassDefFoundError | Exception e) {
            LOGGER.warn("get jdk error", e);
        }
        return "";
    }

    /**
     * 判断插件运行时的环境是否包含某个类(IDEA,Pycharm,WebStorm等所包含的类会有所区别)
     */
    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    /**
     * 判断当前项目是否是Maven项目
     */
    public static boolean isMavenProject(Project project) {
        try {
            MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
            return mavenProjectsManager.isMavenizedProject();
        } catch (NoClassDefFoundError | Exception ignore) {
            return false;
        }
    }

    /**
     * 判断当前IDE是否是Java IDE
     */
    public static boolean isJavaIDE() {
        return PluginManagerCore.isPluginInstalled(PluginId.getId("com.intellij.modules.java"));
    }

    /**
     * 获取当前用户IP
     */
    public static String getIp() {

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            LOGGER.warn("get ip error", e);
        }
        return "";

    }

    /**
     * 获取spring的测试环境配置文件
     */
    public static List<VirtualFile> getSpringResourceConfigFiles(@NotNull Project project, List<String> configFiles) {
        List<VirtualFile> virtualFiles = new ArrayList<>();
        try {
            if (!SpringCommonUtils.isSpringConfigured(project)) {
                return virtualFiles;
            }
        }catch (NoClassDefFoundError | Exception e){
            LOGGER.warn("get spring config error", e);
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots(false);
            for (VirtualFile sourceRoot : sourceRoots) {
                if (StringUtils.endsWith(sourceRoot.getPath(), "resources")) {
                    VirtualFile[] children = sourceRoot.getChildren();
                    for (VirtualFile child : children) {
                        if (child.isDirectory()) {
                            VirtualFile[] childChildren = child.getChildren();
                            for (VirtualFile childChild : childChildren) {
                                if (!childChild.isDirectory() && configFiles.contains(childChild.getName())) {
                                    virtualFiles.add(childChild);
                                }
                            }
                        } else if (configFiles.contains(child.getName())) {
                            virtualFiles.add(child);
                        }
                    }
                }
            }
        }
        return virtualFiles;
    }


    /**
     * 判断是否有后台进程运行,windows传pid,mac传后台任务名
     */
    public static boolean isProcessRunning(String info) {
        try {
            // 根据操作系统执行不同的命令
            String line;
            StringBuilder pidInfo = new StringBuilder();

            Process p;
            if (SystemInfo.isWindows) {
                p = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + info + "\"");
            } else {
                p = Runtime.getRuntime().exec("ps -ef");
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }

            input.close();

            // 检查进程名是否存在
            if (pidInfo.toString().contains(info)) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.warn("获取进程异常", e);
        }
        return false;
    }

    public static String checkProjectLanguage(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();

        // 先从module取
        for (Module module : modules) {
            IdeaModuleType ideaModuleType = IdeaModuleType.fromName(module.getModuleTypeName());
            if (!ideaModuleType.equals(IdeaModuleType.UNKNOWN_MODULE) && !ideaModuleType.equals(IdeaModuleType.WEB_MODULE)) {
                return ideaModuleType.getType();
            } else if (ideaModuleType.equals(IdeaModuleType.WEB_MODULE)) {
                for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
                    if (entry instanceof LibraryOrderEntry) {
                        LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) entry;
                        if (libraryOrderEntry.getLibraryName() != null && libraryOrderEntry.getLibraryName().contains("Go SDK")) {
                            return IdeaModuleType.GO_MODULE.getType();
                        }
                    }
                }
            }
        }

        // module没取到,从活动区文件取
        AtomicReference<String> language = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> language.set(EditorUtil.getLanguage(project)));
        return StringUtils.isEmpty(language.get()) ? IdeaModuleType.UNKNOWN_MODULE.getType() : language.get();
    }


    @Getter
    public enum IdeaModuleType {
        JAVA_MODULE("JAVA_MODULE", "java"),
        WEB_MODULE("WEB_MODULE", "web"),
        ANDROID_MODULE("ANDROID_MODULE", "android"),
        PYTHON_MODULE("PYTHON_MODULE", "python"),
        KOTLIN_MODULE("KOTLIN_MODULE", "kotlin"),
        GO_MODULE("GO_MODULE", "go"),
        PHP_MODULE("PHP_MODULE", "php"),
        RUBY_MODULE("RUBY_MODULE", "ruby"),
        JS_MODULE("JS_MODULE", "js"),
        UNKNOWN_MODULE("UNKNOWN_MODULE", "unknown");

        private final String moduleName;
        private final String type;

        IdeaModuleType(String moduleName, String type) {
            this.moduleName = moduleName;
            this.type = type;
        }


        public static IdeaModuleType fromName(String name) {
            for (IdeaModuleType type : IdeaModuleType.values()) {
                if (type.getModuleName().equals(name)) {
                    return type;
                }
            }
            return UNKNOWN_MODULE;
        }

    }


}
