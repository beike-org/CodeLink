package com.ke.mcp.activity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.ke.mcp.dto.AvailableConfigDTO;
import com.ke.mcp.dto.CommandConfigDTO;
import com.ke.mcp.dto.McpParsedConfig;
import com.ke.mcp.dto.SseConfigDTO;
import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.manager.McpConfigFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 它在sidecar启动之后执行
 * 项目启动时mcp配置初始化
 * 项目启动，如果没有，创建配置文件。如果有，读取配置项
 */
public class McpConfigStartUpActivity implements StartupActivity.Background {
	private final static Logger LOG = Logger.getInstance(McpConfigStartUpActivity.class);
	private final static String DEFAULT_CONFIG = "{\"mcpServers\":{}}";
	public static final String MCP_JSON = "mcp.json";
	public static final String MCP_AVAILABLE_JSON = "mcp_available.json";


	@Override
	public void runActivity(@NotNull Project project) {
		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			try {
				LOG.info("start reading mcp config......");
				// 1. IDE 全局配置 .idea/mcp.json
				Path configDir = Paths.get(PathManager.getConfigPath(), ".idea");
				Path configFilePath = configDir.resolve(MCP_JSON);
				File configFile = configFilePath.toFile();
				boolean configFileExists = configFile.exists();
				if (!configFileExists) {
					configFile.getParentFile().mkdirs();
					Files.writeString(
							configFile.toPath(),
							DEFAULT_CONFIG,
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE_NEW
					);
					LocalFileSystem.getInstance().refreshAndFindFileByIoFile(configFile);
				}

				//2. 项目维度配置 .idea/mcp_available.json
				Path availableFilePath = Paths.get(Objects.requireNonNull(project.getBasePath()), ".idea", MCP_AVAILABLE_JSON);
				File availableFile = availableFilePath.toFile();
				boolean availableFileExists = availableFile.exists();
				if (!availableFileExists) {
					availableFile.getParentFile().mkdirs();
					availableFile.createNewFile();
					LocalFileSystem.getInstance().refreshAndFindFileByIoFile(availableFile);
				}

				McpParsedConfig mcpParsedConfig = project.getService(McpConfigFileManager.class).parseConfig(configFile);
				List<AvailableConfigDTO> availableConfigDTOS = new ArrayList<>();
				if (Boolean.FALSE.equals(availableFileExists)) {
					for (int i = 0; i < mcpParsedConfig.getSseMcp().size(); i++) {
						AvailableConfigDTO availableConfigDTO = new AvailableConfigDTO(mcpParsedConfig.getSseMcp().get(i).getName(), AvailableStatusEnum.ENABLED, false);
						availableConfigDTOS.add(availableConfigDTO);
					}
					for (int i = 0; i < mcpParsedConfig.getCommandMcp().size(); i++) {
						AvailableConfigDTO availableConfigDTO = new AvailableConfigDTO(mcpParsedConfig.getCommandMcp().get(i).getName(), AvailableStatusEnum.ENABLED, false);
						availableConfigDTOS.add(availableConfigDTO);
					}
					McpConfigFileManager.writeToAvailableJson(availableFile, availableConfigDTOS);
				} else {
					availableConfigDTOS = project.getService(McpConfigFileManager.class).parseAvailableConfig(availableFile);
				}

				//3.将状态设入mcpConfigDTOMap中
				for (AvailableConfigDTO availableConfigDTO : availableConfigDTOS) {
					for (CommandConfigDTO command : mcpParsedConfig.getCommandMcp()) {
						if (command.getName().equals(availableConfigDTO.getName())) {
							command.setStatus(availableConfigDTO.getStatus());
							command.setDelete(availableConfigDTO.isDelete());
						}
					}
					for (SseConfigDTO sse : mcpParsedConfig.getSseMcp()) {
						if (sse.getName().equals(availableConfigDTO.getName())) {
							sse.setStatus(availableConfigDTO.getStatus());
							sse.setDelete(availableConfigDTO.isDelete());
						}
					}
				}

				LOG.info("end reading mcp config......");
			} catch (IOException e) {
				LOG.error("read mcp config error", e);
			}
		});
	}
}
