/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.pool.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.util.*;

/**
 * 批量处理数据优化调用器。<br>
 * GATE站点检查和加载账号，然后启动数据优化操作。
 * 
 * @author scott.liang
 * @version 1.0 7/20/2018
 * @since laxcus 1.0
 */
public class GateBatchPressRegulateInvoker extends GateInvoker {

	/**
	 * 构造批量处理数据优化调用器，指定命令
	 * @param cmd 批量处理数据优化
	 */
	public GateBatchPressRegulateInvoker(BatchPressRegulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BatchPressRegulate getCommand() {
		return (BatchPressRegulate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BatchPressRegulate shift = getCommand();

		for (PressRegulate cmd : shift.list()) {
			// 修改为单向处理
			cmd.setDirect(true);
			// 在本地加载账号
			Siger siger = cmd.getIssuer();
			boolean success = StaffOnGatePool.getInstance().contains(siger);
			if (success) {
				success = StaffOnGatePool.getInstance().reloadAccount(siger);
			}
			// 加载CALL站点地址
			if (success) {
				success = CallOnGatePool.getInstance().loadCallSites(siger);
			}
			
			// 交给管理池处理
			if (success) {
				getCommandPool().admit(cmd);
			}
		}
		
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
		
		//		TakeAuthorizerSiteProduct product = null;
		//		int index = findEchoKey(0);
		//		try {
		//			if (isSuccessObjectable(index)) {
		//				product = getObject(TakeAuthorizerSiteProduct.class, index);
		//			}
		//		} catch (VisitException e) {
		//			Logger.error(e);
		//		}
		//
		//		// 判断成功
		//		boolean success = (product != null);
		//		if (!success) {
		//			return false;
		//		}
		//		
		//		BatchPressRegulate shift = getCommand();
		//		TreeMap<Node, BatchPressRegulate> nodes = new TreeMap<Node, BatchPressRegulate>();
		//		// 逐个匹配
		//		for (AuthorizerItem item : product.list()) {
		//			// 签名和GATE站点
		//			Siger siger = item.getAuthorizer();
		//			Node gate = item.getSite();
		//			// 逐一匹配
		//			for (PressRegulate single : shift.list()) {
		//				if (Laxkit.compareTo(siger, single.getIssuer()) == 0) {
		//					BatchPressRegulate batch = nodes.get(siger);
		//					if (batch == null) {
		//						batch = new BatchPressRegulate();
		//						nodes.put(gate, batch);
		//					}
		//					batch.add(single);
		//				}
		//			}
		//		}
		//
		//		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		//		Iterator<Map.Entry<Node, BatchPressRegulate>> iterator = nodes.entrySet().iterator();
		//		while (iterator.hasNext()) {
		//			Map.Entry<Node, BatchPressRegulate> entry = iterator.next();
		//			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
		//			items.add(item);
		//		}
		//		
		//		// 分别发送给不同的GATE站点
		//		int count = super.directTo(items, false);
		//		success = (count>0);
		//		// 退出
		//		return useful(success);
	}

}