package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.PicPreviewDTO;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class PicPreviewHandler extends BaseWTPHandler {

	public PicPreviewHandler(Project project) {
		super(s -> {
			PicPreviewDTO picPreviewDTO = JsonUtil.getData(s, PicPreviewDTO.class);
			if (picPreviewDTO != null && picPreviewDTO.getTitle() != null) {
				if (StringUtils.isNotEmpty(picPreviewDTO.getContent())) {
					ApplicationManager.getApplication().invokeLater(() -> FileUtil.openImageByBase64(project, picPreviewDTO.getTitle(), picPreviewDTO.getContent()));
				} else if (StringUtils.isNotEmpty(picPreviewDTO.getUrl())) {
					ApplicationManager.getApplication().invokeLater(() -> FileUtil.openImageByUrl(project, picPreviewDTO.getTitle(), picPreviewDTO.getUrl()));
				}
			}
			return null;
		});
	}

	@Override
	public String getCommand() {
		return BaseCommandEnums.PIC_PREVIEW.getCommand();
	}
}
