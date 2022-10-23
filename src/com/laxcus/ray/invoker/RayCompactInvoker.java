/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除缓存块删除数据命令异步调用器。<br><br>
 * 
 * <B>注意：COMPACT命令只允许系统管理员操作，用于测试目的，生产环境禁止使用！</B>
 * 
 * @author scott.liang
 * @version 1.0 3/28/2018
 * @since laxcus 1.0
 */
public class RayCompactInvoker extends RayInvoker {

	/**
	 * 构造删除缓存块删除数据命令异步调用器，指定命令
	 * @param cmd 删除缓存块冗余数据命令
	 */
	public RayCompactInvoker(Compact cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Compact getCommand() {
		return (Compact) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到HOME/TOP站点的任意一个
		boolean success = (isTopHub() || isHomeHub());
		if (!success) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return false;
		}
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		CompactProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CompactProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			// 打印结果
			print(product.list());
		} else {
			printFault(); // 打印故障
		}

		return useful(success);
	}

	/**
	 * 打印单元
	 * @param array
	 */
	private void print(List<TissItem> array) {
		// 显示运行时间
		printRuntime();
		
		// 设置标题
		createShowTitle(new String[] { "COMPACT/STATUS", "COMPACT/SITE", "COMPACT/CODE" });

		// 打印
		for (TissItem e : array) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			// 返回码
			item.add(new ShowIntegerCell(2, e.getState()));
			// 显示
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}
}