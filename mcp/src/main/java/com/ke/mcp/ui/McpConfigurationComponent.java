package com.ke.mcp.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.ke.Bundle;
import com.ke.mcp.communication.sidecar.McpPTSHandler;
import com.ke.mcp.dto.McpConfigDTO;
import com.ke.mcp.dto.req.LaunchConfigReq;
import com.ke.mcp.dto.req.ToolListReq;
import com.ke.mcp.dto.resp.ToolListResp;
import com.ke.mcp.enums.AvailableStatusEnum;
import com.ke.mcp.enums.ToolListResponseEnum;
import com.ke.mcp.enums.WebviewRefreshEnum;
import com.ke.mcp.listener.McpConfigurationUpdateListener;
import com.ke.mcp.manager.McpConfigFileManager;
import com.ke.mcp.webview.handler.McpWebviewRefreshPTWHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * MCP 设置页主界面 —— 统一字体/字号、美观对齐
 */
public class McpConfigurationComponent {
	private final static Logger LOG = Logger.getInstance(McpConfigurationComponent.class);
	private boolean isInitialized = false;

	private static final String NO_SERVER_TEXT = Bundle.get("component.mcp.no.server");
	private static final String DEFAULT_TEXT = Bundle.get("component.mcp.default.text");
	private static final String TOOL_EXCEED = Bundle.get("component.mcp.tool.exceed");

	private final Project project;

	private JPanel mainPanel;
	private JButton addServerButton;
	private JPanel subPanelContainer;
	private JTextArea hintArea;

	private static final Font DEFAULT_FONT = UIManager.getFont("Label.font");
	private static final Font BOLD_FONT = DEFAULT_FONT.deriveFont(Font.BOLD, 16.0f);
	private static final Color BORDER_COLOR = new Color(0xDDDDDD);

	public McpConfigurationComponent(Project project) {
		this.project = project;
		initBasicUI();  // 只初始化基本UI结构
	}

	private void initBasicUI() {
		mainPanel = new JPanel(new BorderLayout());

		// 获取系统默认字体
		Font defaultFont = UIManager.getFont("Label.font");
		Font boldFont = defaultFont.deriveFont(Font.BOLD);

		// 顶部区域
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setPreferredSize(new Dimension(100, 50));

		JLabel mcpLabel = new JLabel(Bundle.get("component.mcp.title"));
		mcpLabel.setFont(boldFont.deriveFont((float) EditorColorsManager.getInstance().getGlobalScheme().getEditorFontSize()));
		mcpLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		topPanel.add(mcpLabel, BorderLayout.WEST);

		addServerButton = new JButton(Bundle.get("component.mcp.button.add"));
		addServerButton.setFont(defaultFont);
		addServerButton.setForeground(Color.WHITE);
		addServerButton.setBackground(new Color(0x4B6EAF)); // 添加背景色
		addServerButton.setOpaque(true);
		addServerButton.setBorderPainted(false);

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		rightPanel.setOpaque(false);
		rightPanel.add(addServerButton);
		topPanel.add(rightPanel, BorderLayout.EAST);

		mainPanel.add(topPanel, BorderLayout.NORTH);

		// 初始化监听器
		initListeners();

		// 订阅MCP配置变更消息
		project.getMessageBus().connect().subscribe(
				McpConfigurationUpdateListener.TOPIC, (McpConfigurationUpdateListener) this::refreshSubPanels
		);
	}

	private void initListeners() {
		addServerButton.addActionListener(e -> {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            AddMcpServerDialog dialog = new AddMcpServerDialog(window);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                McpConfigDTO newMcp = dialog.getNewMcp();
                project.getService(McpConfigFileManager.class).addOrUpdateConfig(newMcp, true, true);
                McpPTSHandler.getInstance().launchMcp(
                        LaunchConfigReq.convertDTO(project.getLocationHash(), newMcp),
                        project.getService(McpConfigFileManager.class).getConfigAgentPort()
                );

                // 添加新的服务卡片，使用name作为描述
                addServiceCard(newMcp.getName(), newMcp.getName(), true);
                // 通知配置更新
                project.getMessageBus().syncPublisher(McpConfigurationUpdateListener.TOPIC).onConfigurationChanged();

                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
	}

	private void addServiceCard(String name, String description, boolean status) {
		McpServiceCard card = new McpServiceCard(name, description, status);
		card.setOnConfigureAction(() -> handleConfigureService(name));
	}

	private void handleConfigureService(String serviceName) {
		McpConfigDTO config = project.getService(McpConfigFileManager.class)
				.getMcpConfigDTOMap()
				.values()
				.stream()
				.filter(dto -> serviceName.equals(dto.getName()))
				.findFirst()
				.orElse(null);

		if (config != null) {
			Window window = WindowManager.getInstance().suggestParentWindow(project);
			EditMcpServerDialog dialog = new EditMcpServerDialog(config, project, window);
			dialog.setVisible(true);

			if (dialog.isConfirmed()) {
				project.getMessageBus().syncPublisher(McpConfigurationUpdateListener.TOPIC).onConfigurationChanged();
			}
		}
	}

	// 服务卡片内部类
	private static class McpServiceCard extends JPanel {
		@Getter
		private final String name;
		@Setter
		private Runnable onConfigureAction;
		private Runnable onAddAction;

		public McpServiceCard(String name, String description, boolean status) {
			this.name = name;

			setLayout(new BorderLayout(10, 5));
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
					BorderFactory.createEmptyBorder(12, 15, 12, 15)
			));

			JPanel infoPanel = new JPanel(new GridLayout(2, 1, 4, 6));
			infoPanel.setOpaque(false);

			JLabel nameLabel = new JLabel(name);
			nameLabel.setFont(BOLD_FONT);
			JLabel descLabel = new JLabel(description);
			descLabel.setFont(DEFAULT_FONT);

			infoPanel.add(nameLabel);
			infoPanel.add(descLabel);

			JButton actionButton = new JButton(Boolean.TRUE.equals(status) ?
					Bundle.get("component.mcp.button.configure") :
					Bundle.get("component.mcp.button.add.service"));
			actionButton.setFont(DEFAULT_FONT);
			// 使用与addServerButton相同的样式
			actionButton.setForeground(Color.WHITE);
			actionButton.setBackground(new Color(0x4B6EAF));
			actionButton.setOpaque(true);
			actionButton.setBorderPainted(false);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.setOpaque(false);
			buttonPanel.add(actionButton);

			actionButton.addActionListener(e -> {
				if (Boolean.TRUE.equals(status)) {
					if (onConfigureAction != null) onConfigureAction.run();
				} else {
					if (onAddAction != null) onAddAction.run();
				}
			});

			add(infoPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.EAST);
		}

	}

	private List<ToolListResp> getToolList() {
		List<McpConfigDTO> mcpList = new ArrayList<>(project.getService(McpConfigFileManager.class)
				.getMcpConfigDTOMap().values());
		List<String> mcpNameList = mcpList.stream()
				.filter(dto -> AvailableStatusEnum.ENABLED.equals(dto.getStatus()) && Boolean.FALSE.equals(dto.isDelete()))
				.map(McpConfigDTO::getName)
				.collect(Collectors.toList());
		return McpPTSHandler.getInstance().getToolList(new ToolListReq(project.getLocationHash(), mcpNameList),
				project.getService(McpConfigFileManager.class).getConfigAgentPort());
	}

	private void updateHintText(List<ToolListResp> toolList) {
		AtomicReference<Integer> toolCount = new AtomicReference<>(0);
		if (CollectionUtils.isNotEmpty(toolList)) {
			toolList.forEach(resp -> {
				if (ToolListResponseEnum.ACTIVE.equals(resp.getStatus())) {
					toolCount.updateAndGet(v -> v + resp.getTools().size());
				}
			});
		}

		if (CollectionUtils.isEmpty(toolList)) {
			hintArea.setText(NO_SERVER_TEXT);
		} else if (toolCount.get() > 40) {
			hintArea.setText(String.format(TOOL_EXCEED, toolCount.get()));
		} else {
			hintArea.setText("");
		}
	}

	private void lazyInitialize() {
		if (isInitialized) {
			return;
		}

		JPanel centerPanel = new JPanel(new BorderLayout());
		Font defaultFont = UIManager.getFont("Label.font");
		Font boldFont = defaultFont.deriveFont(Font.BOLD);

		JEditorPane description = new JEditorPane("text/html", DEFAULT_TEXT);
		description.setFont(defaultFont);
		description.setEditable(false);
		description.setOpaque(false);
		description.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

		// 添加超链接事件监听器
		description.addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					Desktop.getDesktop().browse(new URI(e.getURL().toString()));
				} catch (Exception ex) {
					LOG.warn(ex.getMessage());
				}
			}
		});

		hintArea = new JTextArea();
		hintArea.setFont(boldFont);
		hintArea.setEditable(false);
		hintArea.setOpaque(false);
		hintArea.setLineWrap(true);
		hintArea.setForeground(Color.RED);

		// 使用提取的方法获取工具列表和更新提示文本
		List<ToolListResp> toolList = getToolList();
		updateHintText(toolList);

		Box textBox = Box.createVerticalBox();
		textBox.add(description);
		textBox.add(Box.createVerticalStrut(6));
		textBox.add(hintArea);
		centerPanel.add(textBox, BorderLayout.NORTH);

		subPanelContainer = new JPanel();
		subPanelContainer.setLayout(new BoxLayout(subPanelContainer, BoxLayout.Y_AXIS));
		List<McpConfigDTO> mcpList = new ArrayList<>(project.getService(McpConfigFileManager.class)
				.getMcpConfigDTOMap().values());
		if (CollectionUtils.isNotEmpty(mcpList)) {
			for (McpConfigDTO dto : mcpList) {
				if (Boolean.FALSE.equals(dto.isDelete())) {
					subPanelContainer.add(Box.createVerticalStrut(8));
					subPanelContainer.add(new McpSubPanelComponent(dto, project));
				}
			}
		}

		centerPanel.add(subPanelContainer, BorderLayout.CENTER);

		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();

		isInitialized = true;
	}

	public void refreshSubPanels() {
		if (subPanelContainer != null) {
			subPanelContainer.removeAll();
			List<McpConfigDTO> mcpList = new ArrayList<>(project.getService(McpConfigFileManager.class)
					.getMcpConfigDTOMap().values());

			// 使用提取的方法获取工具列表和更新提示文本
			List<ToolListResp> toolList = getToolList();
			updateHintText(toolList);

			if (CollectionUtils.isNotEmpty(mcpList)) {
				for (McpConfigDTO dto : mcpList) {
					if (Boolean.FALSE.equals(dto.isDelete())) {
						subPanelContainer.add(Box.createVerticalStrut(8));
						subPanelContainer.add(new McpSubPanelComponent(dto, project));
					}
				}
			}

			subPanelContainer.revalidate();
			subPanelContainer.repaint();
			McpWebviewRefreshPTWHandler.notifyConfigChange(WebviewRefreshEnum.SERVER_LIST, project);
		}
	}

	public JPanel getPanel() {
		lazyInitialize();  // 在获取面板时才进行初始化
		return mainPanel;
	}
}
