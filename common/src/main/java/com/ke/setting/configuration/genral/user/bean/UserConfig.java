package com.ke.setting.configuration.genral.user.bean;

import com.intellij.openapi.util.SystemInfoRt;
import com.ke.setting.user.bean.ChangeFileTreeOption;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 10:50
 * @Version 1.0
 * @Description
 */
@Data
@NoArgsConstructor
public class UserConfig {


	/**
	 * 发送给前端的数据
	 */
	private PluginConfigVO pluginConfigVO = new PluginConfigVO();

	/**
	 * 是否开启function的辅助图标
	 */
	private Boolean lineMarker = true;


	/**
	 * 是否开启异常分析
	 */
	private Boolean stackAnalysis = true;

	/**
	 * 选中代码时，是否弹出popup
	 */
	private Boolean editorSelectedPopup = true;


	/**
	 * 是否启动全局规则
	 */
	private Boolean enableGlobalRules = false;


	/**
	 * webview是否使用OSR渲染
	 */
	private Boolean webviewOSR = SystemInfoRt.isLinux;



	/**
	 * 文件树的展示策略
	 */
	private ChangeFileTreeOption changeFileTreeOption = new ChangeFileTreeOption();


	/**
	 * 上次选择的文件目录
	 */
	private String storeDir;



	/**
	 * agent是否自动运行
	 */
	private Boolean autoRun = false;


}
