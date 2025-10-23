package com.ke.editor.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.ide.DataManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.ke.Bundle;
import com.ke.utils.EditorUtil;
import com.ke.utils.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/9/19 17:48
 * @Version 1.0
 * @Description
 */
public class KeLineMarkerInfo extends LineMarkerInfo<PsiElement> {



	/**
	 * @param iconElement 图标在那个元素显示的位置
	 */
	public KeLineMarkerInfo(@NotNull PsiElement iconElement) {
		super(iconElement,
				iconElement.getTextRange(),
				IconUtil.getCodeMethodIcon(), psiElement -> Bundle.get("toolWindow.CodeLink.stripe"),

				(e, elt) -> {
					if (elt.isValid()) {
						Editor editor = FileEditorManager.getInstance(elt.getProject()).getSelectedTextEditor();

						if (Objects.nonNull(editor) && Objects.nonNull(elt.getContext())) {

							Point clickPoint = e.getPoint();
							SwingUtilities.convertPointToScreen(clickPoint, e.getComponent());

							String text = elt.getContext().getText();

							try {
								Document document = editor.getDocument();
								int eltStartOffset = elt.getContext().getTextRange().getStartOffset();
								int lineNumber = document.getLineNumber(eltStartOffset);
								int startOffset = document.getLineStartOffset(lineNumber);
								text = document.getText(new TextRange(startOffset, elt.getContext().getTextRange().getEndOffset()));
							} catch (Exception ignore) {

							}

							LineMarkerPopup.createPopup(DataManager.getInstance().getDataContext(editor.getComponent()), EditorUtil.getLanguage(editor), text, elt).showInScreenCoordinates(e.getComponent(), clickPoint);
						}
					}
				},
				GutterIconRenderer.Alignment.LEFT, () -> "");
	}

}
