/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 判断数据库调用器。<br>
 * 只能由注册用户操作。
 * 
 * @author scott.liang
 * @version 1.0 12/13/2019
 * @since laxcus 1.0
 */
public class DesktopAssertSchemaInvoker extends DesktopInvoker {

	/**
	 * 构造判断数据库调用器，指定命令
	 * @param cmd 判断数据库
	 */
	public DesktopAssertSchemaInvoker(AssertSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertSchema getCommand() {
		return (AssertSchema) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		
		// 只允许注册用户操作
		boolean success = isUser();
		// 权限不足
		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			}
			// 提示
			faultX(FaultTip.PERMISSION_MISSING);
			return useful(false);
		}
		return fireToHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AssertSchemaProduct product = null;
		int index = getEchoKeys().get(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AssertSchemaProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		ProductListener listener = getProductListener();
		// 无效！
		if (product == null) {
			if (listener != null) {
				listener.push(null);
			}
			faultX(FaultTip.IMPLEMENT_FAULT);
			return useful(false);
		}
		
		// 显示结果
		if (listener != null) {
			listener.push(product);
		} else {
			// 显示结果
			print(product.isSuccessful());
		}

		return useful();
	}

	/**
	 * 打印结果
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		AssertSchema cmd = getCommand();
		Fame database = cmd.getFame();

		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "ASSERT-DATABASE/DATABASE",
				"ASSERT-DATABASE/STATUS" });

		// 结果
		String res = (success ? getXMLContent("ASSERT-DATABASE/STATUS/YES")
				: getXMLContent("ASSERT-DATABASE/STATUS/NO"));

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, database));
		item.add(new ShowStringCell(1, res));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}