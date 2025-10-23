package com.ke.setting.configuration.genral.user.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.ke.utils.PluginUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 11:09
 * @Version 1.0
 * @Description
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginConfigVO implements Serializable {

	private String theme = PluginUtil.isLightTheme() ? "light" : "dark";

	private String ide = "jetbrains";

	private String ideType = ApplicationInfoImpl.getInstance().getVersionName();


	private String os = PluginUtil.getOS();

	private String version = PluginUtil.getVersion();

	private String displayName;

	private String avatar;

	/**
	 * chat里面代码块是否默认展开
	 */
	private Boolean isCodeBlockTruncated = false;

	/**
	 * 调用大模型的配置
	 */
	@JSONField(name = "provider")
	private ModelConfiguration modelConfiguration;

	/**
	 * 对话模式
	 */
	private String mode;



}
