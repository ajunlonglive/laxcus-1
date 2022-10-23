/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 检索用户分布区域调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/29/2019
 * @since laxcus 1.0
 */
public class TopSeekUserAreaInvoker extends TopInvoker {

	/**
	 * 构造检索用户分布区域调用器，指定命令
	 * @param cmd 检索用户分布区域命令
	 */
	public TopSeekUserAreaInvoker(SeekUserArea cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserArea getCommand() {
		return (SeekUserArea) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ArrayList<Node> a = new ArrayList<Node>();
		a.addAll(BankOnTopPool.getInstance().detail());
		a.addAll(HomeOnTopPool.getInstance().detail());

		boolean success = (a.size() > 0);

		if (success) {
			SeekUserArea sub = getCommand().duplicate();
			int count = incompleteTo(a, sub);
			success = (count > 0);
		}

		if (!success) {
			SeekUserAreaProduct product = new SeekUserAreaProduct();
			replyProduct(product);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekUserAreaProduct product = new SeekUserAreaProduct();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					SeekUserAreaProduct e = getObject(SeekUserAreaProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "element size:%d", product.size());

		return useful(success);
	}
	
}