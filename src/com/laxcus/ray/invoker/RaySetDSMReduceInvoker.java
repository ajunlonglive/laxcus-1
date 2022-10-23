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
 * 设置DSM表压缩倍数异步调用器。<br><br>
 * 
 * <B> 此操作不修改账号中的参数，只对运行中的DATA节点起作用。如果需要修改表的配置参数，请通过FRONT节点使用“SET MAX DSM REDUCE”命令。
 * </B>
 * 
 * @author scott.liang
 * @version 1.0 5/20/2019
 * @since laxcus 1.0
 */
public class RaySetDSMReduceInvoker extends RayInvoker {

	/**
	 * 构造设置DSM表压缩倍数异步调用器，指定命令
	 * @param cmd 设置DSM表压缩倍数
	 */
	public RaySetDSMReduceInvoker(SetDSMReduce cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetDSMReduce getCommand() {
		return (SetDSMReduce) super.getCommand();
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
		SetDSMReduceProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetDSMReduceProduct.class, index);
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
		createShowTitle(new String[] { "SETDSMREDUCE/STATUS",
				"SETDSMREDUCE/TABLE", "SETDSMREDUCE/MULTIPLE",
				"SETDSMREDUCE/SITE", "SETDSMREDUCE/CODE" });

		SetDSMReduce cmd = getCommand();
		
		// 打印
		for (TissItem e : array) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 数据表
			item.add(new ShowStringCell(1, cmd.getSpace()));
			// 压缩倍数
			item.add(new ShowIntegerCell(2, cmd.getMultiple()));
			// 站点地址
			item.add(new ShowStringCell(3, e.getSite()));
			// 返回码
			item.add(new ShowIntegerCell(4, e.getState()));
			// 显示
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}