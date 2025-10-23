package com.ke.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.ke.Bundle;
import com.ke.rules.dto.ProjectRuleDTO;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Service(Service.Level.PROJECT)
public final class ProjectRuleManager {
	private static final Logger LOGGER = Logger.getInstance(ProjectRuleManager.class);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final static String DEFAULT_CONFIG = "";
	private static final String DEFAULT_GLOBAL_RULE_CONTENT = Bundle.get("component.rule.global.default");

	private final Path globalRulePath = Paths.get(PathManager.getConfigPath(), ".idea", "global_rules.txt");
	private final Path projectRulePath;

	public ProjectRuleManager(Project project) {
		String basePath = project.getBasePath();
		if (basePath != null) {
			projectRulePath = Paths.get(basePath, ".idea", "project_rules.json");
		} else {
			// 如果项目路径为空，使用临时目录
			projectRulePath = Paths.get(System.getProperty("java.io.tmpdir"), "ke_copilot", "project_rules.json");
			LOGGER.warn("Project base path is null, using temporary directory for project rules: " + projectRulePath);
		}
	}

	public static String getDefaultGlobalRuleContent() {
		return DEFAULT_GLOBAL_RULE_CONTENT;
	}

	/**
	 * 读取应用级别规则
	 */
	public String getAppRule() {
		try {
			// 确保目录存在
			File configFile = globalRulePath.toFile();
			if (!configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}

			// 如果文件不存在，创建一个空的配置文件
			if (!Files.exists(globalRulePath)) {
				// 使用临时文件写入
				Path tempFile = Files.createTempFile(configFile.getParentFile().toPath(), "rules", ".tmp");
				Files.writeString(tempFile, DEFAULT_CONFIG, StandardCharsets.UTF_8);
				Files.move(tempFile, globalRulePath, StandardCopyOption.REPLACE_EXISTING);
				return DEFAULT_CONFIG;
			}

			return Files.readString(globalRulePath);
		} catch (IOException e) {
			LOGGER.warn("Failed to read app level rules:", e);
			return null;
		}
	}

	/**
	 * 保存应用级别的规则
	 */
	public void saveAppRule(String rules) {
		try {
			// 确保目录存在
			File configFile = globalRulePath.toFile();
			if (!configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}

			// 使用临时文件写入
			Path tempFile = Files.createTempFile(configFile.getParentFile().toPath(), "rules", ".tmp");
			Files.writeString(tempFile, rules, StandardCharsets.UTF_8);
			Files.move(tempFile, globalRulePath, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			LOGGER.warn("Failed to save app level rules:", e);
		}
	}

	/**
	 * 读取项目级别规则
	 */
	public List<ProjectRuleDTO> getProjectRules() {
		try {
			if (!Files.exists(projectRulePath)) {
				return new ArrayList<>();
			}
			String content = Files.readString(projectRulePath);
			List<ProjectRuleDTO> rules = GSON.fromJson(content, new TypeToken<List<ProjectRuleDTO>>() {
			}.getType());
			return rules != null ? rules : new ArrayList<>();
		} catch (IOException e) {
			LOGGER.warn("Failed to read project level rules:", e);
			return new ArrayList<>();
		}
	}


	public void editProjectRules(ProjectRuleDTO rule) {
		try {
			// 确保目录存在
			File ruleFile = projectRulePath.toFile();
			if (!ruleFile.getParentFile().exists()) {
				ruleFile.getParentFile().mkdirs();
			}

			List<ProjectRuleDTO> rules;
			if (!Files.exists(projectRulePath)) {
				rules = new ArrayList<>();
			} else {
				String content = Files.readString(projectRulePath);
				rules = GSON.fromJson(content, new TypeToken<List<ProjectRuleDTO>>() {
				}.getType());
				if (rules == null) {
					rules = new ArrayList<>();
				}
			}

			// 找到旧规则
			Optional<ProjectRuleDTO> oldRule = rules.stream()
					.filter(r -> r.getName().equals(rule.getName()))
					.findFirst();

			// 如果找到旧规则，先删除它
			if (oldRule.isPresent()) {
				rules.removeIf(r -> r.getName().equals(rule.getName()));
			}

			// 添加新规则
			rules.add(rule);

			// 使用临时文件写入
			Path tempFile = Files.createTempFile(ruleFile.getParentFile().toPath(), "rules", ".tmp");
			Files.writeString(tempFile, GSON.toJson(rules), StandardCharsets.UTF_8);
			Files.move(tempFile, projectRulePath, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			LOGGER.warn("Failed to edit project rules:", e);
		}
	}


	public void deleteProjectRules(String ruleName) {
		try {
			// 确保目录存在
			File ruleFile = projectRulePath.toFile();
			if (!ruleFile.getParentFile().exists()) {
				ruleFile.getParentFile().mkdirs();
			}

			// 如果文件不存在，创建空的规则列表
			if (!Files.exists(projectRulePath)) {
				Files.writeString(projectRulePath, "[]", StandardCharsets.UTF_8);
				return;
			}

			String content = Files.readString(projectRulePath);
			List<ProjectRuleDTO> rules = GSON.fromJson(content, new TypeToken<List<ProjectRuleDTO>>() {
			}.getType());
			if (rules == null) {
				rules = new ArrayList<>();
			}
			rules.removeIf(rule -> rule.getName().equals(ruleName));

			// 使用临时文件写入
			Path tempFile = Files.createTempFile(ruleFile.getParentFile().toPath(), "rules", ".tmp");
			Files.writeString(tempFile, GSON.toJson(rules), StandardCharsets.UTF_8);
			Files.move(tempFile, projectRulePath, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			LOGGER.warn("Failed to delete project rules:", e);
		}
	}

}