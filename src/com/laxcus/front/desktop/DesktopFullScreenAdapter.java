/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.awt.*;
import java.beans.*;

import com.laxcus.gui.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;

/**
 * 桌面应用全屏适配器
 * 
 * @author scott.liang
 * @version 1.0 3/21/2022
 * @since laxcus 1.0
 */
class DesktopFullScreenAdapter {
	
	/** 窗口句柄 **/
	private DesktopWindow window;
	
	/** 锁定的窗口 **/
	private LightForm master;
	
	/** 状态栏可视 **/
	private boolean statusVisible;
	
	/** DOCK栏可视 **/
	private boolean dockVisible;
	
	/** 记录全屏前窗口注册级别 **/
	private int layer;

	/**
	 * 构造桌面应用全屏适配器
	 * @param e 桌面窗口句柄
	 */
	public DesktopFullScreenAdapter(DesktopWindow e) {
		super();
		window = e;
	}
	
	/**
	 * 判断已经全屏幕
	 * @return
	 */
	public boolean hasFull() {
		return master != null;
	}

	/**
	 * 全屏
	 * @param form
	 * @return 成功返回真，否则假
	 */
	public boolean full(LightForm form) {
		// 如果已经存在，不能执行
		if (master != null) {
			return false;
		}

		statusVisible = window.status.isVisible();
		dockVisible = window.dock.isVisible();

		// 修改桌面的最大值
		PlatformDesktop desktop = window.desktop;
		layer = PlatformDesktop.getLayer(form);
		// 找到当前窗口的下标位置
		int index = desktop.getIndexOf(form);
		if (index == -1) {
			return false;
		}

		// 隐藏
		if (dockVisible) {
			window.dock.setVisible(false);
		}
		if (statusVisible) {
			window.status.closeTrayWindow();
			window.status.setVisible(false);
		}
		
		// 更新桌面
		Dimension d = desktop.getSize();
		desktop.invalidate();
		desktop.repaint(0, 0, d.width, d.height);

		// 删除
		desktop.remove(index);
		// 重新加入，调整最前面
		desktop.add(form, new Integer(FormLayer.FULL_SCREEN));

		// 调整为最大化
		try {
			form.setMaximum(true);
		} catch (PropertyVetoException e) {
			Logger.error(e);
		}
		// 隐藏标题栏
		form.hideTitlePane();
		// 取消边框
		form.setShowBorder(false);

		// 记录这个句柄
		master = form;
		return true;
	}

	/**
	 * 取消全屏
	 * @param form
	 * @return 成功返回真，否则假
	 */
	public boolean cancel(LightForm form) {
		// 如果不存在，或者句柄不一致时，不执行
		if (master == null || master != form) {
			return false;
		}

		// 修改桌面的最大值
		PlatformDesktop desktop = window.desktop;
		// 找到当前窗口的下标位置
		int index = desktop.getIndexOf(form);
		if (index == -1) {
			return false;
		}

		// 删除
		desktop.remove(index);
		// 重新加入，恢复为原来状态
		desktop.add(form, new Integer(layer));

		// 调整
		form.showTitlePane();
		form.setShowBorder(true);
		// 最大化假，恢复原来的窗口
		try {
			form.setMaximum(false);
		} catch (PropertyVetoException e) {
			Logger.error(e);
		}

		// 隐藏
		if (statusVisible) {
			window.status.setVisible(true);
		}
		if (dockVisible) {
			window.dock.setVisible(true);
		}
		// 选中它
		try {
			form.toFront();
			form.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
		
		master = null;
		return true;
	}
}
