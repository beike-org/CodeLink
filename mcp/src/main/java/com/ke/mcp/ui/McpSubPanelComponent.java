package com.ke.mcp.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import com.ke.Bundle;
import com.ke.mcp.McpIcons;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.CommandConfigDTO;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.SseConfigDTO;
import com.ke.mcp.dto.req.LaunchConfigReq;
import com.ke.mcp.dto.req.SwitchAvailableReq;
import com.ke.mcp.dto.req.ToolListReq;
import com.ke.mcp.dto.req.UpdateConfigReq;
import com.ke.mcp.dto.resp.SwitchAvailableResp;
import com.ke.mcp.dto.resp.ToolListResp;
import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.McpTypeEnum;
import com.ke.mcp.enums.WebviewRefreshEnum;
import com.ke.mcp.manager.McpConfigFileManager;
import com.ke.mcp.webview.handler.McpWebviewRefreshPTWHandler;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用 JBList 的实现，空列表时自动显示 "没有可用工具"。
 */
public class McpSubPanelComponent extends JPanel {

	private static final Font DEFAULT_FONT = UIManager.getFont("Label.font");
	private static final Font BOLD_FONT = DEFAULT_FONT.deriveFont(Font.BOLD);

	private final Project project;
	private McpConfigDTO detail;

	// 按钮
	private JButton availableButton;
	private JButton reconnectButton;
	private JButton editButton;
	private JButton deleteButton;

	// 工具列表（JBList）
	private JBList<String> toolsList;

	// 其他组件
	private JLabel serverStatusIcon;
	private JPanel commandPanel;
	private JPanel contentPanel;

	// 用于跟踪鼠标是否在工具列表上
	private boolean isMouseOverToolList = false;

	public McpSubPanelComponent(McpConfigDTO detail, Project project) {
		this.detail = detail;
		this.project = project;
		initUI();
		setupListeners();
	}

	/**
	 * ---------------------------- UI 初始化 ----------------------------
	 */
	private void initUI() {
		setLayout(new BorderLayout(10, 10));

		List<String> toolNameList = new ArrayList<>();
		DefaultListModel<String> listModel = new DefaultListModel<>();
		if (AvailableStatusEnum.ENABLED.equals(detail.getStatus())) {
			toolNameList = fetchToolNames();
			toolNameList.forEach(listModel::addElement);
		}

		toolsList = new JBList<>(listModel);
		toolsList.setVisibleRowCount(6);
		toolsList.setFont(DEFAULT_FONT);
		toolsList.getEmptyText().setText(Bundle.get("component.mcp.sub.tools.empty"));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		availableButton = new JButton(detail.getStatus() == AvailableStatusEnum.ENABLED ?
				Bundle.get("component.mcp.sub.button.enabled") :
				Bundle.get("component.mcp.sub.button.disabled"),
				detail.getStatus() == AvailableStatusEnum.ENABLED ? McpIcons.ENABLE : McpIcons.DISABLE);
		reconnectButton = new JButton(Bundle.get("component.mcp.sub.button.reconnect"), McpIcons.RECONNECT);
		editButton = new JButton(Bundle.get("component.mcp.sub.button.edit"), McpIcons.EDIT);
		deleteButton = new JButton(Bundle.get("component.mcp.sub.button.delete"), McpIcons.DELETE);

		for (JButton btn : List.of(availableButton, reconnectButton, editButton, deleteButton)) {
			btn.setFont(DEFAULT_FONT);
			buttonPanel.add(btn);
		}

		Color normal = UIUtil.getPanelBackground();
		Color hover = normal.darker();
		Color pressed = normal.darker().darker();
		styleButton(availableButton, normal, hover, pressed);
		styleButton(reconnectButton, normal, hover, pressed);
		styleButton(editButton, normal, hover, pressed);
		styleButton(deleteButton, normal, hover, pressed);


		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		serverStatusIcon = new JLabel("Status");
		serverStatusIcon.setFont(DEFAULT_FONT);
		serverStatusIcon.setIcon(AvailableStatusEnum.DISABLED.equals(detail.getStatus()) ? McpIcons.GREY : CollectionUtils.isNotEmpty(toolNameList) ? McpIcons.GREEN : McpIcons.RED);
		statusPanel.add(serverStatusIcon);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(statusPanel, BorderLayout.WEST);
		topPanel.add(buttonPanel, BorderLayout.EAST);

		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
		toolPanel.setBorder(BorderFactory.createTitledBorder(Bundle.get("component.mcp.sub.tools.title")));
		// 设置固定的首选大小，防止抖动
		toolPanel.setPreferredSize(new Dimension(200, 120));
		toolPanel.setMinimumSize(new Dimension(200, 120));
		JScrollPane scrollPane = new JScrollPane(toolsList);
		scrollPane.setPreferredSize(new Dimension(200, 80));

		// 为滚动面板添加鼠标监听器，跟踪鼠标是否在工具列表上
		scrollPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				isMouseOverToolList = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				isMouseOverToolList = false;
			}
		});

		// 为工具列表添加鼠标滚轮监听器
		scrollPane.addMouseWheelListener(e -> {
			// 当鼠标在工具列表上时，只处理工具列表的滚动，不传播到父容器
			JScrollBar bar = scrollPane.getVerticalScrollBar();
			int delta = e.getWheelRotation();

			// 如果滚动条可见，则处理滚动
			if (bar.isVisible()) {
				// 计算新的滚动位置
				int currentValue = bar.getValue();
				int minValue = bar.getMinimum();
				int maxValue = bar.getMaximum() - bar.getVisibleAmount();

				// 检查是否已经到达边界
				boolean atTop = currentValue <= minValue;
				boolean atBottom = currentValue >= maxValue;

				// 如果向上滚动且已经在顶部，或向下滚动且已经在底部，直接消费事件不做任何处理
				if ((delta < 0 && atTop) || (delta > 0 && atBottom)) {
					// 不要尝试滚动父容器，直接消费事件并返回
					e.consume();
					return;
				}

				// 在滚动条范围内，计算新的滚动位置
				int newValue = currentValue + delta * 16; // 使用固定的滚动单位

				// 确保新值在有效范围内
				newValue = Math.max(minValue, Math.min(newValue, maxValue));

				// 设置新的滚动位置
				bar.setValue(newValue); // 直接设置，不使用invokeLater
			}

			// 始终消费事件，防止传播
			e.consume();
		});

		toolPanel.add(scrollPane);

		commandPanel = createCommandPanel(detail, toolNameList);

		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(toolPanel);
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(commandPanel);

		add(topPanel, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
						detail.getName(),
						TitledBorder.LEFT,
						TitledBorder.TOP,
						BOLD_FONT.deriveFont(18f),
						Color.LIGHT_GRAY),
				null));

		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
	}


	public static void styleButton(JButton btn, Color normal, Color hover, Color pressed) {
		btn.setFocusable(false);
		btn.setContentAreaFilled(false);
		btn.setOpaque(true);
		btn.setBackground(normal);

		btn.getModel().addChangeListener(e -> {
			ButtonModel m = btn.getModel();
			if (m.isPressed()) {
				btn.setBackground(pressed);
			} else if (m.isRollover()) {
				btn.setBackground(hover);
			} else {
				btn.setBackground(normal);
			}
		});
	}


	/**
	 * ---------------------------- 监听 ----------------------------
	 */
	private void setupListeners() {
		availableButton.addActionListener(e -> toggleAvailableStatus());
		reconnectButton.addActionListener(e -> onReconnectClicked());
		editButton.addActionListener(e -> onEditButtonClicked());
		deleteButton.addActionListener(e -> onDeleteButtonClicked());

		// 为整个面板添加鼠标滚轮监听器
		this.addMouseWheelListener(e -> {
			// 如果事件已经被消费，不再处理
			if (e.isConsumed()) {
				return;
			}

			// 如果鼠标在工具列表上，不处理事件
			if (isMouseOverToolList) {
				return;
			}

			// 对于其他区域，直接滚动父容器
			scrollParentScrollPane(e.getWheelRotation());
			e.consume();
		});

	}

	/**
	 * ---------------------------- 工具列表刷新 ----------------------------
	 */
	private void refreshToolsList() {
		List<String> toolNameList = fetchToolNames();
		DefaultListModel<String> model = (DefaultListModel<String>) toolsList.getModel();
		model.clear();
		toolNameList.forEach(model::addElement);
		// server 状态灯
		serverStatusIcon.setIcon(model.getSize() > 0 ? McpIcons.GREEN : McpIcons.RED);
	}

	/**
	 * 调用 PTS 获取工具名称列表
	 */
	private List<String> fetchToolNames() {
		List<ToolListResp> resp = McpPTSHandler.getInstance().getToolList(
				new ToolListReq(project.getLocationHash(), List.of(detail.getName())),
				project.getService(McpConfigFileManager.class).getConfigAgentPort()
		);
		List<String> names = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(resp)) {
			for (ToolListResp.ToolInfo t : resp.get(0).getTools()) {
				names.add(t.getToolName());
			}
		}
		return names;
	}

	/**
	 * ---------------------------- 命令面板 ----------------------------
	 */
	private JPanel createCommandPanel(McpConfigDTO detail, List<String> toolList) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(Bundle.get("component.mcp.sub.connection.title")));
		String typeInfo;
		if (McpTypeEnum.SSE.equals(detail.getType())) {
			typeInfo = Bundle.get("component.mcp.sub.connection.type.sse") + ((SseConfigDTO) detail).getUrl();
		} else if (McpTypeEnum.COMMAND.equals(detail.getType())) {
			CommandConfigDTO c = (CommandConfigDTO) detail;
			typeInfo = Bundle.get("component.mcp.sub.connection.type.command") + c.getCommand() + " " + c.getArgString();
		} else {
			typeInfo = Bundle.get("component.mcp.sub.connection.type.unknown");
		}


		JTextArea area = new JTextArea(typeInfo, 2, 20);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(DEFAULT_FONT);
		area.setBackground(panel.getBackground());
		area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		area.setPreferredSize(new Dimension(200, 40));
		panel.add(area);

		if (CollectionUtils.isEmpty(toolList) && AvailableStatusEnum.ENABLED.equals(detail.getStatus())) {
			JLabel err = new JLabel(Bundle.get("component.mcp.sub.connection.error"));
			err.setForeground(Color.RED);
			err.setFont(BOLD_FONT);
			panel.add(Box.createVerticalStrut(5));
			panel.add(err);
		}
		return panel;
	}

	/* ---------------------------- 业务按钮逻辑（编辑 / 启用禁用 / 重连 / 删除） ---------------------------- */

	private void onEditButtonClicked() {
		EditMcpServerDialog dialog = new EditMcpServerDialog(detail, project, SwingUtilities.getWindowAncestor(this));
		dialog.setVisible(true);

		McpConfigDTO updated = dialog.getMcpConfigDTO();
		if (updated != null) {
			this.detail = updated;
			refreshToolsList();
			contentPanel.remove(commandPanel);
			commandPanel = createCommandPanel(detail, fetchToolNames());
			contentPanel.add(commandPanel);
			revalidate();
			repaint();
		}
	}

	private void toggleAvailableStatus() {
		boolean wantEnable = detail.getStatus() == AvailableStatusEnum.DISABLED;
		SwitchAvailableResp resp = McpPTSHandler.getInstance().switchAvailable(
				new SwitchAvailableReq(project.getLocationHash(), detail.getName(), !wantEnable),
				project.getService(McpConfigFileManager.class).getConfigAgentPort()
		);
		if (Boolean.TRUE.equals(wantEnable)) {
			McpPTSHandler.getInstance().launchMcp(LaunchConfigReq.convertDTO(project.getLocationHash(), detail),
					project.getService(McpConfigFileManager.class).getConfigAgentPort()
			);
		}
		AvailableStatusEnum newStatus = resp.getStatus().getAvailableStatusEnum();
		if (newStatus == detail.getStatus()) {
			JOptionPane.showMessageDialog(this,
					Bundle.get("component.mcp.sub.error.switch"),
					Bundle.get("component.mcp.sub.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		detail.setStatus(newStatus);
		project.getService(McpConfigFileManager.class).switchConfig(detail.getName(), newStatus);
		availableButton.setText(newStatus == AvailableStatusEnum.ENABLED ?
				Bundle.get("component.mcp.sub.button.enabled") :
				Bundle.get("component.mcp.sub.button.disabled"));
		availableButton.setIcon(newStatus == AvailableStatusEnum.ENABLED ? McpIcons.ENABLE : McpIcons.DISABLE);

		if (newStatus == AvailableStatusEnum.ENABLED) {
			refreshToolsList();
		} else {
			((DefaultListModel<String>) toolsList.getModel()).clear();
			serverStatusIcon.setIcon(McpIcons.GREY);
		}
		contentPanel.remove(commandPanel);
		commandPanel = createCommandPanel(detail, fetchToolNames());
		contentPanel.add(commandPanel);
		McpWebviewRefreshPTWHandler.notifyConfigChange(WebviewRefreshEnum.AVAILABLE_STATUS, project);
		revalidate();
		repaint();
	}

	private void onReconnectClicked() {
		if (AvailableStatusEnum.DISABLED.equals(detail.getStatus())) {
			JOptionPane.showMessageDialog(this,
					Bundle.get("component.mcp.sub.error.disabled"),
					Bundle.get("component.mcp.sub.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 显示loading状态
		String originalText = reconnectButton.getText();
		reconnectButton.setText(Bundle.get("component.mcp.sub.button.reconnecting"));
		reconnectButton.setEnabled(false);

		// 使用SwingWorker在后台执行重连操作
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() {
				// 执行重连操作 需要调用update接口
//				McpPTSHandler.getInstance().launchMcp(
//						LaunchConfigReq.convertDTO(project.getLocationHash(), detail),
//						project.getService(McpConfigFileManager.class).getConfigAgentPort()
//				);
				McpPTSHandler.getInstance().updateTools(
						UpdateConfigReq.convertDTO(project.getLocationHash(), detail),
						project.getService(McpConfigFileManager.class).getConfigAgentPort()
				);
				return null;
			}

			@Override
			protected void done() {
				// 在UI线程中完成操作
				refreshToolsList();
				contentPanel.remove(commandPanel);
				commandPanel = createCommandPanel(detail, fetchToolNames());
				contentPanel.add(commandPanel);

				// 恢复按钮状态
				reconnectButton.setText(originalText);
				reconnectButton.setEnabled(true);
				McpWebviewRefreshPTWHandler.notifyConfigChange(WebviewRefreshEnum.SERVER_LIST, project);
				revalidate();
				repaint();
			}
		};

		worker.execute();
	}

	private void onDeleteButtonClicked() {
		int confirm = JOptionPane.showConfirmDialog(
				this,
				Bundle.get("component.mcp.sub.delete.confirm", detail.getName()),
				Bundle.get("component.mcp.sub.delete.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
		);
		if (confirm != JOptionPane.YES_OPTION) return;

		McpPTSHandler.getInstance().deleteMcpServers(project.getLocationHash(), project.getService(McpConfigFileManager.class).getConfigAgentPort(), detail.getName());
		detail.setDelete(true);
		project.getService(McpConfigFileManager.class).deleteConfig(detail.getName());
		McpWebviewRefreshPTWHandler.notifyConfigChange(WebviewRefreshEnum.SERVER_LIST, project);
		Container parent = getParent();
		if (parent != null) {
			parent.remove(this);
			parent.revalidate();
			parent.repaint();
		}
	}

	/**
	 * 查找父容器中的JScrollPane
	 */
	private JScrollPane findParentScrollPane() {
		Container parent = this.getParent();

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

			// 使用较小的固定滚动单位，使滚动更平滑
			int scrollUnit = 16;
			int newValue = verticalBar.getValue() + wheelRotation * scrollUnit;

			// 确保新值在有效范围内
			newValue = Math.max(verticalBar.getMinimum(),
					Math.min(newValue, verticalBar.getMaximum() - verticalBar.getVisibleAmount()));

			// 直接设置滚动条的值，避免使用invokeLater
			verticalBar.setValue(newValue);
		}
	}
}
