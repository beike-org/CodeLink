package com.ke.search;

import com.intellij.ide.actions.BigPopupUI;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManagerImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.WindowStateService;
import com.intellij.util.ui.JBInsets;
import com.ke.utils.ComponentUtil;

import java.awt.*;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/18 17:35
 * @Version 1.0
 * @Description
 */
public class AskEveryWhereService {

	private final Project project;

	private static final String ASK_LOCATION_KEY = "ask.everywhere.popup";

	private AskPopupUI bigPopupUI;

	private Dimension myBalloonFullSize;

	private JBPopup popup;

	public AskEveryWhereService(Project project) {
		this.project = project;
	}

	/**
	 * 参考IDEA sdk做的展示，后续效果优化可以继续参考
	 *
	 * @see SearchEverywhereManagerImpl#show(String, String, AnActionEvent)
	 */
	public void show(AskMode askMode) {
		bigPopupUI = new AskPopupUI(project, askMode);

		if (AskMode.Type.NORMAL.equals(askMode.getType())) {
			//保证以short视图打开
			getStateService().putSize(ASK_LOCATION_KEY, null);

			popup = ComponentUtil.createWebviewPopup(bigPopupUI, bigPopupUI.getSearchField(), project, ASK_LOCATION_KEY);
		} else {
			popup = ComponentUtil.createWebviewPopup(bigPopupUI, bigPopupUI.getSearchField(), project, null);
		}


		//添加View视图切换监听器
		bigPopupUI.addViewTypeListener(viewType -> {
			Dimension minSize = bigPopupUI.getMinimumSize();
			JBInsets.addTo(minSize, popup.getContent().getInsets());
			popup.setMinimumSize(new Dimension(minSize.width, minSize.height + 30));

			if (viewType == BigPopupUI.ViewType.SHORT) {
				myBalloonFullSize = popup.getSize();
				JBInsets.removeFrom(myBalloonFullSize, popup.getContent().getInsets());
				popup.pack(false, true);
			} else {
				myBalloonFullSize = bigPopupUI.getPreferredSize();
				JBInsets.addTo(myBalloonFullSize, popup.getContent().getInsets());
				myBalloonFullSize.height = Integer.max(myBalloonFullSize.height, minSize.height);
				myBalloonFullSize.width = Integer.max(myBalloonFullSize.width, minSize.width);
				popup.setSize(myBalloonFullSize);
			}
		});

		//设置父容器，关闭时调用
		bigPopupUI.setParentContainer(popup);

		//关闭时调用
		Disposer.register(popup, () -> {
			getStateService().putSize(ASK_LOCATION_KEY, null);
			bigPopupUI = null;
			popup = null;
			myBalloonFullSize = null;
		});

		calcPositionAndShow(project, popup, bigPopupUI, askMode.getComponent());

	}


	private void calcPositionAndShow(Project project,
									 JBPopup balloon, BigPopupUI bigPopupUI, Component parentComponent) {

		Dimension minSize = bigPopupUI.getMinimumSize();
		JBInsets.addTo(minSize, balloon.getContent().getInsets());

		//todo 临时+30保证显示边框
		balloon.setMinimumSize(new Dimension(minSize.width, minSize.height + 30));


		if (Objects.nonNull(parentComponent)) {
			balloon.showInCenterOf(parentComponent);
		} else if (project != null) {
			balloon.showCenteredInCurrentWindow(project);
		} else {
			balloon.showInFocusCenter();
		}
	}


	private WindowStateService getStateService() {
		return project != null ? WindowStateService.getInstance(project) : WindowStateService.getInstance();
	}
}
