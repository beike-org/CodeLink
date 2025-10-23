package com.ke.agentic.reg;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.ke.utils.BaseLocalBinaryToolUtil;
import com.ke.utils.PluginUtil;

import java.nio.file.Path;
import java.util.List;

public final class RegUtil {


	public static final Path REG_BIN_DIR = PluginUtil.getPluginBasePath().resolve("agentic-bin/reg");

	public static final String REG_NAME_WIN = "reg.exe";

	public static final String REG_NAME_MAC_ARM = "reg-mac-arm";

	public static final String REG_NAME_MAC_AMD = "reg-mac-amd";



	/**
	 * 获取sidecar agent的可执行文件（mac和windows）
	 */
	public static String getBinaryPath() {
		return BaseLocalBinaryToolUtil.getBinaryPath(REG_BIN_DIR, REG_NAME_WIN, REG_NAME_MAC_ARM, REG_NAME_MAC_AMD, "reg");
	}


	/**
	 * 获取sidecar agent命令行
	 */
	public static GeneralCommandLine getCommandline(List<String> args) {
		return BaseLocalBinaryToolUtil.getCommandline(args, getBinaryPath());
	}


}
