package com.ke.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class PsiUtils {


    private PsiUtils() {
    }

    /**
     * 获取PsiMethod的访问控制
     */
    public static String getMethodVisibility(PsiMethod psiMethod) {
        if (psiMethod.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)) {
            return PsiModifier.PUBLIC;
        } else if (psiMethod.getModifierList().hasModifierProperty(PsiModifier.PROTECTED)) {
            return PsiModifier.PROTECTED;
        } else if (psiMethod.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)) {
            return PsiModifier.PRIVATE;
        } else {
            return PsiModifier.PACKAGE_LOCAL;
        }
    }

    /**
     * 获取需要解析的实体字段
     */
    public static PsiField[] getFields(PsiClass t) {
        PsiField[] fields = t.getAllFields();
        return Arrays.stream(fields).filter(PsiUtils::isNeedField).toArray(PsiField[]::new);
    }


    public static boolean isNeedField(PsiField field) {
        return !field.hasModifierProperty(PsiModifier.STATIC);
    }

    /**
     * 获取枚举类字段
     */
    public static PsiField[] getEnumFields(PsiClass psiClass) {
        return Arrays.stream(psiClass.getFields())
                .filter(field -> field instanceof PsiEnumConstant)
                .toArray(PsiField[]::new);
    }

    /**
     * 获取需要解析的实体字段
     */
    public static PsiField[] getStaticOrFinalFields(PsiClass t) {
        PsiField[] fields = t.getAllFields();
        return Arrays.stream(fields)
                .filter(f -> f.hasModifierProperty(PsiModifier.STATIC) || f.hasModifierProperty(PsiModifier.FINAL))
                .toArray(PsiField[]::new);
    }

    /**
     * 根据类短名来获取PsiClass, 而非类全限定名
     * 优先从当前模块依赖, 其次当前工程作用域
     */
    public static PsiClass findPsiClassByShortName(Project project, Module module, String shortName) {
        PsiClass psiClass = null;
        if (module != null) {
            psiClass = Optional.ofNullable(PsiShortNamesCache.getInstance(project)
                            .getClassesByName(shortName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false)))
                    .filter(it -> it.length >= 1)
                    .map(it -> it[0])
                    .orElse(null);
        }
        if (psiClass == null) {
            psiClass = Optional.ofNullable(PsiShortNamesCache.getInstance(project)
                            .getClassesByName(shortName, GlobalSearchScope.projectScope(project)))
                    .filter(it -> it.length >= 1)
                    .map(it -> it[0])
                    .orElse(null);
        }
        return psiClass;
    }

    /**
     * 根据类全限定名获取PsiClass
     * 优先从当前模块依赖, 其次当前工程作用域
     */
    public static PsiClass findPsiClass(Project project, Module module, String qualifiedName) {
        PsiClass psiClass = null;
        if (module != null) {
            psiClass = JavaPsiFacade.getInstance(project)
                    .findClass(qualifiedName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        }
        if (psiClass == null) {
            psiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project));
        }
        if (psiClass != null && psiClass.canNavigate()) {
            psiClass = (PsiClass) psiClass.getNavigationElement();
        }
        return psiClass;
    }

    /**
     * 获取Getter方法
     */
    public static PsiMethod[] getGetterMethods(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllMethods()).filter(method -> {
            String methodName = method.getName();
            PsiType returnType = method.getReturnType();
            PsiModifierList modifierList = method.getModifierList();
            return !modifierList.hasModifierProperty(PsiModifier.STATIC)
                    && !methodName.equals("getClass")
                    && ((methodName.startsWith("get") && methodName.length() > 3 && returnType != null)
                    || (methodName.startsWith("is") && methodName.length() > 2 && returnType != null && returnType
                    .getCanonicalText().equals("boolean"))
            );
        }).toArray(PsiMethod[]::new);
    }



    public static int getStartOffset(@NotNull PsiElement psiElement) {
        return psiElement.getTextRange().getStartOffset();
    }


    public static int getEndOffset(@NotNull PsiElement psiElement) {
        return psiElement.getTextRange().getEndOffset();
    }

    public static int getLineNumber(@NotNull PsiElement psiElement, boolean start) {
        Document document = psiElement.getContainingFile().getViewProvider().getDocument();
        if (document == null) {
            document = PsiDocumentManager.getInstance(psiElement.getProject()).getDocument(psiElement.getContainingFile());
        }

        int index = start ? getStartOffset(psiElement) : getEndOffset(psiElement);
        if (index > (document != null ? document.getTextLength() : 0)) {
            return 0;
        } else {
            return document != null ? document.getLineNumber(index) : 0;
        }
    }

    public static Integer getLineStartOffset(@NotNull PsiFile psiFile, int line) {
        Document document = psiFile.getViewProvider().getDocument();
        if (document == null) {
            document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
        }

        Document doc = document;
        if (doc != null && line >= 0 && line < doc.getLineCount()) {
            int startOffset = doc.getLineStartOffset(line);
            PsiElement psiElement = psiFile.findElementAt(startOffset);
            if (psiElement == null) {
                return startOffset;
            } else {
                PsiElement element = psiElement;
                if (!(element instanceof PsiWhiteSpace) && !(element instanceof PsiComment)) {
                    return startOffset;
                } else {
                    Class[] var5 = new Class[]{PsiWhiteSpace.class, PsiComment.class};
                    psiElement = PsiTreeUtil.skipSiblingsForward(element, var5);
                    return psiElement != null ? getStartOffset(psiElement) : startOffset;
                }
            }
        } else {
            return null;
        }
    }

}
