package com.ke.editor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.ke.editor.DiffMode;
import com.ke.service.llm.LLMService;
import com.ke.utils.DiffUtil;
import com.ke.utils.EditorUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/14 16:50
 * @Version 1.0
 * @Description
 */
public class JavaDocClassAction extends DocBaseAction {


	public JavaDocClassAction() {
		super();
	}

	@Override
	public void doAction(@NotNull AnActionEvent e) {

		PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);


		if (psiFile instanceof PsiJavaFile) {

			Editor editor = e.getData(CommonDataKeys.EDITOR);


			PsiClass psiClass = PsiTreeUtil.getChildOfType(e.getData(CommonDataKeys.PSI_FILE), PsiClass.class);
			int eltStartOffset = psiClass.getNameIdentifier().getTextOffset();
			int startOffset;
			String startText;
			int endOffset;
			int lineNumber;
			String prompt;
			String code;

			if (editor.getSelectionModel().hasSelection() && editor.getSelectionModel().getSelectionStart() > eltStartOffset) {
				eltStartOffset = Math.max(eltStartOffset, editor.getSelectionModel().getSelectionStart());
				lineNumber = editor.getDocument().getLineNumber(eltStartOffset);
				startOffset = editor.getDocument().getLineStartOffset(lineNumber);
				startText = "";
				endOffset = editor.getSelectionModel().getSelectionEnd();
				code = editor.getDocument().getText(new TextRange(startOffset, endOffset));
				prompt = "你是一个资深Java开发工程师,现在需要对以下字段和以下方法,生成JavaDoc中文注释:" + code + "\n注意只对以上列出来的字段和方法生成注释,不要再增加新的字段或方法,也不要对代码内容有任何改动";
			} else {

				lineNumber = EditorUtil.getLineNumber(editor, eltStartOffset);
				startOffset = EditorUtil.getLineStartOffsetFromLine(editor, lineNumber);
				startText = ReadAction.compute(() -> editor.getDocument().getText(new TextRange(startOffset, editor.getDocument().getLineEndOffset(lineNumber))));
				endOffset = psiClass.getTextRange().getEndOffset();
				PsiField[] fields = psiClass.getFields();
				List<String> fieldList = new ArrayList<>();
				for (PsiField psiField : fields) {
					if (Objects.isNull(psiField.getDocComment()) && psiField.isPhysical()) {
						fieldList.add(psiField.getName());
					}
				}

				PsiMethod[] methods = psiClass.getMethods();
				List<String> methodList = new ArrayList<>();
				for (PsiMethod psiMethod : methods) {
					if (Objects.isNull(psiMethod.getDocComment()) && psiMethod.isPhysical()) {
						methodList.add(psiMethod.getName());
					}
				}

				code = editor.getDocument().getText(new TextRange(startOffset, endOffset));
				prompt = "你是一个资深Java开发工程师,现在需要对以下字段:" + fieldList + "和以下方法:" + methodList + "生成JavaDoc中文注释,注意只对以上列出来的字段和方法生成注释,不要再增加新的字段或方法" + code;
			}


			if (StringUtils.isNotBlank(code)) {

				ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "AI Doc Generate...") {
					@Override
					public void run(@NotNull ProgressIndicator progressIndicator) {

						String result = ApplicationManager.getApplication().getService(LLMService.class).chat(prompt);

						int index = StringUtils.isEmpty(startText) ? 0 : result.indexOf(startText);

						if (StringUtils.isNotEmpty(result) && index != -1) {

							DiffUtil.showDiff(e, DiffMode.REPLACE, result.substring(index), null, startOffset, startOffset + code.length());

						}

					}
				});
			}
		}

	}

}
