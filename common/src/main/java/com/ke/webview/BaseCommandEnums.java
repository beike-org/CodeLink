package com.ke.webview;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/22 16:26
 * @Version 1.0
 * @Description
 */
@Getter
@AllArgsConstructor
public enum BaseCommandEnums implements WebviewCommand {

	/**
	 * 同步应用级配置到webview，初始化或任意缓存数据更改
	 */
	SYNC_PLUGIN_CONFIG("syncPluginConfig", "ptw"),


	/**
	 * webview更新应用级配置，登录、登出、打开新对话、获取用户数据等事件更新缓存数据时
	 */
	SET_PLUGIN_CONFIG("setPluginConfig", "wtp"),


	/**
	 * 同步项目级配置
	 */
	SYNC_PROJECT_CONFIG("syncProjectConfig", "ptw"),


	/**
	 * webview更新项目级配置
	 */
	SET_PROJECT_CONFIG("setProjectConfig", "wtp"),


	/**
	 * 用户选择工作区代码
	 */
	SELECT_CODE_CONTEXT("selectCodeContext", "ptw"),


	/**
	 * 行内提问
	 */
	INLINE_CHAT("inlineChat", "ptw"),



	/**
	 * webview完成加载时
	 */
	INIT_FINISH("initFinish", "wtp"),


	/**
	 * chat完成加载时,能接收选中代码事件（应对先选中代码，然后初始化webview的情况）
	 */
	CHAT_INIT_FINISH("chatInitFinish", "wtp"),



	/**
	 * 用户通知
	 */
	ALERT("alert", "wtp"),

	/**
	 * 刷新
	 */
	REFRESH("refresh", "wtp"),


	/**
	 * 打开浏览器
	 */
	OPEN_BROWSER("openBrowser", "wtp"),

	/**
	 * 取消选择工作区代码
	 */
	DE_SELECT_CODE_CONTEXT("deselectCodeContext", "wtp"),


	/**
	 * 插入代码
	 */
	INSERT_CODE("insertCode", "wtp"),


	/**
	 * 根据传过来的信息将代码打开并选中
	 */
	OPEN_FILE_AND_SELECT("openFileAndSelect", "wtp"),


	/**
	 * popup弹出的页面关闭
	 */
	POPUP_H5_CLOSE("popupClose", "wtp"),

	/**
	 * 放大代码块
	 */
	ZOOM_IN_CODE_BLOCK("zoomInCodeBlock", "wtp"),



	/**
	 * 请求文件代码内容
	 */
	FILE_LIST_CONTENT("getFileListContent", "wtp"),


	/**
	 * 打开图片预览
	 */
	PIC_PREVIEW("picPreview", "wtp"),

	/**
	 * 获得文件内容，从而进行分析代码
	 */
	GET_FILE_CONTENT("getFileContent", "wtp"),

	;

	private final String command;

	private final String type;
}
