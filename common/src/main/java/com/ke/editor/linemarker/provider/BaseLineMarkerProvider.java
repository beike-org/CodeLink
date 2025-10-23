package com.ke.editor.linemarker.provider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.ke.setting.configuration.genral.user.UserConfigState;
import org.jetbrains.annotations.NotNull;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/19 18:21
 * @Version 1.0
 * @Description
 */
public abstract class BaseLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!UserConfigState.getInstance().getState().getLineMarker() || !isValidFile(element) || !isValidFunc(element)) {
            return null;
        }

        return getFuncLineMarkerInfo(element);
    }


    public abstract LineMarkerInfo<?> getFuncLineMarkerInfo(@NotNull PsiElement element);

    /**
     * psiElement的过滤条件可以参考JavaLineMarkerProvider
     * @see com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider
     */
    public abstract boolean isValidFunc(PsiElement element);

    public abstract boolean isValidFile(PsiElement element);

}
