package com.ke.agentic.webview.handler.dto;

import lombok.Builder;
import lombok.Data;


/**
 * 用户选择代码后，点击addToChat按钮发送给webview的
 */
@Data
@Builder
public class SelectCodeDTO {

	private String code;

	private String source;

	private String fsPath;

	private String language;

	private Integer startLine;

	private Integer endLine;

	private Integer startOffset;

	private Integer endOffset;

	private String fullContent;
}
