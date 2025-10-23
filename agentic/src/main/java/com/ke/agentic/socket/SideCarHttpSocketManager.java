package com.ke.agentic.socket;

import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.ke.agentic.socket.handler.RequestHandler;
import com.ke.utils.DynamicReflectionUtil;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 11:29
 * @Description
 */
public class SideCarHttpSocketManager {

	private final static Logger logger = Logger.getInstance(SideCarHttpSocketManager.class);

	// 缓存project locationHash到project的映射
	private final Map<String, Project> projectMap = Maps.newConcurrentMap();

	// 缓存请求路径到处理器的映射
	private final Map<String, RequestHandler> handlerMap = Maps.newConcurrentMap();


	public static SideCarHttpSocketManager getInstance() {
		return ApplicationManager.getApplication().getService(SideCarHttpSocketManager.class);
	}


	/**
	 * 初始化
	 */
	public void init() {
		logger.info("HttpSocketManager init");
		// 初始化处理器,自动装配RequestHandler同包下的所有实现类
		Set<Class<? extends RequestHandler>> handlers = DynamicReflectionUtil.getAllSubTypesOf(RequestHandler.class);
		handlers.forEach(handler -> {
			try {
				RequestHandler baseRequestHandler = handler.getDeclaredConstructor().newInstance();
				handlerMap.put(getHandlerKey(baseRequestHandler.getPath(), baseRequestHandler.getMethod()), baseRequestHandler);
			} catch (Exception e) {
				logger.warn("HttpSocketManager init error", e);
			}
		});
	}

	/**
	 * 处理请求
	 */
	public NanoHTTPD.Response handleRequest(RequestEntity requestEntity) {
		String path = requestEntity.getPath();
		String projectId = requestEntity.getHeaders().get("projectId".toLowerCase());
		if (StringUtils.isEmpty(projectId)) {
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", path + " Not Found ProjectId");
		}
		Project project = getProject(projectId);
		if (project == null) {
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", path + " Not Found Project");
		}
		RequestHandler handler = handlerMap.get(getHandlerKey(path, requestEntity.getMethod()));
		if (handler == null) {
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", path + " Not Found Handler");
		}
		return handler.handleRequest(project, requestEntity);
	}


	/**
	 * 根据projectId(locationHash)获取project
	 */
	@Nullable
	public Project getProject(String projectId) {
		Project project = projectMap.get(projectId);
		if (project == null || project.isDisposed()) {
			// 更新缓存
			projectMap.clear();
			Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
			for (Project openProject : openProjects) {
				projectMap.put(openProject.getLocationHash(), openProject);
			}
			return projectMap.get(projectId);

		}
		return project;
	}


	/**
	 * 生成处理器的唯一key
	 */
	public static String getHandlerKey(String path, NanoHTTPD.Method method) {
		return path + ":" + method;
	}

}
