/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.cross.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 授权人显示自己开放的数据资源调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopShowOpenResourceInvoker extends DesktopInvoker {

	/**
	 * 构造授权人显示自己开放的数据资源调用器，指定命令
	 * @param cmd 授权人显示自己开放的数据资源
	 */
	public DesktopShowOpenResourceInvoker(ShowOpenResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowOpenResource getCommand() {
		return (ShowOpenResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ShareCrossProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShareCrossProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}
		// 结束
		return useful(success);
	}

	/**
	 * 在窗口上显示处理单元
	 * @param array
	 */
	private void print(List<ShareCrossItem> array) {
		// 显示处理时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "SHOW-ACTIVE-RESOURCE/USERNAME",
				"SHOW-ACTIVE-RESOURCE/TABLE", "SHOW-ACTIVE-RESOURCE/OPERATOR",
				"SHOW-ACTIVE-RESOURCE/CREATETIME" });

		ShowOpenResource cmd = getCommand();
		
		for (ShareCrossItem e : array) {
			ShowItem item = new ShowItem();
			
			// 根据用户签名，查找对应的用户明文
			String username = cmd.findPlainText(e.getSiger());
			CrossFlag flag = e.getFlag();
		
			// 用户名
			item.add(new ShowStringCell(0, username));
			// 表名
			item.add(new ShowStringCell(1, flag.getSpace()));
			// 翻译操作符
			String tokens = CrossOperator.translate(flag.getOperator());
			item.add(new ShowStringCell(2, tokens));
			// 建立时间
			String time = splitLaxcusTime(flag.getCreateTime());
			item.add(new ShowStringCell(3, time));

			// 增加一行记录
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

//	/**
//	 * 解析时间参数
//	 * @param time
//	 * @return
//	 */
//	private String splitCreateTime(long time) {
//		Date date = com.laxcus.util.datetime.SimpleTimestamp.format(time);
//		DateFormat dt =  DateFormat.getDateTimeInstance(); // 系统默认的日期/时间格式
//		return dt.format(date);
//	}

}