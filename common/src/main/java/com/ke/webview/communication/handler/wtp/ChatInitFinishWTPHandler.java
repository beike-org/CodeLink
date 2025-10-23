package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ke.utils.EditorUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.SelectCodeContext;
import com.ke.webview.util.PTWUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description chat能接收选中代码事件时
 */
public class ChatInitFinishWTPHandler extends BaseWTPHandler {

	public ChatInitFinishWTPHandler(Project project) {
		super(s -> {

			ApplicationManager.getApplication().invokeLater(() -> {
				try {
					//在打开toolwindow之前，选中了代码
					String selectedContent = EditorUtil.getSelectedContent(project);
					if (StringUtils.isNotEmpty(selectedContent)) {
						PTWUtil.sendMessage(BaseCommandEnums.SELECT_CODE_CONTEXT, SelectCodeContext.builder().code(SelectCodeContext.replaceTableKey(selectedContent)).build(), project);
					}
				} catch (Exception ignore) {

				}
			});

			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.CHAT_INIT_FINISH.getCommand();
	}
}
