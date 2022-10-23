/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application;

import java.awt.*;

import com.laxcus.gui.frame.*;
import com.laxcus.register.*;
import com.laxcus.util.naming.*;

/**
 * 客户端应用窗口框架。<br><br>
 * 所有基于用户桌面或者管理员桌面的内部应用从这个类派生。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/2/2021
 * @since laxcus 1.0
 */
public abstract class ApplicationFrame extends LightFrame {

	private static final long serialVersionUID = 4395385177026180046L;

	/**
	 * 构造默认的内部应用窗口
	 */
	public ApplicationFrame() {
		super();
	}

	/**
	 * 构造内部应用窗口，指定标题
	 * @param title 标题
	 */
	public ApplicationFrame(String title) {
		super(title);
	}

	/**
	 * 构造内部应用窗口，指定参数
	 * @param title
	 * @param resizable
	 */
	public ApplicationFrame(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * 构造内部应用窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public ApplicationFrame(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
	}

	/**
	 * 构造内部应用窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public ApplicationFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
		super(title, resizable, closable, maximizable);
	}

	/**
	 * 构造内部应用窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public ApplicationFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}
	
	/**
	 * 读范围
	 * @param paths
	 * @return
	 */
	public Rectangle readBound(String paths) {
		Naming root = RTEnvironment.ENVIRONMENT_USER;
		return RTKit.readBound(root, paths);
	}

	/**
	 * 取出范围
	 * @param paths
	 * @param rect
	 * @return
	 */
	public boolean writeBound(String paths, Rectangle rect) {
		Naming root = RTEnvironment.ENVIRONMENT_USER;
		return RTKit.writeBound(root, paths, rect);
	}

	/**
	 * 读字体
	 * @param paths
	 * @return
	 */
	public Font readFont(String paths) {
		Naming root = RTEnvironment.ENVIRONMENT_USER;
		return RTKit.readFont(root, paths);
	}

	/**
	 * 取出字体
	 * @param paths
	 * @param rect
	 * @return
	 */
	public boolean writeFont(String paths, Font rect) {
		Naming root = RTEnvironment.ENVIRONMENT_USER;
		return RTKit.writeFont(root, paths, rect);
	}

}


//public boolean isSystem() {
//	ClassLoader c = this.getClass().getClassLoader();
//	if(c.getClass() != ApplicationClassLoader.class) {
//		return false;
//	}
//	
//	ApplicationClassLoader cl = (ApplicationClassLoader)c;
//	cl.ge
//}
