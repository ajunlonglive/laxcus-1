/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.pool;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;
import com.laxcus.watch.*;

/**
 * WATCH站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 10/31/2017
 * @since laxcus 1.0
 */
public class WatchCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static WatchCustomTrustor selfHandle = new WatchCustomTrustor();

	/** WATCH站点显示面板 **/
	private WatchCustomDisplay display = new WatchCustomDisplay();

	/**
	 * 构造默认和私有的WATCH站点自定义资源代理
	 */
	private WatchCustomTrustor() {
		super();
	}

	/**
	 * 返回WATCH站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static WatchCustomTrustor getInstance() {
		return WatchCustomTrustor.selfHandle;
	}

	/**
	 * 返回WATCH站点启动器
	 * @return WATCH站点启动器句柄
	 */
	private WatchLauncher getWatchLauncher(){
		return WatchLauncher.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub()
	 */
	@Override
	public Node getHub() {
		return getWatchLauncher().getHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal()
	 */
	@Override
	public Node getLocal() {
		return getWatchLauncher().getListener();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getSubHubs(com.laxcus.util.Siger)
	 */
	@Override
	public Node[] getSubHubs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getCustomDisplay()
	 */
	@Override
	public CustomDisplay getCustomDisplay() {
		return display;
	}

}