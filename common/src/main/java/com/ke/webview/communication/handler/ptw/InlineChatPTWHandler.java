package com.ke.webview.communication.handler.ptw;

import com.intellij.openapi.project.Project;
import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.util.PTWUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.InlineChatDTO;
import com.ke.utils.ComponentUtil;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 15:32
 * @Version 1.0
 * @Description
 */
public class InlineChatPTWHandler extends BasePTWHandler {


	public InlineChatPTWHandler(BaseH5Panel h5Panel) {
		super(h5Panel);
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.INLINE_CHAT.getCommand();
	}

	public static void sendInlineChat(InlineChatDTO inlineChatDTO, Project project) {
		ComponentUtil.getCodeLinkToolWindow(project).show();
		PTWUtil.sendMessage(BaseCommandEnums.INLINE_CHAT, inlineChatDTO, project);
	}
}
