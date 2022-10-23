/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.echo.*;
import com.laxcus.site.Node;

/**
 * 查询与指定账号关联的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2013
 * @since laxcus 1.0
 */
public class HomeFindRelateHomeInvoker extends HomeInvoker {

	/**
	 * 构造调用器，指定命令
	 * @param cmd
	 */
	public HomeFindRelateHomeInvoker(FindRelateHome cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindRelateHome getCommand() {
		return (FindRelateHome) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindRelateHome cmd = getCommand();
		FindRelateHome sub = cmd.duplicate();
		sub.setSource(super.getListener());
		// 取出TOP站点
		Node hub = this.getHub();
		// 发送到TOP站点
		boolean success = super.completeTo(new Node[] { hub }, sub);

		// 发送错误应答给CALL站点
		if (!success) {
			super.replyFault(Major.FAULTED, Minor.REFUSE);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = reflect();
		return useful(success);
	}

}
