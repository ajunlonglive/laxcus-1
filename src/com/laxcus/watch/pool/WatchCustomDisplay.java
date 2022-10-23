/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.pool;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.util.display.show.*;
import com.laxcus.watch.*;
import com.laxcus.watch.component.*;

/**
 * WATCH站点显示器
 * 
 * @author scott.liang
 * @version 1.0 10/31/2017
 * @since laxcus 1.0
 */
class WatchCustomDisplay implements CustomDisplay {

	/**
	 * 构造默认的WATCH站点显示器
	 */
	protected WatchCustomDisplay() {
		super();
	}

	/**
	 * 返回WATCH站点显示面板
	 * @return WatchMixedPanel实例
	 */
	private WatchMixedPanel getDisplay() {
		return WatchLauncher.getInstance().getWindow().getSkeletonPanel().getMixPanel();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#setShowTitle(com.laxcus.util.display.show.ShowTitle)
	 */
	@Override
	public void setShowTitle(ShowTitle e) {
		getDisplay().setShowTitle(e);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#addShowItem(com.laxcus.util.display.show.ShowItem)
	 */
	@Override
	public void addShowItem(ShowItem e) {
		getDisplay().addShowItem(e);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#message(java.lang.String)
	 */
	@Override
	public void message(String text) {
		getDisplay().message(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#warn(java.lang.String)
	 */
	@Override
	public void warn(String text) {
		getDisplay().warning(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.custom.CustomDisplay#fault(java.lang.String)
	 */
	@Override
	public void fault(String text) {
		getDisplay().fault(text);
	}

}