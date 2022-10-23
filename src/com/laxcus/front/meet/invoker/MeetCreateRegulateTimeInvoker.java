/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 建立数据表优化时间命令调用器。<br>
 * 
 * @author scott.liang
 * @version 1.1 9/2/2012
 * @since laxcus 1.0
 */
public class MeetCreateRegulateTimeInvoker extends MeetInvoker {

	/**
	 * 构造建立数据表优化时间命令调用器，指定命令
	 * @param cmd 建立数据表优化时间命令
	 */
	public MeetCreateRegulateTimeInvoker(CreateRegulateTime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateRegulateTime getCommand() {
		return (CreateRegulateTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		CreateRegulateTimeProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateRegulateTimeProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());

		CreateRegulateTime cmd = getCommand();
		
		if (success) {
			print(true, product.getSwitchTime().getSpace());
			// 保存在本地
			getStaffPool().createRegulateTime(cmd.getSwitchTime());
		} else {
			print(false, cmd.getSwitchTime().getSpace());
		}

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param success 成功
	 * @param space 表名
	 */
	private void print(boolean success, Space space) {
		// 显示运行时间
		printRuntime();

		// 设置标题
		createShowTitle(new String[] { "REGULATE-TIME/STATUS", "REGULATE-TIME/SPACE" });

		// 打印
		ShowItem item = new ShowItem();
		// 图标或者文字
		item.add(createConfirmTableCell(0, success));
		// 表名
		item.add(new ShowStringCell(1, space));
		// 显示
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}