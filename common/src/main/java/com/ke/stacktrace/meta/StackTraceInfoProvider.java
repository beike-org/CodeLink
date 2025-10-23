package com.ke.stacktrace.meta;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.ke.stacktrace.entity.SelectedTraceback;
import org.jetbrains.annotations.NotNull;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/12/11 14:35
 * @Version 1.0
 * @Description 根据语言栈注册不同的堆栈信息获取器
 */
public interface StackTraceInfoProvider {

	ExtensionPointName<StackTraceInfoProvider> STACK_TRACE_INFO_PROVIDER = ExtensionPointName.create("com.ke.codelink.stackTraceInfoProvider");

	/**
	 * 获取选中的堆栈信息
	 */
	@NotNull
	SelectedTraceback getSelectTraceback(Editor editor, int startOffset);
}
