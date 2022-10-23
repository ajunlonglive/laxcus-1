/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;


import javax.swing.*;

import com.laxcus.gui.frame.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;

/**
 * RAY桌面
 * 
 * @author scott.liang
 * @version 1.0 9/18/2021
 * @since laxcus 1.0
 */
final class RayDesktop extends PlatformDesktop {

	private static final long serialVersionUID = 955532080577661608L;

	/** 应用工具条 **/
	private FrameBar frameBar;

	/** 桌面管理器 **/
	private DesktopManager manager;

//	/**
//	 * 构造默认的RAY桌面
//	 */
//	public RayDesktop() {
//		super();
//		init();
//	}

	/**
	 * 构造RAY桌面，同步指定系统监听事件
	 * @param systemListeners 监听事件 
	 */
	public RayDesktop(PlatformListener[] systemListeners, FrameBar bar) {
		super(systemListeners);
		init();
		setFrameBar(bar);
	}
	
//	/**
//	 * 构造RAY桌面，指定应用条
//	 * @param w 桌面
//	 * @param bar 应用条
//	 */
//	public RayDesktop(FrameBar bar) {
//		this();
//		setFrameBar(bar);
//	}

	/**
	 * 初始化参数
	 */
	private void init() {
		manager = new RayDesktopManager(this);
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
		RayLauncher.getInstance().playSound(who);
	}

}