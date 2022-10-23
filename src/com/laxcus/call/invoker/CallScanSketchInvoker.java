/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 检测表分布数据容量调用器。<br>
 * 
 * CALL站点将命令发往全部DATA站点
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public class CallScanSketchInvoker extends CallInvoker {

	/**
	 * 构造检测表分布数据容量调用器，指定命令
	 * @param cmd 检测表分布数据容量命令
	 */
	public CallScanSketchInvoker(ScanSketch cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanSketch getCommand() {
		return (ScanSketch) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanSketch cmd = getCommand();
		Space space = cmd.getSpace();

		// 命令集合
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
		// 生成命令数组
		if (set != null) {
			for (Node node : set.show()) {
				CommandItem item = new CommandItem(node, cmd);
				array.add(item);
			}
		}

		// 必须有效，且全部发送成功
		boolean success = (array.size() > 0);
		if (success) {
			success = completeTo(array);
		}
		// 不成功，通知FRONT站点
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
		}
		// 返回处理结果
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanSketch cmd = getCommand();
		Space space = cmd.getSpace();
		ScanSketchProduct product = new ScanSketchProduct(space);

		int count = 0;
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					ScanSketchProduct e = getObject(ScanSketchProduct.class, index);
					product.add(e);
					count++;
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功
		boolean success = (count == keys.size());
		// 发送处理结果
		if (success) {
			super.replyProduct(product);
		} else {
			super.replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
		}
		// 退出
		return useful(success);
	}

}
