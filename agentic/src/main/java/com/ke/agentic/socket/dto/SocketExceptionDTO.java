package com.ke.agentic.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/12 18:27
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocketExceptionDTO {

	private String exception;
}
