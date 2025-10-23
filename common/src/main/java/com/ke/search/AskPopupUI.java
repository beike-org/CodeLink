package com.ke.search;

import com.intellij.accessibility.TextFieldWithListAccessibleContext;
import com.intellij.ide.actions.BigPopupUI;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ExperimentalUI;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.ke.Bundle;
import com.ke.search.actions.ShowLLMAction;
import com.ke.search.topic.LLMChangeNotifier;
import com.ke.service.llm.LLMService;
import com.ke.service.notify.NotifyServiceImpl;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.setting.configuration.genral.user.bean.ModelConfiguration;
import com.ke.utils.EditorUtil;
import com.ke.utils.IconUtil;
import com.ke.webview.communication.handler.ptw.InlineChatPTWHandler;
import com.ke.webview.dto.InlineChatDTO;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXLabel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/17 11:33
 * @Version 1.0
 * @Description
 */
public class AskPopupUI extends BigPopupUI {

    private static final Logger LOG = Logger.getInstance(AskPopupUI.class);

    AskListModel askListModel;

    Project project;

    @Setter
    private JBPopup parentContainer;

    private final AskMode askMode;

    private final Boolean isOpenInTerminal;


    private final String TERMINAL_PREFIX = "You are a programmer who specializes in using the command line. Your task is to help the Developer craft a command to run on the command line.\n" +
            "\n" +
            "Additional Rules:\n" +
            "1. Generate a response that clearly and accurately answers the user's question. In your response, follow the following:\n" +
            "   - Prefer single line commands.\n" +
            "   - Provide the command suggestions using the active shell and operating system.\n" +
            "   - Do not provide explanations.\n" +
            "   - Say \"I'm not quite sure how to do that.\" when you aren't confident in your explanation\n" +
            "   - Only use a tool like python or perl when it is not possible with the shell.\n" +
            "\n" +
            "Note: Output terminal commands directly, without markdown formatting.\n" +
            "\n" +
            "The active operating system is:" + SystemInfoRt.OS_NAME +
	        "\n" +
            "The user's question is:";

    public AskPopupUI(@Nullable Project project, AskMode askMode) {
        super(project);
        this.project = project;
        this.askMode = askMode;
        this.isOpenInTerminal = !AskMode.Type.NORMAL.equals(askMode.getType());
        init();
        addBottomComponent();

        if (!isOpenInTerminal) {
            myResultsList.setEmptyText(Bundle.get("action.popup.ask.noMatchCommand"));
            myResultsList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (Objects.nonNull(myResultsList.getSelectedValue())) {
                        updateSelectedText();
                    }
                }
            });
        }

    }


    @Override
    protected @NotNull ExtendableTextField createSearchField() {
        return new AskTextField();
    }

    @Override
    protected @NotNull JComponent createHeader() {
        JXLabel jxLabel = new JXLabel(Bundle.get("toolWindow.CodeLink.stripe"));
        jxLabel.setVisible(false);
        return jxLabel;
    }


    @Override
    public @NotNull JBList<Object> createList() {

        if (isOpenInTerminal) {
            return new JBList<>();
        }
        askListModel = new AskListModel();
        addListDataListener(askListModel);
        return new JBList<>(askListModel);
    }

    @Override
    protected @NotNull ListCellRenderer<Object> createCellRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> (JXLabel) value;
    }

    @Override
    protected @NotNull @Nls String getAccessibleName() {
        return "Ask Everywhere";
    }

    private void addBottomComponent() {

        ModelConfiguration modelConfiguration = UserConfigState.getInstance().getPluginConfig().getModelConfiguration();
        if (Objects.isNull(modelConfiguration)) {
            return;
        }
        String modelName = modelConfiguration.getModelName();
        if (StringUtils.isBlank(modelName)) {
            return;
        }

        JPanel bottomPanel = new BorderLayoutPanel();

        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        AnAction modelChooser = new ShowLLMAction();

        ActionButtonWithText actionButtonWithText = new ActionButtonWithText(modelChooser, null, "ask.everywhere.model", new Dimension(20, 20)) {
            @Override
            protected boolean shallPaintDownArrow() {
                return true;
            }
        };
        project.getMessageBus().connect(this).subscribe(LLMChangeNotifier.LLM_CHANGE_NOTIFIER_TOPIC, (LLMChangeNotifier) model -> actionButtonWithText.getPresentation().setText(model));
        modelPanel.add(actionButtonWithText);

        bottomPanel.add(modelPanel, BorderLayout.CENTER);
        addToBottom(bottomPanel);
    }

    private void submit() {

        if (isOpenInTerminal) {
            String result = ApplicationManager.getApplication().getService(LLMService.class).chat(TERMINAL_PREFIX + getSearchField().getText());
            if (StringUtils.isNotEmpty(result)) {
                String context = result.split("\n")[0];
                if (askMode.getType().equals(AskMode.Type.TERMINAL)) {
                    try {
                        askMode.getTtyConnector().write(context);
                    } catch (Exception e) {
                        project.getService(NotifyServiceImpl.class).error("Failed to write to terminal");
                        LOG.warn("Failed to write to terminal", e);
                    }
                } else if (askMode.getComponent() instanceof EditorComponentImpl editorComponent) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        EditorImpl editor = editorComponent.getEditor();
                        DocumentEx document = editor.getDocument();
                        document.insertString(document.getTextLength(), context);
                        editor.getCaretModel().moveToOffset(document.getTextLength());
                    });
                }
            }
        } else {
            String context = "";
            String language = "";
            try {
                language = EditorUtil.getLanguage(project);
                context = EditorUtil.getSelectedContent(project);

            } catch (Exception ignore) {

            }

            InlineChatPTWHandler.sendInlineChat(InlineChatDTO.builder().question(getSearchField().getText()).context(InlineChatDTO.replaceTableKey(context)).language(language).command("").build(), myProject);
        }

        this.dispose();
    }

    private void updateSelectedText() {
        getSearchField().setText(((JXLabel) myResultsList.getSelectedValue()).getText() + "  ");
        myResultsList.clearSelection();
    }

    @Override
    public void dispose() {
        if (Objects.nonNull(parentContainer) && !parentContainer.isDisposed()) {
            parentContainer.dispose();
        }
    }

    @Override
    protected void addListDataListener(@NotNull AbstractListModel<Object> model) {

        model.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                updateViewType(BigPopupUI.ViewType.FULL);
            }

            public void intervalRemoved(ListDataEvent e) {
                if (myResultsList.isEmpty() && getSearchPattern().isEmpty()) {
                    updateViewType(BigPopupUI.ViewType.SHORT);
                }

            }

            public void contentsChanged(ListDataEvent e) {
                updateViewType(myResultsList.isEmpty() ? BigPopupUI.ViewType.SHORT : BigPopupUI.ViewType.FULL);
            }
        });
    }

    class AskTextField extends SearchField {

        private final String DEFAULT_TEXT = isOpenInTerminal ? "输入问题,生成指令" : "你可以选择执行已有命令或输入你的问题";

        public AskTextField() {
            addListeners();
            addExtension(new AskTextFieldExtension());
            setLayout(new BorderLayout());
            setText(DEFAULT_TEXT);
            setForeground(JBColor.GRAY);
        }


        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new TextFieldWithListAccessibleContext(this, myResultsList.getAccessibleContext());
            }
            return accessibleContext;
        }

        private void addListeners() {
            if (!isOpenInTerminal) {
                getDocument().addDocumentListener(new DocumentAdapter() {
                    @Override
                    protected void textChanged(@NotNull DocumentEvent e) {
                        askListModel.updateFromInput();
                    }
                });
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (getText().equals(DEFAULT_TEXT)) {
                        setText("");
                        setForeground(UIUtil.getLabelForeground());
                    }
                }
            });

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (Objects.nonNull(myResultsList.getSelectedValue())) {
                            updateSelectedText();
                        } else {
                            submit();
                        }
                    } else if (getText().equals(DEFAULT_TEXT)) {
                        setText("");
                        setForeground(UIUtil.getLabelForeground());
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    if (getText().startsWith(DEFAULT_TEXT)) {
                        setText(getText().replace(DEFAULT_TEXT, ""));
                        setForeground(UIUtil.getLabelForeground());
                    }
                }
            });
        }
    }


    class AskTextFieldExtension implements ExtendableTextComponent.Extension {
        @Override
        public Icon getIcon(boolean hovered) {
            return IconUtil.getSendIcon();
        }

        @Override
        public Runnable getActionOnClick(@NotNull InputEvent inputEvent) {
            return AskPopupUI.this::submit;
        }

        @Override
        public boolean isIconBeforeText() {
            return false;
        }

        @Override
        public int getIconGap() {
            return JBUIScale.scale(ExperimentalUI.isNewUI() ? 6 : 10);
        }
    }
}
