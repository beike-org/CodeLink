package com.ke.agentic.pts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
public class LLMProviderDetail {

    private Object provider;
    private List<ModelDetail> modules;
}
