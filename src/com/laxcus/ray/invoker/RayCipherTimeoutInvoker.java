/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 设置FIXP本地密文超时调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class RayCipherTimeoutInvoker extends RayInvoker {

	/**
	 * 构造设置FIXP本地密文超时调用器，指定命令
	 * @param cmd 设置FIXP本地密文超时
	 */
	public RayCipherTimeoutInvoker(CipherTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CipherTimeout getCommand() {
		return (CipherTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CipherTimeout cmd = getCommand();

		// 如果是本地，在本地处理显示
		if (cmd.isLocal()) {
			long interval = cmd.getInterval();			
			// 设置本地密文超时时间
			Cipher.setTimeout(interval);
			print(interval);
			return useful();
		} else {
			// 投递到HUB站点，分别处理
			return fireToHub();
		}
	}

	/**
	 * 打印时间
	 * @param interval
	 */
	private void print(long interval) {
		createShowTitle(new String[] { "LOCAL-CIPHER-TIMEOUT/TIME" });
		String text = doStyleTime(interval);

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CipherTimeoutProduct product = null; 
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(CipherTimeoutProduct.class, index);
			}
		} catch(VisitException e){
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 显示反馈结果
	 * @param product
	 */
	private void print(CipherTimeoutProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "CIPHER-TIMEOUT-REMOTE/STATUS", "CIPHER-TIMEOUT-REMOTE/SITE" });
		
		CipherTimeout cmd = this.getCommand();
		// 取出节点
		ArrayList<Node> array = new ArrayList<Node>();
		if (cmd.isLocal() || cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}
		
		// 处理单元
		for (Node node : array) {
			CipherTimeoutItem e = product.find(node);
			if (e == null) {
				continue;
			}
			// 显示一行
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}
