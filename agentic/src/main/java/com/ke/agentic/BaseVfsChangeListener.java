package com.ke.agentic;

import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/4 10:26
 * @Version 1.0
 * @Description 监控删、移动文件的事件
 */
public abstract class BaseVfsChangeListener implements BulkFileListener {
	@Override
	public void after(@NotNull List<? extends VFileEvent> events) {

		for (VFileEvent event : events) {
			if (event instanceof VFilePropertyChangeEvent || event instanceof VFileMoveEvent) {
				String oldPath;
				String newPath;
				if (event instanceof VFilePropertyChangeEvent) {
					oldPath = ((VFilePropertyChangeEvent) event).getOldPath();
					newPath = ((VFilePropertyChangeEvent) event).getNewPath();
				} else {
					oldPath = ((VFileMoveEvent) event).getOldPath();
					newPath = ((VFileMoveEvent) event).getNewPath();
				}
				afterMove(event, oldPath, newPath);
			} else if (event instanceof VFileDeleteEvent) {
				String path = event.getPath();
				afterDelete(event, path);
			}
		}
	}

	public abstract void afterMove(VFileEvent event, String oldPath, String newPath);

	public abstract void afterDelete(VFileEvent event, String path);
}
