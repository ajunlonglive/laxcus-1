/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import javax.swing.*;
import javax.swing.plaf.*;

import com.laxcus.gui.frame.*;
import com.laxcus.util.*;

/**
 * FRONT桌面管理器
 * 最小/还原图标
 * 
 * @author scott.liang
 * @version 1.0 9/23/2021
 * @since laxcus 1.0
 */
public class FrontDesktopManager extends DefaultDesktopManager implements UIResource {

	private static final long serialVersionUID = -7546784251682504006L;

	/** FRONT桌面 **/
	private FrontDesktop desktop;

	/**
	 * 构造FRONT桌面管理器
	 * @param desktop FRONT桌面
	 */
	public FrontDesktopManager(FrontDesktop desktop) {
		super();
		setFrontDesktop(desktop);
	}

	/**
	 * 设置FRONT桌面
	 * @param d
	 */
	public void setFrontDesktop(FrontDesktop d) {
		desktop = d;
	}

	/**
	 * 返回FRONT桌面
	 * @return
	 */
	public FrontDesktop getFrontDesktop() {
		return desktop;
	}

	//	   public void iconifyFrame2(JInternalFrame f) {
	////	        JInternalFrame.JDesktopIcon desktopIcon;
	//	        Container c = f.getParent();
	//		JDesktopPane d = f.getDesktopPane();
	//		boolean findNext = f.isSelected();
	////	        desktopIcon = f.getDesktopIcon();
	////	        if(!wasIcon(f)) {
	////	            Rectangle r = getBoundsForIconOf(f);
	////	            desktopIcon.setBounds(r.x, r.y, r.width, r.height);
	////	            setWasIcon(f, Boolean.TRUE);
	////	        }
	//
	//		if (c == null || d == null) {
	//	            return;
	//	        }
	//
	////		if (c instanceof JLayeredPane) {
	////		    JLayeredPane lp = (JLayeredPane)c;
	////		    int layer = lp.getLayer(f);
	////		    lp.putLayer(desktopIcon, layer);
	////		}
	//
	//	        // If we are maximized we already have the normal bounds recorded
	//	        // don't try to re-record them, otherwise we incorrectly set the
	//	        // normal bounds to maximized state.
	//	        if (!f.isMaximum()) {
	//	            f.setNormalBounds(f.getBounds());
	//	        }
	////	        d.setComponentOrderCheckingEnabled(false); // 用引用处理
	////	        c.remove(f);
	////	        c.add(desktopIcon);
	////	        d.setComponentOrderCheckingEnabled(true);
	//	        
	//	        c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
	//	        if (findNext) {
	//	            if (d.selectFrame(true) == null) {
	//	                // The icon is the last frame.
	//	                f.restoreSubcomponentFocus();
	//	            }
	//	        }
	//	    }

	//	private void throwable() {
	//		throw new NullPointerException("null pointer iconify!");
	//	}

	//	public void iconifyFrameW(JInternalFrame f) {
	//		// super.iconifyFrame(f);
	//
	//		System.out.println("图标化...");
	//		// f.setVisible(false);
	//		// setWasIcon(f, Boolean.TRUE);
	//
	//		try {
	//			throwable();
	//		} catch (Throwable e) {
	//			e.printStackTrace();
	//		}
	//	}

	//	//	public void deiconifyFrame(JInternalFrame f) {
	//	////		super.deiconifyFrame(f);
	//	//		
	//	////		System.out.println("恢复原来状态...");
	//	////		f.setVisible(true);
	//	//		
	//	//	}
	//
	//	private void doIconifyFrame(LightFrame f) {
	//		System.out.println("窗口最小化！");
	//		// JInternalFrame.JDesktopIcon desktopIcon;
	//		Container c = f.getParent();
	//		FrontDesktop d = (FrontDesktop) f.getDesktopPane();
	//		boolean findNext = f.isSelected();
	//
	//		//		// desktopIcon = f.getDesktopIcon();
	//		//		if (!wasIcon(f)) {
	//		//			// Rectangle r = getBoundsForIconOf(f);
	//		//			// desktopIcon.setBounds(r.x, r.y, r.width, r.height);
	//		//
	//		//			setWasIcon(f, Boolean.TRUE);
	//		//		}
	//
	//		if (c == null || d == null) {
	//			return;
	//		}
	//
	//		//		System.out.printf("C %s | D %s\n", c.getClass().getName(), d.getClass().getName());
	//
	//		// if (c instanceof JLayeredPane) {
	//		// JLayeredPane lp = (JLayeredPane)c;
	//		// int layer = lp.getLayer(f);
	//		// lp.putLayer(desktopIcon, layer);
	//		// }
	//
	//		// If we are maximized we already have the normal bounds recorded
	//		// don't try to re-record them, otherwise we incorrectly set the
	//		// normal bounds to maximized state.
	//		if (!f.isMaximum()) {
	//			f.setNormalBounds(f.getBounds());
	//		}
	////		d.setComponentOrderCheckingEnabledEx(false);
	//		c.remove(f);
	//		// c.add(desktopIcon);
	////		d.setComponentOrderCheckingEnabledEx(true);
	//		c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
	//		
	////		if (findNext) {
	////			if (d.selectFrame(true) == null) {
	////				// The icon is the last frame.
	////				f.restoreSubcomponentFocus();
	////			}
	////		}
	//
	//		//		System.out.printf("detach is %s\n", (f.getParent()!=null?"attach":"detach"));
	//	}

	//	private void doDeiconifyFrameX(LightFrame f) {
	//		System.out.println("窗口恢复状态！");
	//		
	////		f.doShow();
	//	}

	//	private void doDeiconifyFrame(LightFrame f) {
	//		System.out.println("窗口恢复状态！");
	//
	//		// JInternalFrame.JDesktopIcon desktopIcon = f.getDesktopIcon();
	//		Container c = f.getParent();
	//		JDesktopPane d = f.getDesktopPane();
	//
	//		if (c == null) {
	//			c = desktop;
	//		}
	//		if (d == null) {
	//			d = desktop;
	//		}
	//
	//		//		System.out.printf("C %s | D %s\n", (c == null ? "Null" : c.getClass()
	//		//				.getName()), (d == null ? "Null" : d.getClass().getName()));
	//
	//
	//		//		if (wasIcon(f)) {
	//		//			// Rectangle r = getBoundsForIconOf(f);
	//		//			// desktopIcon.setBounds(r.x, r.y, r.width, r.height);
	//		//
	//		//			setWasIcon(f, Boolean.FALSE);
	//		//		}
	//
	//		//		// if (c != null && d != null) {
	//		//		 c.add(f);
	//
	//		//		System.out.println("显示窗口...");
	//
	//		// 1. 可视
	////		f.setVisible(true);
	//		// 2. 增加到桌面面板上
	//		c.add(f, JLayeredPane.DEFAULT_LAYER);
	//		
	//		d.validate(); 
	//		
	////		c.add(f);
	//
	//		// If the frame is to be restored to a maximized state make
	//		// sure it still fills the whole desktop.
	//		if (f.isMaximum()) {
	//			Rectangle desktopBounds = c.getBounds();
	//			if (f.getWidth() != desktopBounds.width
	//					|| f.getHeight() != desktopBounds.height) {
	//				setBoundsForFrame(f, 0, 0, desktopBounds.width,
	//						desktopBounds.height);
	//			}
	//		}
	//
	//		// removeIconFor(f);
	//
	//		//		if (f.isSelected()) {
	//		//			f.moveToFront();
	//		//			f.restoreSubcomponentFocus();
	//		//		} else {
	//		//			try {
	//		//				f.setSelected(true);
	//		//			} catch (PropertyVetoException e2) {
	//		//			}
	//		//		}
	//
	//		// 选择窗口
	//		f.moveToFront();
	//		f.restoreSubcomponentFocus();
	//		try {
	//			f.setSelected(true);
	//		} catch (PropertyVetoException e2) {
	//		}
	//	}

	//	private void toHidden(LightFrame frame) {
	//		boolean findNext = frame.isSelected();
	//		
	////		frame.moveToBack();
	//		
	////		frame.toBack();
	//
	//		frame.setVisible(false);
	//		frame.setHidden(true);
	//
	//		if (findNext) {
	////			// 找到下一个窗口
	////			JInternalFrame next = desktop.selectFrame(true);
	////			if (next == null) {
	//////				frame.restoreSubcomponentFocus();
	////			}
	//		}
	//	}
	//	
	//	private void toShow(LightFrame frame) {
	////		frame.setVisible(true);
	//		frame.show();
	//		frame.setHidden(false);
	//		
	//		// 移到前面
	////		frame.moveToFront();
	//		frame.toFront();
	//		
	//		try {
	//			frame.setSelected(true);
	//		} catch (PropertyVetoException ex) {
	//		}
	//
	////		if (frame.isSelected()) {
	////			frame.moveToFront();
	////			frame.restoreSubcomponentFocus();
	////		} else {
	////			try {
	////				frame.setSelected(true);
	////			} catch (PropertyVetoException e2) {
	////			}
	////		}
	//	}

	/**
	 * 转换...
	 * @param f
	 */
	private void reserve(JInternalFrame f) {
		if (!Laxkit.isClassFrom(f, LightFrame.class)) {
			String s = String.format("%s != %s", (f != null ? f.getClass()
					.getName() : "null"), LightFrame.class.getName());
			throw new IllegalInstanceException(s);
		}

		LightFrame frame = (LightFrame) f;
		// 如果在隐藏状态，恢复窗口
		if (frame.isHidden()) {
			frame.setIcon(false);
		} else {
			frame.setIcon(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.DefaultDesktopManager#iconifyFrame(javax.swing.JInternalFrame)
	 */
	@Override
	public void iconifyFrame(JInternalFrame frame) {
		reserve(frame);
	}

	/**
	 * Removes the desktopIcon from its parent and adds its frame
	 * to the parent.
	 * @param frame the <code>JInternalFrame</code> to be de-iconified
	 */
	public void deiconifyFrame(JInternalFrame frame) {
		// try {
		// throwable();
		// } catch (Throwable e) {
		// e.printStackTrace();
		// }
		reserve(frame);
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.DefaultDesktopManager#iconifyFrame(javax.swing.JInternalFrame)
	//	 */
	//	@Override
	//	public void iconifyFrame(JInternalFrame f) {
	//		System.out.println("最小化窗口！...");
	//		try {
	//			throwable();
	//		} catch (Throwable e) {
	//			e.printStackTrace();
	//		}
	//		super.iconifyFrame(f);
	//	}
	//
	//	/**
	//	 * Removes the desktopIcon from its parent and adds its frame
	//	 * to the parent.
	//	 * @param f the <code>JInternalFrame</code> to be de-iconified
	//	 */
	//	public void deiconifyFrame(JInternalFrame f) {
	//		System.out.println("还原窗口！...");
	//		try {
	//			throwable();
	//		} catch (Throwable e) {
	//			e.printStackTrace();
	//		}
	//		super.deiconifyFrame(f);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.DefaultDesktopManager#iconifyFrame(javax.swing.JInternalFrame)
	//	 */
	//	@Override
	//	public void iconifyFrame(JInternalFrame f) {
	//		reserve(f);
	//		
	//		System.out.println("图标化，窗口移到任务栏");
	//		
	//		// JInternalFrame.JDesktopIcon desktopIcon;
	//		Container c = f.getParent();
	//		FrontDesktop d = (FrontDesktop) f.getDesktopPane();
	//		boolean findNext = f.isSelected();
	//		// desktopIcon = f.getDesktopIcon();
	//		if (!wasIcon(f)) {
	//			// Rectangle r = getBoundsForIconOf(f);
	//			// desktopIcon.setBounds(r.x, r.y, r.width, r.height);
	//
	//			setWasIcon(f, Boolean.TRUE);
	//		}
	//
	//		if (c == null || d == null) {
	//			return;
	//		}
	//		
	//		System.out.printf("C %s | D %s\n", c.getClass().getName(), d.getClass().getName());
	//
	//		// if (c instanceof JLayeredPane) {
	//		// JLayeredPane lp = (JLayeredPane)c;
	//		// int layer = lp.getLayer(f);
	//		// lp.putLayer(desktopIcon, layer);
	//		// }
	//
	//		// If we are maximized we already have the normal bounds recorded
	//		// don't try to re-record them, otherwise we incorrectly set the
	//		// normal bounds to maximized state.
	//		if (!f.isMaximum()) {
	//			f.setNormalBounds(f.getBounds());
	//		}
	//		d.setComponentOrderCheckingEnabledEx(false);
	//		c.remove(f);
	//		// c.add(desktopIcon);
	//		d.setComponentOrderCheckingEnabledEx(true);
	//		c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
	//		if (findNext) {
	//			if (d.selectFrame(true) == null) {
	//				// The icon is the last frame.
	//				f.restoreSubcomponentFocus();
	//			}
	//		}
	//		
	//		System.out.printf("detach is %s\n", (f.getParent()!=null?"attach":"detach"));
	//	}
	//	
	//	/**
	//     * Removes the desktopIcon from its parent and adds its frame
	//     * to the parent.
	//     * @param f the <code>JInternalFrame</code> to be de-iconified
	//     */
	//	public void deiconifyFrame(JInternalFrame f) {
	//		System.out.println("恢复窗口，窗口在桌面显示!");
	//
	//		// JInternalFrame.JDesktopIcon desktopIcon = f.getDesktopIcon();
	//		Container c = f.getParent();
	//		JDesktopPane d = f.getDesktopPane();
	//
	//		if (c == null) {
	//			c = desktop;
	//		}
	//		if (d == null) {
	//			d = desktop;
	//		}
	//
	////		System.out.printf("C %s | D %s\n", (c == null ? "Null" : c.getClass()
	////				.getName()), (d == null ? "Null" : d.getClass().getName()));
	//
	//
	////		if (wasIcon(f)) {
	////			// Rectangle r = getBoundsForIconOf(f);
	////			// desktopIcon.setBounds(r.x, r.y, r.width, r.height);
	////
	////			setWasIcon(f, Boolean.FALSE);
	////		}
	//
	////		// if (c != null && d != null) {
	////		 c.add(f);
	//
	//		System.out.println("显示窗口...");
	//
	//		// 1. 可视
	//		f.setVisible(true);
	//		// 2. 增加到桌面面板上
	//		d.add(f, JLayeredPane.DEFAULT_LAYER);
	//
	//		// If the frame is to be restored to a maximized state make
	//		// sure it still fills the whole desktop.
	//		if (f.isMaximum()) {
	//			Rectangle desktopBounds = c.getBounds();
	//			if (f.getWidth() != desktopBounds.width
	//					|| f.getHeight() != desktopBounds.height) {
	//				setBoundsForFrame(f, 0, 0, desktopBounds.width,
	//						desktopBounds.height);
	//			}
	//		}
	//
	//		// removeIconFor(f);
	//
	//		if (f.isSelected()) {
	//			f.moveToFront();
	//			f.restoreSubcomponentFocus();
	//		} else {
	//			try {
	//				f.setSelected(true);
	//			} catch (PropertyVetoException e2) {
	//			}
	//		}
	//
	//		// }
	//
	//	}

}