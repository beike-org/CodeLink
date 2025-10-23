package com.ke.setting.configuration.genral.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.FormBuilder;
import com.ke.Bundle;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.utils.ApplicationUtil;
import com.ke.utils.ComponentUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.SideCarIDEInfo;
import com.ke.webview.WebViewManager;
import com.ke.webview.util.PTWUtil;

import javax.swing.*;
import java.util.Objects;


public class GeneralConfigurationComponent {

	private final static Logger LOGGER = Logger.getInstance(GeneralConfigurationComponent.class);
	private final JPanel mainPanel;

	private JToggleButton openLineMarkerToggleButton;
	private JToggleButton openStackAnalysisToggleButton;
	private JToggleButton editorSelectedPopupToggleButton;
	private JToggleButton openCodeBlockTrunkToggleButton;
	private JToggleButton webviewOSRToggleButton;
	private JToggleButton autoRunToggleButton;


	public GeneralConfigurationComponent() {

		FormBuilder formBuilder = FormBuilder.createFormBuilder()
				.addComponent(new TitledSeparator(Bundle.get("general.editor.title")))
				.addVerticalGap(4)
				.addComponent(createLineMarkerPanel())
				.addVerticalGap(4)
				.addComponent(createStackAnalysisPanel())
				.addVerticalGap(4)
				.addComponent(createEditorSelectedPopupPanel())
				.addVerticalGap(8)
				.addComponent(new TitledSeparator(Bundle.get("general.agent.title")))
				.addVerticalGap(4)
				.addComponent(createAutoRunPanel())
				.addVerticalGap(8)
				.addComponent(new TitledSeparator(Bundle.get("general.chat.title")))
				.addVerticalGap(4)
				.addComponent(createWebviewOSRPanel())
				.addVerticalGap(4)
				.addComponent(createCodeBlockTrunkPanel())
				.addVerticalGap(8)
				.addComponent(ComponentUtil.createIconTitledSeparator(Bundle.get("general.shortcut.title"),
						Bundle.get("general.shortcut.tip"), 115))
				.addVerticalGap(4)
				.addComponent(createShortCutKeysPanel());

		mainPanel = formBuilder.getPanel();
	}


	public JPanel getPanel() {
		return mainPanel;
	}


	public Boolean getOpenLineMarker() {
		return Objects.nonNull(openLineMarkerToggleButton) && openLineMarkerToggleButton.isSelected();
	}


	public Boolean getOpenCodeBlockTrunk() {
		return Objects.nonNull(openCodeBlockTrunkToggleButton) && openCodeBlockTrunkToggleButton.isSelected();
	}

	public Boolean getOpenStackAnalysis() {
		return Objects.nonNull(openStackAnalysisToggleButton) && openStackAnalysisToggleButton.isSelected();
	}

	public Boolean getEditorSelectedPopup() {
		return Objects.nonNull(editorSelectedPopupToggleButton) && editorSelectedPopupToggleButton.isSelected();
	}

	public Boolean getOpenWebviewOSR() {
		return Objects.nonNull(webviewOSRToggleButton) && webviewOSRToggleButton.isSelected();
	}




	public void setOpenLineMarker(Boolean openLineMarker) {
		if (Objects.nonNull(openLineMarkerToggleButton)) {
			openLineMarkerToggleButton.setSelected(openLineMarker);
		}
	}



	public void setOpenCodeBlockTrunkToggleButton(Boolean openCodeBlockTrunkToggle) {
		if (Objects.nonNull(openCodeBlockTrunkToggleButton)) {
			openCodeBlockTrunkToggleButton.setSelected(openCodeBlockTrunkToggle);
		}
	}

	public void setOpenStackAnalysis(Boolean openStackAnalysis) {
		if (Objects.nonNull(openStackAnalysisToggleButton)) {
			openStackAnalysisToggleButton.setSelected(openStackAnalysis);
		}
	}

	public void setEditorSelectedPopup(Boolean editorSelectedPopup) {
		if (Objects.nonNull(editorSelectedPopupToggleButton)) {
			editorSelectedPopupToggleButton.setSelected(editorSelectedPopup);
		}
	}


	public void setOpenWebviewOSR(Boolean webviewOSR) {
		if (Objects.nonNull(webviewOSRToggleButton)) {
			webviewOSRToggleButton.setSelected(webviewOSR);
		}
	}


	public Boolean getAutoRun() {
		return Objects.nonNull(autoRunToggleButton) && autoRunToggleButton.isSelected();
	}

	public void setAutoRun(Boolean autoRun) {
		if (Objects.nonNull(autoRunToggleButton)) {
			autoRunToggleButton.setSelected(autoRun);
		}
	}




	private JPanel createLineMarkerPanel() {
		openLineMarkerToggleButton = new JToggleButton();
		return ComponentUtil.createTogglePanel(Bundle.get("general.linemarker.title"),
				Bundle.get("general.linemarker.desc"), openLineMarkerToggleButton,
				Objects.requireNonNull(UserConfigState.getInstance().getState()).getLineMarker());
	}


	private JPanel createCodeBlockTrunkPanel() {
		openCodeBlockTrunkToggleButton = new JToggleButton();
		return ComponentUtil.createTogglePanel(Bundle.get("general.codeblock.title"),
				Bundle.get("general.codeblock.desc"), openCodeBlockTrunkToggleButton,
				Objects.requireNonNull(UserConfigState.getInstance().getPluginConfig()).getIsCodeBlockTruncated());
	}

	private JPanel createStackAnalysisPanel() {
		openStackAnalysisToggleButton = new JToggleButton();
		return ComponentUtil.createTogglePanel(Bundle.get("general.stackanalysis.title"),
				Bundle.get("general.stackanalysis.desc"), openStackAnalysisToggleButton,
				Objects.requireNonNull(UserConfigState.getInstance().getState()).getStackAnalysis());
	}

	private JPanel createEditorSelectedPopupPanel() {
		editorSelectedPopupToggleButton = new JToggleButton();
		return ComponentUtil.createTogglePanel(Bundle.get("general.editorpopup.title"),
				Bundle.get("general.editorpopup.desc"), editorSelectedPopupToggleButton,
				Objects.requireNonNull(UserConfigState.getInstance().getState()).getEditorSelectedPopup());
	}

	private JPanel createWebviewOSRPanel() {
		webviewOSRToggleButton = new JToggleButton();
		return ComponentUtil.createTogglePanel(Bundle.get("general.webviewosr.title"),
				Bundle.get("general.webviewosr.desc"), webviewOSRToggleButton,
				Objects.requireNonNull(UserConfigState.getInstance().getState()).getWebviewOSR());
	}


	private JPanel createShortCutKeysPanel() {

		try {
			return new ActionsKeyMapPanel();
		} catch (Exception e) {
			LOGGER.warn("render shortcut error:", e);
			JPanel jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
			jPanel.add(ComponentUtil.createIconLinkLabel("快捷键配置", null, 160, () -> ShowSettingsUtil.getInstance().showSettingsDialog(ApplicationUtil.findCurrentProject(), "Keymap")));
			return jPanel;
		}
	}

	private JPanel createAutoRunPanel() {
		autoRunToggleButton = new JToggleButton();
		autoRunToggleButton.addItemListener(e -> {
			UserConfigState.getInstance().getState().setAutoRun(autoRunToggleButton.isSelected());
			Project project = ApplicationUtil.findCurrentProject();
			if (Objects.nonNull(project)) {
				WebViewManager webViewManager = project.getService(WebViewManager.class);
				SideCarIDEInfo sideCarIDEInfo = webViewManager.getWebViewProjectConfig().getSideCarIDEInfo();
				sideCarIDEInfo.setAutoRun(autoRunToggleButton.isSelected());
				PTWUtil.sendMessage(BaseCommandEnums.SYNC_PROJECT_CONFIG, webViewManager.getWebViewProjectConfig(), project);
			}
		});
		return ComponentUtil.createTogglePanel(Bundle.get("general.autorun.title"), Bundle.get("general.autorun.desc"),
				autoRunToggleButton, Objects.requireNonNull(UserConfigState.getInstance().getState()).getAutoRun());
	}



}
