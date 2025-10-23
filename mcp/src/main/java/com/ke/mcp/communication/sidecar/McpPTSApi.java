package com.ke.mcp.communication.sidecar;


import com.ke.agentic.pts.PTSApi;
import com.ke.agentic.pts.resp.SideCarResp;
import com.ke.mcp.dto.resp.SwitchAvailableResp;
import com.ke.mcp.dto.resp.ToolListResp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface McpPTSApi extends PTSApi {

	@POST("/api/mcp/launch")
	Call<SideCarResp<Void>> launchMcp(@Body Object request);

	@POST("/api/mcp/tools/list")
	Call<SideCarResp<List<ToolListResp>>> getToolList(@Body Object request);

	@POST("/api/mcp/available/switch")
	Call<SideCarResp<SwitchAvailableResp>> switchAvailable(@Body Object request);

	@POST("/api/mcp/shutdown")
	Call<SideCarResp<Void>> shutdownMcp(@Body Object request);

	@POST("/api/mcp/tools/update")
	Call<SideCarResp<Void>> updateTools(@Body Object request);

	@POST("/api/mcp/delete")
	Call<SideCarResp<Void>> deleteMcpServers(@Body Object request);

}
