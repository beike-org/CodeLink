package com.ke.utils;

import com.alibaba.fastjson.annotation.JSONField;
import com.intellij.application.options.CodeStyle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.lang.Language;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.BinaryLightVirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.ke.exception.BusinessException;
import com.ke.exception.ExceptionEnum;
import com.ke.service.notify.NotifyServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/7/27 10:45
 * @Version 1.0
 * @Description
 */
public class FileUtil {
    private static final Logger LOGGER = Logger.getInstance(FileUtil.class);

    private static final List<String> excludeExtensionList = com.google.common.collect.Lists.newArrayList("yml", "properties", "xml", "yaml", "ini", "toml");
    private static final long MAX_FILE_SIZE = 800L * 1024;

    private static final List<String> IMG_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    private static final long MAX_IMG_SIZE = 20L * 1024 * 1024;


    /**
     * 根据文件路径获取文件名
     */
    public static String getFileName(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return "";
        }
        String[] split = filePath.split(Matcher.quoteReplacement(File.separator));
        return split[split.length - 1];
    }

    /**
     * 获取项目根目录
     */
    public static String getProjectRootPath(@NotNull Project project) {

        String presentableUrl = project.getPresentableUrl();
        if (Objects.isNull(presentableUrl)) {
            return "";
        }
        if (SystemInfoRt.isWindows) {
            return presentableUrl.replaceAll("/", Matcher.quoteReplacement(File.separator));
        }
        return presentableUrl;
    }

    /**
     * 打开项目文件，并定位到指定文件
     */
    public static void openFile(@NotNull Project project, @NotNull String absPath) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(absPath);
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true);
            ToolWindow projectToolWindow = ComponentUtil.getProjectToolWindow(project);
            if (Objects.nonNull(projectToolWindow)) {
                if (!projectToolWindow.isVisible()) {
                    projectToolWindow.show();
                }
                // 展开目录树并定位文件
                ProjectView projectView = ProjectView.getInstance(project);
                projectView.select(null, file, false);
            }

        }
    }

    /**
     * 转换VirtualFile为PsiFile
     */
    public static PsiFile convertVirtualFileToPsiFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    /**
     * 创建临时文件
     */
    public static LightVirtualFile createTempFile(String name, String content) {
        return new LightVirtualFile(name, content);
    }

    /**
     * 根据文件绝对路径获取目录
     */
    @NotNull
    public static VirtualFile findVirtualFile(@NotNull String path) {

        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);

        if (Objects.isNull(virtualFile)) {
            ExceptionEnum.FILE_NOT_EXISTS_EXCEPTION.asBusinessException();
        }

        assert virtualFile != null;
        return virtualFile;
    }

    /**
     * 根据文件绝对路径获取目录
     */
    public static PsiDirectory findPsiDirectory(@NotNull Project project, @NotNull String path) {
        VirtualFile virtualFile = findVirtualFile(path);
        if (!virtualFile.isDirectory()) {
            ExceptionEnum.DIR_NOT_EXISTS_EXCEPTION.asBusinessException(path);
        }
        // 使用 PsiManager 将 VirtualFile 转换为 PsiDirectory
        return PsiManager.getInstance(project).findDirectory(virtualFile);
    }

    /**
     * 根据文件绝对路径获取目录，如果不存在则创建
     */
    public static PsiDirectory findPsiDirectoryOrCreate(@NotNull Project project, @NotNull String path) throws IOException {
        try {
            return findPsiDirectory(project, path);
        } catch (BusinessException e) {
            VirtualFile directory = VfsUtil.createDirectoryIfMissing(path);
            if (Objects.isNull(directory)) {
                ExceptionEnum.DIR_CREATE_EXCEPTION.asBusinessException(path);
            }
            assert directory != null;
            return PsiManager.getInstance(project).findDirectory(directory);
        }
    }

    /**
     * 格式化virtualFile,需要在WriteCommandAction.runWriteCommandAction中调用
     */
    public static void reformatFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {

        // 获取 PsiManager
        PsiManager psiManager = PsiManager.getInstance(project);

        // 将 virtualFile 转换为 PsiFile
        PsiFile psiFile = psiManager.findFile(virtualFile);

        if (psiFile != null) {
            reformatFile(project, psiFile);
        }
    }

    /**
     * 格式化文件,需要在WriteCommandAction.runWriteCommandAction中调用
     */
    public static void reformatFile(@NotNull Project project, @NotNull PsiFile psiFile) {
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        CodeStyle.getSettings(project).getIndentOptionsByFile(psiFile).USE_TAB_CHARACTER = CodeStyle.getSettings(project).getIndentOptions().USE_TAB_CHARACTER;

        codeStyleManager.reformat(psiFile);
    }


    /**
     * 根据绝对路径获取本地文件内容
     */
    public static String getLocalFileContent(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            LOGGER.warn("获取本地文件内容失败", e);
            return "";
        }
    }


    /**
     * 根据文件绝对路径获取当前项目文件内容
     */
    public static String getFileContent(String path) throws IOException, BusinessException {
        return getFileContent(path, null, null);
    }

    /**
     * 根据文件绝对路径获取当前项目文件内容，支持按行读取
     */
    public static String getFileContent(String path, Integer startLine, Integer endLine) throws IOException, BusinessException {
        Project currentProject = ApplicationUtil.findCurrentProject();
        if (Objects.isNull(currentProject)) {
            ExceptionEnum.PROJECT_NOT_EXISTS_EXCEPTION.asBusinessException();
        }

        VirtualFile virtualFile = findVirtualFile(path);
        final Integer finalStartLine = startLine;
        final Integer finalEndLine = endLine;

        assert currentProject != null;
        return ReadAction.compute(() -> {
            FileEditor[] fileEditors = FileEditorManager.getInstance(currentProject).getAllEditors(virtualFile);
            if (fileEditors.length > 0 && fileEditors[0] instanceof TextEditor) {
                Document document = ((TextEditor) fileEditors[0]).getEditor().getDocument();
                return getContentByLineRange(document, finalStartLine, finalEndLine);
            }

            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document != null) {
                return getContentByLineRange(document, finalStartLine, finalEndLine);
            }

            //文件没有活动editor，则读取文件内容
            try {
                String content = new String(virtualFile.contentsToByteArray(), StandardCharsets.UTF_8);
                if (finalStartLine == null || finalEndLine == null) {
                    return content;
                }

                String[] lines = content.split("\n", -1);
                int effectiveStartLine = finalStartLine < 1 ? 1 : finalStartLine;
                int effectiveEndLine = finalEndLine > lines.length ? lines.length : finalEndLine;
                if (effectiveStartLine > effectiveEndLine) return "";

                return String.join("\n", Arrays.copyOfRange(lines, effectiveStartLine - 1, effectiveEndLine));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 根据行范围获取文档内容
     */
    private static String getContentByLineRange(Document document, Integer startLine, Integer endLine) {
        if (startLine == null || endLine == null) {
            return document.getText();
        }

        if (startLine < 1) startLine = 1;
        if (endLine > document.getLineCount()) endLine = document.getLineCount();
        if (startLine > endLine) return "";

        int startOffset = document.getLineStartOffset(startLine - 1);
        int endOffset = document.getLineEndOffset(endLine - 1);
        return document.getText(new TextRange(startOffset, endOffset));
    }


    /**
     * 按行读取文件内容
     */
    public static List<String> readFile(@NotNull String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            LOGGER.error("读取文件失败", e);
            return Collections.emptyList();
        }
    }


    /**
     * 获取插件中目录的绝对路径
     *
     * @param directoryName 目录名
     */
    @Nullable
    public static Path findPluginDirectory(@NotNull String directoryName) {
        Path envPath;
        Path basePath = PluginUtil.getPluginBasePath();
        envPath = basePath.resolve(directoryName);
        if (Files.exists(envPath)) {
            return envPath;
        } else {
            LOGGER.error("Unable to locate the directory path in base path: " + directoryName);
            return null;
        }
    }

    /**
     * 根据virtualFile获取文件相对路径
     */
    public static String getRelativePath(VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        return getRelativePathByBasePath(path);

    }

    public static String getRelativePathByBasePath(String path) {

        String basePath = ApplicationUtil.findCurrentProject().getBasePath();
        if (basePath == null) {
            return "";
        }
        if (path.startsWith(basePath)) {
            return path.substring(basePath.length());
        }
        return path;
    }


    public static VirtualFile findFileInProjectByRelativePath(Project project, String relativePath) {
        // 获取项目的根目录路径
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return null;
        }

        // 构造文件的绝对路径
        String absoluteFilePath = projectBasePath + File.separator + relativePath;

        // 尝试找到文件的VirtualFile对象
        return LocalFileSystem.getInstance().findFileByPath(absoluteFilePath);
    }

    /**
     * 根据psiFile返回最匹配的语言
     */
    public static @NotNull Language findBestLanguage(@NotNull PsiFile file) {


        Language language = file.getLanguage();
        if (language == Language.ANY && file.getFileType() instanceof LanguageFileType) {
            language = ((LanguageFileType) file.getFileType()).getLanguage();
        }

        return language;
    }

    /**
     * 根据文件返回最匹配的语言
     */
    public static @NotNull String findBestLanguage(@NotNull VirtualFile file) {

        FileType fileType = file.getFileType();
        if (fileType instanceof LanguageFileType) {
            return ((LanguageFileType) fileType).getLanguage().getDisplayName();
        }

        return getFileExtension(file.getName());
    }


    /**
     * 根据插件相对路径获取 插件中的文件内容
     */
    public static String getResource(String name) {
        try (var stream = Objects.requireNonNull(FileUtil.class.getResourceAsStream(name))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read resource", e);
        }
    }

    /**
     * 获取文件后缀名
     */
    public static String getFileExtension(String filename) {
        Pattern pattern = Pattern.compile("[^.]+$");
        Matcher matcher = pattern.matcher(filename);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * 获取文件类型
     */
    @NotNull
    public static FileType getFileType(String fileName) {
        // 获取 FileTypeManager 实例
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        // 通过文件扩展名获取 FileType
        String fileExtension = getFileExtension(fileName);
        if (StringUtils.isNotEmpty(fileExtension)) {
            return fileTypeManager.getFileTypeByExtension(fileExtension);
        }
        return FileTypes.UNKNOWN;
    }

    /**
     * 根据文件名获取图标
     */
    public static Icon getIconByFileName(String fileName) {
        FileType fileType = getFileType(fileName);
        if (FileTypes.UNKNOWN.equals(fileType) || Objects.isNull(fileType.getIcon())) {
            return AllIcons.FileTypes.Text;
        }

        return fileType.getIcon();
    }

    /**
     * 判断文件路径在当前项目中是否存在
     *
     * @param filePath 文件绝对路径
     * @return 如果文件存在返回 true，否则返回 false
     */
    public static boolean isFileExist(String filePath) {
        // 获取 LocalFileSystem 实例
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        // 通过文件路径获取 VirtualFile
        VirtualFile virtualFile = localFileSystem.findFileByPath(filePath);
        // 检查 VirtualFile 是否存在
        return virtualFile != null && virtualFile.exists();
    }


    /**
     * 根据文件绝对路径获取 PsiFile 对象
     *
     * @param project      IntelliJ IDEA 项目
     * @param absolutePath 文件的绝对路径
     * @return 对应的 PsiFile 对象，如果文件不存在则返回 null
     */
    public static PsiFile getPsiFileFromAbsolutePath(Project project, String absolutePath) {
        try {
            return convertVirtualFileToPsiFile(project, findVirtualFile(absolutePath));
        } catch (Exception e) {
            LOGGER.warn("获取 PsiFile 失败", e);
        }
        return null;
    }


    /**
     * 创建项目文件
     */
    public static boolean createProjectFile(@NotNull String path,
                                            @NotNull Project project,
                                            @Nullable String content) {
        return createProjectFile(path, project, getFileName(path), getFileType(path), Objects.isNull(content) ? "" : content);
    }

    /**
     * 创建项目文件
     */
    public static boolean createProjectFile(@NotNull String path,
                                            @NotNull Project project,
                                            @NotNull String fileName,
                                            @NotNull FileType fileType,
                                            @NotNull String content) {
        String dir = path.substring(0, path.lastIndexOf(File.separator));
        try {
            PsiDirectory psiDirectory = findPsiDirectoryOrCreate(project, dir);
            if (Objects.isNull(psiDirectory)) {
                throw new BusinessException("创建文件夹失败:" + dir);
            }
            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, fileType, content);
            psiDirectory.add(psiFile);
            return true;

        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
    }


    /**
     * 将内存中的VirtualFile根据路径保存到磁盘
     */
    public static boolean insertFile(@NotNull Project project,
                                     @NotNull VirtualFile virtualFile,
                                     @NotNull String path) {
        return insertFile(project, virtualFile, path, !isFileExist(path));
    }

    /**
     * 将内存中的VirtualFile根据路径保存到磁盘
     */
    public static boolean insertFile(@NotNull Project project,
                                     @NotNull VirtualFile virtualFile,
                                     @NotNull String path,
                                     @NotNull Boolean isNewFile) {
        String content = ReadAction.compute(() -> Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(virtualFile)).getText());
        return insertFile(project, virtualFile.getName(), virtualFile.getFileType(), content, path, isNewFile);
    }


    /**
     * 将内存中的VirtualFile根据路径保存到磁盘
     */
    public static boolean insertFile(@NotNull Project project,
                                     @NotNull String fileName,
                                     @NotNull FileType fileType,
                                     @NotNull String content,
                                     @NotNull String path,
                                     @NotNull Boolean isNewFile) {

        EditorSettingsExternalizable settings = EditorSettingsExternalizable.getInstance();
        if (settings.isEnsureNewLineAtEOF()) {
            settings.setEnsureNewLineAtEOF(false);
        }

        return (boolean) WriteCommandAction.runWriteCommandAction(project, (Computable<Object>) () -> {
            if (isNewFile) {
                return createProjectFile(path, project, fileName, fileType, content);
            } else {
                VirtualFile originFile = findVirtualFile(path);
                try {
                    changeFileContent(project, originFile, content, false);
                } catch (Exception e) {
                    LOGGER.error(e);
                    return false;
                }
                return true;

            }
        });

    }


    /**
     * 修改文件内容,注意需要在 WriteCommandAction.runWriteCommandAction 中调用
     */
    private static void changeFileContent(@NotNull Project project, @NotNull VirtualFile originFile, @NotNull String newContent, @NotNull Boolean reformat) {

        Document document = FileDocumentManager.getInstance().getDocument(originFile);
        assert document != null;
        document.setText(newContent);
        FileDocumentManager.getInstance().saveDocument(document);
        PsiDocumentManager.getInstance(project).commitDocument(document);
        if (reformat) {
            FileUtil.reformatFile(project, FileUtil.convertVirtualFileToPsiFile(project, originFile));
        }
    }


    /**
     * 通过base64编码的图片内容下载图片
     */
    public static void downloadPic(Project project, String path, String base64Content) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading...", false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    byte[] bytes = Base64.getDecoder().decode(base64Content);
                    FileUtil.writeFile(path, bytes);
                    String notifyContent = "<li><a href=\"" + path + "\">" + path + "</a></li></br>";
                    project.getService(NotifyServiceImpl.class).createFileLinkNotification("下载成功", notifyContent, NotificationType.INFORMATION);
                } catch (Exception e) {
                    LOGGER.error("downloading failed...", e);
                }
            }
        });

    }

    /**
     * 写入文件
     */
    public static Path writeFile(String dir, String fileName, String content) {
        return writeFile(dir + File.separator + fileName, content);
    }


    /**
     * 写入文件
     */
    public static Path writeFile(String absPath, String content) {
        try {

            Path filePath = createFile(absPath);
            Files.writeString(filePath, content);
            return filePath.toAbsolutePath();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to resolve path " + absPath, ex);
        }
    }

    /**
     * 写入文件
     */
    public static Path writeFile(String absPath, byte[] content) {
        try {

            Path filePath = createFile(absPath);
            Files.write(filePath, content);
            return filePath.toAbsolutePath();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to resolve path " + absPath, ex);
        }
    }

    /**
     * 创建文件
     */
    private static Path createFile(String absPath) throws IOException {
        Path filePath = Paths.get(absPath);
        Path dirPath = filePath.getParent();
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        if (!SystemInfo.isWindows) {
            Files.createFile(filePath, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        }
        return filePath;
    }

    /**
     * 是否在过滤列表里
     */
    public static boolean filterByExtension(String extension) {
        return excludeExtensionList.contains(extension);
    }


    /**
     * 根据url打开文件
     */
    public static void openFileByUrl(@NotNull Project project, @NotNull String title, @NotNull String url, @Nullable FileType fileType) {
        try {
            byte[] bytesFromUrl = UrlUtil.getBytesFromUrl(url);
            if (Objects.nonNull(bytesFromUrl)) {
                openBinaryLightVirtualFile(project, title, bytesFromUrl, fileType);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("打开文件%s失败", url), e);
        }
    }

    /**
     * 根据url打开图片
     */
    public static void openImageByUrl(@NotNull Project project, @NotNull String title, @NotNull String url) {
        openFileByUrl(project, title, url, null);
    }


    /**
     * 根据base64编码的图片内容打开图片
     */
    public static void openImageByBase64(@NotNull Project project, @NotNull String title, @NotNull String content) {
        openBinaryLightVirtualFile(project, title, Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)), null);
    }


    /**
     * 打开临时二进制文件
     */
    public static void openBinaryLightVirtualFile(@NotNull Project project, @NotNull String title, @NotNull byte[] content, @Nullable FileType fileType) {
        // 3. 创建一个 BinaryLightVirtualFile，给它一个文件名
        BinaryLightVirtualFile virtualFile = new BinaryLightVirtualFile(title, content);
        if (Objects.nonNull(fileType)) {
            virtualFile.setFileType(fileType);
        }

        // 5. 使用 FileEditorManager 打开文件
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(virtualFile, true);
    }


    /**
     * bfs遍历文件夹
     */
    public static ListFileResultDTO bfsDirectory(@NotNull String path, @NotNull Boolean recursive, @NotNull Integer limit) {
        Set<String> filePaths = Sets.newHashSet();
        Path startPath = Paths.get(path);
        AtomicBoolean limitReached = new AtomicBoolean(false);

        try {
            Files.walkFileTree(startPath, new FileVisitor<>() {
                private int currentDepth = 0;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!recursive && !dir.equals(startPath)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    if (currentDepth > limit) {
                        limitReached.set(true);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    currentDepth++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    filePaths.add(file.toAbsolutePath().toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    currentDepth--;
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            LOGGER.error("遍历文件夹失败", e);
        }

        return ListFileResultDTO.builder().limitReached(limitReached.get()).files(filePaths).build();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class ListFileResultDTO {

    private Set<String> files;

    @JSONField(name = "limit_reached")
    private Boolean limitReached;

}
