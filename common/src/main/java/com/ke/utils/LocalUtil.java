package com.ke.utils;

import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.messages.Topic;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.action.TerminalNewPredefinedSessionAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/2/17 18:24
 * @Description
 */
public class LocalUtil {


    private static final Logger LOG = Logger.getInstance(LocalUtil.class);

    // 缓存shell
    private static Map<String, String> shellMap = new HashMap<>();


    /**
     * 获取默认shell
     */
    public static String getDefaultShell() {
        Map<String, String> shells = detectShells();
        if (MapUtils.isNotEmpty(shells)) {
            if (SystemInfo.isUnix) {
                String bash = shells.get("bash");
                return StringUtils.isEmpty(bash) ? shells.values().stream().findFirst().orElse(null) : bash;
            } else if (SystemInfo.isWindows) {
                String cmd = shells.get("cmd");
                return StringUtils.isEmpty(cmd) ? shells.values().stream().findFirst().orElse(null) : cmd;
            }

        }
        return null;
    }

    /**
     * 查找shell
     *
     * @see TerminalNewPredefinedSessionAction#detectShells()
     */
    public static @NotNull Map<String, String> detectShells() {
        if (MapUtils.isNotEmpty(shellMap)) {
            return shellMap;
        }

        if (SystemInfo.isUnix) {
            if (Files.exists(Path.of("/bin/bash"))) {
                shellMap.put("bash", "/bin/bash");
            }
            if (Files.exists(Path.of("/usr/local/bin/zsh"))) {
                shellMap.put("zsh", "/usr/local/bin/zsh");
            } else if (Files.exists(Path.of("/usr/bin/zsh"))) {
                shellMap.put("zsh", "/usr/bin/zsh");
            }
            if (Files.exists(Path.of("/usr/bin/fish"))) {
                shellMap.put("fish", "/usr/bin/fish");
            }

        } else if (SystemInfo.isWindows) {
            File powershell = PathEnvironmentVariableUtil.findInPath("powershell.exe");
            if (powershell != null && powershell.exists() && StringUtil.startsWithIgnoreCase(powershell.getAbsolutePath(), "C:\\Windows\\System32\\WindowsPowerShell\\")) {
                shellMap.put("powershell", powershell.getAbsolutePath());
            }

            File cmd = PathEnvironmentVariableUtil.findInPath("cmd.exe");
            if (cmd != null && cmd.exists() && StringUtil.startsWithIgnoreCase(cmd.getAbsolutePath(), "C:\\Windows\\System32\\")) {
                shellMap.put("cmd", cmd.getAbsolutePath());
            }

            File pwsh = PathEnvironmentVariableUtil.findInPath("pwsh.exe");
            if (pwsh != null && pwsh.exists() && StringUtil.startsWithIgnoreCase(pwsh.getAbsolutePath(), "C:\\Program Files\\PowerShell\\")) {
                shellMap.put("pwsh", pwsh.getAbsolutePath());
            }

            File gitBash = new File("C:\\Program Files\\Git\\bin\\bash.exe");
            if (gitBash.isFile() && gitBash.exists()) {
                shellMap.put("gitBash", gitBash.getAbsolutePath());
            }

        }

        return shellMap;
    }


    /**
     * 判断端口是否可用
     */
    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            // 端口可用
            if(SystemInfo.isMac){
                return isPortLocalAvailable(port);
            }
            return true;
        } catch (Exception e) {
            LOG.warn("Port all " + port + " is not available", e);
            // 端口不可用
            return false;
        }

    }

    /**
     * 判断本地端口是否可用
     */
    public static boolean isPortLocalAvailable(int port) {
        try (ServerSocket local = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))) {
            local.setReuseAddress(true);
            // 端口可用
            return true;
        } catch (Exception e) {
            LOG.warn("Port local " + port + " is not available", e);
            // 端口不可用
            return false;
        }

    }


    /**
     * 获取某个端口的启动命令
     */
    public static String getCommandByPort(int port) {
        try {

            if (isPortAvailable(port)) {
                // 端口未被占用，没有进程
                return "";
            }

            if (SystemInfoRt.isWindows) {
                String pid = getPidByPort(port);
                if (StringUtils.isNotEmpty(pid)) {
                    String winProcessInfo = getWinProcessInfo(pid);
                    if (StringUtils.isNotEmpty(winProcessInfo)) {
                        try {
                            return (winProcessInfo.split("\n")[0]).split(":")[1].trim();
                        } catch (Exception e) {
                            LOG.warn("getCommandByPort error, winProcessInfo is error:" + winProcessInfo, e);
                            return "";
                        }
                    }
                    LOG.warn("getCommandByPort error, winProcessInfo is null");
                    return "";
                }
                LOG.warn("getCommandByPort error, pid is null");
                return "";
            }
            // 类 Unix 系统使用 lsof 命令获取占用端口的进程
            String command = "lsof -i :" + port + " | awk 'NR>1 {print $1}'";

            // 执行命令
            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // 处理命令输出
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line.trim());
            }

            process.waitFor();
            return stringBuilder.toString();
        }catch (InterruptedException interruptedException){
            Thread.currentThread().interrupt();
            LOG.warn("getCommandByPort error", interruptedException);
        } catch (Exception e) {
            LOG.warn("getCommandByPort error", e);
        }
        return "";
    }


    /**
     * windows根据端口获取pid
     */
    public static String getPidByPort(int port) {
        try {
            // 执行 netstat 命令来查看端口占用情况
            String command = "netstat -ano | findstr :" + port;
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});

            // 获取命令执行的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING")) {
                    // 解析 PID（PID 是最后一个字段）
                    String[] parts = line.trim().split("\\s+");
                    return parts[parts.length - 1];
                }
            }
        } catch (IOException e) {
            LOG.warn("getPidByPort error", e);
        }
        return null;
    }

    /**
     * windows 获取进程信息
     */
    public static String getWinProcessInfo(String pid) {
        try {
            // 执行 tasklist 命令获取 PID 的详细信息
            String command = "tasklist /FI \"PID eq " + pid + "\" /FO LIST";
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});

            // 获取命令执行的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    stringBuilder.append(line.trim()).append("\n");
                }
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            LOG.warn("getProcessInfo error", e);
        }
        return "";
    }

    /**
     * 执行命令
     */
    public static ProcessHandler executeCommand(@NotNull Project project, @NotNull String workDir, @NotNull List<String> cmdList, @Nullable String commandId) {
        try {
            GeneralCommandLine commandLine = new GeneralCommandLine(cmdList);
            commandLine.setWorkDirectory(workDir);

            ProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);

            processHandler.addProcessListener(new OutputListener() {

                private final StringBuilder stringBuilder = new StringBuilder();
                private String type = "stdout";

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    stringBuilder.append(event.getText());
                    if (outputType.toString().equals("stderr")) {
                        type = "stderr";
                    }
                    project.getMessageBus().syncPublisher(CommandExecuteOutputNotifier.COMMAND_EXECUTE_OUTPUT_NOTIFIER_TOPIC).onTextAvailable(event, outputType, commandId);
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    project.getMessageBus().syncPublisher(CommandExecuteOutputNotifier.COMMAND_EXECUTE_OUTPUT_NOTIFIER_TOPIC).processTerminated(event, commandId, stringBuilder.toString(), type);
                }

            });

            processHandler.startNotify();

            return processHandler;
        } catch (Exception e) {
            LOG.error("executeCommand error", e);
        }
        return null;
    }


    public interface CommandExecuteOutputNotifier {

        Topic<CommandExecuteOutputNotifier> COMMAND_EXECUTE_OUTPUT_NOTIFIER_TOPIC =
                Topic.create("Command Execute Output", CommandExecuteOutputNotifier.class);

        void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType, @Nullable String commandId);

        void processTerminated(@NotNull ProcessEvent event, @Nullable String commandId, @NotNull String output, @NotNull String type);
    }
}
