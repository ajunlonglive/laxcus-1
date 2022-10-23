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
import com.laxcus.access.stub.sign.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.stub.sign.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 检查表数据一致性调用器。<br>
 * 
 * CALL站点收集全部DATA站点的签名数据，进行判断，返回处理结果
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class CallCheckEntityConsistencyInvoker extends CallInvoker {

	/**
	 * 构造检查表数据一致性调用器，指定命令
	 * @param cmd 检查表数据一致性命令
	 */
	public CallCheckEntityConsistencyInvoker(CheckEntityConsistency cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckEntityConsistency getCommand() {
		return (CheckEntityConsistency) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckEntityConsistency cmd = getCommand();
		Space space = cmd.getSpace();

		// 被发送的命令
		TakeSign sub = new TakeSign(space);
		// 命令集合
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();

		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
		// 生成命令数组
		if (set != null) {
			for (Node node : set.show()) {
				CommandItem item = new CommandItem(node, sub);
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
		ArrayList<SignSite> array = new ArrayList<SignSite>();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					TakeSignProduct product = getObject(TakeSignProduct.class, index);
					array.add(product.getSite());
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功
		boolean success = (keys.size() == array.size());
		// 不成功，通知FRONT站点
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
			return useful(false);
		}

		// 返回命令
		CheckEntityConsistency cmd = getCommand();
		// 保存签名
		SignSheet sheet = new SignSheet(cmd.getSpace());
		for (SignSite site : array) {
			Node node = site.getNode();
			SignTable table = site.getTable();
			for (StubSign sign : table.list()) {
				sheet.add(node, sign);
			}
		}

		// 总数据块和有效数据块数目
		long stubs = sheet.countStubs();
		long avalidates = sheet.countIdenticals();

		// 生成处理结果
		CheckEntityConsistencyProduct product = new CheckEntityConsistencyProduct();
		product.setSpace(cmd.getSpace());
		product.setStubs(stubs);
		product.setValidates(avalidates);

		// 如果要求详细记录，输出全部
		if (cmd.isDetail()) {
			product.addSites(array);
		}

		// 发送命令
		success = replyProduct(product);
		// 退出
		return useful(success);
	}

}