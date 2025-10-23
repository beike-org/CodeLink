package com.ke.mcp.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteServerReq {
    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("server_name")
    private String serverName;
}
