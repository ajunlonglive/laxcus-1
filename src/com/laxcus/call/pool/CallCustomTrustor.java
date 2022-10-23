/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;
import com.laxcus.call.*;

/**
 * CALL站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 11/1/2017
 * @since laxcus 1.0
 */
public class CallCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static CallCustomTrustor selfHandle = new CallCustomTrustor();

	/**
	 * 构造默认和私有的CALL站点自定义资源代理
	 */
	private CallCustomTrustor() {
		super();
	}

	/**
	 * 返回CALL站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static CallCustomTrustor getInstance() {
		return CallCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return CallLauncher.getInstance().getHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal(com.laxcus.util.Siger)
	 */
	@Override
	public Node getLocal() {
		return CallLauncher.getInstance().getListener();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getSubHubs(com.laxcus.util.Siger)
	 */
	@Override
	public Node[] getSubHubs() {
		List<Node> hubs = HomeOnCallPool.getInstance().getNodes();
		if (hubs == null || hubs.isEmpty()) {
			return null;
		}
		Node[] a = new Node[hubs.size()];
		return hubs.toArray(a);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getCustomDisplay()
	 */
	@Override
	public CustomDisplay getCustomDisplay() {
		return null;
	}

}