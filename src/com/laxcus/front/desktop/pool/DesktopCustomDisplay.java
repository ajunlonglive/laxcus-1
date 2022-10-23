/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.pool;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.util.display.show.*;

/**
 * FRONT交互模式站点显示器
 * 
 * @author scott.liang
 * @version 1.0 11/1/2017
 * @since laxcus 1.0
 */
public class DesktopCustomDisplay implements CustomDisplay {

	/**
	 * 构造默认的FRONT交互模式站点显示器
	 */
	protected DesktopCustomDisplay() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#setShowTitle(com.laxcus.util.display.show.ShowTitle)
	 */
	@Override
	public void setShowTitle(ShowTitle e) {
//		DesktopInvoker.getDisplay().setShowTitle(e);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#addShowItem(com.laxcus.util.display.show.ShowItem)
	 */
	@Override
	public void addShowItem(ShowItem e) {
//		DesktopInvoker.getDisplay().addShowItem(e);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#message(java.lang.String)
	 */
	@Override
	public void message(String text) {
//		DesktopInvoker.getDisplay().message(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#warn(java.lang.String)
	 */
	@Override
	public void warn(String text) {
//		DesktopInvoker.getDisplay().warning(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#fault(java.lang.String)
	 */
	@Override
	public void fault(String text) {
//		DesktopInvoker.getDisplay().fault(text);
	}

}