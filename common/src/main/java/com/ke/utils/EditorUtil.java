package com.ke.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.ke.exception.BusinessException;
import com.ke.exception.ExceptionEnum;
import com.ke.webview.WebViewManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EditorUtil {

	public static SelectionModel getSelectionModel(Project project) throws BusinessException {

		Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
		if (Objects.isNull(editor)) {
			ExceptionEnum.EDITOR_IS_NULL_EXCEPTION.asBusinessException();
		}
		return editor.getSelectionModel();
	}


	public static String getFileName(Project project) {
		FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
		String fileName = "";
		if (selectedFiles.length > 0) {
			fileName = selectedFiles[0].getName();
		}
		return fileName;
	}


	public static String getSelectedContent(Project project) {
		SelectionModel selectionModel = getSelectionModel(project);
		return selectionModel.getSelectedText();
	}

	public static String getFileExtension(Project project) {
		return FileUtil.getFileExtension(Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getVirtualFile().getName());
	}


	public static String getLanguage(Project project) {
		return getLanguage(FileEditorManager.getInstance(project).getSelectedTextEditor());
	}

	public static String getLanguage(Editor editor) {
		if (Objects.isNull(editor)) {
			return "";
		}
		Document document = editor.getDocument();
		PsiFile psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(document);
		assert psiFile != null;
		return FileUtil.findBestLanguage(psiFile).getDisplayName();
	}


	public static boolean isSelectedFileEditor(@NotNull Editor editor) {

		Project project = editor.getProject();
		if (project != null && !project.isDisposed()) {
			FileEditorManager editorManager = FileEditorManager.getInstance(project);
			if (editorManager == null) {
				return false;
			} else if (editorManager instanceof FileEditorManagerImpl) {
				Editor current = ((FileEditorManagerImpl) editorManager).getSelectedTextEditor(true);
				return current != null && current.equals(editor);
			} else {
				FileEditor current = editorManager.getSelectedEditor();
				return current instanceof TextEditor && editor.equals(((TextEditor) current).getEditor());
			}
		} else {
			return false;
		}
	}

	public static void openFile(String fileName, String fileContent, Project project) {
		VirtualFile file = new LightVirtualFile(fileName, fileContent);
		ApplicationManager.getApplication().invokeLater(() -> {
			FileEditor[] fileEditors = FileEditorManager.getInstance(project).openFile(file, true);
			if(fileEditors.length > 0) {
				project.getService(WebViewManager.class).transferFocus(fileEditors[0].getComponent());
			}
		});
	}

	public static int getLineNumber(@NotNull Editor editor, int offset) {
		return getLineNumber(editor.getDocument(), offset);
	}

	public static int getLineNumber(@NotNull Document document, int offset) {
		return document.getLineNumber(offset);
	}

	public static int getLineStartOffsetFromOffset(@NotNull Editor editor, int offset) {
		return editor.getDocument().getLineStartOffset(getLineNumber(editor, offset));
	}

	public static int getLineStartOffsetFromLine(@NotNull Editor editor, int line) {
		return editor.getDocument().getLineStartOffset(line);
	}


    public static String getVirtualFilePathByEditAndPsiFile(Editor editor, PsiFile psiFile) {
        try {
            VirtualFile virtualFile = editor.getVirtualFile();
            if (virtualFile == null) {
                // 尝试通过 PsiFile 获取 VirtualFile
                virtualFile = psiFile.getVirtualFile();
                if (virtualFile == null) {
                    // 尝试通过 Document 获取 VirtualFile
                    Document document = editor.getDocument();
                    virtualFile = FileDocumentManager.getInstance().getFile(document);
                    if (virtualFile == null) {
                        return "";
                    }
                    return FileUtil.getRelativePathByBasePath(((LightVirtualFile) virtualFile).getOriginalFile().getPath());
                }
            }
            return FileUtil.getRelativePath(virtualFile);
        } catch (Exception e) {
            return "";
        }
    }
}
