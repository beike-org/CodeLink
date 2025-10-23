package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/17 15:12
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Position {
	private Integer line;
	private String character;
	@JSONField(name = "byte_offset")
	private Integer byteOffset;
}
