package com.ke.stacktrace.render;

import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.ke.Bundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/10/12 15:30
 * @Version 1.0
 * @Description
 */
public class DelegateClickPresentation extends StaticDelegatePresentation {

	private final EditorImpl editor;

	private final Runnable runnable;

	private JBPopup jbPopup;

	private boolean popupIsShow = false;

	public DelegateClickPresentation(@NotNull InlayPresentation base,
									 Runnable runnable,
									 EditorImpl editor) {
		super(base);
		this.runnable = runnable;
		this.editor = editor;
	}

	@Override
	public void mouseClicked(@NotNull MouseEvent event, @NotNull Point translated) {
		runnable.run();
	}

	@Override
	public void mouseMoved(@NotNull MouseEvent event, @NotNull Point translated) {
		Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		editor.setCustomCursor(this, handCursor);

		if (!popupIsShow) {
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(80, 30));

			// 创建一个 JLabel 实例并设置文本为 "hello"
			JLabel label = new JLabel(Bundle.get("action.console.stacktrace"));
			label.setForeground(new JBColor(new Color(59, 173, 173), new Color(59, 173, 173)));

			// 将 JLabel 实例添加到 JPanel 中
			panel.add(label);
			this.jbPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null)
					//设置不损失焦点后就自动关闭
					.setCancelOnClickOutside(true)
					//设置展示border
					.setShowBorder(true).createPopup();
			jbPopup.show(new RelativePoint(event.getComponent(), new Point(event.getPoint().x + 10, event.getY() + 30)));
			popupIsShow = true;
		}
	}

	@Override
	public void mouseExited() {
		editor.setCustomCursor(this, null);
		if (Objects.nonNull(jbPopup) && jbPopup.isVisible()) {
			jbPopup.dispose();
		}
		popupIsShow = false;
	}
}
