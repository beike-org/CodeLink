package com.ke.agentic.pts;

import com.ke.agentic.pts.dto.ChatQueryDTO;
import com.ke.agentic.pts.dto.LLMProviderDTO;
import com.ke.agentic.pts.resp.HealthCheckResponse;
import com.ke.agentic.pts.resp.SideCarResp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PTSApi {
	@GET("/api/health")
	Call<HealthCheckResponse> healthCheck();


	@POST("/api/plugin/completion/chat")
	Call<SideCarResp<String>> chat(@Body ChatQueryDTO chatQueryDTO);


	@GET("/api/plugin/llm_properties/get")
	Call<SideCarResp<LLMProviderDTO>> getProviders();
}