package com.ke.stacktrace;

import com.intellij.execution.filters.FileHyperlinkInfo;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.ke.utils.PsiUtils;
import com.ke.webview.communication.handler.ptw.InlineChatPTWHandler;
import com.ke.stacktrace.entity.CodePlace;
import com.ke.stacktrace.entity.ErrorAIExplainPrompt;
import com.ke.stacktrace.entity.ErrorPlace;
import com.ke.stacktrace.entity.SelectedTraceback;
import com.ke.webview.dto.InlineChatDTO;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RuntimeErrorAIExplanationProvider {
	// 给定堆栈文本 text，调用模型接口，返回解释文本

	private final Project project;
	private final Editor editor;
	private final String tracebackText;

	private final Integer consoleLineFrom;

	private final Integer consoleLineTo;

	private String language = "";

	// 一个异常文件的最大token数
	private final int CODE_MAX_TOKENS = 200;


	public RuntimeErrorAIExplanationProvider(Project project, Editor editor, SelectedTraceback selectedTraceback) {
		this.project = project;
		this.editor = editor;
		this.tracebackText = selectedTraceback.getTraceback();
		this.consoleLineFrom = selectedTraceback.getLineFrom();
		this.consoleLineTo = selectedTraceback.getLineTo();

	}

	public void run() {
		String prompt = this.getPrompt().getPrompt();
		InlineChatPTWHandler.sendInlineChat(InlineChatDTO.builder().question(prompt).build(), project);
	}


	private ErrorAIExplainPrompt getPrompt() {
		RangeHighlighter[] highlighters = editor.getMarkupModel().getAllHighlighters();
		// 先获取高亮文本。再检查高亮文本是不是文件超链接，再判断是不是项目文件。

		// 获取当前editor的全部文本偏移量
		TextRange textRange = new TextRange(editor.getDocument().getLineStartOffset(consoleLineFrom), editor.getDocument().getLineEndOffset(consoleLineTo != null ? consoleLineTo : editor.getDocument().getLineCount() - 1));

		List<CodePlace> result = new ArrayList<>();
		for (RangeHighlighter rangeHighlighter : highlighters) {
			TextRange highlighterTextRange = new TextRange(rangeHighlighter.getStartOffset(), rangeHighlighter.getEndOffset());

			// 如果不在editor的全部文本偏移量内，就跳过
			if (!textRange.contains(highlighterTextRange)) {
				continue;
			}
			ErrorPlace errorPlace = this.extractErrorPlaceFromHighlighter(tracebackText, rangeHighlighter, project);

			// 检查是不是项目中自己写的文件
			if (errorPlace == null || !errorPlace.isProjectFile()) {
				continue;
				// result.add(errorPlace);
			}

			PsiElement containingElement = errorPlace.findContainingElement();
			if (containingElement == null) {
				continue;
			}

			if (StringUtils.isEmpty(language)) {
				language = containingElement.getLanguage().getDisplayName();
			}

			CodePlace codePlace = findEnclosingScopeGreedy(errorPlace, CODE_MAX_TOKENS, containingElement.getLanguage().getDisplayName());

			if (Objects.nonNull(codePlace)) {
				result.add(codePlace);
			}

		}

		return ErrorAIExplainPrompt.builder().codePlaces(result).language(language).tracebackText(tracebackText).build();
	}


	private @Nullable CodePlace tryFitContainingElement(String filename, PsiElement currentContainingElement, int maxTokenCount, String language, VirtualFile virtualFile) {
		int lineNumberStart = PsiUtils.getLineNumber(currentContainingElement, true);
		int lineNumberFinish = PsiUtils.getLineNumber(currentContainingElement, false);
		String prefix = "filename: " + filename + "\n  startLine: " + (lineNumberStart + 1) + "\n\n";
		String candidate;
		try {
			Document document = currentContainingElement.getContainingFile().getViewProvider().getDocument();
			int startOffset = document.getLineStartOffset(lineNumberStart);
			candidate = prefix + "```" + language + "\n" + document.getText(new TextRange(startOffset, currentContainingElement.getTextRange().getEndOffset())) + "\n```\n";

		} catch (Exception ignore) {
			candidate = prefix + "```" + language + "\n" + currentContainingElement.getText() + "\n```\n";
		}

		if (candidate.length() < maxTokenCount) {
			return new CodePlace(lineNumberStart, lineNumberFinish, candidate, virtualFile);
		}

		return null;
	}

	private CodePlace findEnclosingScopeGreedy(ErrorPlace errorPlace, int maxTokenCount, String language) {
		CodePlace result = null;

		PsiElement currentContainingElement = errorPlace.findContainingElement();

		while (!(currentContainingElement instanceof PsiFile) && currentContainingElement != null) {
			CodePlace codePlace = this.tryFitContainingElement(errorPlace.getHyperlinkText(), currentContainingElement, maxTokenCount, language, errorPlace.getVirtualFile());
			if (codePlace == null) {
				break;
			}
			result = codePlace;
			currentContainingElement = currentContainingElement.getParent();
		}
		return result;
	}

	/*
	 * 获取高亮文本
	 */
	private @Nullable FileHyperlinkInfo getFileHyperlinkInfo(RangeHighlighter rangeHighlighter) {
		HyperlinkInfo hyperlinkInfo = EditorHyperlinkSupport.getHyperlinkInfo(rangeHighlighter);
		return hyperlinkInfo instanceof FileHyperlinkInfo ? (FileHyperlinkInfo) hyperlinkInfo : null;
	}

	private @Nullable ErrorPlace extractErrorPlaceFromHighlighter(String consoleText, RangeHighlighter highlighter, Project project) {
		FileHyperlinkInfo fileHyperlinkInfo = this.getFileHyperlinkInfo(highlighter);
		if (fileHyperlinkInfo == null) {
			return null;
		}
		OpenFileDescriptor openFileDescriptor = fileHyperlinkInfo.getDescriptor();
		if (openFileDescriptor == null) {
			return null;
		}

		VirtualFile virtualFile = openFileDescriptor.getFile();

		// 问题代码行数
		int lineNumber = Math.max(0, openFileDescriptor.getLine());

		// 兜底,Pycharm有时行数取得不对
		if (lineNumber == 0) {
			try {
				lineNumber = openFileDescriptor.getRangeMarker().getDocument().getLineNumber(openFileDescriptor.getOffset());
			} catch (Exception ignore) {

			}
		}

		TextRange highlighterTextRange = new TextRange(highlighter.getStartOffset(), highlighter.getEndOffset());
		String rangeT = editor.getDocument().getText(highlighterTextRange);

		ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);

		return ErrorPlace.builder().project(project).virtualFile(virtualFile).isProjectFile(projectFileIndex.isInContent(virtualFile) && !projectFileIndex.isInLibrary(virtualFile)).lineNumber(lineNumber).hyperlinkText(rangeT).build();
	}
}
