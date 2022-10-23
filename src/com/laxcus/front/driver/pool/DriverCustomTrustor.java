/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.pool;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;
import com.laxcus.front.driver.*;

/**
 * DRIVER站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 11/1/2017
 * @since laxcus 1.0
 */
public class DriverCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static DriverCustomTrustor selfHandle = new DriverCustomTrustor();

	/**
	 * 构造默认和私有的DRIVER站点自定义资源代理
	 */
	private DriverCustomTrustor() {
		super();
	}

	/**
	 * 返回DRIVER站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static DriverCustomTrustor getInstance() {
		return DriverCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return DriverLauncher.getInstance().getHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal(com.laxcus.util.Siger)
	 */
	@Override
	public Node getLocal() {
		return DriverLauncher.getInstance().getListener();
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