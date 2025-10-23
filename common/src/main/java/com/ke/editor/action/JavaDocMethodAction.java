package com.ke.editor.action;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
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
public class JavaDocMethodAction extends DocMethodBaseAction {

	public JavaDocMethodAction(PsiElement psiElement) {
		super(psiElement);
	}

	@Override
	public String getDoc(String code, Editor editor) {
		if (Objects.isNull(code)) {
			return "";
		}

		Pattern pattern = Pattern.compile("/\\*\\*([^*]|\\*[^/])*\\*/", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(code);

		if (matcher.find()) {
			return matcher.group(0);
		}

		return "";
	}


	@Override
	public int getReplaceEndOffset() {
		PsiDocComment originalDocComment = ReadAction.compute(() -> ((PsiMethod) psiElement).getDocComment());
		if (Objects.nonNull(originalDocComment) && StringUtils.isNotEmpty(originalDocComment.getText())) {
			return originalDocComment.getTextRange().getEndOffset();
		}
		return -1;
	}


	@Override
	public PsiElement getCodeElement(PsiElement psiElement) {
		return PsiTreeUtil.getContextOfType(psiElement, PsiMethod.class);
	}

	@NotNull
	@Override
	public String getPrompt() {
		return "基于以下代码，帮我在代码上方生成JavaDoc中文注释，注意不要对代码有任何改动";
	}


}
