package com.ke.setting.configuration.genral.user.bean;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/11 14:31
 * @Description 同步给sidecar的IDE信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelConfiguration {

    // 上次使用的模型名
    @JSONField(name = "model_name")
    @JsonProperty("model_name")
    private String modelName;

    // 上次使用的模型提供商
    @JSONField(name = "selected_provider")
    @JsonProperty("selected_provider")
    private String selectedProvider;

    // 上次使用的模型参数
    @JSONField(name = "selected_provider_api_key")
    @JsonProperty("selected_provider_api_key")
    private Map<String, JSONObject> selectedProviderApiKey;

    @JSONField(name = "slow_model")
    @JsonProperty("slow_model")
    @Builder.Default
    private String slowModel = "";

    @JSONField(name = "fast_model")
    @JsonProperty("fast_model")
    @Builder.Default
    private String fastModel = "";

    @Builder.Default
    private Object models = new JSONObject();

    @Builder.Default
    private List<Object> providers = new ArrayList<>();

}
