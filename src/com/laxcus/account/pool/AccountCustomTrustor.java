/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import com.laxcus.account.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;

/**
 * ACCOUNT站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class AccountCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static AccountCustomTrustor selfHandle = new AccountCustomTrustor();

	/**
	 * 构造默认和私有的ACCOUNT站点自定义资源代理
	 */
	private AccountCustomTrustor() {
		super();
	}

	/**
	 * 返回ACCOUNT站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static AccountCustomTrustor getInstance() {
		return AccountCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return AccountLauncher.getInstance().getHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal()
	 */
	@Override
	public Node getLocal() {
		return AccountLauncher.getInstance().getListener();
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