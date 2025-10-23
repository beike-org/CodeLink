package com.ke.webview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/11 14:31
 * @Description 同步给sidecar的IDE信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SideCarIDEInfo {

	// 项目唯一标识，locationHash
	private String projectId;

	// socket url
	private String editorUrl;

	// 启动的socket进程的端口
	private Integer socketPort;

	// sidecar的端口
	private Integer sideCarPort;

	// 项目根路径
	private String rootDirectory;

	// 所有打开的文件(绝对路径)
	private Set<String> allFiles;

	// 当前编辑的文件(绝对路径),以及用户通过对话框选中的文件
	private Set<String> openFiles;

	// 执行命令
	private String shell;

	// agent是否自动运行
	private Boolean autoRun;

}
