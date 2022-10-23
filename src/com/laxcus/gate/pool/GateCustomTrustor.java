/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import com.laxcus.gate.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;

/**
 * GATE站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class GateCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static GateCustomTrustor selfHandle = new GateCustomTrustor();

	/**
	 * 构造默认和私有的GATE站点自定义资源代理
	 */
	private GateCustomTrustor() {
		super();
	}

	/**
	 * 返回GATE站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static GateCustomTrustor getInstance() {
		return GateCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return GateLauncher.getInstance().getHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal()
	 */
	@Override
	public Node getLocal() {
		return GateLauncher.getInstance().getListener();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getSubHubs(com.laxcus.util.Siger)
	 */
	@Override
	public Node[] getSubHubs() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getCustomDisplay()
	 */
	@Override
	public CustomDisplay getCustomDisplay() {
		return null;
	}

}