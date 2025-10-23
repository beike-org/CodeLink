package com.ke.agentic.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/17 14:40
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiagnosticsResultDTO {
	private List<DiagnosticsResult> diagnostics;
}


