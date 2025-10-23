package com.ke.editor.linemarker.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.ke.editor.action.JavaDocClassAction;
import com.ke.editor.action.JavaDocMethodAction;

import java.util.List;


/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:24
 * @Version 1.0
 * @Description
 */
public class JavaLineMarkerGroupProvider implements LineMarkerGroupAdapter {
    @Override
    public List<AnAction> getActionGroup(PsiElement psiElement) {
        if ("java".equalsIgnoreCase(psiElement.getContainingFile().getFileType().getName())) {
            if (psiElement.getParent() instanceof PsiClass) {
                return List.of(new JavaDocClassAction());
            } else {
                return List.of(new JavaDocMethodAction(psiElement));
            }
        }
        return null;
    }
}
