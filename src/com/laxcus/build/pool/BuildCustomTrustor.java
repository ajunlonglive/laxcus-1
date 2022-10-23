/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;
import com.laxcus.build.*;

/**
 * BUILD站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 11/1/2017
 * @since laxcus 1.0
 */
public class BuildCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static BuildCustomTrustor selfHandle = new BuildCustomTrustor();

	/**
	 * 构造默认和私有的BUILD站点自定义资源代理
	 */
	private BuildCustomTrustor() {
		super();
	}

	/**
	 * 返回BUILD站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static BuildCustomTrustor getInstance() {
		return BuildCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return BuildLauncher.getInstance().getHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal(com.laxcus.util.Siger)
	 */
	@Override
	public Node getLocal() {
		return BuildLauncher.getInstance().getListener();
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