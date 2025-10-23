package com.ke.agentic.pts.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ke.setting.configuration.genral.user.bean.ModelConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ChatQueryDTO {

    private String query;

    @JsonProperty("model_configuration")
    @JSONField(name = "model_configuration")
    private ModelConfiguration modelConfiguration;
}
