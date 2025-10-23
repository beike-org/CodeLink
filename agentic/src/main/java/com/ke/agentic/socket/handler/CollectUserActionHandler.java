package com.ke.agentic.socket.handler;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import com.ke.agentic.SideCarUserActionTrace;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.AgentUserActionMonitorDTO;
import fi.iki.elonen.NanoHTTPD;

public class CollectUserActionHandler implements RequestHandler {

    private static final Logger LOG = Logger.getInstance(CollectUserActionHandler.class);

    @Override
    public String getPath() {
        return "/agentic/collect_user_action";
    }

    @Override
    public NanoHTTPD.Method getMethod() {
        return NanoHTTPD.Method.POST;
    }

    @Override
    public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
        AgentUserActionMonitorDTO agentUserActionMonitorDTO = AgentUserActionMonitorDTO.builder()
                .changedFiles(project.getService(SideCarUserActionTrace.class).getChangedFiles())
                .build();
        return parseResponse(NanoHTTPD.Response.Status.OK, agentUserActionMonitorDTO);

    }
}
