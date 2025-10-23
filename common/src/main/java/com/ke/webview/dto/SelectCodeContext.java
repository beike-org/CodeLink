package com.ke.webview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/29 11:00
 * @Version 1.0
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelectCodeContext {

	private String code;

	private Integer startLine;

	private Integer endLine;

	private Integer startOffset;

	private Integer endOffset;

	private String source;

	private String fileName;


	public static String replaceTableKey(String original) {
		return original.replace("\t", " ");
	}
}
