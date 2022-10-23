/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.tray;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.util.skin.*;

/**
 * 托盘
 * @author scott.liang
 * @version 1.0 2/25/2022
 * @since laxcus 1.0
 */
public class Tray {

	/** 标准托盘图片 **/
	private Icon icon;
	
	/** 鼠标移入时的图标 **/
	private Icon rolloverIcon;

	/** 鼠标按下时的图标 **/
	private Icon pressedIcon;

	/** 工具提示 **/
	private String tooltip;

	/** 监听器 **/
	transient MouseListener mouseListener;
	transient MouseMotionListener mouseMotionListener;
	transient ActionListener actionListener;

	/**
	 * 构造默认的托盘
	 */
	public Tray() {
		super();
	}

	/**
	 * 构造托盘，指定图标
	 * @param icon
	 */
	public Tray(Icon icon) {
		super();
		setIcon(icon);
	}

	/**
	 * 构造托盘，指定工具提示
	 * @param icon
	 * @param tooltip
	 */
	public Tray(Icon icon, String tooltip) {
		this(icon);
		setTooltip(tooltip);
	}

	/**
	 * 
	 * @param <T>
	 * @param listenerType
	 * @return
	 */
	<T extends EventListener> T[] getListeners(Class<T> listenerType) {
		EventListener l = null;
		if (listenerType == MouseListener.class) {
			l = mouseListener;
		} else if (listenerType == MouseMotionListener.class) {
			l = mouseMotionListener;
		} else if (listenerType == ActionListener.class) {
			l = actionListener;
		}
		return AWTEventMulticaster.getListeners(l, listenerType);
	}

	/**
	 * 添加监听器
	 * @param listener
	 */
	public synchronized void addActionListener(ActionListener listener) {
		if (listener == null) {
			return;
		}
		actionListener = AWTEventMulticaster.add(actionListener, listener);
	}

	/**
	 * 删除监听器
	 * @param listener
	 */
	public synchronized void removeActionListener(ActionListener listener) {
		if (listener == null) {
			return;
		}
		actionListener = AWTEventMulticaster.remove(actionListener, listener);
	}

	/**
	 * 返回匹配的监听器
	 * @return
	 */
	public synchronized ActionListener[] getActionListeners() {
		return (ActionListener[]) (getListeners(ActionListener.class));
	}

	public synchronized void addMouseListener(MouseListener listener) {
		if (listener == null) {
			return;
		}
		mouseListener = AWTEventMulticaster.add(mouseListener, listener);
	}

	public synchronized void removeMouseListener(MouseListener listener) {
		if (listener == null) {
			return;
		}
		mouseListener = AWTEventMulticaster.remove(mouseListener, listener);
	}

	public synchronized MouseListener[] getMouseListeners() {
		return (MouseListener[]) (getListeners(MouseListener.class));
	}

	public synchronized void addMouseMotionListener(MouseMotionListener listener) {
		if (listener == null) {
			return;
		}
		mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener,
				listener);
	}

	public synchronized void removeMouseMotionListener(
			MouseMotionListener listener) {
		if (listener == null) {
			return;
		}
		mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener,
				listener);
	}

	public synchronized MouseMotionListener[] getMouseMotionListeners() {
		return (MouseMotionListener[]) (getListeners(MouseMotionListener.class));
	}

	/**
	 * 设置图标
	 * @param e
	 */
	public void setIcon(Icon e) {
		if (e == null) {
			throw new NullPointerException("setting null Icon");
		}
		icon = e;
	}

	/**
	 * 返回图标
	 * @return
	 */
	public Icon getIcon() {
		return icon;
	}
	
	/**
	 * 设置鼠标移入时的图标
	 * @param e 图标
	 */
	public void setRolloverIcon(Icon e) {
		rolloverIcon = e;
	}

	/**
	 * 返回鼠标移入时的图标
	 * @return 图标
	 */
	public Icon getRolloverIcon() {
		return rolloverIcon;
	}

	/**
	 * 设置鼠标按下时的图标
	 * @param e 图标
	 */
	public void setPressedIcon(Icon e) {
		pressedIcon = e;
	}

	/**
	 * 返回鼠标按下时的图标
	 * @return 图标
	 */
	public Icon getPressedIcon() {
		return pressedIcon;
	}
	
	/**
	 * 生成其它的图标
	 */
	public void doDefaultIcon() {
		// 指定的图标
		if (icon.getClass() == ImageIcon.class) {
			ImageIcon img = (ImageIcon) icon;
			ImageIcon dark = ImageUtil.dark(img, -30);
			ImageIcon light = ImageUtil.brighter(img, 20);
			setRolloverIcon(light);
			setPressedIcon(dark);
		}
	}

	/**
	 * 设置工具提示
	 * @param s
	 */
	public void setTooltip(String s) {
		tooltip = s;
	}

	/**
	 * 返回工具提示
	 * @return
	 */
	public String getTooltip() {
		return tooltip;
	}

}