package com.ke.utils;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.util.EnvironmentUtil;
import com.ke.exception.ExceptionEnum;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/7 16:03
 * @Description
 */
public class BaseLocalBinaryToolUtil {


    public static String getBinaryPath(Path binDir, String windowsName, String macArmName, String macAmdName, String binaryName) {

        Path executable = null;
        if (SystemInfoRt.isWindows) {
            executable = binDir.resolve(windowsName);
        } else if (SystemInfoRt.isMac) {
            if (SystemInfo.OS_ARCH.equals("aarch64") || SystemInfo.OS_ARCH.equals("arm64")) {
                executable = binDir.resolve(macArmName);
            } else if (SystemInfo.OS_ARCH.equals("x86_64") || SystemInfo.OS_ARCH.equals("amd64")) {
                executable = binDir.resolve(macAmdName);
            }
        }
        if (Objects.isNull(executable) || !Files.exists(executable)) {
            ExceptionEnum.BINARY_COMMAND_NOT_COMPATIBLE_EXCEPTION.asBusinessException(binaryName);
        }
        assert executable != null;

        //检查文件是否有可执行权限
        if (!SystemInfoRt.isWindows) {
            try {
                Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(executable);
                posixFilePermissions.add(PosixFilePermission.OWNER_EXECUTE);
                Files.setPosixFilePermissions(executable, posixFilePermissions);
            } catch (Exception ignore) {

            }
        }
        return executable.toString();
    }


    /**
     * 获取执行命令
     */
    public static GeneralCommandLine getCommandline(List<String> args,String binaryPath) {
        final Map<String, String> environment = new HashMap<>(EnvironmentUtil.getEnvironmentMap());
        environment.put("DISABLE_SPINNER", "true");
        if (SystemInfo.isMac || SystemInfo.isLinux) {
            String path = environment.get("PATH");
            if (StringUtils.isNotEmpty(binaryPath) && StringUtils.contains(binaryPath, "/")) {
                path = binaryPath.substring(0, binaryPath.lastIndexOf("/")) + ":" + path;
                environment.put("PATH", path);
            }
        }
        GeneralCommandLine generalCommandLine = new GeneralCommandLine(args).withEnvironment(environment);
        generalCommandLine.setCharset(StandardCharsets.UTF_8);
        return generalCommandLine;
    }

    /**
     * 获取执行命令
     */
    public static GeneralCommandLine getCommandlineWithShell(List<String> args,String binaryPath) {
        if (SystemInfo.isWindows){
            List<String> command = new ArrayList<>();
            command.add("cmd");
            command.add("/c");
            command.addAll(args);
            return getCommandline(command,binaryPath);
        }
        return getCommandline(args,binaryPath);
    }
}
