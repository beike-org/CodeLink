package com.ke.webview.communication.handler.wtp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.WebviewCallbackResponse;
import com.ke.utils.FileUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 根据文件名列表获取文件内容列表
 */
public class GetFileListContentWTPHandler extends BaseWTPHandler {

	private static final Logger LOGGER = Logger.getInstance(GetFileListContentWTPHandler.class);

	public GetFileListContentWTPHandler(Project project) {
		super(s -> {
			String basedir = project.getBasePath();
			if (Objects.nonNull(s)) {
				try {
					List<String> pathList = JSON.parseArray(s, String.class);
					if (CollectionUtils.isEmpty(pathList)) {
						return new JBCefJSQuery.Response(JSONObject.toJSONString(WebviewCallbackResponse.builder().success(true).reason("").data(null).build()));
					}
					Map<String, String> map = new HashMap<>(pathList.size());
					ReadAction.run(() -> {
						pathList.forEach(path -> {
							String finalPath = basedir + path;
							PsiFile psiFile = FileUtil.getPsiFileFromAbsolutePath(project, finalPath);
							if (psiFile == null) {
								return;
							}
							map.put(path, psiFile.getText());
						});
					});
					return new JBCefJSQuery.Response(JSONObject.toJSONString(WebviewCallbackResponse.builder().success(true).reason("").data(JSON.toJSONString(map, SerializerFeature.DisableCircularReferenceDetect)).build()));
				} catch (Exception e) {
					LOGGER.warn("GetFileListContent error", e);
					return new JBCefJSQuery.Response(JSONObject.toJSONString(WebviewCallbackResponse.builder().success(false).reason("获取文件列表异常").data(null).build()));
				}
			}
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.FILE_LIST_CONTENT.getCommand();
	}
}
