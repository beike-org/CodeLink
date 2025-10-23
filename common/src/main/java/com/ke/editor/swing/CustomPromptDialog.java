package com.ke.editor.swing;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import com.ke.utils.ComponentUtil;

import javax.swing.*;

public class CustomPromptDialog extends DialogWrapper {


  private final JTextArea userPromptTextArea;

  public CustomPromptDialog(String previousUserPrompt) {
    super(true);
    this.userPromptTextArea = new JTextArea(previousUserPrompt);
    this.userPromptTextArea.setCaretPosition(previousUserPrompt.length());
    setTitle("Custom Prompt");
    setSize(400, getRootPane().getPreferredSize().height);
    init();
  }

  public JComponent getPreferredFocusedComponent() {
    return userPromptTextArea;
  }

  @Override
  protected JComponent createCenterPanel() {
    userPromptTextArea.setLineWrap(true);
    userPromptTextArea.setWrapStyleWord(true);
    userPromptTextArea.setMargin(JBUI.insets(5));
    ComponentUtil.addShiftEnterInputMap(userPromptTextArea, this::clickDefaultButton);

    return FormBuilder.createFormBuilder()
        .addComponent(UI.PanelFactory.panel(userPromptTextArea)
            .withLabel("Prefix:")
            .moveLabelOnTop()
            .withComment(
                "Example: Find bugs in the following code")
            .createPanel())
        .getPanel();
  }

  public String getFullPrompt() {
    return userPromptTextArea.getText();
  }

  public String getUserPrompt() {
    return userPromptTextArea.getText();
  }
}

