package com.ke.agentic.pts;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.ke.agentic.SideCarAgentManager;
import com.ke.agentic.pts.dto.ChatQueryDTO;
import com.ke.agentic.pts.dto.LLMProviderDTO;
import com.ke.agentic.pts.resp.SideCarResp;
import com.ke.exception.BusinessException;
import com.ke.exception.ExceptionEnum;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PTSHandler implements Disposable {

	private static final Logger logger = Logger.getInstance(PTSHandler.class);
	public static final String BASE_URL_PREFIX = "http://localhost:";



	// Retrofit实例缓存
	public final Map<Integer, Object> apiMap = new ConcurrentHashMap<>();

	@NotNull
	public static PTSHandler getInstance() {
		return ApplicationManager.getApplication().getService(PTSHandler.class);
	}

	@Override
	public void dispose() {
		apiMap.clear();
	}

	@NotNull
	public PTSApi getApi(Integer port) {
		try {
			if (port == 0) {
				port = SideCarAgentManager.getInstance().getAgentPort();
			}
			Integer finalPort = port;
			return (PTSApi) apiMap.computeIfAbsent(finalPort, k -> {
				OkHttpClient client = new OkHttpClient.Builder()
						.connectTimeout(60, TimeUnit.SECONDS)
						.readTimeout(60, TimeUnit.SECONDS)
						.build();
				Retrofit retrofit = new Retrofit.Builder()
						.baseUrl(BASE_URL_PREFIX + finalPort)
						.addConverterFactory(JacksonConverterFactory.create())
						.client(client)
						.build();

				return retrofit.create(PTSApi.class);
			});
		} catch (Exception e) {
			logger.error("Failed to create or get PTSApi instance for port: " + port, e);
			throw new RuntimeException("Failed to create or get PTSApi instance for port: " + port, e);
		}
	}

	public <T> T executeCall(retrofit2.Call<T> call) throws RuntimeException {
		try {
			T response = call.execute().body();
			if (response == null) {
				logger.warn("Empty response from server");
				ExceptionEnum.HTTP_REQUEST_EXCEPTION.asBusinessException("Empty response from server");
				return null;
			}
			if(response instanceof SideCarResp<?> sideCarResp && !sideCarResp.isSuccess()) {
				logger.warn("Server error: " + JSONObject.toJSON(response));
				ExceptionEnum.HTTP_REQUEST_EXCEPTION.asBusinessException("Server error: " + sideCarResp.getError());
				return null;
			}
			return response;
		} catch (IOException e) {
			logger.warn("API call error", e);
			return null;
		}
	}


	/**
	 * 健康检查
	 */
	@NotNull
	public Boolean healthCheckPort(@NotNull Integer port) throws RuntimeException {
		try {
			return Boolean.TRUE.equals(executeCall(getApi(port).healthCheck()).getDone());
		}catch (BusinessException ignore){
			return false;
		}
	}

	/**
	 * chat调用大模型
	 */
	@NotNull
	public String chat(@NotNull Integer port, @NotNull ChatQueryDTO chatQueryDTO) throws RuntimeException {
		return executeCall(getApi(port).chat(chatQueryDTO)).getData();
	}

	/**
	 * 获取模型列表
	 */
	@NotNull
	public LLMProviderDTO getProviders(@NotNull Integer port) throws RuntimeException {
		return executeCall(getApi(port).getProviders()).getData();
	}

}
