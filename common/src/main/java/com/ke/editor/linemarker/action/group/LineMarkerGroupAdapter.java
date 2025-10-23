package com.ke.editor.linemarker.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:16
 * @Version 1.0
 * @Description 根据不同的语言注册不同的行前Action
 */
public interface LineMarkerGroupAdapter {

    ExtensionPointName<LineMarkerGroupAdapter> LINE_MARKER_GROUP_ADAPTER = ExtensionPointName.create("com.ke.codelink.lineMarkerGroupAdapter");

    /**
     * 获取行前Action
     *
     * @param psiElement
     */
    List<AnAction> getActionGroup(PsiElement psiElement);


    default boolean isValidFunc(PsiElement element) {
        return false;
    }

}
