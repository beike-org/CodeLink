package com.ke.editor.linemarker.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.ke.editor.action.PythonDocMethodAction;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:24
 * @Version 1.0
 * @Description
 */
public class PythonLineMarkerGroupProvider implements LineMarkerGroupAdapter {
	@Override
	public List<AnAction> getActionGroup(PsiElement psiElement) {
		return List.of(new PythonDocMethodAction(psiElement));
	}
}
