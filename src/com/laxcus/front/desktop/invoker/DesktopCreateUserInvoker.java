/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 建立账号命令调用器。<br><br>
 * 
 * 建立用户账号，只能由系统管理员或者等同管理员身份的用户发出。<br><br>
 * 
 * 操作流程：<br>
 * 1. FRONT -> GATE -> BANK -> ACCOUNT -> BANK -> TOP <br>
 * 2. TOP (AWARD CREATE REFER) -> HOME <br>
 * 3. HOME -> CALL/WORK/BUILD/DATA -> HOME <br>
 * 4. HOME -> TOP -> BANK -> GATE -> FRONT <br>
 *  
 * 说明：操作成功后，反馈结果是一个“CreateUserProduct”实例，其中包含被部署的ENTRNACE站点。
 * 
 * @author scott.liang
 * @version 1.1 2/23/2014
 * @since laxcus 1.0
 */
public class DesktopCreateUserInvoker extends DesktopInvoker {

	/**
	 * 构造“建立用户账号”命令的调用器，指定命令。
	 * @param cmd 建立用户账号命令
	 */
	public DesktopCreateUserInvoker(CreateUser cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateUser getCommand() {
		return (CreateUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CreateUserProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		if (success) {
			// 如果成功，输出登录站点地址
			Node local = getLocal();
			if (local.getAddress().isWideAddress()) {
				print(success, product.getEntranceOuter());
			} else {
				print(success, product.getEntranceInner());
			}
		} else {
			print(false, null);
		}

		// 处理完成
		return useful(success);
	}

	/**
	 * 在表格栏显示账号结果
	 * @param success 成功
	 * @param node AID站点
	 */
	private void print(boolean success, Node node) {
		// 显示运行时间
		printRuntime();
		CreateUser cmd = getCommand();

		// 显示标题
		createShowTitle(new String[] { "CREATE-USER/STATUS",
				"CREATE-USER/USERNAME", "CREATE-USER/PUBLISH" });

		ShowItem item = new ShowItem();
		item.add(createConfirmTableCell(0, success));
		item.add(new ShowStringCell(1, cmd.getPlainText()));
		if (success) {
			item.add(new ShowStringCell(2, node));
		} else {
			item.add(new ShowStringCell(2, ""));
		}
		addShowItem(item);
		
		// 输出全部记录
		flushTable();

		if (!success) {
			String content = getXMLContent("CREATE-USER/FAILED");
			fault(content, true);
		}
	}

}