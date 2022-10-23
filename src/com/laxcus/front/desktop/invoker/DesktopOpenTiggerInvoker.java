/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 站点开放TIGGER操作类型命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopOpenTiggerInvoker extends DesktopInvoker {

	/**
	 * 构造站点开放TIGGER操作类型命令调用器，指定命令
	 * @param cmd 站点开放TIGGER操作类型命令
	 */
	public DesktopOpenTiggerInvoker(OpenTigger cmd) {
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
		// 本地操作
		if (cmd.isLocal()) {
			reset();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 重置Tigger操作类型
	 */
	private boolean reset() {
		OpenTigger cmd = getCommand();

		// 加操作
		Tigger.add(cmd.getType());

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
		
		String types = TigType.translateString(Tigger.getDefaultType());

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