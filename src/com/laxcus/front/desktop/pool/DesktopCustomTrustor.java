/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.pool;

import java.util.*;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.desktop.*;
import com.laxcus.front.pool.*;
import com.laxcus.site.*;

/**
 * FRONT交互模式站点的自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 10/31/2017
 * @since laxcus 1.0
 */
public class DesktopCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static DesktopCustomTrustor selfHandle = new DesktopCustomTrustor();

	/** 显示器 **/
	private DesktopCustomDisplay display = new DesktopCustomDisplay();

	/** FRONT交互站点启动器 **/
	private DesktopLauncher launcher;

	/**
	 * 构造默认和私有的FRONT交互模式站点的自定义资源代理
	 */
	private DesktopCustomTrustor() {
		super();
	}

	/**
	 * 返回FRONT交互模式站点的自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static DesktopCustomTrustor getInstance() {
		return DesktopCustomTrustor.selfHandle;
	}

	/**
	 * 设置FRONT交互站点启动器
	 * @param e FRONT交互站点启动器句柄。
	 */
	public void setDesktopLauncher(DesktopLauncher e) {
		launcher = e;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub()
	 */
	@Override
	public Node getHub() {
		return launcher.getHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal()
	 */
	@Override
	public Node getLocal() {
		return launcher.getListener();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getSubHubs()
	 */
	@Override
	public Node[] getSubHubs() {
		List<Node> hubs = CallOnFrontPool.getInstance().getHubs();
		if (hubs == null || hubs.isEmpty()) {
			return null;
		}
		Node[] a = new Node[hubs.size()];
		return hubs.toArray(a);
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getSpaces(com.laxcus.util.Siger)
	//	 */
	//	@Override
	//	public Space[] getSpaces(Siger issuer) {
	//		List<Space> spaces = launcher.getStaffPool().getSpaces();
	//		if (spaces == null || spaces.isEmpty()) {
	//			return null;
	//		}
	//		Space[] a = new Space[spaces.size()];
	//		return spaces.toArray(a);
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#hasTable(com.laxcus.access.schema.Space, com.laxcus.util.Siger)
	//	 */
	//	@Override
	//	public boolean hasTable(Space space, Siger issuer) {
	//		boolean success = false;
	//		try {
	//			success = launcher.getStaffPool().hasTable(space);
	//		} catch (ResourceException e) {
	//			Logger.error(e);
	//		}
	//		return success;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#findTable(com.laxcus.access.schema.Space, com.laxcus.util.Siger)
	//	 */
	//	@Override
	//	public Table findTable(Space space, Siger issuer) {
	//		return launcher.getStaffPool().findTable(space);
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getCustomDisplay()
	 */
	@Override
	public CustomDisplay getCustomDisplay() {
		return this.display;
	}

}