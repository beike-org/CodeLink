package com.ke.mcp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RemoteMcpDTO {
	private List<SseConfigDTO> configList;
}
