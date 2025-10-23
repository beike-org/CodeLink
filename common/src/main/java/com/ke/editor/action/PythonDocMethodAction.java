package com.ke.editor.action;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.ke.exception.ExceptionEnum;
import com.ke.utils.EditorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/14 16:50
 * @Version 1.0
 * @Description
 */
public class PythonDocMethodAction extends DocMethodBaseAction {

	public PythonDocMethodAction(PsiElement psiElement) {
		super(psiElement);
	}

	@Override
	public String getDoc(String code, Editor editor) {
		if (Objects.isNull(code)) {
			return "";
		}

		Pattern pattern = Pattern.compile("\"\"\"(.*?)\"\"\"", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(code);

		if (matcher.find()) {
			String res = matcher.group(0);
			if(Objects.nonNull(editor)){
				String indentStr = getIndentStr(editor);
				String[] split = res.split("\n");
				if(split.length > 1){
					StringBuilder sb = new StringBuilder(split[0]);
					for (int i = 1; i < split.length; i++) {
						sb.append("\n").append(indentStr).append(split[i].trim());
					}
					return sb.toString();
				}
			}
			return res;
		}

		return "";
	}

	@Override
	public int getReplaceEndOffset() {
		return -1;
	}

	@Override
	protected int getChangeStartOffset(Editor editor) {
		if(Objects.isNull(editor)) {
			ExceptionEnum.EDITOR_IS_NULL_EXCEPTION.asBusinessException();
		}
		return ReadAction.compute(()->{
			int functionNameStartOffset = getFunctionNameStartOffset();
			int lineNumber = EditorUtil.getLineNumber(editor, functionNameStartOffset);
			int lineStartOffset = EditorUtil.getLineStartOffsetFromOffset(editor, functionNameStartOffset);
			int changeStartLineOffset = EditorUtil.getLineStartOffsetFromLine(editor,lineNumber+1);
			return changeStartLineOffset + (functionNameStartOffset - lineStartOffset);
		});

	}

	@Override
	protected String getIndentStr(Editor editor) {
		return ReadAction.compute(()->{
			try {
				int functionNameStartOffset = getFunctionNameStartOffset();
				int lineNumber = EditorUtil.getLineNumber(editor, functionNameStartOffset);
				int lineStartOffset = EditorUtil.getLineStartOffsetFromOffset(editor, functionNameStartOffset);
				int changeStartLineOffset = EditorUtil.getLineStartOffsetFromLine(editor, lineNumber + 1);
				int changeStartOffset = changeStartLineOffset + (functionNameStartOffset - lineStartOffset);

				return editor.getDocument().getText(new TextRange(changeStartLineOffset, changeStartOffset));
			}catch (Exception ignore){

			}

			return "";
		});
	}

	@Override
	public PsiElement getCodeElement(PsiElement psiElement) {
		return psiElement.getParent();
	}
	

	@NotNull
	@Override
	public String getPrompt() {
		return "基于以下代码，帮我在代码上方生成中文文档字符串注释，注意不要对代码有任何改动";
	}

	private int getFunctionNameStartOffset() {
		return Objects.requireNonNull(((PyFunctionImpl) psiElement).getNameIdentifier()).getTextRange().getStartOffset();
	}


}
