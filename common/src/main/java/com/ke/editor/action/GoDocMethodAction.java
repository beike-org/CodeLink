package com.ke.editor.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/14 16:50
 * @Version 1.0
 * @Description
 */
public class GoDocMethodAction extends DocMethodBaseAction {

	public GoDocMethodAction(PsiElement psiElement) {
		super(psiElement);
	}

	@Override
	public String getDoc(String code, Editor editor) {
		if (Objects.isNull(code)) {
			return "";
		}

		// 匹配以 // 开头的注释块，直到遇到下一个非注释的代码行
		String[] split = code.split("\n");

		StringBuilder stringBuilder = new StringBuilder();
		boolean start = false;
		for (String s : split) {
			if (s.trim().startsWith("//")) {
				start = true;
				stringBuilder.append(s).append("\n");
			} else if (start){
				break;
			}
		}

		if(!stringBuilder.isEmpty()){
			return stringBuilder.substring(0,stringBuilder.length() - 1);
		}

		return "";
	}


	/**
	 * 插入模式
	 */
	@Override
	public int getReplaceEndOffset() {
		return -1;
	}


	@Override
	public PsiElement getCodeElement(PsiElement psiElement) {
		return psiElement.getParent();
	}

	@NotNull
	@Override
	public String getPrompt() {
		return "基于以下代码，帮我在代码上方生成中文godoc注释(直接以//开头)，注意不要对代码有任何改动";
	}

}
