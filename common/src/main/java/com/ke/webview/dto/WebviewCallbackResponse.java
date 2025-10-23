package com.ke.webview.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/3/20 11:41
 * @Version 1.0
 * @Description
 */
@Data
@Builder
public class WebviewCallbackResponse {

	//是否进行下一步
	private Boolean success;

	//不建议进行下一步的原因
	private String reason;

	//回调数据
	private String data;
}
