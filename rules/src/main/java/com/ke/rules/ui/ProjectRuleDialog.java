package com.ke.rules.ui;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.ke.Bundle;
import com.ke.rules.ProjectRuleManager;
import com.ke.rules.dto.ProjectRuleDTO;
import com.ke.rules.enums.RuleType;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectRuleDialog extends DialogWrapper {
	private static final int MAX_DESCRIPTION_CHARS = 6000;
	private static final String DEFAULT_DESCRIPTION = Bundle.get("dialog.project.rule.description.default");
	private final RuleConfigurationComponent parent;
	private final JBTextField nameField;
	private final JComboBox<RuleType> typeComboBox;
	private final JPanel fileTypesPanel;
	private final JBTextField fileTypeField;
	private final JPanel tagsPanel;
	private final List<String> fileTypes;
	private final JBTextArea descriptionArea;
	private final JBLabel charCountLabel;
	private boolean isEditMode;
	private String oldRuleName;


	public ProjectRuleDialog(RuleConfigurationComponent parent) {
		super(true);
		this.parent = parent;
		this.isEditMode = false;
		setTitle(Bundle.get("dialog.project.rule.new.title"));

		// 初始化组件
		nameField = new JBTextField();
		typeComboBox = new JComboBox<>(RuleType.values());
		fileTypesPanel = new JPanel(new BorderLayout());
		fileTypeField = new JBTextField();
		tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fileTypes = new ArrayList<>();

		// 初始化描述区域
		descriptionArea = new JBTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setBackground(new JBColor(new Color(43, 43, 43), new Color(43, 43, 43)));
		descriptionArea.setText(DEFAULT_DESCRIPTION);
		descriptionArea.setForeground(new Color(255, 255, 255));  // 使用白色

		// 初始化字符计数标签
		charCountLabel = new JBLabel("0/" + MAX_DESCRIPTION_CHARS);

		// 添加自定义 TransferHandler 以保留粘贴文本的换行符
		descriptionArea.setTransferHandler(new TransferHandler() {
			@Override
			public boolean importData(TransferSupport support) {
				try {
					String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
					descriptionArea.replaceSelection(data);
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(DataFlavor.stringFlavor);
			}
		});

		// 替换原有的文本变化监听器
		descriptionArea.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(() -> {
					int length = descriptionArea.getText().length();
					if (length > MAX_DESCRIPTION_CHARS) {
						Messages.showWarningDialog(
								Bundle.get("dialog.project.rule.warning.description.length", MAX_DESCRIPTION_CHARS),
								Bundle.get("dialog.project.rule.warning.title")
						);
						descriptionArea.setText(descriptionArea.getText().length() > 6000 ? descriptionArea.getText().substring(0, MAX_DESCRIPTION_CHARS) : descriptionArea.getText());
						descriptionArea.setCaretPosition(MAX_DESCRIPTION_CHARS);
					}
					updateCharCount();
				});
			}

			public void removeUpdate(DocumentEvent e) {
				updateCharCount();
			}

			public void changedUpdate(DocumentEvent e) {
				updateCharCount();
			}
		});

		// 添加按键监听器以预防字符输入
		descriptionArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (descriptionArea.getText().length() >= MAX_DESCRIPTION_CHARS &&
						!e.isControlDown() && !e.isMetaDown() &&
						e.getKeyChar() != KeyEvent.VK_BACK_SPACE &&
						e.getKeyChar() != KeyEvent.VK_DELETE) {
					e.consume();
				}
			}
		});

		// 添加焦点监听器
		descriptionArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (DEFAULT_DESCRIPTION.equals(descriptionArea.getText())) {
					SwingUtilities.invokeLater(descriptionArea::selectAll);
				}
			}
		});

		init();
		updateCharCount();
	}


	// 添加带有规则参数的构造函数
	public ProjectRuleDialog(ProjectRuleDTO rule, RuleConfigurationComponent parent) {
		this(parent);
		this.isEditMode = true;
		this.oldRuleName = rule.getName();  // 保存旧规则名称
		setTitle(Bundle.get("dialog.project.rule.edit.title"));
		nameField.setText(rule.getName());
		typeComboBox.setSelectedItem(rule.getType());
		descriptionArea.setText(rule.getContent());
		descriptionArea.setForeground(new Color(255, 255, 255));  // 使用白色

		// 如果是SPECIFIED_TYPE类型，初始化文件类型
		if (RuleType.SPECIFIED_TYPE.equals(rule.getType())) {
			fileTypesPanel.setVisible(true);
			if (rule.getRegex() != null) {
				rule.getRegex().forEach(this::addFileTypes);
			}
		}
	}

	@Override
	protected @Nullable JComponent createCenterPanel() {
		return createDialogPanel();
	}

	private JPanel createDialogPanel() {
		JPanel dialogPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = JBUI.insets(5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// 规则名称
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		JLabel nameLabel = new JBLabel(Bundle.get("dialog.project.rule.name.label"));
		nameLabel.setPreferredSize(new Dimension(80, 25));
		dialogPanel.add(nameLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		nameField.setPreferredSize(new Dimension(300, 25));
		dialogPanel.add(nameField, gbc);

		// 规则类型
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.insets = JBUI.insets(10, 5, 5, 5);
		JLabel typeLabel = new JBLabel(Bundle.get("dialog.project.rule.type.label"));
		typeLabel.setPreferredSize(new Dimension(80, 25));
		dialogPanel.add(typeLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		typeComboBox.setPreferredSize(new Dimension(300, 25));
		dialogPanel.add(typeComboBox, gbc);

		typeComboBox.addItemListener(e -> {
			RuleType selectedType = (RuleType) typeComboBox.getSelectedItem();
			fileTypesPanel.setVisible(selectedType == RuleType.SPECIFIED_TYPE);
			pack();
		});

		// 文件类型选择（初始隐藏）
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = JBUI.insets(5);

		JPanel fileTypeContainer = new JPanel(new BorderLayout(0, 5));
		fileTypeContainer.setBorder(JBUI.Borders.empty(5));

		JPanel fileTypeHeaderPanel = new JPanel(new BorderLayout());
		fileTypeHeaderPanel.add(new JBLabel(Bundle.get("dialog.project.rule.file.type.label")), BorderLayout.WEST);

		JLabel tipLabel = new JLabel(Bundle.get("dialog.project.rule.file.type.tip"));
		tipLabel.setForeground(Color.GRAY);
		tipLabel.setFont(tipLabel.getFont().deriveFont((float) EditorColorsManager.getInstance().getGlobalScheme().getEditorFontSize()));
		fileTypeHeaderPanel.add(tipLabel, BorderLayout.SOUTH);

		fileTypeContainer.add(fileTypeHeaderPanel, BorderLayout.NORTH);

		fileTypeField.setToolTipText(Bundle.get("dialog.project.rule.file.type.input.tip"));
		fileTypeField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addFileTypes(fileTypeField.getText());
				}
			}
		});

		JButton addButton = new JButton(Bundle.get("dialog.project.rule.file.type.add"));
		addButton.setPreferredSize(new Dimension(60, 25));
		addButton.addActionListener(e -> addFileTypes(fileTypeField.getText()));

		JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
		inputPanel.add(fileTypeField, BorderLayout.CENTER);
		inputPanel.add(addButton, BorderLayout.EAST);

		JPanel fileTypeInputContainer = new JPanel(new BorderLayout(0, 5));
		fileTypeInputContainer.add(inputPanel, BorderLayout.NORTH);
		fileTypeInputContainer.add(tagsPanel, BorderLayout.CENTER);

		fileTypeContainer.add(fileTypeInputContainer, BorderLayout.CENTER);

		fileTypesPanel.add(fileTypeContainer);
		fileTypesPanel.setVisible(false);

		dialogPanel.add(fileTypesPanel, gbc);

		// 规则描述
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.gridwidth = 1;
		gbc.insets = JBUI.insets(10, 5, 5, 5);
		JLabel descLabel = new JBLabel(Bundle.get("dialog.project.rule.description.label"));
		descLabel.setPreferredSize(new Dimension(80, 25));
		dialogPanel.add(descLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane scrollPane = new JScrollPane(descriptionArea);
		scrollPane.setPreferredSize(new Dimension(300, 150));
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.add(scrollPane, BorderLayout.CENTER);
		descriptionPanel.add(charCountLabel, BorderLayout.SOUTH);
		dialogPanel.add(descriptionPanel, gbc);

		// 设置首选大小
		dialogPanel.setPreferredSize(new Dimension(450, 400));

		return dialogPanel;
	}

	private void addFileTypes(String types) {
		if (types == null || types.trim().isEmpty()) {
			return;
		}

		Arrays.stream(types.split(","))
				.map(String::trim)
				.filter(type -> !type.isEmpty())
				.forEach(type -> {
					if (!fileTypes.contains(type)) {
						fileTypes.add(type);
						addTagLabel(type);
					}
				});

		fileTypeField.setText("");
		pack();
	}

	private void addTagLabel(String type) {
		JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		tagPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		JLabel tagLabel = new JLabel(type);
		JButton removeButton = new JButton("×");
		removeButton.setPreferredSize(new Dimension(16, 16));
		removeButton.addActionListener(e -> {
			fileTypes.remove(type);
			tagsPanel.remove(tagPanel);
			tagsPanel.revalidate();
			tagsPanel.repaint();
			pack();
		});

		tagPanel.add(tagLabel);
		tagPanel.add(removeButton);
		tagsPanel.add(tagPanel);
		tagsPanel.revalidate();
		tagsPanel.repaint();
	}

	private void updateCharCount() {
		int length = descriptionArea.getText().length();
		charCountLabel.setText(length + "/" + MAX_DESCRIPTION_CHARS);

		// 更新文本颜色
		if (DEFAULT_DESCRIPTION.equals(descriptionArea.getText())) {
			descriptionArea.setForeground(JBColor.GRAY);
		} else {
			descriptionArea.setForeground(new Color(255, 255, 255));
		}
	}

	@Override
	protected @Nullable ValidationInfo doValidate() {
		if (nameField.getText().trim().isEmpty()) {
			return new ValidationInfo(Bundle.get("dialog.project.rule.error.name.empty"), nameField);
		}
		if (typeComboBox.getSelectedItem() == RuleType.SPECIFIED_TYPE && fileTypes.isEmpty()) {
			return new ValidationInfo(Bundle.get("dialog.project.rule.error.file.type.empty"), fileTypeField);
		}
		return null;
	}


	/**
	 * 当用户点击确认按钮后
	 * 1.确保规则名称有内容输入，如果没有，则提示规则名称必填
	 * 2.确保用户输入的规则名称，项目的.idea/rules/里面的文件重名，如果重名，则提示更换名字
	 * 3.如果规则类型是指定类型的话，则用户至少填入一个文件后缀名，否则给用户提示
	 * 4.规则描述里面的内容不能为空，给用户提示下。还有字数限制仿照全局规则对话框GlobalRuleDialog
	 */
	@Override
	protected void doOKAction() {
		String ruleName = nameField.getText().trim();
		String description = descriptionArea.getText().trim();
		RuleType selectedType = (RuleType) typeComboBox.getSelectedItem();

		// 检查规则名称是否为空
		if (ruleName.isEmpty()) {
			Messages.showErrorDialog(
					Bundle.get("dialog.project.rule.error.name.empty"),
					Bundle.get("dialog.project.rule.error.title")
			);
			return;
		}

		ProjectRuleManager ruleManager = parent.getProject().getService(ProjectRuleManager.class);
		List<ProjectRuleDTO> existingRules = ruleManager.getProjectRules();

		// 检查规则名称是否重复
		if (isEditMode) {
			// 在编辑模式下，如果名称被修改了，需要检查新名称是否与其他规则重名
			if (!ruleName.equals(oldRuleName)) {
				if (existingRules.stream().anyMatch(rule -> rule.getName().equals(ruleName))) {
					Messages.showErrorDialog(
							Bundle.get("dialog.project.rule.error.name.exists"),
							Bundle.get("dialog.project.rule.error.title")
					);
					return;
				}
			}
		} else {
			// 新建模式下的重名检查
			if (existingRules.stream().anyMatch(rule -> rule.getName().equals(ruleName))) {
				Messages.showErrorDialog(
						Bundle.get("dialog.project.rule.error.name.exists"),
						Bundle.get("dialog.project.rule.error.title")
				);
				return;
			}
		}

		// 检查指定类型规则是否有文件类型
		if (selectedType == RuleType.SPECIFIED_TYPE && fileTypes.isEmpty()) {
			Messages.showErrorDialog(
					Bundle.get("dialog.project.rule.error.file.type.empty"),
					Bundle.get("dialog.project.rule.error.title")
			);
			return;
		}

		// 检查规则描述
		if (description.isEmpty() || DEFAULT_DESCRIPTION.equals(description)) {
			Messages.showErrorDialog(
					Bundle.get("dialog.project.rule.error.description.empty"),
					Bundle.get("dialog.project.rule.error.title")
			);
			return;
		}

		if (description.length() > MAX_DESCRIPTION_CHARS) {
			Messages.showErrorDialog(
					Bundle.get("dialog.project.rule.error.description.length", MAX_DESCRIPTION_CHARS),
					Bundle.get("dialog.project.rule.error.title")
			);
			return;
		}

		// 创建新规则
		result = new ProjectRuleDTO(ruleName, description, selectedType, null);
		if (RuleType.SPECIFIED_TYPE.equals(selectedType)) {
			result = new ProjectRuleDTO(ruleName, description, selectedType, fileTypes);
		}

		// 如果是编辑模式且规则名称发生变化，先删除旧规则
		if (isEditMode && !ruleName.equals(oldRuleName)) {
			ruleManager.deleteProjectRules(oldRuleName);
		}

		// 保存新规则
		ruleManager.editProjectRules(result);
		super.doOKAction();

		// 通知父组件更新
		parent.rebuildUI();
	}

	@Getter
	private ProjectRuleDTO result;


}