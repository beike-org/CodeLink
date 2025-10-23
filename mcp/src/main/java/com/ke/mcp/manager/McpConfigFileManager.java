package com.ke.mcp.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.ke.mcp.dto.*;
import com.ke.mcp.enums.AvailableStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public class McpConfigFileManager {
	private final static Logger LOG = Logger.getInstance(McpConfigFileManager.class);
	public static final String MCP_JSON = "mcp.json";
	public static final String MCP_AVAILABLE_JSON = "mcp_available.json";
	public static final ObjectMapper mapper = new ObjectMapper();
	private final Project project;

	@Getter
	@Setter
	private Integer configSocketPort;

	@Getter
	@Setter
	private Integer configAgentPort;

	//保存从配置文件读取的，包括删除和禁用的
	@Getter
	public final Map<String, McpConfigDTO> mcpConfigDTOMap;


	public McpConfigFileManager(Project project) {
		this.project = project;
		this.mcpConfigDTOMap = new ConcurrentHashMap<>();
		this.configAgentPort = 0;
		this.configSocketPort = 0;
	}

	/**
	 * 解析mcp.json文件
	 */
	public McpParsedConfig parseConfig(File jsonFile) {
		List<CommandConfigDTO> commandConfigDTOS = new ArrayList<>();
		List<SseConfigDTO> sseConfigDTOS = new ArrayList<>();

		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
			JsonNode root = mapper.readTree(json);

			JsonNode serversNode = root.get("mcpServers");
			if (serversNode != null && serversNode.isObject()) {
				Iterator<Map.Entry<String, JsonNode>> fields = serversNode.fields();

				while (fields.hasNext()) {
					Map.Entry<String, JsonNode> entry = fields.next();
					String name = entry.getKey();
					JsonNode configNode = entry.getValue();

					if (configNode.has("command")) {
						List<String> argList = mapper.convertValue(configNode.get("args"), new TypeReference<>() {
						});
						String command = mapper.convertValue(configNode.get("command"), String.class);
						String joinedArgs = (argList != null && !argList.isEmpty()) ? String.join(" ", argList) : "";

						// 解析 env 字段（可选）
						Map<String, String> envMap = null;
						if (configNode.has("env")) {
							envMap = mapper.convertValue(configNode.get("env"), new TypeReference<>() {
							});
						}

						// 构造支持 env 的 CommandConfigDTO
						CommandConfigDTO commandDTO = new CommandConfigDTO(name, command, joinedArgs, false, envMap);

						commandConfigDTOS.add(commandDTO);
						mcpConfigDTOMap.put(name, commandDTO);
					} else if (configNode.has("url")) {
						String url = configNode.get("url").asText();
						// 解析 env 字段（可选）
						Map<String, String> envMap = null;
						if (configNode.has("env")) {
							envMap = mapper.convertValue(configNode.get("env"), new TypeReference<>() {
							});
						}
						SseConfigDTO sse = new SseConfigDTO(name, url, false, envMap);
						sseConfigDTOS.add(sse);
						mcpConfigDTOMap.put(name, sse);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("mcp配置文件解析失败");
		}

		return McpParsedConfig.builder()
				.commandMcp(commandConfigDTOS)
				.sseMcp(sseConfigDTOS)
				.build();
	}

	/**
	 * 解析mcp可用配置文件
	 */
	public List<AvailableConfigDTO> parseAvailableConfig(File jsonFile) {
		List<AvailableConfigDTO> configList = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			if (!jsonFile.exists()) {
				return configList;
			}
			String json = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
			if (json.isBlank()) {
				return configList;
			}

			JsonNode root = mapper.readTree(json);

			// 确保根节点是数组
			if (root.isArray()) {
				for (JsonNode node : root) {
					// node 类似 {"youtube": {"status": "enabled", "delete": true}}
					// 需要遍历它的 key
					Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
					while (fields.hasNext()) {
						Map.Entry<String, JsonNode> entry = fields.next();
						String name = entry.getKey();
						JsonNode value = entry.getValue();

						// 解析 status
						String statusStr = value.has("status") ? value.get("status").asText() : "";
						AvailableStatusEnum status = AvailableStatusEnum.fromString(statusStr);

						// 解析 delete
						boolean deleteFlag = value.has("delete") && value.get("delete").asBoolean(false);

						// 使用 Builder 方式构建 AvailableConfigDTO
						AvailableConfigDTO configDTO = AvailableConfigDTO.builder()
								.name(name)
								.status(status)
								.delete(deleteFlag)
								.build();

						configList.add(configDTO);

						// 如果 mcpConfigDTOMap 中有对应的配置，更新它的状态
						McpConfigDTO mcpConfigDTO = mcpConfigDTOMap.get(name);
						if (mcpConfigDTO != null) {
							// 更新 delete 标志
							mcpConfigDTO.setDelete(deleteFlag);
							// 更新 enable/disable 状态
							mcpConfigDTO.setStatus(status);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("mcp_available配置文件解析失败");
		}

		return configList;
	}


	public void deleteConfig(String serverName) {
		McpConfigDTO mcpConfigDTO = mcpConfigDTOMap.get(serverName);
		if (mcpConfigDTO != null) {
			mcpConfigDTO.setDelete(true);
			mcpConfigDTOMap.remove(serverName);
			Path availableFilePath = Paths.get(Objects.requireNonNull(project.getBasePath()), ".idea", MCP_AVAILABLE_JSON);
			File availableFile = availableFilePath.toFile();
			List<AvailableConfigDTO> availableConfigDTOS = parseAvailableConfig(availableFile);

			availableConfigDTOS.removeIf(a -> a.getName().equals(serverName));
			AvailableConfigDTO updated = AvailableConfigDTO.builder()
					.name(serverName)
					.status(mcpConfigDTO.getStatus())
					.delete(true)
					.build();
			availableConfigDTOS.add(updated);

			// 4. 写回文件
			try {
				writeToAvailableJson(availableFile, availableConfigDTOS);
			} catch (IOException e) {
				LOG.warn(e.getMessage());
			}
		}
	}


	public void switchConfig(String serverName, AvailableStatusEnum statusEnum) {
		McpConfigDTO mcpConfigDTO = mcpConfigDTOMap.get(serverName);
		if (mcpConfigDTO != null) {
			Path availableFilePath = Paths.get(Objects.requireNonNull(project.getBasePath()), ".idea", MCP_AVAILABLE_JSON);
			File availableFile = availableFilePath.toFile();
			List<AvailableConfigDTO> availableConfigDTOS = parseAvailableConfig(availableFile);
			availableConfigDTOS.removeIf(a -> a.getName().equals(serverName));
			AvailableConfigDTO updated = AvailableConfigDTO.builder()
					.name(serverName)
					.status(statusEnum)
					.delete(mcpConfigDTO.isDelete())
					.build();
			availableConfigDTOS.add(updated);
			mcpConfigDTO.setStatus(statusEnum);
			mcpConfigDTOMap.put(serverName, mcpConfigDTO);

			// 4. 写回文件
			try {
				writeToAvailableJson(availableFile, availableConfigDTOS);
			} catch (IOException e) {
				LOG.warn(e.getMessage());
			}
		}
	}


	/**
	 * 增加或更新 MCP 配置，并写入到 mcp.json、mcp_available.json 文件，同时通知 sidecar
	 *
	 * @param config  需要新增或更新的配置
	 * @param addFlag true表示新增，false表示更新
	 *                在EditButton和项目启动处处调用的就不谈配置已存在覆盖了
	 */
	public void addOrUpdateConfig(McpConfigDTO config, Boolean addFlag, Boolean notify) {
		try {
			Path configDir = Paths.get(PathManager.getConfigPath(), ".idea");
			Path availableDir = Paths.get(Objects.requireNonNull(project.getBasePath()), ".idea");

			Path configFilePath = configDir.resolve(MCP_JSON);
			Path availableFilePath = availableDir.resolve(MCP_AVAILABLE_JSON);

			File configFile = configFilePath.toFile();
			File availableFile = availableFilePath.toFile();

			// 解析本地已保存的配置
			McpParsedConfig mcpParsedConfig = parseConfig(configFile);
			List<AvailableConfigDTO> availableConfigDTOS = parseAvailableConfig(availableFile);

			// 检查重名
			if (mcpConfigDTOMap.containsKey(config.getName())) {
				if (Boolean.FALSE.equals(mcpConfigDTOMap.get(config.getName()).isDelete())) {
					if (Boolean.TRUE.equals(notify)) {
						int result = Messages.showYesNoDialog(
								project,
								"配置已存在，是否覆盖？",
								"警告",
								Messages.getWarningIcon()
						);
						if (result != Messages.YES) {
							return;
						}
					}
				} else {
					addFlag = false;
				}
			}

			// 处理新增或更新逻辑
			if (Boolean.TRUE.equals(addFlag)) {
				// 新增逻辑
				if (config instanceof CommandConfigDTO commandConfigDTO) {
					mcpParsedConfig.getCommandMcp().removeIf(c -> c.getName().equals(commandConfigDTO.getName()));
					mcpParsedConfig.getCommandMcp().add(commandConfigDTO);
					mcpConfigDTOMap.put(commandConfigDTO.getName(), commandConfigDTO);

				} else if (config instanceof SseConfigDTO sseConfigDTO) {
					mcpParsedConfig.getSseMcp().removeIf(s -> s.getName().equals(sseConfigDTO.getName()));
					mcpParsedConfig.getSseMcp().add(sseConfigDTO);
					mcpConfigDTOMap.put(sseConfigDTO.getName(), sseConfigDTO);

				}

				// 新增时，默认添加为 ENABLED
				availableConfigDTOS.removeIf(a -> a.getName().equals(config.getName()));
				availableConfigDTOS.add(new AvailableConfigDTO(config.getName(), AvailableStatusEnum.ENABLED, false));

			} else {
				// 更新逻辑
				McpConfigDTO existingConfig = mcpConfigDTOMap.get(config.getName());
				config.setDelete(false);
				config.setStatus(AvailableStatusEnum.ENABLED);

				availableConfigDTOS.removeIf(s -> s.getName().equals(config.getName()));
				availableConfigDTOS.add(new AvailableConfigDTO(config.getName(), AvailableStatusEnum.ENABLED, false));

				if (existingConfig != null) {
					// 移除旧配置
					mcpParsedConfig.getCommandMcp().removeIf(c -> c.getName().equals(config.getName()));
					mcpParsedConfig.getSseMcp().removeIf(s -> s.getName().equals(config.getName()));

					// 重新添加
					if (config instanceof CommandConfigDTO) {
						mcpParsedConfig.getCommandMcp().add((CommandConfigDTO) config);
					} else if (config instanceof SseConfigDTO) {
						mcpParsedConfig.getSseMcp().add((SseConfigDTO) config);
					}

					mcpConfigDTOMap.put(config.getName(), config);
				}
			}


			// 分别写入 mcp.json 和 mcp_available.json
			writeToMcpJson(configFile, mcpParsedConfig);
			writeToAvailableJson(availableFile, availableConfigDTOS);

		} catch (Exception e) {
			LOG.warn(e.getMessage());
			Messages.showErrorDialog(project, e.getMessage(), "操作失败");
		}
	}

	/**
	 * 将当前内存中的配置写入 mcp.json
	 *
	 * @param configFile      mcp.json 文件
	 * @param mcpParsedConfig 包含 commandMcp 和 sseMcp 的解析对象
	 */
	private static void writeToMcpJson(File configFile, McpParsedConfig mcpParsedConfig) throws IOException {
		Map<String, Object> serversNode = new LinkedHashMap<>();

		// command配置
		for (CommandConfigDTO commandDTO : mcpParsedConfig.getCommandMcp()) {
			Map<String, Object> node = new HashMap<>();
			node.put("command", commandDTO.getCommand());
			node.put("args", commandDTO.getArgs());
			if (commandDTO.getEnv() != null && !commandDTO.getEnv().isEmpty()) {
				node.put("env", commandDTO.getEnv());
			}
			serversNode.put(commandDTO.getName(), node);
		}

		// sse配置
		for (SseConfigDTO sseDTO : mcpParsedConfig.getSseMcp()) {
			Map<String, Object> node = new HashMap<>();
			node.put("url", sseDTO.getUrl());
			if (sseDTO.getEnv() != null && !sseDTO.getEnv().isEmpty()) {
				node.put("env", sseDTO.getEnv());
			}
			serversNode.put(sseDTO.getName(), node);
		}

		// 组装最终JSON
		Map<String, Object> finalJson = new HashMap<>();
		finalJson.put("mcpServers", serversNode);

		// 写入文件
		mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, finalJson);
	}

	/**
	 * 将状态写入 mcp_available.json
	 *
	 * @param availableFile       mcp_available.json 文件
	 * @param availableConfigDTOS 配置状态列表
	 */
	public static void writeToAvailableJson(File availableFile, List<AvailableConfigDTO> availableConfigDTOS) throws IOException {
		List<Map<String, Map<String, Object>>> availableJsonList = availableConfigDTOS.stream().map(a -> {
			Map<String, Object> statusMap = new HashMap<>();
			statusMap.put("status", a.getStatus().toString().toLowerCase());
			statusMap.put("delete", a.isDelete());
			Map<String, Map<String, Object>> configMap = new HashMap<>();
			configMap.put(a.getName(), statusMap);
			return configMap;
		}).collect(Collectors.toList());

		// 写入文件
		mapper.writerWithDefaultPrettyPrinter().writeValue(availableFile, availableJsonList);
	}


}

