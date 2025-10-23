package com.ke.agentic.webview.handler.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgenticRejectChangeDTO {

    private List<String> fileName;

    private List<String> oldContent;
}
