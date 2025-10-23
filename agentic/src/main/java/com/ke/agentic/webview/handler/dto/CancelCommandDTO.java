package com.ke.agentic.webview.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/19 16:34
 * @Version 1.0
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelCommandDTO {

    private String commandId;

}
