package com.ke.mcp.ui;

import com.ke.Bundle;
import com.ke.mcp.dto.CommandConfigDTO;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.SseConfigDTO;
import com.ke.mcp.enums.McpTypeEnum;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

public class AddMcpServerDialog extends JDialog {

	private final JTextField nameField;
	@Getter
	private final JComboBox<McpTypeEnum> typeComboBox;
	@Getter
	private final JTextField commandField;
	@Getter
	private boolean confirmed = false;
	private McpConfigDTO addedMcp;
	private final JButton confirmButton; // 添加这行

	private final JPanel envListPanel;
	private final JPanel envPanelContainer;
	private final List<RowEntry> envEntries = new ArrayList<>();

	public AddMcpServerDialog(Window owner) {
		super(owner);
		setTitle(Bundle.get("dialog.mcp.add.title"));
		setModal(true);

		// 设置最小尺寸
		setMinimumSize(new Dimension(400, 300));
		setLocationRelativeTo(owner); // 设置相对于父窗口的位置

		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		// ===== 顶部输入区域 =====
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		nameField = new JTextField();
		nameField.setToolTipText(Bundle.get("dialog.mcp.name.tip"));

		typeComboBox = new JComboBox<>(McpTypeEnum.visibleValues());
		typeComboBox.addActionListener(e -> updateEnvPanelState());

		commandField = new JTextField();
		commandField.setToolTipText(Bundle.get("dialog.mcp.command.tip"));

		addToInputPanel(inputPanel, gbc, 0, Bundle.get("dialog.mcp.name.label"), nameField);
		addToInputPanel(inputPanel, gbc, 1, Bundle.get("dialog.mcp.type.label"), typeComboBox);
		addToInputPanel(inputPanel, gbc, 2, Bundle.get("dialog.mcp.command.label"), commandField);

		envPanelContainer = new JPanel(new BorderLayout());
		envPanelContainer.setBorder(BorderFactory.createTitledBorder(Bundle.get("dialog.mcp.env.title")));

		// ===== 环境变量配置区域 =====
		envListPanel = new JPanel();
		envListPanel.setLayout(new BoxLayout(envListPanel, BoxLayout.Y_AXIS));

		// 添加一个带有添加/删除按钮的空面板
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("+");
		JButton removeButton = new JButton("-");
		addButton.addActionListener(e -> addNewRow());
		removeButton.addActionListener(e -> removeLastRow());
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);

		JPanel envControlPanel = new JPanel(new BorderLayout());
		envControlPanel.add(envListPanel, BorderLayout.CENTER);
		envControlPanel.add(buttonPanel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane(envControlPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		envPanelContainer.add(scrollPane, BorderLayout.CENTER);

		// ===== 底部按钮 =====
		JPanel confirmButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		confirmButton = new JButton(Bundle.get("dialog.mcp.button.confirm")); // 修改这行，使用成员变量
		JButton cancelButton = new JButton(Bundle.get("dialog.mcp.button.cancel"));
		getRootPane().setDefaultButton(confirmButton);
		confirmButtonPanel.add(confirmButton);
		confirmButtonPanel.add(cancelButton);

		confirmButton.addActionListener(e -> onConfirm());
		cancelButton.addActionListener(e -> dispose());

		// ===== 总体布局 =====
		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.add(inputPanel, BorderLayout.NORTH);
		centerPanel.add(envPanelContainer, BorderLayout.CENTER);

		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(confirmButtonPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);

		pack();
		setResizable(true);
		setLocationRelativeTo(owner);
		updateEnvPanelState();
	}

	private void addToInputPanel(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent comp) {
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.weightx = 0.2;
		panel.add(new JLabel(label, SwingConstants.LEFT), gbc);

		gbc.gridx = 1;
		gbc.weightx = 0.8;
		panel.add(comp, gbc);
	}


	private void addNewRow() {
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		JTextField keyField = new JTextField(10);
		JTextField valueField = new JTextField(10);

		RowEntry rowEntry = new RowEntry(keyField, valueField);
		envEntries.add(rowEntry);

		row.add(new JLabel(Bundle.get("dialog.mcp.env.key")));
		row.add(keyField);
		row.add(new JLabel(Bundle.get("dialog.mcp.env.value")));
		row.add(valueField);

		envListPanel.add(row);
		envListPanel.revalidate();
		envListPanel.repaint();
		updateEnvPanelState();

		SwingUtilities.invokeLater(keyField::requestFocusInWindow);
	}

	// 新方法：删除最后一行
	private void removeLastRow() {
		int rowCount = envListPanel.getComponentCount();
		// 如果只有一行，不执行删除操作
		if (rowCount <= 1) {
			return;
		}

		// 删除最后一行
		envListPanel.remove(rowCount - 1);
		// 同时从entries中移除
		if (!envEntries.isEmpty()) {
			envEntries.remove(envEntries.size() - 1);
		}

		envListPanel.revalidate();
		envListPanel.repaint();
		updateEnvPanelState();
	}

	private void updateEnvPanelState() {
		boolean visible = true; // 移除类型限制，所有类型都可以设置环境变量

		envPanelContainer.setVisible(visible);

		for (RowEntry entry : envEntries) {
			entry.keyField.setEnabled(visible);
			entry.valueField.setEnabled(visible);
		}

		SwingUtilities.invokeLater(() -> {
			setPreferredSize(null); // 清除首选大小以允许自动调整
			pack();
			revalidate();
			repaint();
		});
	}

	private void onConfirm() {
		String name = nameField.getText().trim();
		McpTypeEnum type = (McpTypeEnum) typeComboBox.getSelectedItem();
		String commandOrUrl = commandField.getText().trim();

		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(this, Bundle.get("dialog.mcp.error.name.empty"), Bundle.get("dialog.mcp.error.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (commandOrUrl.isEmpty()) {
			JOptionPane.showMessageDialog(this, Bundle.get("dialog.mcp.error.command.empty"), Bundle.get("dialog.mcp.error.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 显示loading状态
		setUIEnabled(false);
		confirmButton.setText(Bundle.get("dialog.mcp.button.processing"));

		// 使用SwingWorker在后台执行操作
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				// 收集环境变量
				Map<String, String> env = new HashMap<>();
				for (RowEntry entry : envEntries) {
					String key = entry.keyField.getText().trim();
					String value = entry.valueField.getText().trim();
					if (!key.isEmpty() && !value.isEmpty()) {
						env.put(key, value);
					}
				}
				if (env.isEmpty()) {
					env = null;
				}

				// 创建MCP配置
				switch (Objects.requireNonNull(type)) {
					case COMMAND:
						String[] commandParts = commandOrUrl.split("\\s+", 2);
						String command = commandParts[0];
						String args = commandParts.length > 1 ? commandParts[1] : "";
						addedMcp = new CommandConfigDTO(name, command, args, false, env);
						break;
					case SSE:
						addedMcp = new SseConfigDTO(name, commandOrUrl, false, env);
						break;
					default:
						SwingUtilities.invokeLater(() -> {
							JOptionPane.showMessageDialog(AddMcpServerDialog.this,
									Bundle.get("dialog.mcp.error.type.unsupported", type), Bundle.get("dialog.mcp.error.title"), JOptionPane.ERROR_MESSAGE);
							setUIEnabled(true);
							confirmButton.setText(Bundle.get("dialog.mcp.button.confirm"));
						});
						return null;
				}

				// 模拟处理时间（可根据实际需要调整或移除）
				Thread.sleep(300);
				return null;
			}

			@Override
			protected void done() {
				// 在UI线程中完成操作
				confirmed = true;
				dispose();
			}
		};

		worker.execute();
	}

	private void setUIEnabled(boolean enabled) {
		nameField.setEnabled(enabled);
		typeComboBox.setEnabled(enabled);
		commandField.setEnabled(enabled);
		confirmButton.setEnabled(enabled);

		// 禁用/启用环境变量输入框
		for (RowEntry entry : envEntries) {
			entry.keyField.setEnabled(enabled);
			entry.valueField.setEnabled(enabled);
		}
	}

	public McpConfigDTO getNewMcp() {
		return addedMcp;
	}

	private static class RowEntry {
		JTextField keyField;
		JTextField valueField;

		RowEntry(JTextField keyField, JTextField valueField) {
			this.keyField = keyField;
			this.valueField = valueField;
		}
	}

	public void addEnvEntry(String key, String value) {
		// 首先检查是否有空行可以使用
		boolean foundEmptyRow = false;
		for (RowEntry entry : envEntries) {
			if (entry.keyField.getText().trim().isEmpty() && entry.valueField.getText().trim().isEmpty()) {
				// 使用现有的空行
				entry.keyField.setText(key);
				entry.valueField.setText(value);
				foundEmptyRow = true;
				break;
			}
		}

		// 如果没有找到空行，则添加新行
		if (!foundEmptyRow) {
			JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			JTextField keyField = new JTextField(10);
			JTextField valueField = new JTextField(10);

			keyField.setText(key);
			valueField.setText(value);

			RowEntry rowEntry = new RowEntry(keyField, valueField);
			envEntries.add(rowEntry);

			row.add(new JLabel("Key:"));
			row.add(keyField);
			row.add(new JLabel("Value:"));
			row.add(valueField);

			envListPanel.add(row);
		}

		envListPanel.revalidate();
		envListPanel.repaint();
		updateEnvPanelState();
	}
}
