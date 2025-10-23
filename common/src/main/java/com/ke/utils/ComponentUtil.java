package com.ke.utils;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.labels.LinkLabel;
import com.ke.Bundle;
import com.ke.setting.configuration.genral.user.UserConfigState;
import com.ke.toolwindow.content.BasePopupH5Panel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ComponentUtil {

    private final static Logger LOGGER = Logger.getInstance(ComponentUtil.class);

    public static ListPopup createListPopup(String title, ActionGroup actionGroup, DataContext dataContext) {
        return JBPopupFactory.getInstance().createActionGroupPopup(title, actionGroup, dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
    }

    public static JBPopup createWebviewPopup(JComponent component, JComponent focusComponent, Project project, String locationKey) {
        return getBaseWebviewPopupBuilder(component, focusComponent, project, locationKey).createPopup();
    }


    public static JBPopup createWebviewPopup(JComponent component, JComponent focusComponent, Project project, String locationKey, List<? extends Pair<ActionListener, KeyStroke>> keyboardActions) {
        ComponentPopupBuilder baseComponentPopupBuilder = getBaseWebviewPopupBuilder(component, focusComponent, project, locationKey);
        baseComponentPopupBuilder.setKeyboardActions(keyboardActions);
        return baseComponentPopupBuilder.createPopup();
    }

    public static ComponentPopupBuilder getBaseComponentPopupBuilder(JComponent component, JComponent focusComponent) {
        return JBPopupFactory.getInstance().createComponentPopupBuilder(component, focusComponent)
                //设置损失焦点后就自动关闭
                .setCancelOnClickOutside(true)
                //设置不默认取得焦点
                .setRequestFocus(false)
                //设置能获取焦点
                .setFocusable(true)
                //设置能拖动大小
                .setResizable(true)
                //设置可移动
                .setMovable(true);
    }


    private static ComponentPopupBuilder getBaseWebviewPopupBuilder(JComponent component, JComponent focusComponent, Project project, String locationKey) {
        ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(component, focusComponent)
                //设置不损失焦点后就自动关闭
                .setCancelOnClickOutside(false)
                //设置能获取焦点
                .setRequestFocus(true)
                //设置能拖动大小
                .setResizable(true)
                //设置关闭按钮
                .setCancelButton(new IconButton(Bundle.get("component.close"), AllIcons.Actions.Close, AllIcons.Actions.CloseDarkGrey))
                //设置title
                .setTitle(Bundle.get("plugin.title"))
                //设置可移动
                .setMovable(true)
                //设置展示border
                .setShowBorder(true);
        if (StringUtils.isNotBlank(locationKey)) {
            //继承上次出现位置和大小
            componentPopupBuilder.setDimensionServiceKey(project, locationKey, true);
        }
        return componentPopupBuilder;

    }


    public static void setCursorStatus(int status) {
        Cursor cursor = new Cursor(status);
        try {
            SwingUtilities.invokeLater(() -> Objects.requireNonNull(WindowManager.getInstance().getIdeFrame(ApplicationUtil.findCurrentProject())).getComponent().setCursor(cursor));
        } catch (Exception ignore) {

        }
    }

    public static ActionButton createActionButton(AnAction action) {
        return new ActionButton(action, action.getTemplatePresentation(), action.getTemplateText(), ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
    }


    public static void addShiftEnterInputMap(JTextArea textArea, Runnable onEnter) {
        var input = textArea.getInputMap();
        var enterStroke = KeyStroke.getKeyStroke("ENTER");
        var shiftEnterStroke = KeyStroke.getKeyStroke("shift ENTER");
        input.put(shiftEnterStroke, "insert-break");
        input.put(enterStroke, "text-submit");

        var actions = textArea.getActionMap();
        actions.put("text-submit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onEnter.run();
            }
        });
    }

    public static ToolWindow getCodeLinkToolWindow(Project project) {
        return getToolWindow(project, Bundle.get("toolWindow.CodeLink.id"));
    }

    public static ToolWindow getProjectToolWindow(Project project) {
        return getToolWindow(project, "Project");
    }

    public static ToolWindow getToolWindow(Project project, String name) {
        return ToolWindowManager.getInstance(project).getToolWindow(name);
    }


    /**
     * 通用TogglePanel
     *
     * @param label                label文本
     * @param toolTip              tooltip
     * @param toggleButton         开关,如果不需要加listener,直接new一个JToggleButton就行
     * @param toggleButtonSelected 开关是否默认打开
     */
    public static JPanel createTogglePanel(String label, String toolTip, JToggleButton toggleButton, Boolean toggleButtonSelected) {
        JPanel jPanel = new JPanel();

        toggleButton.setSelectedIcon(IconUtil.getToggleOpenIcon());
        toggleButton.setIcon(IconUtil.getToggleCloseIcon());
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorder(null);

        JPanel linePanel = new JPanel();
        JLabel lineLabel = createIconLabel(label, toolTip, 160);
        linePanel.add(lineLabel);
        linePanel.add(toggleButton);
        linePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        if (toggleButtonSelected) {
            toggleButton.setSelected(true);
        }

        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.add(linePanel);

        return jPanel;
    }

    /**
     * 创建一个带提示的TitledSeparator
     *
     * @param title
     * @param tooltip
     * @return
     */
    public static TitledSeparator createIconTitledSeparator(String title, String tooltip, int width) {
        TitledSeparator titledSeparator = new TitledSeparator("");
        titledSeparator.remove(0);
        titledSeparator.add(createIconLabel(title, tooltip, width), 0);
        return titledSeparator;
    }


    /**
     * 创建一个带提示的iconLabel
     *
     * @param title
     * @param tooltip
     * @param width
     * @return
     */
    public static JLabel createIconLabel(String title, String tooltip, int width) {
        return createIconLinkLabel(title, tooltip, width, null);
    }


    /**
     * 创建一个带提示,可点击的iconLabel
     *
     * @param title
     * @param tooltip
     * @param width
     * @param runnable
     * @return
     */
    public static JLabel createIconLinkLabel(String title, String tooltip, int width, Runnable runnable) {
        JLabel lineLabel;
        if (Objects.nonNull(runnable)) {
            lineLabel = new LinkLabel<>(title, null);
            ((LinkLabel<String>) lineLabel).setListener((LinkLabel<String> aSource, String aLinkData) -> runnable.run(), null);
        } else {
            lineLabel = new JLabel(title);
        }
        lineLabel.setPreferredSize(new Dimension(width, lineLabel.getPreferredSize().height));
        lineLabel.setMinimumSize(new Dimension(width, lineLabel.getPreferredSize().height));
        lineLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        if (StringUtils.isNotEmpty(tooltip)) {
            lineLabel.setIcon(IconUtil.getDarkGuideIcon());
            lineLabel.setToolTipText(tooltip);
        }
        return lineLabel;
    }

    /**
     * 创建一个单选框
     */
    public static JPanel createRadioPanel(List<JBRadioButton> buttonList) {

        JPanel envPanel = new JPanel();
        // 创建一个按钮组,来保证单选
        ButtonGroup group = new ButtonGroup();

        envPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonList.forEach(radioButton -> {
            group.add(radioButton);
            envPanel.add(radioButton);
        });

        return envPanel;
    }


    /**
     * 通过popup的方式展示webview
     */
    public static void showWebView(Project project, BasePopupH5Panel h5Panel, String locationKey, Dimension defaultSize) {

        h5Panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JBPopup componentPopup = ComponentUtil.createWebviewPopup(h5Panel, h5Panel.getJbCefBrowser().getComponent(), project, locationKey, Collections.singletonList(Pair.create(e -> h5Panel.openDevTools(), KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK))));
        h5Panel.setPopup(componentPopup);
        h5Panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                WindowStateService.getInstance(project).putSize(locationKey, componentPopup.getSize());
            }
        });

        //windows中popup在不同显示器上移动时，会显示异常，所以需要重置一下大小，刷新一下。property事件为graphicsContextScaleTransform
        if (SystemInfo.isWindows) {

            ((JPanel) componentPopup.getContent().getComponent(1)).getComponent(0).addMouseListener(new MouseAdapter() {

                GraphicsDevice graphicsDevice;

                int offset = -10;

                @Override
                public void mousePressed(MouseEvent e) {
                    graphicsDevice = componentPopup.getContent().getGraphicsConfiguration().getDevice();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!componentPopup.getContent().getGraphicsConfiguration().getDevice().equals(graphicsDevice)) {
                        graphicsDevice = componentPopup.getContent().getGraphicsConfiguration().getDevice();
                        SwingUtilities.invokeLater(() -> componentPopup.setSize(new Dimension(componentPopup.getSize().width + offset, defaultSize.height)));
                        offset = Math.negateExact(offset);
                    }
                }
            });
        }

        Dimension lastOpenSize = WindowStateService.getInstance(project).getSize(locationKey);
        if (SystemInfo.isWindows && !WindowUtil.isDefaultGraphicsDevice(project)) {
            componentPopup.setSize(Objects.isNull(lastOpenSize) ? new Dimension(defaultSize.width - 10, defaultSize.height) : new Dimension(lastOpenSize.width - 10, lastOpenSize.height));
        } else {
            componentPopup.setSize(Objects.isNull(lastOpenSize) ? defaultSize : lastOpenSize);
        }

        //关闭时调用
        Disposer.register(componentPopup, h5Panel);

        componentPopup.showCenteredInCurrentWindow(project);


    }






    /**
     * 打开文件夹选择器
     */
    public static String openFolderChooseDialog(@NotNull Project project) {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        fileChooserDescriptor.setTitle("选择文件夹");

        VirtualFile selectedDir = null;
        try {
            String storeDir = UserConfigState.getInstance().getState().getStoreDir();
            selectedDir = FileUtil.findVirtualFile(storeDir);
        } catch (Exception ignore) {

        }
        VirtualFile virtualFile = FileChooser.chooseFile(fileChooserDescriptor, project, selectedDir);

        if (Objects.nonNull(virtualFile)) {
            UserConfigState.getInstance().getState().setStoreDir(virtualFile.getPath());
            return virtualFile.getPath();
        }
        return null;
    }


    /**
     * 打开文件选择器
     */
    public static List<String> openFilesChooseDialog(@NotNull Project project,@NotNull String title) {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, true);
        fileChooserDescriptor.setTitle(title);

        VirtualFile selectedDir = null;
        try {
            String storeDir = UserConfigState.getInstance().getState().getStoreDir();
            selectedDir = FileUtil.findVirtualFile(storeDir);
        } catch (Exception ignore) {

        }

        VirtualFile[] virtualFiles = FileChooser.chooseFiles(fileChooserDescriptor, project, selectedDir);

        if (virtualFiles.length > 0) {
            UserConfigState.getInstance().getState().setStoreDir(virtualFiles[0].getParent().getPath());
            return Arrays.stream(virtualFiles).map(VirtualFile::getPath).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * 创建一个主要的蓝色按钮
     *
     * @param text           按钮文本
     * @param actionListener 点击事件监听器
     * @return JButton实例
     */
    public static JButton createPrimaryButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text); // 使用 JBButton 支持 JetBrains 风格
        button.putClientProperty("JButton.buttonType", "primary"); // 标记为 primary 类型
        button.setFocusPainted(false);

        if (actionListener != null) {
            button.addActionListener(actionListener);
        }

        // 添加鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));  // 设置鼠标指针为手型
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));  // 恢复默认指针
            }
        });

        return button;
    }
}
