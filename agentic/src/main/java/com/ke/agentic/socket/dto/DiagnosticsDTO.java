package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/17 14:40
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticsDTO {

	@JSONField(name = "fs_file_path")
	private String filePath;
	private Range range;

}



