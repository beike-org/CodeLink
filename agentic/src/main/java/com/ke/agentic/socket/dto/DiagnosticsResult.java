package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/17 15:11
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiagnosticsResult {
	@JSONField(name = "fs_file_path")
	private String filePath;
	private Range range;
	private String message;
}
