package com.ke.agentic.pts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/27 18:12
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LLMProviderDTO {

    private Map<String, LLMProviderDetail> providers;
}
