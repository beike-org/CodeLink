package com.ke.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import com.ke.BaseAction;
import com.ke.Bundle;
import com.ke.editor.action.BaseEditorPopupAction;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.utils.ComponentUtil;
import com.ke.utils.IconUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.WebViewManager;
import com.ke.webview.dto.InlineChatDTO;
import com.ke.webview.dto.SelectCodeContext;
import com.ke.webview.util.PTWUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;

import static com.ke.editor.action.DefaultPromptActionFactory.DEFAULT_ACTIONS;


/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/28 17:39
 * @Version 1.0
 * @Description
 */
public class KeEditorListener implements EditorFactoryListener {

	//代码选中后弹出
	private JBPopup popup;

	//选中代码弹出的actionGroup
	private final DefaultActionGroup actionGroup;

	//popup弹出时一个action的宽度
	private int popupActionWidth;

	//上一次弹出时的字体大小
	private int prevFontSize;

	//actionGroup中的action的图标Map
	private final Map<String, Icon> actionIconMap = Map.of(
			Bundle.get("action.editor.default.label.checkBug"), IconUtil.getCheckBugIcon(),
			Bundle.get("action.editor.default.label.createUnitTest"), IconUtil.getCreateUnitTestIcon(),
			Bundle.get("action.editor.default.label.explainCode"), IconUtil.getExplainCodeIcon(),
			Bundle.get("action.editor.default.label.refactorCode"), IconUtil.getRefactorCodeIcon(),
			Bundle.get("action.editor.default.label.optimizeCode"), IconUtil.getOptimizeCodeIcon()
	);

	//代码选择实时监听，用于给chat发送选择的代码(代码选择的内容只要有变化就会发送)
	private final SelectionListener selectionListener = new SelectionListener() {

		private String lastSelected = null;

		@Override
		public void selectionChanged(@NotNull SelectionEvent e) {
			String text = e.getEditor().getDocument().getText(e.getNewRange());
			if (!text.equals(lastSelected)) {
				if (com.ke.utils.EditorUtil.isSelectedFileEditor(e.getEditor()) || "CONSOLE".equalsIgnoreCase(e.getEditor().getEditorKind().name())) {
					sendSelectCode(text, e);
				}
			}
		}

		private void sendSelectCode(String text, SelectionEvent e) {
			try {
				// 获取开始和结束的偏移量
				int startOffset = e.getNewRange().getStartOffset();
				int endOffset = e.getNewRange().getEndOffset();

				// 计算行号
				Document document = e.getEditor().getDocument();
				int startLine = document.getLineNumber(startOffset) + 1;
				int endLine = document.getLineNumber(endOffset) + 1;

				// 获取文件路径
				VirtualFile file = FileDocumentManager.getInstance().getFile(document);
				String filePath = file != null ? file.getPath() : "Unknown";
				// 构建 SelectCodeContext 并发送消息
				SelectCodeContext context = SelectCodeContext.builder()
						.source(filePath)
						.code(SelectCodeContext.replaceTableKey(text))
						.startLine(startLine)
						.endLine(endLine)
						.startOffset(startOffset)
						.endOffset(endOffset)
						.fileName(e.getEditor().getVirtualFile().getName())
						.build();
				System.out.println(text + "\n");

				PTWUtil.sendMessage(BaseCommandEnums.SELECT_CODE_CONTEXT, context, Objects.requireNonNull(e.getEditor().getProject()));

				// 防止重复发消息
				lastSelected = text;
			} catch (Exception ignore) {

			}
		}
	};


	//代码选择监听,只在选择结束(释放鼠标)后触发
	private final EditorMouseListener editorMouseListener = new EditorMouseListener() {

		@Override
		public void mouseReleased(@NotNull EditorMouseEvent e) {
			if (e.getMouseEvent().getButton() == MouseEvent.BUTTON1) {
				SwingUtilities.invokeLater(() -> {
					if (Boolean.TRUE.equals(UserConfigState.getInstance().getState().getEditorSelectedPopup())) {

						Editor editor = e.getEditor();
						String selectText;
						// 如果有选中的代码,则弹出actionGroup
						if (ReadAction.compute(() -> editor.getSelectionModel().hasSelection()) && StringUtils.isNotBlank(selectText = ReadAction.compute(() -> editor.getSelectionModel().getSelectedText()))) {

							ActionToolbarImpl actionToolbar = new ActionToolbarImpl("EditorActionToolbarImpl", actionGroup, true);
							actionToolbar.setTargetComponent(null);

							Point point = ReadAction.compute(() -> editor.visualPositionToXY(Objects.requireNonNull(editor.getSelectionModel().getSelectionStartPosition())));
							AnAction[] childActionsOrStubs = actionGroup.getChildActionsOrStubs();

							if (Objects.nonNull(popup) && !popup.isDisposed()) {
								popup.dispose();
							}
							if (!(editor instanceof EditorImpl) || Objects.isNull(editor.getVirtualFile())) {
								return;
							}

							if (!selectText.contains("\n") && StringUtils.isAsciiPrintable(selectText)) {
								return;
							}


							// 计算弹出的宽度
							int totalWidth = getPopupActionWidth(actionToolbar) * (childActionsOrStubs.length / 2 - 1) + 20;

							if (totalWidth > 0) {
								popup = ComponentUtil.getBaseComponentPopupBuilder(actionToolbar, null).createPopup();
								popup.setSize(new Dimension(totalWidth, -1));
								popup.show(new RelativePoint(editor.getContentComponent(), new Point(point.x, point.y - editor.getLineHeight())));
							}
						}
					}
				});
			}
		}
	};

	public KeEditorListener() {
		actionGroup = new DefaultActionGroup();

		EditorPopupActionFactory.EP_NAME.getExtensionList().forEach(factory -> factory.getActions().forEach(action -> {
			actionGroup.add(action);
			actionGroup.addSeparator();
		}));

		DEFAULT_ACTIONS.forEach((label, prompt) -> {

			var action = new BaseEditorPopupAction(label, null, actionIconMap.get(label)) {

				@Override
				protected void actionPerformed(Project project, Editor editor, String selectedText) {
					sendMessage(InlineChatDTO.builder()
							.command(prompt)
							.context(InlineChatDTO.replaceTableKey(selectedText))
							.language(com.ke.utils.EditorUtil.getLanguage(editor))
							.build(), project);
					popup.dispose();

				}

				@Override
				public @NotNull ActionUpdateThread getActionUpdateThread() {
					return super.getActionUpdateThread();
				}

				@Override
				public void update(@NotNull AnActionEvent e) {
					boolean visible = canShow(e) &&
							(!isWebviewNeeded() || WebViewManager.isWebviewSupported.get());
					e.getPresentation().setEnabledAndVisible(visible);
					if (Objects.nonNull(popup) && !popup.isDisposed() && !visible) {
						popup.dispose();
					}
				}
			};
			actionGroup.add(action);
			actionGroup.addSeparator();
		});
	}

	/**
	 * 在editor创建的时候调用，如git diff的时候也会调用
	 */
	@Override
	public void editorCreated(@NotNull EditorFactoryEvent event) {

		Editor editor = event.getEditor();
		Project project = editor.getProject();
		if (project != null && !project.isDisposed()) {
			Disposable editorDisposable = Disposer.newDisposable("KeEditorListener");
			EditorUtil.disposeWithEditor(editor, editorDisposable);
			editor.getSelectionModel().addSelectionListener(selectionListener, editorDisposable);
			editor.addEditorMouseListener(editorMouseListener, editorDisposable);

			//文档内容监听,主要是自动关闭popup
			//注意这里documentListener需要new一个新的实例,否则当创建新的editor(如git diff)时,会报错已经register了
			editor.getDocument().addDocumentListener(new PopupCloser(), editorDisposable);

		}
	}


	/**
	 * 计算弹出popup时,单个ActionButton的宽度
	 * 因为ActionToolbarImpl只添加ActionGroup时,无法获取到宽度,
	 * 所以这里使用了一个临时的ActionToolbarImpl,添加一个ActionButtonWithText,然后获取宽度
	 */
	private int getPopupActionWidth(Component component) {
		if (popupActionWidth != 0 && component.getFont().getSize() == prevFontSize) {
			return popupActionWidth;
		}
		prevFontSize = component.getFont().getSize();
		BaseAction baseAction = new BaseAction("计算长度", null, IconUtil.getCreateUnitTestIcon()) {
			@Override
			protected void doAction(@NotNull AnActionEvent e) {

			}
		};
		ActionButtonWithText baseActionButton = new ActionButtonWithText(baseAction, null, "EditorActionToolbarImpl", new Dimension(20, 20));
		ActionToolbarImpl initWidthToolbar = new ActionToolbarImpl("initWidth", new DefaultActionGroup(), true);
		initWidthToolbar.add(baseActionButton);
		popupActionWidth = (int) initWidthToolbar.getComponent().getPreferredSize().getWidth();
		return popupActionWidth;
	}


	class PopupCloser implements DocumentListener {
		@Override
		public void documentChanged(@NotNull DocumentEvent event) {
			if (Objects.nonNull(popup) && !popup.isDisposed()) {
				popup.dispose();
			}
		}
	}

}


