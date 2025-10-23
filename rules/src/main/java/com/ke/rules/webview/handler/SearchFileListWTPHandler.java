package com.ke.rules.webview.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.LightVirtualFile;
import com.ke.rules.ProjectRuleManager;
import com.ke.rules.dto.ProjectRuleDTO;
import com.ke.rules.enums.RuleType;
import com.ke.rules.webview.RulesWebviewCommandEnums;

import com.ke.rules.webview.dto.SearchFileListRespDTO;
import com.ke.utils.ApplicationUtil;
import com.ke.utils.EditorUtil;
import com.ke.utils.FileUtil;
import com.ke.utils.WorkspaceUtil;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.rules.webview.dto.FileSugDTO;
import com.ke.webview.dto.FileContentDTO;
import com.ke.webview.util.PTWUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFileListWTPHandler extends BaseWTPHandler {

	private static List<String> excludeSubfixList = Lists.newArrayList("class");

	public SearchFileListWTPHandler(Project project) {
		super(s -> {
			try {
				FileSugDTO fileSugDTO = JSON.parseObject(s.toString(), FileSugDTO.class);
				String userInput = fileSugDTO.getUserInput();
				String uuid = fileSugDTO.getUuid();
				if (StringUtils.isBlank(userInput) || userInput.length() < 3) {
					PTWUtil.sendMessage(RulesWebviewCommandEnums.SEARCH_FILE_LIST_RESP,
							new SearchFileListRespDTO(uuid, Collections.emptyList()));
					return null;
				}
				List<FileContentDTO> resultList = new ArrayList<>();

				//在rules模式下，如果用户配了指定类型的文件，将按照用户填写的通配符表达式过滤
				List<ProjectRuleDTO> projectRules = project.getService(ProjectRuleManager.class).getProjectRules();
				List<PathMatcher> matchers = new ArrayList<>();
				projectRules
						.stream()
						.filter(projectRuleDTO -> RuleType.SPECIFIED_TYPE.equals(projectRuleDTO.getType()) && CollectionUtils.isNotEmpty(projectRuleDTO.getRegex()))
						.forEach(projectRuleDTO -> projectRuleDTO.getRegex().forEach(pattern ->
								matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern))
						));

				// 将耗时操作移到后台线程中执行
				ApplicationManager.getApplication().executeOnPooledThread(() -> {
					VirtualFileManager.getInstance().getFileSystem("file").refresh(true); // 异步刷新
					ApplicationManager.getApplication().runReadAction(() -> {
						// 获取项目中的所有文件
						ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

						fileIndex.iterateContent(virtualFile -> {
							Icon icon = virtualFile.getFileType().getIcon();
							String iconStr = WorkspaceUtil.convertIconToBase64(icon);

							// 需要在 readAction 中获取 Document
							Document document = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
									FileDocumentManager.getInstance().getDocument(virtualFile)
							);

							if (virtualFile.getName().toLowerCase().contains(userInput.toLowerCase()) && !excludeSubfixList.contains(virtualFile.getExtension()) && !(virtualFile instanceof LightVirtualFile) && Boolean.FALSE.equals(FileUtil.filterByExtension(virtualFile.getExtension())) && !virtualFile.isDirectory()) {
								boolean matchesPattern = matchers.isEmpty() || matchers.stream()
										.anyMatch(matcher -> matcher.matches(Paths.get(virtualFile.getName())));

								if (matchesPattern) {
									int lastLineNumber = document.getLineCount() - 1;
									int lastLineStartOffset = document.getLineStartOffset(lastLineNumber);
									int lastLineLength = document.getTextLength() - lastLineStartOffset;
									FileContentDTO fileContentDTO = FileContentDTO.builder()
											.icon(iconStr)
											.name(virtualFile.getName())
											.path(virtualFile.getPath().replace(project.getBasePath(), ""))
											.content(document != null ? document.getText() : "")
											.startLine(0)
											.endLine(document != null ? document.getLineCount() - 1 : 0)
											.startOffset(0)
											.endOffset(lastLineLength)
											.language(EditorUtil.getLanguage(ApplicationUtil.findCurrentProject()))
											.type("File")
											.build();
									resultList.add(fileContentDTO);
								}
							}
							return true;
						});
					});

					PTWUtil.sendMessage(RulesWebviewCommandEnums.SEARCH_FILE_LIST_RESP,
							new SearchFileListRespDTO(uuid, resultList));
				});
				return null;
			} catch (Exception e) {
				return null;
			}
		});
	}

	@Override
	public String getCommand() {
		return RulesWebviewCommandEnums.SEARCH_FILE_LIST.getCommand();
	}
}
