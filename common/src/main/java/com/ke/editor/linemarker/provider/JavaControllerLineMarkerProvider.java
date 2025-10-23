package com.ke.editor.linemarker.provider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiTypeParameter;
import com.ke.editor.linemarker.KeLineMarkerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 规定了只有controller的类有图标
 *
 * @Author: zhangshaoxun001
 * @Date: 2023/7/18 14:16
 * @Version 1.0
 * @Description
 */
public class JavaControllerLineMarkerProvider extends BaseLineMarkerProvider {
    private static final String ALLOW_LANGUAGE = "java";

    private static final Logger LOG = Logger.getInstance(JavaControllerLineMarkerProvider.class);

    @Override
    public LineMarkerInfo<?> getFuncLineMarkerInfo(@NotNull PsiElement element) {
        return new KeLineMarkerInfo(element);
    }

    @Override
    public boolean isValidFunc(PsiElement element) {
        return element instanceof PsiIdentifier && element.getParent() instanceof PsiClass && !(element.getParent() instanceof PsiTypeParameter);
    }

    @Override
    public boolean isValidFile(PsiElement element) {
        if (Objects.nonNull(element) && isValidFunc(element)) {
            FileType fileType = element.getContainingFile().getFileType();

            try {
                return ALLOW_LANGUAGE.equalsIgnoreCase(fileType.getName()) && !fileType.isBinary();
            } catch (Exception e) {
                LOG.warn("isValidFile error", e);
            }
        }
        return false;
    }


}
