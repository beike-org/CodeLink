package com.ke.agentic.diagnostics;

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.ke.utils.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/17 11:47
 * @Description
 */
public class DiagnosticsProcessor implements Processor<HighlightInfo> {

	private final List<HighlightInfo> foundInfoList = new SmartList<>();

	private final boolean highestPriorityOnly;

	public DiagnosticsProcessor(boolean highestPriorityOnly) {
		this.highestPriorityOnly = highestPriorityOnly;
	}

	@Override
	public boolean process(HighlightInfo info) {
		if (info.getSeverity() != HighlightInfoType.ELEMENT_UNDER_CARET_SEVERITY && info.type != HighlightInfoType.TODO) {
			if (!this.foundInfoList.isEmpty() && this.highestPriorityOnly) {
				HighlightInfo foundInfo = this.foundInfoList.get(0);
				int compare = foundInfo.getSeverity().compareTo(info.getSeverity());
				if (compare < 0) {
					this.foundInfoList.clear();
				} else if (compare > 0) {
					return true;
				}
			}

			this.foundInfoList.add(info);
			return true;
		} else {
			return true;
		}
	}


	@Nullable
	public List<HighlightInfo> getResults() {
		return foundInfoList.isEmpty() ? null : foundInfoList;
	}

	/**
	 * 获取诊断信息
	 *
	 * @param highestPriorityOnly 是否只返回最高优先级的诊断信息
	 * @param document            当前文档
	 * @param project             当前项目
	 * @param startOffset         起始偏移量
	 * @param endOffset           结束偏移量
	 * @return 诊断信息列表
	 */
	@Nullable
	public static List<HighlightInfo> getDiagnosticsInfo(@NotNull Document document,
														 @NotNull Project project,
														 boolean highestPriorityOnly,
														 int startOffset,
														 int endOffset) {
		DiagnosticsProcessor processor = new DiagnosticsProcessor(highestPriorityOnly);
		DaemonCodeAnalyzerImpl.processHighlights(document, project, HighlightSeverity.WARNING, startOffset, endOffset, processor);
		return processor.getResults();
	}


	/**
	 * 获取诊断信息
	 *
	 * @param highestPriorityOnly 是否只返回最高优先级的诊断信息
	 * @param file                文件
	 * @param project             当前项目
	 * @param startOffset         起始偏移量
	 * @param endOffset           结束偏移量
	 * @return 诊断信息列表
	 */
	@Nullable
	public static List<HighlightInfo> getDiagnosticsInfo(@NotNull VirtualFile file,
														 @NotNull Project project,
														 boolean highestPriorityOnly,
														 int startOffset,
														 int endOffset) {
		Document document = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(file));
		if (document == null) {
			return null;
		}
		return getDiagnosticsInfo(document, project, highestPriorityOnly, startOffset, endOffset);

	}

	/**
	 * 获取诊断信息
	 *
	 * @param highestPriorityOnly 是否只返回最高优先级的诊断信息
	 * @param file                文件绝对路径
	 * @param project             当前项目
	 * @param startOffset         起始偏移量
	 * @param endOffset           结束偏移量
	 * @return 诊断信息列表
	 */
	@Nullable
	public static List<HighlightInfo> getDiagnosticsInfo(@NotNull String file,
														 @NotNull Project project,
														 boolean highestPriorityOnly,
														 int startOffset,
														 int endOffset) {
		try {
			VirtualFile virtualFile = FileUtil.findVirtualFile(file);
			return getDiagnosticsInfo(virtualFile, project, highestPriorityOnly, startOffset, endOffset);
		} catch (Exception e) {
			return null;
		}

	}


}
