package com.ke.mcp.ui;

import com.intellij.openapi.project.Project;
import com.ke.Bundle;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.CommandConfigDTO;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.SseConfigDTO;
import com.ke.mcp.dto.req.UpdateConfigReq;
import com.ke.mcp.enums.McpTypeEnum;
import com.ke.mcp.enums.WebviewRefreshEnum;
import com.ke.mcp.manager.McpConfigFileManager;
import com.ke.mcp.webview.handler.McpWebviewRefreshPTWHandler;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditMcpServerDialog extends JDialog {

	private JTextField nameField;
	private JComboBox<McpTypeEnum> typeComboBox;
	private JTextField detailField;
	private JLabel detailLabel;

	private JPanel envListPanel;
	private JPanel envPanelContainer;
	private final List<RowEntry> envEntries = new ArrayList<>();

	private JPanel centerPanel;
	private final Project project;
	@Getter
	private McpConfigDTO mcpConfigDTO;
	@Getter
	private boolean confirmed = false;

	private String originalDetail;
	private Map<String, String> originalEnv;
	private McpTypeEnum originalType;

	private static class RowEntry {
		JTextField keyField;
		JTextField valueField;

		RowEntry(JTextField keyField, JTextField valueField) {
			this.keyField = keyField;
			this.valueField = valueField;
		}
	}

	public EditMcpServerDialog(McpConfigDTO mcpConfigDTO, Project project, Window owner) {
		super(owner);
		setTitle(Bundle.get("dialog.mcp.edit.title"));
		setModal(true);
		this.mcpConfigDTO = mcpConfigDTO;
		this.project = project;

		initUI();
		populateFields();
		saveOriginalValues();
		pack(); // 首次自适应
		setResizable(false);
		setLocationRelativeTo(owner);
	}

	private void initUI() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		nameField = new JTextField();
		nameField.setEditable(false);

		// === 类型处理 ===
		if (mcpConfigDTO.getType() == McpTypeEnum.COMMAND) {
			typeComboBox = null;
			addToInputPanel(inputPanel, gbc, 1, Bundle.get("dialog.mcp.edit.type.label"), new JLabel(Bundle.get("dialog.mcp.edit.command.type")));
		} else {
			typeComboBox = new JComboBox<>(McpTypeEnum.visibleValues());
			addToInputPanel(inputPanel, gbc, 1, Bundle.get("dialog.mcp.edit.type.label"), typeComboBox);
			typeComboBox.addActionListener(e -> onTypeChanged());
		}

		detailLabel = new JLabel(Bundle.get("dialog.mcp.edit.command.label"));
		detailField = new JTextField();

		addToInputPanel(inputPanel, gbc, 0, Bundle.get("dialog.mcp.edit.name.label"), nameField);
		addToInputPanel(inputPanel, gbc, 2, detailLabel.getText(), detailField);

		centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.add(inputPanel, BorderLayout.NORTH);

		// === 配置面板（无滚动容器）===
		envPanelContainer = new JPanel(new BorderLayout());
		envPanelContainer.setBorder(BorderFactory.createTitledBorder(Bundle.get("dialog.mcp.edit.env.title")));
		envListPanel = new JPanel();
		envListPanel.setLayout(new BoxLayout(envListPanel, BoxLayout.Y_AXIS));
		envPanelContainer.add(envListPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(Bundle.get("dialog.mcp.edit.button.ok"));
		JButton cancelButton = new JButton(Bundle.get("dialog.mcp.edit.button.cancel"));

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		okButton.addActionListener(e -> onOK());
		cancelButton.addActionListener(e -> dispose());

		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);
		getRootPane().setDefaultButton(okButton);
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

	private void onTypeChanged() {
		McpTypeEnum type = getCurrentType();
		updateDetailLabel(type);

		envListPanel.removeAll();
		envEntries.clear();
		centerPanel.remove(envPanelContainer);

		if (addEnvRowsFromCurrentConfig(type)) {
			centerPanel.add(envPanelContainer, BorderLayout.CENTER);
		}

		SwingUtilities.invokeLater(this::pack);
	}

	private void populateFields() {
		nameField.setText(mcpConfigDTO.getName());
		updateDetailLabel(mcpConfigDTO.getType());

		if (mcpConfigDTO instanceof CommandConfigDTO cmd) {
			detailField.setText(cmd.getCommand() + " " + cmd.getArgString());
		} else if (mcpConfigDTO instanceof SseConfigDTO sse) {
			detailField.setText(sse.getUrl());
		}

		if (typeComboBox != null) {
			typeComboBox.setSelectedItem(mcpConfigDTO.getType());
			onTypeChanged();
		} else {
			if (addEnvRowsFromCurrentConfig(McpTypeEnum.COMMAND)) {
				centerPanel.add(envPanelContainer, BorderLayout.CENTER);
			}
			SwingUtilities.invokeLater(this::pack);
		}
	}

	private boolean addEnvRowsFromCurrentConfig(McpTypeEnum type) {
		Map<String, String> env = mcpConfigDTO.getEnv();
		boolean hasValid = false;

		if (env != null && !env.isEmpty()) {
			int i = 0;
			for (Map.Entry<String, String> entry : env.entrySet()) {
				String key = entry.getKey();
				if (key == null || key.isBlank()) continue;
				hasValid = true;
				addEnvRow(key, entry.getValue(), i == 0 && type == McpTypeEnum.COMMAND);
				i++;
			}
		}
		if ((type == McpTypeEnum.COMMAND || type == McpTypeEnum.SSE) && !hasValid) {
			addEnvRow("", "", true);
			return true;
		}
		return hasValid;
	}

	private void updateDetailLabel(McpTypeEnum type) {
		String label = switch (type) {
			case COMMAND -> Bundle.get("dialog.mcp.edit.command.command");
			case SSE -> Bundle.get("dialog.mcp.edit.command.sse");
		};
		detailLabel.setText(label);
	}

	private McpTypeEnum getCurrentType() {
		return typeComboBox != null
				? (McpTypeEnum) typeComboBox.getSelectedItem()
				: McpTypeEnum.COMMAND;
	}

	private void addEnvRow(String key, String value, boolean withButton) {
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JTextField keyField = new JTextField(12);
		JTextField valueField = new JTextField(16);

		keyField.setText(key);
		valueField.setText(value);

		// === 类型判断 ===
		McpTypeEnum type = getCurrentType();
		// === 默认值处理逻辑（置灰 + 聚焦全选） ===
		if (value != null && !value.isEmpty()) {
			valueField.setForeground(Color.GRAY);

			valueField.addFocusListener(new java.awt.event.FocusAdapter() {
				private boolean hasFocused = false;

				@Override
				public void focusGained(java.awt.event.FocusEvent e) {
					if (!hasFocused) {
						hasFocused = true;
						SwingUtilities.invokeLater(() -> {
							valueField.selectAll(); // 全选内容
							valueField.setForeground(UIManager.getColor("TextField.foreground")); // 恢复颜色
						});
					}
				}
			});
		}

		// === 添加行组件 ===
		RowEntry entry = new RowEntry(keyField, valueField);
		envEntries.add(entry);

		row.add(new JLabel(Bundle.get("dialog.mcp.edit.env.key")));
		row.add(keyField);
		row.add(new JLabel(Bundle.get("dialog.mcp.edit.env.value")));
		row.add(valueField);

		// === COMMAND 类型才显示 + / - 按钮 ===
		if (withButton && type == McpTypeEnum.COMMAND) {
			JButton addButton = new JButton("+");
			JButton removeButton = new JButton("-");

			addButton.addActionListener(e -> {
				addEnvRow("", "", false);
				pack();
			});
			removeButton.addActionListener(e -> {
				removeLastRow();
				pack();
			});

			row.add(addButton);
			row.add(removeButton);
		}

		envListPanel.add(row);
		envListPanel.revalidate();
		envListPanel.repaint();
	}


	private void removeLastRow() {
		if (envListPanel.getComponentCount() > 1) {
			envListPanel.remove(envListPanel.getComponentCount() - 1);
			envEntries.remove(envEntries.size() - 1);
			envListPanel.revalidate();
			envListPanel.repaint();
			pack();
		}
	}

	private void saveOriginalValues() {
		originalType = getCurrentType();
		originalDetail = detailField.getText().trim();
		originalEnv = new LinkedHashMap<>();
		if (mcpConfigDTO.getEnv() != null) {
			originalEnv.putAll(mcpConfigDTO.getEnv());
		}
	}

	private boolean isModified() {
		// 检查类型是否变化
		if (originalType != getCurrentType()) {
			return true;
		}

		// 检查详情是否变化
		String currentDetail = detailField.getText().trim();
		if (!originalDetail.equals(currentDetail)) {
			return true;
		}

		// 检查环境变量是否变化
		Map<String, String> currentEnv = new LinkedHashMap<>();
		for (RowEntry entry : envEntries) {
			String key = entry.keyField.getText().trim();
			String value = entry.valueField.getText().trim();
			if (!key.isEmpty() && !value.isEmpty()) {
				currentEnv.put(key, value);
			}
		}

		if (originalEnv.size() != currentEnv.size()) {
			return true;
		}

		for (Map.Entry<String, String> entry : originalEnv.entrySet()) {
			String currentValue = currentEnv.get(entry.getKey());
			if (currentValue == null || !currentValue.equals(entry.getValue())) {
				return true;
			}
		}

		return false;
	}

	private void onOK() {
		if (!isModified()) {
			dispose();
			return;
		}

		McpTypeEnum type = getCurrentType();
		String name = nameField.getText().trim();
		String detail = detailField.getText().trim();

		if (detail.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					Bundle.get("dialog.mcp.edit.error.command.empty"),
					Bundle.get("dialog.mcp.edit.error.title"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Map<String, String> env = new LinkedHashMap<>();
		for (RowEntry entry : envEntries) {
			String key = entry.keyField.getText().trim();
			String value = entry.valueField.getText().trim();
			if (!key.isEmpty() && !value.isEmpty()) {
				env.put(key, value);
			}
		}
		if (env.isEmpty()) env = null;

		if (type == McpTypeEnum.COMMAND) {
			String[] parts = detail.split("\\s+", 2);
			String cmd = parts[0];
			String args = parts.length > 1 ? parts[1] : "";
			mcpConfigDTO = new CommandConfigDTO(name, cmd, args, false, env);
		} else if (type == McpTypeEnum.SSE) {
			mcpConfigDTO = new SseConfigDTO(name, detail, false, env);
		}

		McpPTSHandler.getInstance().updateTools(
				UpdateConfigReq.convertDTO(project.getLocationHash(), mcpConfigDTO),
				project.getService(McpConfigFileManager.class).getConfigAgentPort()
		);
		project.getService(McpConfigFileManager.class).addOrUpdateConfig(mcpConfigDTO, false, false);
		confirmed = true;

		McpWebviewRefreshPTWHandler.notifyConfigChange(WebviewRefreshEnum.SERVER_LIST, project);
		dispose();
	}

}
