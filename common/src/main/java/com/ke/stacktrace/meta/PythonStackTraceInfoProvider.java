package com.ke.stacktrace.meta;

import com.intellij.openapi.editor.Editor;
import com.ke.stacktrace.entity.SelectedTraceback;
import com.ke.utils.EditorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/12/11 14:37
 * @Version 1.0
 * @Descriptio 通用的堆栈信息获取器
 */
public class PythonStackTraceInfoProvider implements StackTraceInfoProvider {

	@NotNull
	@Override
	public SelectedTraceback getSelectTraceback(Editor editor, int startOffset) {
		int lineFrom = EditorUtil.getLineNumber(editor, startOffset);

		List<String> textList = Arrays.asList(editor.getDocument().getText().split("\n"));
		int lineTo = textList.size();
		for (int i = lineFrom + 1; i < textList.size(); i++) {
			String lineContent = textList.get(i);
			if (!lineContent.startsWith(" ")) {
				lineTo = i + 1 == textList.size() ? i : i + 1;
				break;
			}
		}

		String traceback = org.apache.commons.lang3.StringUtils.join(textList.subList(lineFrom, lineTo), "\n");
		return SelectedTraceback.builder()
				.traceback(org.apache.commons.lang3.StringUtils.join(new String[]{"```\n", traceback, "\n```\n"}))
				.lineFrom(lineFrom).lineTo(lineTo).build();
	}
}
