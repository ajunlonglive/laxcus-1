/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog;

import com.laxcus.gui.dialog.*;

/**
 * 桌面轻量级对话窗口
 * 
 * @author scott.liang
 * @version 1.0 6/15/2021
 * @since laxcus 1.0
 */
public abstract class RayLightDialog extends LightDialog {

	private static final long serialVersionUID = -4970895114498457776L;

	/**
	 * 构造默认的桌面轻量级对话窗口
	 */
	public RayLightDialog() {
		super();
	}

	/**
	 * 构造桌面轻量级对话窗口，指定标题
	 * @param title 标题
	 */
	public RayLightDialog(String title) {
		super(title);
	}

	/**
	 * 构造桌面轻量级对话窗口，指定参数
	 * @param title
	 * @param resizable
	 */
	public RayLightDialog(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * 构造桌面轻量级对话窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public RayLightDialog(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
	}

	/**
	 * 构造桌面轻量级对话窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public RayLightDialog(String title, boolean resizable, boolean closable, boolean maximizable) {
		super(title, resizable, closable, maximizable);
	}

	/**
	 * 构造桌面轻量级对话窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public RayLightDialog(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}

//	/**
//	 * 解析标签属性
//	 * @param xmlPath
//	 * @return
//	 */
//	protected String findCaption(String xmlPath) {
//		return DesktopLauncher.getInstance().findCaption(xmlPath);
//	}
//	
//	/**
//	 * 解析标签中的内容
//	 * @param xmlPath
//	 * @return
//	 */
//	protected String findContent(String xmlPath) {
//		return DesktopLauncher.getInstance().findCaption(xmlPath);
//	}

}
