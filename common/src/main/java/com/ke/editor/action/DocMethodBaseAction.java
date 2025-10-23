package com.ke.editor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.ke.editor.DiffMode;
import com.ke.service.llm.LLMService;
import com.ke.service.notify.NotifyServiceImpl;
import com.ke.utils.DiffUtil;
import com.ke.utils.EditorUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/14 16:50
 * @Version 1.0
 * @Description 通用的生成注释的action
 */
public abstract class DocMethodBaseAction extends DocBaseAction {

	protected final PsiElement psiElement;

	private final static Logger logger = Logger.getInstance(DocMethodBaseAction.class);

	public DocMethodBaseAction(PsiElement psiElement) {
		super();
		this.psiElement = getCodeElement(psiElement);
	}

	@Override
	public void doAction(@NotNull AnActionEvent e) {

		if (Objects.isNull(psiElement)) {
			logger.warn("get lineMarker psiElement is null");
			return;
		}

		String code = psiElement.getText();

		ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "AI Doc Generate...") {
			@Override
			public void run(@NotNull ProgressIndicator progressIndicator) {

				String result = getDoc(ApplicationManager.getApplication().getService(LLMService.class).chat(getPrompt() + code), e.getData(CommonDataKeys.EDITOR));

				if (StringUtils.isNotEmpty(result)) {
					int replaceEndOffset = getReplaceEndOffset();
					if (replaceEndOffset == -1) {
						Editor editor = e.getData(CommonDataKeys.EDITOR);
						String indentStr = "";
						if (Objects.nonNull(editor)) {
							indentStr = getIndentStr(editor);
						}
						DiffUtil.showDiff(e, DiffMode.INSERT, result, indentStr, getChangeStartOffset(editor), replaceEndOffset);
					} else {
						DiffUtil.showDiff(e, DiffMode.REPLACE, result, null, getChangeStartOffset(e.getData(CommonDataKeys.EDITOR)), replaceEndOffset);
					}
				} else {
					Objects.requireNonNull(e.getProject()).getService(NotifyServiceImpl.class).warn("AI Doc Generate Failed,Please try again");
				}
			}
		});


	}


	/**
	 * 获取变更的开始位置
	 *
	 * @return 开始位置
	 */
	protected int getChangeStartOffset(Editor editor) {
		return psiElement.getTextRange().getStartOffset();
	}

	/**
	 * 获取缩进
	 *
	 * @param editor 编辑器
	 * @return 缩进
	 */
	protected String getIndentStr(Editor editor) {
		int textOffset = psiElement.getTextRange().getStartOffset();
		return ReadAction.compute(() -> {
			int lineStartOffset = EditorUtil.getLineStartOffsetFromOffset(editor, textOffset);
			if (lineStartOffset < textOffset) {
				return editor.getDocument().getText(new TextRange(lineStartOffset, textOffset));
			}
			return "";
		});
	}


	/**
	 * 规则匹配获取大模型返回的注释内容
	 *
	 * @param code   大模型返回结果
	 * @param editor 编辑器
	 * @return 注释
	 */
	public abstract String getDoc(String code, Editor editor);


	/**
	 * 获取diff模式为replace时,结束的位置,如果为-1,则表示为insert模式
	 *
	 * @return 替换结束的位置
	 */
	public abstract int getReplaceEndOffset();


	/**
	 * 获取实际的代码元素
	 *
	 * @return 代码元素
	 */
	public abstract PsiElement getCodeElement(PsiElement psiElement);

	/**
	 * 获取提示词
	 *
	 * @return 提示词
	 */
	@NotNull
	public abstract String getPrompt();


}
