package com.ke.agentic.pts;

import com.ke.agentic.SideCarAgentManager;
import com.ke.agentic.pts.dto.ChatQueryDTO;
import com.ke.agentic.pts.dto.LLMProviderDTO;
import com.ke.agentic.pts.dto.ModelDetail;
import com.ke.service.llm.LLMService;
import com.ke.setting.configuration.genral.user.UserConfigState;

import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/27 16:05
 * @Description
 */
public class LLMServiceImpl implements LLMService {


    private final PTSHandler ptsHandler = PTSHandler.getInstance();

    private final Integer port = SideCarAgentManager.getInstance().getAgentPort();

    /**
     * 获取指定提供商的所有大模型名称
     */
    @Override
    public Set<String> getLLMs(String provider) {
        LLMProviderDTO providers = ptsHandler.getProviders(port);
        return providers.getProviders().get(provider).getModules().stream().map(ModelDetail::getName).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 与大模型进行对话
     */
    @Override
    public String chat(String content) {
        return ptsHandler.chat(port, ChatQueryDTO.builder().query(content).modelConfiguration(UserConfigState.getInstance().getPluginConfig().getModelConfiguration()).build());
    }
}
