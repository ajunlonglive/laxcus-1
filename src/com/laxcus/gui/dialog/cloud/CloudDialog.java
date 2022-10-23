/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud;

import com.laxcus.gui.dialog.*;

/**
 * 云端对话框
 * @author scott.liang
 * @version 1.0 12/9/2021
 * @since laxcus 1.0
 */
public abstract class CloudDialog extends LightDialog {

	private static final long serialVersionUID = 8086099304525998012L;

	/**
	 * 
	 */
	public CloudDialog() {
		super();
	}

	/**
	 * @param title
	 */
	public CloudDialog(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param resizable
	 */
	public CloudDialog(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public CloudDialog(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public CloudDialog(String title, boolean resizable, boolean closable,
			boolean maximizable) {
		super(title, resizable, closable, maximizable);
	}

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public CloudDialog(String title, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}

}
