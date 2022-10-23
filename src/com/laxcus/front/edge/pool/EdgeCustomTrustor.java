/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge.pool;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;
import com.laxcus.front.edge.*;

/**
 * EDGE站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 7/4/2019
 * @since laxcus 1.0
 */
public class EdgeCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static EdgeCustomTrustor selfHandle = new EdgeCustomTrustor();

	/**
	 * 构造默认和私有的EDGE站点自定义资源代理
	 */
	private EdgeCustomTrustor() {
		super();
	}

	/**
	 * 返回EDGE站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static EdgeCustomTrustor getInstance() {
		return EdgeCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return EdgeLauncher.getInstance().getHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal(com.laxcus.util.Siger)
	 */
	@Override
	public Node getLocal() {
		return EdgeLauncher.getInstance().getListener();
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