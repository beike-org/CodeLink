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
import com.ke.agentic.webview.handler.dto.DirectorySugDTO;
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
public class SearchDirectoryWTPHandler extends BaseWTPHandler {
	// 最大结果数量限制
	private static final int MAX_RESULTS = 100;

	public SearchDirectoryWTPHandler(Project project) {
		super(s -> {
			CompletableFuture<JBCefJSQuery.Response> future = new CompletableFuture<>();

			ApplicationManager.getApplication().executeOnPooledThread(() -> {
				try {
					DirectorySugDTO dto = JSON.parseObject(String.valueOf(s), DirectorySugDTO.class);
					String userInput = dto.getUserInput();
					String uuid = dto.getUuid();

					if (StringUtils.isBlank(userInput) || userInput.length() < 3) {
						future.complete(new JBCefJSQuery.Response(JSON.toJSONString(
								WebviewCallbackResponse.builder()
										.success(true)
										.data(JSON.toJSONString(new SearchDirectoryRespDTO(uuid, Collections.emptyList())))
										.build()
						)));
						return;
					}

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

					// 转换为小写，避免重复转换
					String searchKeywordLower = userInput.toLowerCase();

					ApplicationManager.getApplication().runReadAction(() -> {
						processDirectoryStatic(baseDir, searchKeywordLower, project, resultList, excludeDirectories, resultCount);
					});

					future.complete(new JBCefJSQuery.Response(JSON.toJSONString(
							WebviewCallbackResponse.builder()
									.success(true)
									.data(JSON.toJSONString(new SearchDirectoryRespDTO(uuid, resultList)))
									.build()
					)));
				} catch (Exception e) {
					future.complete(new JBCefJSQuery.Response(JSON.toJSONString(
							WebviewCallbackResponse.builder()
									.success(false)
									.data("Error processing directory search: " + e.getMessage())
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
								.data("Error getting search results: " + e.getMessage())
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

	public static void processDirectoryStatic(VirtualFile directory,
											  String searchKeyword,
											  Project project,
											  List<SearchDirectoryRespDTO.FileTreeNode> parentList,
											  Set<String> excludeDirectories,
											  AtomicInteger resultCount) {
		if (directory == null || !directory.isDirectory()) return;
		String basePath = project.getBasePath();
		if (basePath == null) return;

		// 如果已经达到最大结果数，提前返回
		if (resultCount.get() >= MAX_RESULTS) return;

		// 调用新的递归方法，从0层开始
		processDirectoryWithDepthLimit(directory, searchKeyword, project, parentList, 0, excludeDirectories, resultCount);
	}

	/**
	 * 处理目录，带有深度限制
	 *
	 * @param directory          当前目录
	 * @param searchKeyword      搜索关键字（已转为小写）
	 * @param project            项目
	 * @param parentList         父列表
	 * @param currentDepth       当前深度
	 * @param excludeDirectories 排除目录列表
	 * @param resultCount        结果计数器
	 */
	private static void processDirectoryWithDepthLimit(VirtualFile directory,
													   String searchKeyword,
													   Project project,
													   List<SearchDirectoryRespDTO.FileTreeNode> parentList,
													   int currentDepth,
													   Set<String> excludeDirectories,
													   AtomicInteger resultCount) {
		if (directory == null || !directory.isDirectory()) return;
		String basePath = project.getBasePath();
		if (basePath == null) return;

		// 如果已经达到最大结果数，提前返回
		if (resultCount.get() >= MAX_RESULTS) return;

		// 检查是否已经达到最大深度限制（从匹配的文件夹开始算，最多向下3级）
		String directoryNameLower = directory.getName().toLowerCase();
		boolean isKeywordMatched = directoryNameLower.contains(searchKeyword);
		int maxDepth = isKeywordMatched ? 3 : -1; // -1表示没有限制，只有匹配关键字的文件夹才有深度限制

		// 如果当前深度超过最大深度且有深度限制，则不再继续处理
		if (maxDepth != -1 && currentDepth > maxDepth) return;

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

			String name = child.getName();
			String nameLower = name.toLowerCase();
			SearchDirectoryRespDTO.FileTreeNode matchedNode = null;

			// 如果文件夹名称包含关键字，添加到结果中
			if (nameLower.contains(searchKeyword)) {
				matchedNode = SearchDirectoryRespDTO.FileTreeNode.builder()
						.name(name)
						.path(StringUtils.removeStart(child.getPath(), basePath))
						.isDirectory(true) // 确保标记为目录
						.build();
				parentList.add(matchedNode);
				resultCount.incrementAndGet();
			}

			// 处理子目录，深度+1
			List<SearchDirectoryRespDTO.FileTreeNode> children = new ArrayList<>();

			// 如果当前节点匹配了关键字，则从0开始计算深度，否则继承父节点的深度
			int nextDepth = isKeywordMatched ? 0 : currentDepth + 1;
			processDirectoryWithDepthLimit(child, searchKeyword, project, children, nextDepth, excludeDirectories, resultCount);

			if (matchedNode != null) {
				matchedNode.setChildren(children);
			} else if (!children.isEmpty()) {
				SearchDirectoryRespDTO.FileTreeNode dirNode = SearchDirectoryRespDTO.FileTreeNode.builder()
						.name(name)
						.path(StringUtils.removeStart(child.getPath(), basePath))
						.isDirectory(true)
						.children(children)
						.build();
				parentList.add(dirNode);
				resultCount.incrementAndGet();
			}
		}
	}

	@Override
	public String getCommand() {
		return AgenticWebviewCommandEnums.SEARCH_DIRECTORY.getCommand();
	}
}

