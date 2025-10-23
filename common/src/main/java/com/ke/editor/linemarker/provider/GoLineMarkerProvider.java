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
public class GoLineMarkerProvider extends BaseLineMarkerProvider {
    private static final String ALLOW_LANGUAGE = "go";

    @Override
    public LineMarkerInfo<?> getFuncLineMarkerInfo(@NotNull PsiElement element) {
        return new KeLineMarkerInfo(element);
    }

    @Override
    public boolean isValidFunc(PsiElement element) {
        // 修改为只要是个函数就可以有go对应的一些动作
        return element.getNode().getElementType().toString().equalsIgnoreCase("func") && element.getParent().getParent().getNode().getText().startsWith("package");
    }

    @Override
    public boolean isValidFile(PsiElement element) {
        return element.getContainingFile().getName().toLowerCase().endsWith(ALLOW_LANGUAGE);
    }



}
