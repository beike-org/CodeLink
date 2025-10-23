package com.ke.webview.communication.handler.wtp;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.ke.service.notify.NotifyServiceImpl;
import com.ke.webview.BaseCommandEnums;
import com.ke.toolwindow.content.BasePopupH5Panel;
import com.ke.utils.H5Util;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class PopupH5CloseWTPHandler extends BaseWTPHandler {

	public PopupH5CloseWTPHandler(BasePopupH5Panel basePopupH5Panel) {
		super(s -> {
			SwingUtilities.invokeLater(basePopupH5Panel::disposeParent);
			return null;
		});
	}


	public PopupH5CloseWTPHandler(Project project, BasePopupH5Panel basePopupH5Panel, String title) {
		super(s -> {
			if (StringUtils.isNotEmpty(s)) {
				project.getService(NotifyServiceImpl.class).createLinkNotification(title, H5Util.createALink(s, s), NotificationType.INFORMATION);
			}
			SwingUtilities.invokeLater(basePopupH5Panel::disposeParent);
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.POPUP_H5_CLOSE.getCommand();
	}
}
