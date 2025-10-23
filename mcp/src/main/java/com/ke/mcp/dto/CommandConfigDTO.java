package com.ke.mcp.dto;

import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.McpTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class CommandConfigDTO extends McpConfigDTO {
	private String command;
	private String argString;
	private List<String> args;

	public CommandConfigDTO(String name, String command, String argString) {
		super(name, McpTypeEnum.COMMAND, AvailableStatusEnum.ENABLED, false, null);
		this.command = command;
		this.argString = argString;
		this.args = new ArrayList<>(Arrays.asList(argString.trim().split("\\s+")));
		this.args.removeIf(String::isEmpty);
	}

	public CommandConfigDTO(String name, String command, String argString, Map<String, String> env) {
		super(name, McpTypeEnum.COMMAND, AvailableStatusEnum.ENABLED, false, env);
		this.command = command;
		this.argString = argString;
		this.args = new ArrayList<>(Arrays.asList(argString.trim().split("\\s+")));
		this.args.removeIf(String::isEmpty);
	}


	public CommandConfigDTO(String name, String command, String argString, boolean delete) {
		super(name, McpTypeEnum.COMMAND, AvailableStatusEnum.ENABLED, delete, null);
		this.command = command;
		this.argString = argString;
		this.args = new ArrayList<>(Arrays.asList(argString.trim().split("\\s+")));
		this.args.removeIf(String::isEmpty);
	}

	public CommandConfigDTO(String name, String command, String argString, boolean delete, Map<String, String> env) {
		super(name, McpTypeEnum.COMMAND, AvailableStatusEnum.ENABLED, delete, env);
		this.command = command;
		this.argString = argString;
		this.args = new ArrayList<>(Arrays.asList(argString.trim().split("\\s+")));
		this.args.removeIf(String::isEmpty);
	}
}

