/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import javax.swing.*;

import com.laxcus.gui.frame.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;

/**
 * FRONT桌面
 * 
 * @author scott.liang
 * @version 1.0 9/18/2021
 * @since laxcus 1.0
 */
final class FrontDesktop extends PlatformDesktop {

	private static final long serialVersionUID = 955532080577661608L;

	/** 应用工具条 **/
	private FrameBar frameBar;

	/** 桌面管理器 **/
	private DesktopManager manager;

	/**
	 * 构造FRONT桌面，指定应用条
	 * @param systemListeners 系统监听器
	 * @param bar 应用条
	 */
	public FrontDesktop(PlatformListener[] systemListeners, FrameBar bar) {
		super(systemListeners);
		init();
		setFrameBar(bar);
	}

	/**
	 * 初始化参数
	 */
	private void init() {
		manager = new FrontDesktopManager(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JDesktopPane#getDesktopManager()
	 */
	@Override
	public DesktopManager getDesktopManager(){
		return manager;
	}


	/**
	 * 设置应用条
	 * @param e
	 */
	public void setFrameBar(FrameBar e) {
		frameBar = e;
	}

	/**
	 * 返回应用条
	 * @return
	 */
	public FrameBar getFrameBar() {
		return frameBar;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.platform.PlatformPane#register(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void register(LightFrame frame) {
		frameBar.register(frame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.platform.PlatformPane#unregister(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void unregister(LightFrame frame) {
		frameBar.unregister(frame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.platform.FrameBar#activate(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void activate(LightFrame frame) {
		frameBar.activate(frame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.platform.FrameBar#deactivate(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void deactivate(LightFrame frame) {
		frameBar.deactivate(frame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.PlatformDesktop#playSound(int)
	 */
	@Override
	public void playSound(int who) {
		DesktopLauncher.getInstance().playSound(who);
	}

}