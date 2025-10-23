package com.ke.service.llm;

import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/27 15:10
 * @Description
 */
public interface LLMService {

    /**
     * 获取指定提供商的所有大模型名称
     */
    Set<String> getLLMs(String provider);


    /**
     * 与大模型进行对话
     */
    String chat(String content);
}
