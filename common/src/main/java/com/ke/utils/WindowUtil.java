package com.ke.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/8 11:35
 * @Version 1.0
 * @Description
 */
public class WindowUtil {

	public static GraphicsDevice getDefaultGraphicsDevice() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}

	public static GraphicsDevice getCurrentDevice(Project project) {
		JFrame jFrame = WindowManager.getInstance().getFrame(project);
		if (Objects.nonNull(jFrame)) {
			return jFrame.getGraphicsConfiguration().getDevice();
		}
		return null;
	}

	public static boolean isDefaultGraphicsDevice(Project project) {
		return getDefaultGraphicsDevice().equals(getCurrentDevice(project));
	}
}
