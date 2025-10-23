package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/12 10:53
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadFileDTO {

	@JSONField(name = "fs_file_path")
	private String fsFilePath;

	@JSONField(name = "start_line")
	private Integer startLine;

	@JSONField(name = "end_line")
	private Integer endLine;

}
