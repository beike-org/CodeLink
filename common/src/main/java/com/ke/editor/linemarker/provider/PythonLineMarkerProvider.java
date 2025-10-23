package com.ke.editor.linemarker.provider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.ke.editor.linemarker.KeLineMarkerInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/7/18 14:16
 * @Version 1.0
 * @Description
 */
public class PythonLineMarkerProvider extends BaseLineMarkerProvider {
    private static final String ALLOW_LANGUAGE = "py";


    @Override
    public LineMarkerInfo<?> getFuncLineMarkerInfo(@NotNull PsiElement element) {
        return new KeLineMarkerInfo(element);
    }

    @Override
    public boolean isValidFunc(PsiElement element) {
        return element.isValid() && element.getNode().getElementType().toString().equalsIgnoreCase("Py:DEF_KEYWORD") && element.getParent() != null && element.getParent().getNode().getElementType().toString().equalsIgnoreCase("Py:FUNCTION_DECLARATION");
    }

    @Override
    public boolean isValidFile(PsiElement element) {
        return element.getContainingFile().getName().toLowerCase().endsWith(ALLOW_LANGUAGE);
    }



}
