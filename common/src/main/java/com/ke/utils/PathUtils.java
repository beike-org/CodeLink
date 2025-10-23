package com.ke.utils;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * URL中路径处理工具
 */
public class PathUtils {

    public static @NotNull String getRootPath(Project project) {
        return Objects.isNull(project.getBasePath()) ? "" : project.getBasePath();
    }

    /**
     * 获取当前项目所属路径的路径名称
     * /a/b/c return c
     *
     * @param project
     * @return
     */
    public static @NotNull String getProjectPathName(Project project) {
        Path path = Paths.get(PathUtils.getRootPath(project)).getFileName();
        return Objects.isNull(path) ? "" : path.toString();
    }

    /**
     * 返回上级目录，操作系统安全，需要传递当前操作系统的路径格式
     *
     * @param filePath
     * @return
     */
    public static @NotNull String getParentPath(@NotNull String filePath) {
        Path path = Paths.get(filePath).getParent();
        return Objects.isNull(path) ? "" : path.toString();

    }

    public static List<String> generatePathCombinations(String inputPath) {
        List<String> combinations = new ArrayList<>();

        // 如果路径以不可分割路径开头，先处理不可分割部分
        Path path = Paths.get(inputPath).normalize(); // 规范化路径
        Path current = path.getRoot(); // 获取根路径（如 / 或 C:\）

        // 遍历路径部分
        for (Path part : path) {
            current = current.resolve(part);
            combinations.add(current.toString());
        }

        return combinations;
    }

    public static String toLinuxPath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        // Step 1: Replace backslashes (Windows-style) with forward slashes
        String linuxPath = path.replace("\\", "/");

//        // Step 2: Handle Windows drive letters (e.g., "C:\path" -> "/path")
//        if (linuxPath.matches("^[a-zA-Z]:/.*")) {
//            linuxPath = linuxPath.substring(2); // Remove the drive letter
//        }
//
//        // Step 3: Ensure path starts with "/"
//        if (!linuxPath.startsWith("/")) {
//            linuxPath = "/" + linuxPath;
//        }

        // Step 4: Remove redundant slashes (e.g., "//path///to" -> "/path/to")
        linuxPath = linuxPath.replaceAll("//+", "/");

        return linuxPath;
    }

    /**
     * Converts a Linux-style path to the current operating system's format.
     *
     * @param linuxPath The Linux-style path (e.g., "/home/user/file").
     * @return The path formatted for the current operating system.
     */
    public static String toSystemPath(String linuxPath) {
        if (linuxPath == null || linuxPath.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        // Get the current operating system
        String osName = System.getProperty("os.name").toLowerCase();

        // If the OS is Windows, convert slashes
        if (osName.contains("win")) {
            return linuxPath.replace("/", "\\");
        }

        // For macOS and Linux, return as-is (or normalize using File.separator)
        return linuxPath.replace("/", File.separator);
    }

    public static String pathJoinWithSystem(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String newPart = toSystemPath(parts[i]);
            if (i == 0) {
                // 对第一个路径部分，不添加路径分隔符
                result.append(newPart);
            } else {
                if (!newPart.startsWith(File.separator)) {
                    result.append(File.separator);
                }
                result.append(newPart);
            }
        }
        return result.toString();
    }

    /**
     * 路径处理, 会增加前缀
     */
    public static String path(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.startsWith("/")) {
            return path.trim();
        }
        return "/" + path.trim();
    }

    /**
     * 路径拼接
     */
    public static String path(String path, String subPath) {
        path = path(path);
        subPath = path(subPath);
        if (path == null) {
            return subPath;
        }
        if (subPath == null) {
            return path;
        }
        if (path.endsWith("/") && subPath.startsWith("/")) {
            return path + subPath.substring(1);
        }
        return path + subPath;
    }

    /**
     * 清除路径变量中的正则表达式
     */
    public static String clearPathPattern(String path) {
        StringBuilder thePath = new StringBuilder();

        char[] chars = path.toCharArray();

        int pairCount = 0;                  // 匹配的括号对数量
        boolean inExpress = false;          // 是否在表达式中
        boolean inExpressPatten = false;    // 是否在表达式的正则表达式中
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '{' && !inExpress) {
                pairCount++;
                inExpress = true;
            } else if (c == '/' && inExpress) {
                inExpress = false;
            } else if (c == '}' && inExpress) {
                pairCount--;
                if (pairCount == 0) {
                    inExpress = false;
                }
            } else {
                if (inExpress && c == ':') {
                    inExpressPatten = true;
                }
            }

            boolean isPathVariablePattern = inExpress && inExpressPatten;
            if (!isPathVariablePattern) {
                thePath.append(c);
            }
        }

        return thePath.toString();
    }
}
