package com.ke.webview;

import com.ke.utils.PluginUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 16:34
 * @Version 1.0
 * @Description webview的项目级缓存，如git信息等
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebViewProjectConfig {

	//sidecar相关信息
	private SideCarIDEInfo sideCarIDEInfo;

	//活跃editor中的文件类型
	private String workspaceFileType;

	// 最近一次打开的queryString
	@Builder.Default
	private String queryString = PluginUtil.isLightTheme() ? "" : "?theme=dark";

	// 当前项目使用的jdk版本
	private String jdkVersion;

	// 当前项目是否是maven项目
	private Boolean isMavenProject;

	// 上次api调试使用的url
	private String apiDebugUrl;
}
