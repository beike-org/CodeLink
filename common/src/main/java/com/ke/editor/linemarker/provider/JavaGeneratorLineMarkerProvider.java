package com.ke.editor.linemarker.provider;


import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.ke.editor.linemarker.KeLineMarkerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.ke.editor.linemarker.action.group.LineMarkerGroupAdapter.LINE_MARKER_GROUP_ADAPTER;

/**
 * 规定了method有图标
 *
 * @Author: zhangshaoxun001
 * @Date: 2023/7/18 14:16
 * @Version 1.0
 * @Description
 */
public class JavaGeneratorLineMarkerProvider extends BaseLineMarkerProvider {
    private static final String ALLOW_LANGUAGE = "java";

    @Override
    public LineMarkerInfo<?> getFuncLineMarkerInfo(@NotNull PsiElement element) {
        return new KeLineMarkerInfo(element);
    }

    @Override
    public boolean isValidFunc(PsiElement element) {

        PsiElement parent = element.getParent();
        if (element instanceof PsiIdentifier && parent instanceof PsiMethod) {
            try {
                if (LINE_MARKER_GROUP_ADAPTER.getExtensionList().stream().anyMatch(provider -> provider.isValidFunc(element))) {
                    return true;
                }
            } catch (Exception ignore) {

            }
            return Objects.nonNull(((PsiMethod) parent).getBody());
        }
        return false;

    }

    @Override
    public boolean isValidFile(PsiElement element) {
        FileType fileType = element.getContainingFile().getFileType();
        return ALLOW_LANGUAGE.equalsIgnoreCase(fileType.getName()) && !fileType.isBinary();
    }


}
