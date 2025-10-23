package com.ke.agentic.webview.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchDirectoryRespDTO {
	private String uuid;
	private List<FileTreeNode> fileTreeNodes;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class FileTreeNode {
		private String name;
		private String path;
		private boolean isDirectory;
		private List<FileTreeNode> children;
	}
}