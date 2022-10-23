/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

/**
 * 平面窗口UI
 * 
 * @author scott.liang
 * @version 1.0 9/30/2021
 * @since laxcus 1.0
 */
public class FlatInternalFrameUI extends MetalInternalFrameUI {

	/**
	 * 构造平面窗口UI
	 * @param frame 窗口句柄
	 */
	public FlatInternalFrameUI(JInternalFrame frame) {
		super(frame);
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatInternalFrameUI((JInternalFrame) c);
	}

	/*
	 * 返回标题面板
	 * 这个方法被上级的“installComponents”方法调用，“installComponents”又被“installUI”调用。
	 * @see javax.swing.plaf.metal.MetalInternalFrameUI#createNorthPane(javax.swing.JInternalFrame)
	 */
	@Override
	protected JComponent createNorthPane(JInternalFrame frame) {
		return new FlatInternalFrameTitlePane(frame);
	}
	
	@Override
	protected MouseInputAdapter createBorderListener(JInternalFrame w) {
		return new FlatBorderListener();
	}
	
	/**
	 * 这个类是作用是屏蔽“mouseClicked”方法，但是保留其它方法。
	 * “mouseClicked”方法中的操作，放在FlatInternalFrameTitlePane中处理，
	 * 处理内容：
	 * 1. 判断是图标位置，显示弹出菜单
	 * 2. 其它空白区域，最大最小化处理
	 */
	protected class FlatBorderListener extends BorderListener implements SwingConstants {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// 空操作，不要执行任何处理
		}

		//		public void mouseClicked(MouseEvent e) {
		//			if (e.getClickCount() > 1 && e.getSource() == getNorthPane()) {
		//				if (frame.isIconifiable() && frame.isIcon()) {
		//					try {
		//						frame.setIcon(false);
		//					} catch (PropertyVetoException e2) {
		//					}
		//				} else if (frame.isMaximizable()) {
		//					if (!frame.isMaximum())
		//						try {
		//							frame.setMaximum(true);
		//						} catch (PropertyVetoException e2) {
		//						}
		//					else
		//						try {
		//							frame.setMaximum(false);
		//						} catch (PropertyVetoException e3) {
		//						}
		//				}
		//
		//				System.out.println("单击标题面板!!!");
		//			}
		//		}

	}

}