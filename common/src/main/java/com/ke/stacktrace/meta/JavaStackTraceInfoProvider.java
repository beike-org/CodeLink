package com.ke.stacktrace.meta;

import com.intellij.openapi.editor.Editor;
import com.ke.stacktrace.entity.SelectedTraceback;
import com.ke.utils.EditorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/12/11 14:41
 * @Version 1.0
 * @Description Java堆栈信息获取器
 */
public class JavaStackTraceInfoProvider implements StackTraceInfoProvider {

	@NotNull
	@Override
	public SelectedTraceback getSelectTraceback(Editor editor, int startOffset) {

		int lineFrom = EditorUtil.getLineNumber(editor, startOffset);

		List<String> textList = Arrays.asList(editor.getDocument().getText().split("\n"));
		int lineTo = textList.size();
		for (int i = lineFrom; i < textList.size(); i++) {
			String lineContent = textList.get(i);
			if (!isExceptionLine(lineContent)) {
				lineTo = i;
				break;
			}
		}

		String traceback = org.apache.commons.lang3.StringUtils.join(textList.subList(lineFrom, lineTo), "\n");
		return SelectedTraceback.builder()
				.traceback(org.apache.commons.lang3.StringUtils.join(new String[]{"```\n", traceback, "\n```\n"}))
				.lineFrom(lineFrom).lineTo(lineTo).build();
	}

	/**
	 * 判断是否是异常行
	 */
	public static boolean isExceptionLine(String lineContent) {
		return lineContent.trim().startsWith("at ") || lineContent.trim().startsWith("Caused by") || lineContent.trim().startsWith("...");
	}
}
