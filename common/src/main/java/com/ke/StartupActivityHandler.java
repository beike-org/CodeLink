package com.ke;


import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;

/**
 * 项目启动的时候把别的模块的初始化行为加载进来
 */
public interface StartupActivityHandler {
	ExtensionPointName<StartupActivityHandler> EP_NAME = ExtensionPointName.create("com.ke.codelink.startupActivityHandler");


	void init(Project project);

	default void beforeWebViewInit(Project project){}

}
