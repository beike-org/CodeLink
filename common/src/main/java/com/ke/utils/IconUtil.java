package com.ke.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/7/3 15:49
 * @Version 1.0
 * @Description
 */
public class IconUtil {

	public static Icon getCodeMethodIcon() {
		return IconLoader.findIcon("/icons/code_method.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getToggleOpenIcon() {
		return IconLoader.findIcon("/icons/toggle_on.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getToggleCloseIcon() {
		return IconLoader.findIcon("/icons/toggle_off.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getSettingIcon() {
		return IconLoader.findIcon("/icons/setting.svg", IconUtil.class.getClassLoader());
	}


	public static Icon getDarkGuideIcon() {
		return IconLoader.findIcon("/icons/guide_dark.svg", IconUtil.class.getClassLoader());
	}


	public static Icon getSendIcon() {
		return IconLoader.findIcon("/icons/send.svg", IconUtil.class.getClassLoader());
	}


	public static Icon getStackTraceIcon() {
		return IconLoader.findIcon("/icons/stacktrace.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getExplainCodeIcon() {
		return IconLoader.findIcon("/icons/explain_code.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getCheckBugIcon() {
		return IconLoader.findIcon("/icons/check_bug.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getCreateUnitTestIcon() {
		return IconLoader.findIcon("/icons/create_unit_test.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getOptimizeCodeIcon() {
		return IconLoader.findIcon("/icons/optimize_code.svg", IconUtil.class.getClassLoader());
	}

	public static Icon getRefactorCodeIcon() {
		return IconLoader.findIcon("/icons/refactor_code.svg", IconUtil.class.getClassLoader());
	}


	public static Icon getAdd2ChatIcon() {
		return IconLoader.getIcon("/icons/add.svg", IconUtil.class.getClassLoader());
	}
}
