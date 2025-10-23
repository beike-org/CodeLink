package com.ke.notepad.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.ke.Bundle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Vector;

public class NotepadPanel extends JPanel {
	private final static Logger LOG = Logger.getInstance(NotepadPanel.class);
	private final Project project;
	private final DefaultTableModel tableModel;
	private final Path notepadDir;

	public NotepadPanel(Project project) {
		this.project = project;
		this.setLayout(new BorderLayout());
		// 创建记事本目录
		notepadDir = Path.of(Objects.requireNonNull(project.getBasePath()), ".idea", "notepad");
		try {
			Files.createDirectories(notepadDir);
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}

		// 创建表格模型
		Vector<String> columnNames = new Vector<>();
		columnNames.add(Bundle.get("notepad.table.column.filename"));
		columnNames.add(Bundle.get("notepad.table.column.operation"));
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// 文件名列不可编辑
				return column == 1;
			}
		};

		// 创建表格
		JBTable table = new JBTable(tableModel);
		table.getColumnModel().getColumn(1).setCellRenderer(new ButtonsRenderer());
		table.getColumnModel().getColumn(1).setCellEditor(new ButtonsEditor());

		// 添加表格优化设置
		table.setRowHeight(30); // 设置适当的行高
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumnModel().getColumn(0).setPreferredWidth(250);
		table.getColumnModel().getColumn(1).setMinWidth(200);
		table.getColumnModel().getColumn(1).setMaxWidth(250);
		table.setShowGrid(false); // 隐藏网格线
		table.setIntercellSpacing(new Dimension(0, 0)); // 设置单元格间距

		// 创建新建按钮
		JButton newNoteButton = new JButton(Bundle.get("notepad.button.new"));
		newNoteButton.addActionListener(e -> createNewNote());

		// 添加组件
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(newNoteButton);

		this.add(topPanel, BorderLayout.NORTH);
		this.add(new JBScrollPane(table), BorderLayout.CENTER);

		// 加载现有文件
		loadExistingFiles();
	}

	private void loadExistingFiles() {
		tableModel.setRowCount(0);
		try (var paths = Files.list(notepadDir)) {
			paths.filter(path -> path.toString().endsWith(".md"))
					.forEach(path -> {
						Vector<Object> row = new Vector<>();
						row.add(path.getFileName().toString());
						row.add("");  // 这列用于按钮
						tableModel.addRow(row);
					});
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}
	}

	private void createNewNote() {
		String fileName = JOptionPane.showInputDialog(this, Bundle.get("notepad.dialog.new.title"));
		if (fileName != null && !fileName.trim().isEmpty()) {
			if (!fileName.endsWith(".md")) {
				fileName += ".md";
			}
			try {
				Path filePath = notepadDir.resolve(fileName);
				Files.createFile(filePath);
				loadExistingFiles();
				openFileInEditor(filePath.toFile());
			} catch (IOException e) {
				LOG.warn(e.getMessage());
				JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.create.failed"));
			}
		}
	}

	private void openFileInEditor(File file) {
		VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
		if (virtualFile != null) {
			FileEditorManager.getInstance(project).openFile(virtualFile, true);
		}
	}


	private void renameNote(String oldFileName) {
		String newFileName = JOptionPane.showInputDialog(this, Bundle.get("notepad.dialog.rename.title"), oldFileName);
		if (newFileName != null && !newFileName.trim().isEmpty() && !newFileName.equals(oldFileName)) {
			// 确保新文件名有.md后缀
			if (!newFileName.endsWith(".md")) {
				newFileName += ".md";
			}

			try {
				Path oldPath = notepadDir.resolve(oldFileName);
				Path newPath = notepadDir.resolve(newFileName);

				// 检查新文件名是否已存在
				if (Files.exists(newPath)) {
					JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.rename.exists", newFileName));
					return;
				}

				// 关闭文件编辑器（如果打开）
				VirtualFile oldVirtualFile = LocalFileSystem.getInstance().findFileByNioFile(oldPath);
				if (oldVirtualFile != null) {
					FileEditorManager.getInstance(project).closeFile(oldVirtualFile);
				}

				// 重命名文件
				Files.move(oldPath, newPath);

				// 刷新本地文件系统
				LocalFileSystem.getInstance().refreshAndFindFileByIoFile(newPath.toFile());

				// 打开重命名后的文件
				openFileInEditor(newPath.toFile());

				// 更新UI
				loadExistingFiles();

				JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.rename.success"));
			} catch (IOException e) {
				LOG.warn(e.getMessage());
				JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.rename.failed", e.getMessage()));
			}
		}
	}

	private void deleteNote(String fileName) {
		int confirm = JOptionPane.showConfirmDialog(this,
				Bundle.get("notepad.dialog.delete.message", fileName),
				Bundle.get("notepad.dialog.delete.title"),
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				Path filePath = notepadDir.resolve(fileName);
				if (Files.exists(filePath)) {
					// 关闭文件编辑器（如果打开）
					VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByNioFile(filePath);
					if (virtualFile != null) {
						FileEditorManager.getInstance(project).closeFile(virtualFile);
					}

					// 删除文件
					Files.delete(filePath);

					// 刷新本地文件系统
					LocalFileSystem.getInstance().refreshAndFindFileByIoFile(filePath.toFile());

					// 更新UI
					loadExistingFiles();
					JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.delete.success"));
				} else {
					JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.delete.notfound", fileName));
				}
			} catch (IOException e) {
				LOG.warn(e.getMessage());
				JOptionPane.showMessageDialog(this, Bundle.get("notepad.message.delete.failed", e.getMessage()));
			}
		}
	}

	// 按钮渲染器
	private static class ButtonsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

		public ButtonsRenderer() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			JButton editButton = new JButton(Bundle.get("notepad.button.edit"));
			JButton renameButton = new JButton(Bundle.get("notepad.button.rename"));
			JButton deleteButton = new JButton(Bundle.get("notepad.button.delete"));

			// 设置按钮样式
			editButton.setFocusPainted(false);
			renameButton.setFocusPainted(false);
			deleteButton.setFocusPainted(false);

			// 添加弹性空间
			add(Box.createHorizontalGlue());
			add(editButton);
			add(Box.createHorizontalStrut(5));
			add(renameButton);
			add(Box.createHorizontalStrut(5));
			add(deleteButton);
			add(Box.createHorizontalGlue());

			// 设置按钮首选大小
			Dimension buttonSize = new Dimension(60, 25);
			editButton.setPreferredSize(buttonSize);
			renameButton.setPreferredSize(buttonSize);
			deleteButton.setPreferredSize(buttonSize);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}
	}

	// 按钮编辑器
	private class ButtonsEditor extends DefaultCellEditor {
		private final JPanel panel;
		private String fileName;

		public ButtonsEditor() {
			super(new JTextField());
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

			JButton editButton = new JButton(Bundle.get("notepad.button.edit"));
			JButton renameButton = new JButton(Bundle.get("notepad.button.rename"));
			JButton deleteButton = new JButton(Bundle.get("notepad.button.delete"));

			// 设置按钮首选大小
			Dimension buttonSize = new Dimension(60, 25);
			editButton.setPreferredSize(buttonSize);
			renameButton.setPreferredSize(buttonSize);
			deleteButton.setPreferredSize(buttonSize);

			// 添加鼠标监听器
			editButton.addActionListener(e -> {
				fireEditingStopped();
				openFileInEditor(new File(notepadDir.toFile(), fileName));
			});

			renameButton.addActionListener(e -> {
				fireEditingStopped();
				renameNote(fileName);
			});

			deleteButton.addActionListener(e -> {
				fireEditingStopped();
				deleteNote(fileName);
			});

			// 添加弹性空间和按钮
			panel.add(Box.createHorizontalGlue());
			panel.add(editButton);
			panel.add(Box.createHorizontalStrut(5));
			panel.add(renameButton);
			panel.add(Box.createHorizontalStrut(5));
			panel.add(deleteButton);
			panel.add(Box.createHorizontalGlue());

			// 设置单击编辑
			setClickCountToStart(1);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
													 boolean isSelected, int row, int column) {
			fileName = (String) table.getValueAt(row, 0);
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			return "";
		}
	}
}