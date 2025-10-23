package com.ke.webview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileContentDTO {

	private String icon;

	private String name;

	private String path;

	private String content;

	private String uuid;

	private Integer startLine;

	private Integer endLine;

	/**
	 * 起始行的字符偏移量
	 */
	private Integer startOffset;

	/**
	 * 结束行的字符偏移量
	 */
	private Integer endOffset;

	private String language;

	private String type;
}
