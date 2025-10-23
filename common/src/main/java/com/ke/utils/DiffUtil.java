package com.ke.utils;

import com.intellij.application.options.CodeStyle;
import com.intellij.diff.DiffContentFactoryImpl;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.EmptyContent;
import com.intellij.diff.contents.FileDocumentContentImpl;
import com.intellij.diff.editor.ChainDiffVirtualFile;
import com.intellij.diff.editor.DiffEditorTabFilesManager;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.ke.Bundle;
import com.ke.diff.UnifiedDiffRequestChainProcessor;
import com.ke.diff.UnifiedDiffWindow;
import com.ke.editor.DiffMode;
import com.ke.service.notify.NotifyServiceImpl;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/25 16:41
 * @Version 1.0
 * @Description 显示Diff的工具类
 * 设置Diff策略 可以参考TextDiffSettingsHolder.TextDiffSettings.getSettings().setIgnorePolicy(IgnorePolicy.TRIM_WHITESPACES);
 */
public class DiffUtil {

    /**
     * 展示ai生成文档和原文档的对比,后台进程掉调起
     */
    public static void showDiff(@NotNull AnActionEvent e,
                                @NotNull DiffMode diffMode,
                                @NotNull String newContent,
                                @Nullable String indentStr,
                                int startOffset,
                                int endOffset) {
        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {

            Editor editor = FileEditorManager.getInstance(Objects.requireNonNull(e.getProject())).getSelectedTextEditor();
            VirtualFile originalFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

            boolean useTab = CodeStyle.getSettings(e.getProject()).getIndentOptionsByFile(e.getData(CommonDataKeys.PSI_FILE)).USE_TAB_CHARACTER;

            if (Objects.nonNull(originalFile) && Objects.nonNull(editor)) {

                Document document = editor.getDocument();

                DiffRequestFactory diffRequestFactory = DiffRequestFactory.getInstance();

                Document tempDocument = getReformatDocument(e.getProject(), diffMode, editor.getDocument().getText(), newContent, indentStr, startOffset, endOffset, useTab, originalFile.getFileType());

                FileDocumentContentImpl content1 = new FileDocumentContentImpl(e.getProject(), document, originalFile, originalFile);
                DocumentContent content2 = DiffContentFactoryImpl.getInstanceEx().create(e.getProject(), tempDocument.getText(), originalFile.getFileType());

                DiffManager.getInstance().showDiff(e.getProject(), createDiffRequest(Bundle.get("action.editor.doc.title", originalFile.getName()),
                                                                                     diffRequestFactory.getContentTitle(originalFile),
                                                                                     Bundle.get("action.editor.doc.title2"),
                                                                                     content1,
                                                                                     content2));
            }
        });
    }

    /**
     * 输入新文件路径和内容，判断是新增还是修改文件后展示
     */
    public static void showDiff(@NotNull Project project,
                                @NotNull String newFileName,
                                @NotNull String content,
                                @NotNull String path,
                                @NotNull String dialogTitle) {
        if (FileUtil.isFileExist(path)) {
            showDiffContent(project, newFileName, content, path, dialogTitle);
        } else {
            showNewContent(project, newFileName, content, path, dialogTitle);
        }
    }

    /**
     * 输入字符串,展示新生成的文档
     */
    public static void showNewContent(@NotNull Project project,
                                      @NotNull String newFileName,
                                      @NotNull String newContent,
                                      @NotNull String newFilePath,
                                      @NotNull String dialogTitle) {

        DiffManager.getInstance().showDiff(project, createNewContentDiffRequest(project, newFileName, newContent, newFilePath, dialogTitle));
    }

    /**
     * 输入字符串和文件路径，展示新文档文档和原文档的对比,UI进程调起
     */
    public static void showDiffContent(@NotNull Project project,
                                       @NotNull String newFileName,
                                       @NotNull String newContent,
                                       @NotNull String originalFilePath,
                                       @NotNull String dialogTitle) {
        SimpleDiffRequest diffRequest = createDiffRequest(project, newFileName, newContent, originalFilePath, dialogTitle);
        if (Objects.nonNull(diffRequest)) {
            DiffManager.getInstance().showDiff(project, diffRequest);
            return;
        }
        project.getService(NotifyServiceImpl.class).error("origin file not found");

    }


    /**
     * 输入字符串和文件路径，展示新文档文档和原文档的对比,UI进程调起
     */
    public static void showDiffContent(@NotNull Project project,
                                       @NotNull String newFileName,
                                       @NotNull String newContent,
                                       @NotNull String originalFilePath,
                                       @NotNull String dialogTitle,
                                       @NotNull String rightTitle) {
        SimpleDiffRequest diffRequest = createDiffRequest(project, newFileName, newContent, originalFilePath, dialogTitle, rightTitle);
        if (Objects.nonNull(diffRequest)) {
            DiffManager.getInstance().showDiff(project, diffRequest);
            return;
        }
        project.getService(NotifyServiceImpl.class).error("origin file not found");

    }


    /**
     * 输入字符串和文件路径，展示新文档文档和原文档的对比,UI进程调起
     * 弹窗展示，默认打开为单边diff
     */
    public static void showUnifiedDiffContent(@NotNull Project project,
                                              @NotNull String fileName,
                                              @NotNull String diffContent,
                                              @NotNull String filePath,
                                              @NotNull String dialogTitle,
                                              @NotNull String rightTitle,
                                              @NotNull Boolean changed) {
        SimpleDiffRequest diffRequest = createDiffRequest(project, fileName, diffContent, filePath, dialogTitle, rightTitle, changed);
        if (Objects.nonNull(diffRequest)) {
            DiffRequestChain requestChain = new SimpleDiffRequestChain(diffRequest);
            (new UnifiedDiffWindow(project, requestChain, DiffDialogHints.DEFAULT)).show();
            return;
        }
        project.getService(NotifyServiceImpl.class).error("origin file not found");

    }

    /**
     * 输入字符串和文件路径，展示新文档文档和原文档的对比,UI进程调起
     * 弹窗展示，默认打开为单边diff
     */
    public static void showUnifiedDiffContentInEditor(@NotNull Project project,
                                                      @NotNull String fileName,
                                                      @NotNull String diffContent,
                                                      @NotNull String filePath,
                                                      @NotNull String dialogTitle,
                                                      @NotNull String rightTitle,
                                                      @NotNull Boolean changed) {
        // 关闭指定fileName的diff窗口
        closeSpecificDiffWindow(project, fileName);

        SimpleDiffRequest diffRequest = createDiffRequest(project, fileName, diffContent, filePath, dialogTitle, rightTitle,changed);
        if (Objects.nonNull(diffRequest)) {
            DiffRequestChain requestChain = new SimpleDiffRequestChain(diffRequest);
            ChainDiffVirtualFile diffFile = new ChainDiffVirtualFile(requestChain, fileName) {
                @NotNull
                @Override
                public DiffRequestProcessor createProcessor(@NotNull Project project) {
                    return new UnifiedDiffRequestChainProcessor(project, requestChain);
                }
            };
            DiffEditorTabFilesManager.getInstance(project).showDiffFile(diffFile, true);
            ComponentUtil.getCodeLinkToolWindow(project).hide();
            ComponentUtil.getCodeLinkToolWindow(project).show();
            return;
        }
        project.getService(NotifyServiceImpl.class).error("origin file not found");
    }


    private static void closeSpecificDiffWindow(@NotNull Project project, @NotNull String fileName) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Arrays.stream(fileEditorManager.getOpenFiles())
                .filter(file -> file instanceof ChainDiffVirtualFile)
                .filter(file -> {
                    String diffFileName = file.getName();
                    // 更精确的匹配逻辑
                    return FilenameUtils.equalsNormalized(diffFileName, fileName) ||
                            FilenameUtils.wildcardMatch(diffFileName, "*" + fileName + "*");
                })
                .forEach(fileEditorManager::closeFile);
    }


    /**
     * 生成临时文档，根据diffMode的不同，插入或替换原始文本
     */
    private static Document getReformatDocument(@NotNull Project project,
                                                @NotNull DiffMode diffMode,
                                                @NotNull String originalText,
                                                @NotNull String newContent,
                                                @Nullable String indentStr,
                                                int startOffset,
                                                int endOffset,
                                                boolean useTab,
                                                @NotNull FileType fileType) {

        DocumentImpl tempDocument = new DocumentImpl(originalText);

        if (diffMode.equals(DiffMode.INSERT)) {
            if (StringUtils.isNotEmpty(indentStr)) {
                tempDocument.insertString(startOffset, newContent + "\n" + indentStr);
            } else {
                tempDocument.insertString(startOffset, newContent + "\n");
            }
        } else {
            tempDocument.replaceString(startOffset, endOffset, newContent);
        }

        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("action.editor.doc.title2", fileType, tempDocument.getText());

        return getReformatDocument(project, startOffset, startOffset + newContent.length(), useTab, tempDocument, psiFile);
    }


    /**
     * 根据字符串生成临时文档,格式化根据系统设置
     */
    public static Document getReformatDocument(@NotNull Project project,
                                               @NotNull String newFileName,
                                               @NotNull String newContent,
                                               @NotNull FileType fileType) {

        return getReformatDocument(project, newFileName, newContent, fileType, CodeStyle.getSettings(project).getIndentOptions().USE_TAB_CHARACTER);
    }


    /**
     * 根据字符串生成临时文档,格式化根据useTab设置
     */
    public static Document getReformatDocument(@NotNull Project project,
                                               @NotNull String newFileName,
                                               @NotNull String newContent,
                                               @NotNull FileType fileType,
                                               @NotNull Boolean useTab) {

        DocumentImpl tempDocument = new DocumentImpl(newContent);

        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(newFileName, fileType, tempDocument.getText());

        return getReformatDocument(project, 0, newContent.length(), useTab, tempDocument, psiFile);
    }

    /**
     * 获取根据文件类型格式化后的临时文档
     */
    @NotNull
    private static Document getReformatDocument(@NotNull Project project, int startOffset, int endOffset, boolean useTab, DocumentImpl tempDocument, PsiFile psiFile) {
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        CodeStyle.getSettings(psiFile.getProject()).getIndentOptionsByFile(psiFile).USE_TAB_CHARACTER = useTab;

        codeStyleManager.reformatText(psiFile, startOffset, endOffset);

        WriteCommandAction.runWriteCommandAction(project, (Computable<Object>) () -> {
            tempDocument.setText(psiFile.getText());
            return true;
        });

        tempDocument.setReadOnly(true);
        return tempDocument;
    }


    /**
     * 创建DiffRequest,修改模式
     */
    public static SimpleDiffRequest createDiffRequest(@NotNull Project project,
                                                      @NotNull String newFileName,
                                                      @NotNull String newContent,
                                                      @NotNull String originalFilePath,
                                                      @NotNull String dialogTitle) {
        return createDiffRequest(project, newFileName, newContent, originalFilePath, dialogTitle, newFileName + "(\uD83D\uDE00CodeLink Generate)");
    }


    /**
     * 创建DiffRequest,修改模式
     */
    public static SimpleDiffRequest createDiffRequest(@NotNull Project project,
                                                      @NotNull String newFileName,
                                                      @NotNull String newContent,
                                                      @NotNull String originalFilePath,
                                                      @NotNull String dialogTitle,
                                                      @NotNull String rightTitle) {

        return createDiffRequest(project, newFileName, newContent, originalFilePath, dialogTitle, rightTitle, false);

    }


    /**
     * 创建DiffRequest,修改模式
     */
    public static SimpleDiffRequest createDiffRequest(@NotNull Project project,
                                                      @NotNull String fileName,
                                                      @NotNull String diffContent,
                                                      @NotNull String filePath,
                                                      @NotNull String dialogTitle,
                                                      @NotNull String rightTitle,
                                                      @NotNull Boolean changed) {
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(filePath.replace(File.separatorChar, '/')));
        if (Objects.isNull(virtualFile)) {
            return null;
        }

        boolean useTab = CodeStyle.getSettings(project).getIndentOptionsByFile(FileUtil.convertVirtualFileToPsiFile(project,virtualFile)).USE_TAB_CHARACTER;


        Document fileDocument = FileDocumentManager.getInstance().getDocument(virtualFile);

        DiffRequestFactory diffRequestFactory = DiffRequestFactory.getInstance();

        Document tempDocument = getReformatDocument(project, fileName, diffContent, FileUtil.getFileType(fileName), useTab);

        assert fileDocument != null;
        DocumentContent originalContent;
        DocumentContent changedContent;
        if (!changed) {
            originalContent = new FileDocumentContentImpl(project, fileDocument, virtualFile, virtualFile);
            changedContent = DiffContentFactoryImpl.getInstanceEx().create(project, tempDocument.getText(), virtualFile.getFileType());
        } else {
            originalContent = DiffContentFactoryImpl.getInstanceEx().create(project, tempDocument.getText(), virtualFile.getFileType());
            changedContent = new FileDocumentContentImpl(project, fileDocument, virtualFile, virtualFile);
        }

        String leftTitle = diffRequestFactory.getContentTitle(virtualFile);

        return createDiffRequest(dialogTitle, leftTitle, rightTitle, originalContent, changedContent);

    }


    /**
     * 创建DiffRequest,修改模式
     */
    public static SimpleDiffRequest createDiffRequest(@NotNull String dialogTile,
                                                      @NotNull String leftTitle,
                                                      @NotNull String rightTitle,
                                                      @NotNull DiffContent originContent,
                                                      @NotNull DiffContent newContent) {
        return new SimpleDiffRequest(dialogTile,
                                     originContent,
                                     newContent,
                                     leftTitle,
                                     rightTitle);

    }


    /**
     * 创建DiffRequest,新增模式
     */
    public static SimpleDiffRequest createNewContentDiffRequest(@NotNull Project project,
                                                                @NotNull String newFileName,
                                                                @NotNull String newContent,
                                                                @NotNull String newFilePath,
                                                                @NotNull String dialogTitle) {


        FileType fileType = FileUtil.getFileType(newFileName);

        Document tempDocument = getReformatDocument(project, newFileName, newContent, fileType);

        DocumentContent content = DiffContentFactoryImpl.getInstanceEx().create(project, tempDocument.getText(), fileType);

        return createDiffRequest(dialogTitle, newFilePath + "(\uD83D\uDE00CodeLink Generate)", content);
    }


    /**
     * 创建DiffRequest,新增模式
     */
    public static SimpleDiffRequest createDiffRequest(@NotNull String dialogTile,
                                                      @NotNull String newFileTitle,
                                                      @NotNull DiffContent newContent) {
        return new SimpleDiffRequest(dialogTile,
                                     new EmptyContent(),
                                     newContent,
                                     "",
                                     newFileTitle);

    }
}
