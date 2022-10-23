/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

import java.io.*;

import javax.swing.*;

import com.laxcus.gui.dialog.input.*;
import com.laxcus.gui.dialog.message.*;

/**
 * 输入目录对话框
 * 
 * @author scott.liang
 * @version 1.0 9/8/2021
 * @since laxcus 1.0
 */
class InputDirectoryDialog extends InputDialog {
	
	private static final long serialVersionUID = -6941285383917542639L;
	
	/** 父目录 **/
	private File dir;
	
	/**
	 * 设置父目录
	 * @param f
	 */
	public void setParentPath(File f) {
		dir = f;
	}
	
	/**
	 * 返回父目录
	 * @return
	 */
	public File getParentPath() {
		return dir;
	}

	/**
	 * 构造默认的输入目录对话框
	 */
	public InputDirectoryDialog() {
		super();
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 */
	public InputDirectoryDialog(String title) {
		super(title);
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 * @param resizable
	 */
	public InputDirectoryDialog(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public InputDirectoryDialog(String title, boolean resizable,
			boolean closable) {
		super(title, resizable, closable);
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public InputDirectoryDialog(String title, boolean resizable,
			boolean closable, boolean maximizable) {
		super(title, resizable, closable, maximizable);
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public InputDirectoryDialog(String title, boolean resizable,
			boolean closable, boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.input.InputDialog#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String text) {
		boolean b = (text == null || text.trim().isEmpty());
		// 判断有分隔符
		if (!b) {
			int index = text.indexOf(File.separator);
			b = (index > -1);
		}
		if (b) {
			String title = UIManager.getString("ChoiceDialog.InputFolderInvalidTitle");
			String content = UIManager.getString("ChoiceDialog.InputFolderInvalidContent");
			MessageBox.showWarning(this, title, content);
			return false;
		}
		
		text = text.trim();

		File file = new File(dir, text);
		b = (file.exists() && file.isDirectory());
		if (b) {
			String title = UIManager.getString("ChoiceDialog.InputFolderExistsTitle");
			String content = UIManager.getString("ChoiceDialog.InputFolderExistsContent");
			MessageBox.showWarning(this, title, content);
			return false;
		}
		// 成功
		return true;
	}

}
