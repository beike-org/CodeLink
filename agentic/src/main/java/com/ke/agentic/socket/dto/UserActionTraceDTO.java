package com.ke.agentic.socket.dto;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserActionTraceDTO {

    @JSONField(name = "session_id")
    private String sessionId;

}
