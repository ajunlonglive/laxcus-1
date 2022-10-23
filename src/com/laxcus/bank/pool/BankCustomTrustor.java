/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;


import com.laxcus.echo.invoker.custom.*;
import com.laxcus.site.*;
import com.laxcus.bank.*;

/**
 * BANK站点自定义资源代理
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public class BankCustomTrustor implements CustomTrustor {

	/** 静态句柄 **/
	private static BankCustomTrustor selfHandle = new BankCustomTrustor();

	/**
	 * 构造默认和私有的BANK站点自定义资源代理
	 */
	private BankCustomTrustor() {
		super();
	}

	/**
	 * 返回BANK站点自定义资源代理
	 * @return 自定义资源代理句柄
	 */
	public static BankCustomTrustor getInstance() {
		return BankCustomTrustor.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getHub(com.laxcus.util.Siger)
	 */
	@Override
	public Node getHub() {
		return BankLauncher.getInstance().getHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomTrustor#getLocal(com.laxcus.util.Siger)
	 */
	@Override
	public Node getLocal() {
		return BankLauncher.getInstance().getListener();
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