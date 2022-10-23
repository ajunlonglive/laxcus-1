/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application;



import javax.swing.JPanel;
import javax.swing.border.*;

import com.laxcus.util.event.SwingDispatcher;
import com.laxcus.util.event.SwingEvent;

/**
 * 应用面板
 * 提供线程转发处理
 * 
 * @author scott.liang
 * @version 1.0 1/12/2022
 * @since laxcus 1.0
 */
public class ApplicationPanel extends JPanel {

	private static final long serialVersionUID = 892717720039426062L;

	/**
	 * 
	 */
	public ApplicationPanel() {
		super();
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 设置根面板的边框范围
	 * @param panel 面板
	 */
	protected void setRootBorder(JPanel panel) {
		panel.setBorder(new EmptyBorder(4,4,4,4));
	}

}
