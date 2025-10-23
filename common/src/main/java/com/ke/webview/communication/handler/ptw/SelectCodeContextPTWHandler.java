package com.ke.webview.communication.handler.ptw;

import com.ke.toolwindow.content.BaseH5Panel;
import com.ke.webview.BaseCommandEnums;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 15:32
 * @Version 1.0
 * @Description
 */
public class SelectCodeContextPTWHandler extends BasePTWHandler {


	public SelectCodeContextPTWHandler(BaseH5Panel h5Panel) {
		super(h5Panel);
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.SELECT_CODE_CONTEXT.getCommand();
	}
}
