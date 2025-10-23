package com.ke.setting.configuration.genral.ui;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeyMapBundle;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.impl.ActionShortcutRestrictions;
import com.intellij.openapi.keymap.impl.KeymapImpl;
import com.intellij.openapi.keymap.impl.SystemShortcuts;
import com.intellij.openapi.keymap.impl.ui.KeymapPanel;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DoubleClickListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/8/22 15:16
 * @Description
 */
public class ActionsKeyMapPanel extends JPanel {

	private final ActionsTree myActionsTree = new ActionsTree();

	public ActionsKeyMapPanel() {
		createPanel();
	}

	public void createPanel() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		new DoubleClickListener() {
			@Override
			protected boolean onDoubleClick(@NotNull MouseEvent e) {
				editSelection(e);
				return true;
			}
		}.installOn(myActionsTree.getTree());

		myActionsTree.getTree().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					editSelection(e);
				}
			}
		});

		myActionsTree.reset(new KeymapImpl());
//		TreeUtil.expandAll(myActionsTree.getTree());
		currentKeymapChanged(KeymapManager.getInstance().getActiveKeymap());

		this.add(myActionsTree.getComponent());
	}


	private void editSelection(InputEvent e) {
		String actionId = myActionsTree.getSelectedActionId();
		if (actionId == null) return;

		Keymap selectedKeymap = KeymapManager.getInstance().getActiveKeymap();
		DefaultActionGroup group = createEditActionGroup(actionId, selectedKeymap);

		ActionManager.getInstance()
				.createActionPopupMenu("GeneralConfigure", group)
				.getComponent()
				.show(e.getComponent(), ((MouseEvent) e).getX(), ((MouseEvent) e).getY());

	}


	@NotNull
	private DefaultActionGroup createEditActionGroup(@NotNull String actionId, Keymap selectedKeymap) {
		DefaultActionGroup group = new DefaultActionGroup();

		group.add(new AddKeyboardShortcutAction(actionId));

		group.addSeparator();

		Shortcut[] shortcuts = selectedKeymap.getShortcuts(actionId);
		for (Shortcut shortcut : shortcuts) {
			group.add(new RemoveShortcutAction(shortcut, actionId));
		}

		if (shortcuts.length >= 2) {
			group.add(new RemoveAllShortcuts(actionId));
		}

		return group;
	}


	private class AddKeyboardShortcutAction extends DumbAwareAction {

		private final String myActionId;

		protected AddKeyboardShortcutAction(String actionId) {
			super(IdeBundle.messagePointer("action.Anonymous.text.add.keyboard.shortcut"));
			myActionId = actionId;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {

			KeymapManager km = KeymapManager.getInstance();
			Keymap activeKeymap = km != null ? km.getActiveKeymap() : null;
			if (activeKeymap == null) {
				return;
			}

			KeymapPanel.addKeyboardShortcut(myActionId, ActionShortcutRestrictions.getInstance().getForActionId(myActionId), activeKeymap, ActionsKeyMapPanel.this);
		}
	}


	private class RemoveShortcutAction extends DumbAwareAction {
		private final Shortcut myShortcut;
		private final Keymap mySelectedKeymap;
		private @NotNull
		final String myActionId;

		private RemoveShortcutAction(Shortcut shortcut, @NotNull String actionId) {
			super(IdeBundle.message("action.text.remove.0", KeymapUtil.getShortcutText(shortcut)));
			myShortcut = shortcut;
			mySelectedKeymap = KeymapManager.getInstance().getActiveKeymap();
			myActionId = actionId;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {

			mySelectedKeymap.removeShortcut(myActionId, myShortcut);
			if (StringUtil.startsWithChar(myActionId, '$')) {
				mySelectedKeymap.removeShortcut(KeyMapBundle.message("editor.shortcut", myActionId.substring(1)), myShortcut);
			}
			currentKeymapChanged(mySelectedKeymap);
			repaintLists();
		}
	}


	private class RemoveAllShortcuts extends DumbAwareAction {
		private final Keymap mySelectedKeymap;
		private final String myActionId;

		private RemoveAllShortcuts(@NotNull String actionId) {
			super(IdeBundle.messagePointer("action.text.remove.all.shortcuts"));
			mySelectedKeymap = KeymapManager.getInstance().getActiveKeymap();
			myActionId = actionId;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent event) {
			mySelectedKeymap.removeAllActionShortcuts(myActionId);
			currentKeymapChanged(mySelectedKeymap);
		}
	}

	private void repaintLists() {
		myActionsTree.getComponent().repaint();
	}


	private void currentKeymapChanged(Keymap selectedKeymap) {
		if (selectedKeymap == null) selectedKeymap = new KeymapImpl();
		SystemShortcuts systemShortcuts = SystemShortcuts.getInstance();
		systemShortcuts.updateKeymapConflicts(selectedKeymap);
		myActionsTree.reset(selectedKeymap);
	}


}
