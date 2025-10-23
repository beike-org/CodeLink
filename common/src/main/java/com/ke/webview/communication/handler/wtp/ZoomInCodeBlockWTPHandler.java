package com.ke.webview.communication.handler.wtp;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.ZoomInCodeBlockDTO;
import com.ke.utils.EditorUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class ZoomInCodeBlockWTPHandler extends BaseWTPHandler {

	public ZoomInCodeBlockWTPHandler(Project project) {
		super(s -> {
			if (StringUtils.isNotEmpty(s)) {
				ZoomInCodeBlockDTO zoomInCodeBlockDTO = JSONObject.parseObject(s, ZoomInCodeBlockDTO.class);
				String time = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
				String fileName = String.format("CodeLink_ZoomInCode_%s.md", time);
				String content = String.format("```%s\n%s\n```", zoomInCodeBlockDTO.getLanguage(), zoomInCodeBlockDTO.getCode());
				EditorUtil.openFile(fileName, content, project);
			}

			return null;
		});
	}


	@Override
	public String getCommand() {
		return BaseCommandEnums.ZOOM_IN_CODE_BLOCK.getCommand();
	}
}
