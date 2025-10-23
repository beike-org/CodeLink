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
public class ListFileDTO {

	@JSONField(name = "directory_path")
	private String directoryPath;

	private Boolean recursive;

	private Integer limit;
}
