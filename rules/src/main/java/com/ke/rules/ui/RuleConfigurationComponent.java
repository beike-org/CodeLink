package com.ke.rules.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.ke.Bundle;
import com.ke.rules.ProjectRuleManager;
import com.ke.rules.dto.ProjectRuleDTO;
import com.ke.rules.enums.RuleType;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.setting.configuration.genral.user.bean.UserConfig;
import com.ke.utils.ComponentUtil;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RuleConfigurationComponent {

	private static final Logger LOGGER = Logger.getInstance(RuleConfigurationComponent.class);
	private final CardLayout cardLayout;
	private final JPanel contentPanel;
	private static final String MAIN_CARD = "MAIN_CARD";
	private final JPanel mainPanel;
	private boolean isRuleListExpanded = false;
	private final JBTextArea globalRulesTextArea;
	private JBLabel globalRulesCharCountLabel;
	private static final int MAX_CHARS = 6000;

	@Getter
	private final Project project;


	public RuleConfigurationComponent(Project project) {
		this.project = project;
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
		contentPanel = new JPanel(new MigLayout("fillx, insets 0, wrap 1", "[grow,fill]", "[]"));

		// 初始化全局规则相关组件
		globalRulesTextArea = new JBTextArea(10, 50);
		globalRulesCharCountLabel = new JBLabel();

		// 创建滚动面板并设置滚动策略
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// 增加滚动速度
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);

		// 启用鼠标滚轮滚动
		scrollPane.setWheelScrollingEnabled(true);

		// 为整个面板添加鼠标滚轮监听器
		mainPanel.addMouseWheelListener(e -> {
			scrollParentScrollPane(e.getWheelRotation());
			e.consume(); // 消费事件，防止事件传播
		});

		// 为内容面板也添加鼠标滚轮监听器
		contentPanel.addMouseWheelListener(e -> {
			scrollParentScrollPane(e.getWheelRotation());
			e.consume(); // 消费事件，防止事件传播
		});

		mainPanel.add(scrollPane, MAIN_CARD);
		buildUI();
	}

	private void buildUI() {
		contentPanel.removeAll();

		// 添加标题和描述
		JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]"));
		headerPanel.add(ComponentUtil.createIconTitledSeparator(Bundle.get("component.rule.title"), Bundle.get("component.rule.description"), 125), "wrap");
		contentPanel.add(headerPanel, "growx");

		// 添加规则面板
		contentPanel.add(createRulesPanel(), "growx");

		contentPanel.revalidate();
		contentPanel.repaint();

		SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, MAIN_CARD));
	}

	private JPanel createRulesPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// 全局规则面板
		JPanel globalRulesPanel = new JPanel();
		globalRulesPanel.setLayout(new BoxLayout(globalRulesPanel, BoxLayout.Y_AXIS));
		globalRulesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel titleLabel = new JLabel(Bundle.get("component.rule.global.title"));
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		globalRulesPanel.add(titleLabel);
		globalRulesPanel.add(Box.createVerticalStrut(8));

		JLabel descLabel = new JLabel(Bundle.get("component.rule.global.desc"));
		descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		globalRulesPanel.add(descLabel);
		globalRulesPanel.add(Box.createVerticalStrut(6));

		// 添加开关按钮
		JToggleButton ruleButton = new JToggleButton();
		ruleButton.setSelected(UserConfigState.getInstance().getState().getEnableGlobalRules());
		ruleButton.addActionListener(e -> {
			UserConfig userConfig = UserConfigState.getInstance().getState();
			userConfig.setEnableGlobalRules(ruleButton.isSelected());
		});

		JPanel togglePanel = ComponentUtil.createTogglePanel(Bundle.get("component.rule.global.enable"), Bundle.get("component.rule.global.enable.tip"), ruleButton, ruleButton.isSelected());
		togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		globalRulesPanel.add(togglePanel);

		globalRulesPanel.add(Box.createVerticalStrut(6));

		// 配置全局规则编辑框
		globalRulesTextArea.setLineWrap(true);
		globalRulesTextArea.setWrapStyleWord(true);
		globalRulesTextArea.setBackground(new JBColor(new Color(43, 43, 43), new Color(43, 43, 43)));

		// 添加自定义 TransferHandler 以保留粘贴文本的换行符
		globalRulesTextArea.setTransferHandler(new TransferHandler() {
			@Override
			public boolean importData(TransferSupport support) {
				try {
					String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
					globalRulesTextArea.replaceSelection(data);
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

		// 加载内容
		loadGlobalRulesContent();

		// 添加文本变化监听器
		globalRulesTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
					int length = globalRulesTextArea.getText().length();
					if (length > MAX_CHARS) {
						com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
							Messages.showWarningDialog(
									Bundle.get("component.rule.global.warning.length", MAX_CHARS),
									Bundle.get("component.rule.global.warning.title")
							);
							globalRulesTextArea.setText(globalRulesTextArea.getText().substring(0, MAX_CHARS));
							globalRulesTextArea.setCaretPosition(MAX_CHARS);
						});
					}
					updateGlobalRulesCharCount();
				});
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				updateGlobalRulesCharCount();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				updateGlobalRulesCharCount();
			}
		});

		// 添加按键监听器以预防字符输入
		globalRulesTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent e) {
				if (globalRulesTextArea.getText().length() >= MAX_CHARS &&
						!e.isControlDown() && !e.isMetaDown() &&
						e.getKeyChar() != KeyEvent.VK_BACK_SPACE &&
						e.getKeyChar() != KeyEvent.VK_DELETE) {
					e.consume();
				}
			}
		});

		// 添加失去焦点监听器，在失去焦点时保存内容
		globalRulesTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				saveGlobalRulesContent();
			}

			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				// 当获得焦点且内容为默认文本时，自动全选
				if (ProjectRuleManager.getDefaultGlobalRuleContent().equals(globalRulesTextArea.getText())) {
					globalRulesTextArea.selectAll();
				}
			}
		});
		// 编辑框里面的滚动条
		JBScrollPane scrollPane = new JBScrollPane(globalRulesTextArea);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 增加滚动速度

		globalRulesPanel.add(scrollPane);

		globalRulesPanel.add(Box.createVerticalStrut(6));

		// 添加字符计数标签
		globalRulesCharCountLabel = new JBLabel();
		globalRulesCharCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		globalRulesPanel.add(globalRulesCharCountLabel);
		updateGlobalRulesCharCount();

		// 项目规则面板
		JPanel projectRulesPanel = new JPanel();
		projectRulesPanel.setLayout(new BoxLayout(projectRulesPanel, BoxLayout.Y_AXIS));
		projectRulesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel projectTitleLabel = new JLabel(Bundle.get("component.rule.project.title"));
		projectTitleLabel.setFont(projectTitleLabel.getFont().deriveFont(Font.BOLD));
		projectTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		projectRulesPanel.add(projectTitleLabel);
		projectRulesPanel.add(Box.createVerticalStrut(8));

		JLabel projectDescLabel = new JLabel(Bundle.get("component.rule.project.desc"));
		projectDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		projectRulesPanel.add(projectDescLabel);
		projectRulesPanel.add(Box.createVerticalStrut(6));

		JButton addRuleButton = ComponentUtil.createPrimaryButton(Bundle.get("component.rule.project.add"), e -> projectRuleButtonAction());
		addRuleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		projectRulesPanel.add(addRuleButton);
		projectRulesPanel.add(Box.createVerticalStrut(10));

		// 创建可折叠面板
		JPanel collapsiblePanel = new JPanel(new BorderLayout());
		collapsiblePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		collapsiblePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		// 创建折叠按钮和标题面板
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		JButton toggleButton = new JButton("▶");
		toggleButton.setPreferredSize(new Dimension(20, 20));
		toggleButton.setBorderPainted(false);
		toggleButton.setContentAreaFilled(false);
		toggleButton.setFocusPainted(false);
		toggleButton.setOpaque(false);

		titleLabel = new JLabel(Bundle.get("component.rule.project.list.title"));
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) EditorColorsManager.getInstance().getGlobalScheme().getEditorFontSize()));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		headerPanel.add(toggleButton, BorderLayout.WEST);
		headerPanel.add(titleLabel, BorderLayout.CENTER);

		// 创建折叠内容面板
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 0));

		// 创建规则列表面板
		JPanel rulesListPanel = new JPanel();
		rulesListPanel.setLayout(new BoxLayout(rulesListPanel, BoxLayout.Y_AXIS));
		rulesListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// 获取ProjectRuleManager实例并加载规则
		List<ProjectRuleDTO> projectRules = new ArrayList<>();
		try {
			ProjectRuleManager ruleManager = project.getService(ProjectRuleManager.class);
			if (ruleManager != null) {
				projectRules = ruleManager.getProjectRules();
			} else {
				LOGGER.warn("ProjectRuleManager service is not available");
			}
		} catch (Exception e) {
			LOGGER.warn("Error loading project rules", e);
		}

		AtomicReference<List<ProjectRuleDTO>> projectRulesRef = new AtomicReference<>(projectRules);

		if (!projectRulesRef.get().isEmpty()) {
			JLabel projectRulesLabel = new JLabel(Bundle.get("component.rule.project.list.level"));
			projectRulesLabel.setFont(projectRulesLabel.getFont().deriveFont(Font.BOLD));
			rulesListPanel.add(projectRulesLabel);
			rulesListPanel.add(Box.createVerticalStrut(5));

			for (ProjectRuleDTO rule : projectRules) {
				JPanel rulePanel = createProjectRulePanel(rule);
				rulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				rulesListPanel.add(rulePanel);
				rulesListPanel.add(Box.createVerticalStrut(5));
			}
		}

		// 初始状态为折叠
		contentPanel.add(rulesListPanel, BorderLayout.CENTER);
		contentPanel.setVisible(false);

		// 添加折叠按钮事件
		toggleButton.addActionListener(e -> {
			isRuleListExpanded = !contentPanel.isVisible();
			toggleButton.setText(isRuleListExpanded ? "▼" : "▶");

			if (isRuleListExpanded) {
				// 展开时重新加载规则列表
				try {
					ProjectRuleManager ruleManager = project.getService(ProjectRuleManager.class);
					if (ruleManager != null) {
						projectRulesRef.set(ruleManager.getProjectRules());
					} else {
						LOGGER.warn("ProjectRuleManager service is not available");
						projectRulesRef.set(new ArrayList<>());
					}
				} catch (Exception error) {
					LOGGER.warn("Error loading project rules", error);
					projectRulesRef.set(new ArrayList<>());
				}
				rulesListPanel.removeAll();

				if (!projectRulesRef.get().isEmpty()) {
					JLabel projectRulesLabel = new JLabel(Bundle.get("component.rule.project.list.level"));
					projectRulesLabel.setFont(projectRulesLabel.getFont().deriveFont(Font.BOLD));
					rulesListPanel.add(projectRulesLabel);
					rulesListPanel.add(Box.createVerticalStrut(5));

					for (ProjectRuleDTO rule : projectRulesRef.get()) {
						JPanel rulePanel = createProjectRulePanel(rule);
						rulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
						rulesListPanel.add(rulePanel);
						rulesListPanel.add(Box.createVerticalStrut(5));
					}
				}

				rulesListPanel.revalidate();
				rulesListPanel.repaint();
			}

			contentPanel.setVisible(isRuleListExpanded);

			SwingUtilities.invokeLater(() -> {
				collapsiblePanel.revalidate();
				collapsiblePanel.repaint();

				// 在展开时，将父容器的滚动条设置到最低端
				if (isRuleListExpanded) {
					// 查找父容器中的JScrollPane
					JScrollPane parentScrollPane = findParentScrollPane();
					if (parentScrollPane != null) {
						JScrollBar verticalBar = parentScrollPane.getVerticalScrollBar();

						// 将滚动条设置到最低端
						verticalBar.setValue(verticalBar.getMaximum() - verticalBar.getVisibleAmount());

						// 尝试强制刷新UI
						parentScrollPane.revalidate();
						parentScrollPane.repaint();
					}
				}
			});
		});

		collapsiblePanel.add(headerPanel, BorderLayout.NORTH);
		collapsiblePanel.add(contentPanel, BorderLayout.CENTER);

		projectRulesPanel.add(collapsiblePanel);

		mainPanel.add(globalRulesPanel);
		mainPanel.add(Box.createVerticalStrut(16));
		mainPanel.add(projectRulesPanel);

		return mainPanel;
	}

	private JPanel createProjectRulePanel(ProjectRuleDTO rule) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createLineBorder(JBColor.border(), 1, true)
		));
		// 设置固定宽度，高度自适应
		panel.setMaximumSize(new Dimension(450, 120));
		panel.setPreferredSize(new Dimension(450, 120));
		panel.setMinimumSize(new Dimension(450, 120));

		// 第一行：文件信息和按钮
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		// 左侧：文件名和类型
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
//        leftPanel.setBackground(new Color(250, 250, 250));
		JLabel nameLabel = new JLabel(rule.getName());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		JLabel typeLabel = new JLabel("(" + rule.getType().getDisplayName() + ")");
		typeLabel.setForeground(JBColor.GRAY);
		leftPanel.add(nameLabel);
		leftPanel.add(typeLabel);

		// 右侧：编辑和删除按钮
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		JButton editButton = new JButton(Bundle.get("component.rule.project.edit"));
		JButton deleteButton = new JButton(Bundle.get("component.rule.project.delete"));
		editButton.setPreferredSize(new Dimension(60, 25));
		deleteButton.setPreferredSize(new Dimension(60, 25));

		editButton.addActionListener(e -> {
			ProjectRuleDialog dialog = new ProjectRuleDialog(rule, this);
			if (dialog.showAndGet()) {
				ProjectRuleDTO editedRule = dialog.getResult();
				project.getService(ProjectRuleManager.class).editProjectRules(editedRule);
			}
			rebuildUI();
		});

		deleteButton.addActionListener(e -> {
			int result = Messages.showYesNoDialog(
					Bundle.get("component.rule.project.delete.confirm", rule.getName()),
					Bundle.get("component.rule.project.delete.confirm.title"),
					Messages.getQuestionIcon()
			);

			if (result == Messages.YES) {
				project.getService(ProjectRuleManager.class).deleteProjectRules(rule.getName());
				rebuildUI();
			}
		});

		buttonsPanel.add(editButton);
		buttonsPanel.add(deleteButton);

		headerPanel.add(leftPanel, BorderLayout.WEST);
		headerPanel.add(buttonsPanel, BorderLayout.EAST);

		// 第二行：规则内容预览
		JTextArea contentPreview = new JTextArea();
		contentPreview.setEditable(false);
		contentPreview.setLineWrap(true);
		contentPreview.setWrapStyleWord(true);
		contentPreview.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPreview.setFont(contentPreview.getFont().deriveFont((float) EditorColorsManager.getInstance().getGlobalScheme().getEditorFontSize()));
		contentPreview.setForeground(new Color(100, 100, 100));
		contentPreview.setRows(2);

		StringBuilder previewText = new StringBuilder();
		previewText.append(rule.getContent());

		// 如果是SPECIFIED_TYPE类型，显示文件类型信息
		if (RuleType.SPECIFIED_TYPE.equals(rule.getType()) && rule.getRegex() != null && !rule.getRegex().isEmpty()) {
			previewText.append("\n").append(Bundle.get("component.rule.project.applicable.file.types")).append(": ").append(String.join(", ", rule.getRegex()));
		}

		String finalText = previewText.toString();
		if (finalText.length() > 100) {
			finalText = finalText.substring(0, 100) + "...";
		}
		contentPreview.setText(finalText);

		panel.add(headerPanel);
		panel.add(contentPreview);

		return panel;
	}

	private void loadGlobalRulesContent() {
		ProjectRuleManager projectRuleManager = project.getService(ProjectRuleManager.class);
		String content = projectRuleManager.getAppRule();
		if (content != null && !content.trim().isEmpty()) {
			globalRulesTextArea.setText(content);
			globalRulesTextArea.setForeground(new Color(255, 255, 255)); // 使用RGB纯白色
		} else {
			globalRulesTextArea.setText(ProjectRuleManager.getDefaultGlobalRuleContent());
			globalRulesTextArea.setForeground(JBColor.GRAY);
		}
	}

	private void updateGlobalRulesCharCount() {
		int length = globalRulesTextArea.getText().length();
		globalRulesCharCountLabel.setText(length + "/" + MAX_CHARS);

		// 更新文本颜色
		if (ProjectRuleManager.getDefaultGlobalRuleContent().equals(globalRulesTextArea.getText())) {
			globalRulesTextArea.setForeground(JBColor.GRAY);
		} else {
			globalRulesTextArea.setForeground(new Color(255, 255, 255));
		}
	}

	private void saveGlobalRulesContent() {
		String content = globalRulesTextArea.getText();
		if (!ProjectRuleManager.getDefaultGlobalRuleContent().equals(content)) {
			project.getService(ProjectRuleManager.class).saveAppRule(content);
		}
	}

	public void rebuildUI() {
		final boolean wasExpanded = isRuleListExpanded;
		SwingUtilities.invokeLater(() -> {
			buildUI();
			cardLayout.show(mainPanel, MAIN_CARD);

			// 找到折叠面板并恢复展开状态
			for (Component comp : mainPanel.getComponents()) {
				if (comp instanceof JScrollPane scrollPane) {
					Component[] components = scrollPane.getViewport().getView().getParent().getComponents();
					for (Component c : components) {
						if (c instanceof JPanel) {
							findAndRestoreExpandState((JPanel) c, wasExpanded);
						}
					}

					// 将滚动条设置到最低端，而不是顶部
					JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
					verticalBar.setValue(verticalBar.getMaximum() - verticalBar.getVisibleAmount());

					// 水平滚动条仍然设置为0
					scrollPane.getHorizontalScrollBar().setValue(0);
					break;
				}
			}
		});
	}

	private void findAndRestoreExpandState(JPanel panel, boolean shouldExpand) {
		for (Component comp : panel.getComponents()) {
			if (comp instanceof JPanel innerPanel) {
                if (innerPanel.getLayout() instanceof BorderLayout) {
					for (Component c : innerPanel.getComponents()) {
						if (c instanceof JPanel && "Center".equals(((BorderLayout) innerPanel.getLayout()).getConstraints(c))) {
							c.setVisible(shouldExpand);
							for (Component headerComp : innerPanel.getComponents()) {
								if (headerComp instanceof JPanel) {
									for (Component btn : ((JPanel) headerComp).getComponents()) {
										if (btn instanceof JButton) {
											((JButton) btn).setText(shouldExpand ? "▼" : "▶");
											break;
										}
									}
								}
							}
							break;
						}
					}
				}
				findAndRestoreExpandState(innerPanel, shouldExpand);
			}
		}
	}

	private void projectRuleButtonAction() {
		ProjectRuleDialog dialog = new ProjectRuleDialog(this);
		if (dialog.showAndGet()) {
			// 设置规则列表为展开状态
			isRuleListExpanded = true;
			rebuildUI();
		}
	}

	public JComponent getPanel() {
		return mainPanel;
	}

	/**
	 * 查找父容器中的JScrollPane
	 */
	private JScrollPane findParentScrollPane() {
		Container parent = this.mainPanel.getParent();

		while (parent != null) {
			if (parent instanceof JScrollPane) {
				return (JScrollPane) parent;
			}

			// 检查当前容器是否被JScrollPane包装
			Container parentOfParent = parent.getParent();
			if (parentOfParent instanceof JScrollPane) {
				return (JScrollPane) parentOfParent;
			}

			parent = parentOfParent;
		}

		return null;
	}

	/**
	 * 直接滚动父容器的滚动条
	 */
	private void scrollParentScrollPane(int wheelRotation) {
		// 查找父容器中的JScrollPane
		JScrollPane parentScrollPane = findParentScrollPane();
		if (parentScrollPane != null) {
			JScrollBar verticalBar = parentScrollPane.getVerticalScrollBar();

			// 尝试强制设置滚动条的值
			int newValue = verticalBar.getValue() + wheelRotation * 16; // 调整滚动增量

			// 确保新值在有效范围内
			newValue = Math.max(verticalBar.getMinimum(),
					Math.min(newValue, verticalBar.getMaximum() - verticalBar.getVisibleAmount()));

			// 直接设置滚动条的值
			verticalBar.setValue(newValue);

			// 尝试强制刷新UI
			parentScrollPane.revalidate();
			parentScrollPane.repaint();
		}
	}
}
