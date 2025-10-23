package com.ke.agentic.socket.handler;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.ke.agentic.SideCarUserActionTrace;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.UserActionTraceDTO;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

public class StartUserActionMonitorHandler implements RequestHandler{

    private static final Logger LOG = Logger.getInstance(StartUserActionMonitorHandler.class);

    @Override
    public String getPath() {
        return "/agentic/start_user_action_monitor";
    }

    @Override
    public NanoHTTPD.Method getMethod() {
        return NanoHTTPD.Method.POST;
    }

    @Override
    public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
        UserActionTraceDTO userActionTraceDTO = JsonUtil.getData(requestEntity.getBody(), UserActionTraceDTO.class);
        project.getService(SideCarUserActionTrace.class).startUserActionMonitor(userActionTraceDTO.getSessionId());
        return null;
    }
}
