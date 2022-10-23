/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

import javax.swing.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.gui.dialog.input.*;
import com.laxcus.gui.dialog.message.*;

/**
 * 输入云目录对话框
 * 
 * @author scott.liang
 * @version 1.0 12/13/2021
 * @since laxcus 1.0
 */
class InputCloudDirectoryDialog extends InputDialog {
	
	private static final long serialVersionUID = -6941285383917542639L;
	
	/** 父目录 **/
	private VPath dir;
	
	/**
	 * 设置父目录
	 * @param f
	 */
	public void setParentPath(VPath f) {
		dir = f;
	}
	
	/**
	 * 返回父目录
	 * @return
	 */
	public VPath getParentPath() {
		return dir;
	}

	/**
	 * 构造默认的输入目录对话框
	 */
	public InputCloudDirectoryDialog() {
		super();
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 */
	public InputCloudDirectoryDialog(String title) {
		super(title);
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 * @param resizable
	 */
	public InputCloudDirectoryDialog(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * 构造输入目录对话框
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public InputCloudDirectoryDialog(String title, boolean resizable,
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
	public InputCloudDirectoryDialog(String title, boolean resizable,
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
	public InputCloudDirectoryDialog(String title, boolean resizable,
			boolean closable, boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}
	
	/**
	 * 判断是字符串
	 * @param w
	 * @return
	 */
	private boolean isAlpha(char w) {
		return ((w >= 'A' && w < 'Z') || (w >= 'a' && w <= 'z') || w == '_');
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.input.InputDialog#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String text) {
		boolean b = (text == null || text.trim().isEmpty());
		if (b) {
			return false;
		}
		
		text = text.trim();
		
		// 判断是有效的字符串
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char w = text.charAt(i);
			b = isAlpha(w);
			if (!b) {
				String title = UIManager.getString("CloudChoiceDialog.InputFolderInvalidTitle");
				String content = UIManager.getString("CloudChoiceDialog.InputFolderInvalidContent");
				MessageBox.showWarning(this, title, content);
				return false;
			}
		}
		
		// 判断这个目录存在
		if (dir != null) {
			for (VPath sub : dir.list()) {
				String s = sub.getName();
				// 忽略大小写，判断一致
				if (text.equalsIgnoreCase(s)) {
					String title = UIManager.getString("CloudChoiceDialog.InputFolderExistsTitle");
					String content = UIManager.getString("CloudChoiceDialog.InputFolderExistsContent");
					MessageBox.showWarning(this, title, content);
					return false;
				}
			}
		}

		// 成功
		return true;
	}

}
