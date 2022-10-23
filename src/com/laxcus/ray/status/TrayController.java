/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.status;

import java.awt.*;

import com.laxcus.gui.tray.*;
import com.laxcus.platform.*;

/**
 * 托盘控制器
 * 显示/关闭托盘窗口
 * 
 * @author scott.liang
 * @version 1.0 2/20/2022
 * @since laxcus 1.0
 */
class TrayController {
	
	private RayStatusBar statusBar;

	/** 托盘窗口 **/
	private TrayWindow window;
	
	/**
	 * 构造托盘控制器
	 */
	public TrayController(RayStatusBar e) {
		super();
		statusBar = e;
	}

	/**
	 * 判断是显示状态
	 * @return
	 */
	public boolean isShowing() {
		return window != null && window.isVisible();
	}

	/**
	 * 判断已经隐藏
	 * @return
	 */
	public boolean isHided() {
		return window == null;
	}

	/**
	 * 隐藏
	 */
	public void hide() {
		if (window != null) {
			window.setVisible(false);
			window = null;
		}
	}

	/**
	 * 弹出窗口
	 * @param event
	 * @param invoker
	 */
	public boolean show(Component invoker) {
		if (isShowing()) {
			hide();
		}

		// 拿到管理器
		RayTrayManager manager = (RayTrayManager) PlatformKit.getTrayManager();
		// 输出
		Tray[] trays = manager.toArray();
		int size = (trays != null ? trays.length : 0);
		if (size == 0) {
			statusBar.setTrayStatus(true);
			return false;
		}

		// 生成新的实例
		window = new TrayWindow();
		window.show(invoker, trays);
		return true;
	}

	/**
	 * 重新绘制
	 */
	public void redraw() {
		if (isShowing()) {
			Component invoker = window.getInvoker();
			show(invoker);
		}
	}

}