/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.gui.dialog.input.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2022-1-24
 * @since laxcus 1.0
 */
public class NameInputDialog extends InputDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public NameInputDialog() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 */
	public NameInputDialog(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @param resizable
	 */
	public NameInputDialog(String title, boolean resizable) {
		super(title, resizable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public NameInputDialog(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public NameInputDialog(String title, boolean resizable, boolean closable,
			boolean maximizable) {
		super(title, resizable, closable, maximizable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public NameInputDialog(String title, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.laxcus.gui.dialog.input.InputDialog#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String text) {
		return (text != null && text.trim().length() > 0);
	}

}
