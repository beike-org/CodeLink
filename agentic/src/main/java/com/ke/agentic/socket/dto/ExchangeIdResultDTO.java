package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class ExchangeIdResultDTO {

	@JSONField(name = "exchange_id")
	private String exchangeId;

}
