package com.ke.toolwindow.content;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.WindowStateService;
import com.ke.utils.WindowUtil;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public abstract class BasePopupH5Panel extends BaseH5Panel implements Disposable, ToolWindowContentPanel {

	@Setter
    private JBPopup popup;

	private final String locationKey;

	/**
	 * @param project     the project associated with this panel
	 * @param locationKey the location key
	 */
	public BasePopupH5Panel(@NotNull Project project, @NotNull String locationKey) {
		super(project);
		this.locationKey = locationKey;
	}

    public void disposeParent() {
		popup.dispose();
	}

	public void resize() {
		Dimension size = WindowStateService.getInstance(getProject()).getSize(locationKey);
		if (Objects.nonNull(size)) {
			popup.setSize(size);
		} else {
			popup.setSize(getDefaultSize());
		}

	}


	@Override
	public void dispose() {
		if (!jbCefBrowser.isDisposed()) {
			jbCefBrowser.dispose();
		}

		if (!jbCefBrowser.getJBCefClient().isDisposed()) {
			jbCefBrowser.getJBCefClient().dispose();
		}
	}


	@Override
	public String getErrorH5Path() {
		return "/html/error.html";
	}


	@Override
	protected boolean resizeOnOpen() {
		//是windows的话，且不是默认的显示器，打开重置大小解决显示不完全的问题
		return SystemInfo.isWindows && !WindowUtil.isDefaultGraphicsDevice(getProject());
	}

	@Override
	protected void resizePanel() {
		resize();
	}

	public abstract Dimension getDefaultSize();

}
