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
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 开放节点TIGGER操作类型调用器。
 * 
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public class RayOpenTiggerInvoker extends RayInvoker {

	/**
	 * 构造开放节点TIGGER操作类型调用器，指定命令
	 * @param cmd 节点TIGGER操作类型
	 */
	public RayOpenTiggerInvoker(OpenTigger cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OpenTigger getCommand() {
		return (OpenTigger) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		OpenTigger cmd = getCommand();
		// 本地节点
		if (cmd.isLocal()) {
			boolean success = reset();
			return useful(success);
		} else {
			// 投递到HUB站点，分别处理
			return fireToHub();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ProcessTiggerProduct product = null; 
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(ProcessTiggerProduct.class, index);
			}
		} catch(VisitException e){
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 显示反馈结果
	 * @param a
	 */
	private void print(List<ProcessTiggerItem> a) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "OPEN-TIGGER/STATUS", "OPEN-TIGGER/SITE", "OPEN-TIGGER/TYPE" });
		// 处理单元
		for (ProcessTiggerItem e : a) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			String types = TigType.translateString(e.getType());
			item.add(new ShowStringCell(2, types));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

	/**
	 * 重置节点TIGGER操作类型
	 */
	private boolean reset() {
		OpenTigger cmd = getCommand();

		//		System.out.printf("source type : %s\n", TigType.translateString(cmd.getType()));
		//		System.out.printf("open after type : %s\n", TigType.translateString(Tigger.getDefaultType()));

		// 加操作
		Tigger.add(cmd.getType());

		//		System.out.printf("open before type : %s\n", TigType.translateString(Tigger.getDefaultType()));

		// 打印结果
		print(true);

		return true;
	}

	/**
	 * 打印本地参数
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		// 设置标题
		createShowTitle(new String[] { "OPEN-TIGGER/LOCAL", "OPEN-TIGGER/TYPE" });
		
		int type = Tigger.getDefaultType();
		String types = TigType.translateString(type);

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("OPEN-TIGGER/LOCAL/SUCCESS")
				: getXMLContent("OPEN-TIGGER/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		item.add(new ShowStringCell(1, types));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}