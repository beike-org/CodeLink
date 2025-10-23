package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/17 14:47
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Range {
	@JSONField(name = "start_position")
	private Position startPosition;
	@JSONField(name = "end_position")
	private Position endPosition;
}

