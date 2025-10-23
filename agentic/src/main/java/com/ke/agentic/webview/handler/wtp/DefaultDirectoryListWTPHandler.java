package com.ke.agentic.webview.handler.wtp;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.webview.dto.WebviewCallbackResponse;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;
import com.ke.agentic.webview.handler.dto.SearchDirectoryRespDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Folders 相关
 */
public class DefaultDirectoryListWTPHandler extends BaseWTPHandler {
	// 最大结果数量限制
	private static final int MAX_RESULTS = 500;

	public DefaultDirectoryListWTPHandler(Project project) {
		super(s -> {
			CompletableFuture<JBCefJSQuery.Response> future = new CompletableFuture<>();

			ApplicationManager.getApplication().executeOnPooledThread(() -> {
				try {
					VirtualFile baseDir = VirtualFileManager.getInstance().findFileByUrl("file://" + project.getBasePath());
					if (baseDir == null) {
						future.complete(new JBCefJSQuery.Response(JSON.toJSONString(
								WebviewCallbackResponse.builder()
										.success(false)
										.data("Project base directory not found")
										.build()
						)));
						return;
					}

					// 预先获取排除目录列表
					Set<String> excludeDirectories = getExcludeDirectories();

					List<SearchDirectoryRespDTO.FileTreeNode> resultList = new ArrayList<>();
					AtomicInteger resultCount = new AtomicInteger(0);

					ApplicationManager.getApplication().runReadAction(() -> processDirectory(baseDir, project, resultList, excludeDirectories, resultCount));

					future.complete(new JBCefJSQuery.Response(JSON.toJSONString(
							WebviewCallbackResponse.builder()
									.success(true)
									.data(JSON.toJSONString(resultList))
									.build()
					)));
				} catch (Exception e) {
					future.complete(new JBCefJSQuery.Response(JSON.toJSONString(
							WebviewCallbackResponse.builder()
									.success(false)
									.data("Error processing directory list: " + e.getMessage())
									.build()
					)));
				}
			});

			try {
				return future.get();
			} catch (Exception e) {
				return new JBCefJSQuery.Response(JSON.toJSONString(
						WebviewCallbackResponse.builder()
								.success(false)
								.data("Error getting directory list: " + e.getMessage())
								.build()
				));
			}
		});
	}

	// 缓存排除目录列表
	private static Set<String> getExcludeDirectories() {
		return Sets.newHashSet();
	}

	private static boolean isExcludeDirectory(String path, Set<String> excludeDirectories) {
		Path pathObj = Paths.get(path);
		Set<String> names = new HashSet<>();
		for (Path part : pathObj) {
			names.add(part.toString());
		}
		return CollectionUtils.containsAny(names, excludeDirectories);
	}

	private static void processDirectory(VirtualFile directory, Project project,
										 List<SearchDirectoryRespDTO.FileTreeNode> parentList,
										 Set<String> excludeDirectories,
										 AtomicInteger resultCount) {
		if (directory == null || !directory.isDirectory()) return;
		String basePath = project.getBasePath();
		if (basePath == null) return;

		// 如果已经达到最大结果数，提前返回
		if (resultCount.get() >= MAX_RESULTS) return;

		// 获取子目录列表并预先过滤
		List<VirtualFile> childDirectories = new ArrayList<>();
		for (VirtualFile child : directory.getChildren()) {
			if (child instanceof LightVirtualFile) continue;
			if (!child.isDirectory()) continue;
			if (child.getName().startsWith(".")) continue;
			if (isExcludeDirectory(child.getPath(), excludeDirectories)) continue;

			childDirectories.add(child);
		}

		// 如果没有子目录，提前返回
		if (childDirectories.isEmpty()) return;

		for (VirtualFile child : childDirectories) {
			// 如果已经达到最大结果数，提前返回
			if (resultCount.get() >= MAX_RESULTS) return;

			List<SearchDirectoryRespDTO.FileTreeNode> children = new ArrayList<>();
			processDirectory(child, project, children, excludeDirectories, resultCount);

			SearchDirectoryRespDTO.FileTreeNode dirNode = SearchDirectoryRespDTO.FileTreeNode.builder()
					.name(child.getName())
					.path(StringUtils.removeStart(child.getPath(), basePath))
					.isDirectory(true)
					.children(children)
					.build();
			parentList.add(dirNode);
			resultCount.incrementAndGet();
		}
	}

	@Override
	public String getCommand() {
		return AgenticWebviewCommandEnums.DEFAULT_DIRECTORY_LIST.getCommand();
	}
}
