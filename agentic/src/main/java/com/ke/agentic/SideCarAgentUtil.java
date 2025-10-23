package com.ke.agentic;

import com.google.common.collect.Sets;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.ke.utils.BaseLocalBinaryToolUtil;
import com.ke.utils.FileUtil;
import com.ke.utils.LocalUtil;
import com.ke.utils.PluginUtil;
import com.ke.webview.SideCarIDEInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

public final class SideCarAgentUtil {


	public static final String SIDECAR_AGENT_NAME_WIN = "webserver.exe";

	public static final String SIDECAR_AGENT_NAME_MAC_ARM = "webserver-mac-arm";

	public static final String SIDECAR_AGENT_NAME_MAC_AMD = "webserver-mac-amd";


	/**
	 * 获取sidecar agent的可执行文件目录
	 */
	public static Path getSideCarBinDir() {
		return PluginUtil.getPluginBasePath().resolve("agentic-bin/sidecar");
	}


	/**
	 * 获取sidecar agent的可执行文件（mac和windows）
	 */
	public static String getBinaryPath() {
		return BaseLocalBinaryToolUtil.getBinaryPath(getSideCarBinDir(), SIDECAR_AGENT_NAME_WIN, SIDECAR_AGENT_NAME_MAC_ARM, SIDECAR_AGENT_NAME_MAC_AMD, "sidecar agent");
	}


	/**
	 * 获取sidecar agent命令行
	 */
	public static GeneralCommandLine getCommandline(List<String> args) {
		return BaseLocalBinaryToolUtil.getCommandline(args, getBinaryPath());
	}


	/**
	 * 生成SideCarIDEInfo
	 */
	public static SideCarIDEInfo generateSideCarIDEInfo(@NotNull Project project) {

		SideCarIDEInfo sideCarIDEInfo = SideCarIDEInfo.builder()
				.projectId(project.getLocationHash())
				.rootDirectory(FileUtil.getProjectRootPath(project))
				.shell(LocalUtil.getDefaultShell())
				.socketPort(SideCarAgentManager.getInstance().getSocketPort())
				.editorUrl("http://127.0.0.1:" + SideCarAgentManager.getInstance().getSocketPort() + "/")
				.sideCarPort(SideCarAgentManager.getInstance().getAgentPort())
				.build();
		updateSideCarIDEInfo(project, sideCarIDEInfo);
		return sideCarIDEInfo;
	}


	/**
	 * 更新SideCarIDEInfo
	 */
	public static void updateSideCarIDEInfo(@NotNull Project project, @NotNull SideCarIDEInfo sideCarIDEInfo) {
		AtomicReference<String> currentEditFile = new AtomicReference<>();
		Set<String> allOpenFiles = Sets.newHashSet();
		ReadAction.compute(() -> {
			FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
			FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
			if (Objects.nonNull(selectedEditor)) {
				VirtualFile file = selectedEditor.getFile();
				if (Objects.nonNull(file)) {
					String path = file.getPath();
					if (SystemInfoRt.isWindows){
						path = path.replaceAll("/", Matcher.quoteReplacement(File.separator));
					}
					currentEditFile.set(path);
				}
			}

			for (FileEditor fileEditor : fileEditorManager.getAllEditors()) {
				VirtualFile file = fileEditor.getFile();
				if (Objects.nonNull(file)) {
					String path = file.getPath();
					if (SystemInfoRt.isWindows) {
						path = path.replaceAll("/", Matcher.quoteReplacement(File.separator));
					}
					allOpenFiles.add(path);
				}
			}

			return true;
		});
		Set<String> openFiles = Sets.newHashSet();
		openFiles.add(currentEditFile.get());
		sideCarIDEInfo.setOpenFiles(openFiles);
		sideCarIDEInfo.setAllFiles(allOpenFiles);
	}

}
