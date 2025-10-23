package com.ke.webview.dto;

import lombok.Data;

@Data
public class OpenAndSelectDTO {

	private String fileUri;

	private Integer startLine;

	private Integer endLine;

	private Integer startOffset;

	private Integer endOffset;
}
