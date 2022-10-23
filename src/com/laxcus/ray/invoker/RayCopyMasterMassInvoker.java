/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.stub.transfer.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 复制DATA主节点数据块调用器
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class RayCopyMasterMassInvoker extends RayInvoker {

	/**
	 * 构造复制DATA主节点数据块调用器，指定命令
	 * @param cmd 复制DATA主节点数据块
	 */
	public RayCopyMasterMassInvoker(CopyMasterMass cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CopyMasterMass getCommand() {
		return (CopyMasterMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 不是登录到HOME节点，拒绝发送
		if (!isHomeHub()) {
			faultX(FaultTip.HOME_RETRY);
			return useful(false);
		}
		// 投递到HOME站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		// 判断有错误
		if (isFaultCompleted(index)) {
			printFault();
			return useful(false);
		}

		// 取出实例
		CopyMassProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CopyMassProduct.class, index);
			}
		} catch (VisitException e) {
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
	 * 打印结果
	 * @param product
	 */
	private void print(CopyMassProduct product) {
		// 打印消耗的时间
		printRuntime();

		// 显示标题
		createShowTitle(new String[] { "COPY-MASTER-MASS/STATUS",
				"COPY-MASTER-MASS/STUB" });

		for (CopyMassItem e : product.list()) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			String s = String.format("%x", e.getStub());
			item.add(new ShowStringCell(1, s));
			addShowItem(item);
		}

		// 输出全部记录
		flushTable();
	}

}