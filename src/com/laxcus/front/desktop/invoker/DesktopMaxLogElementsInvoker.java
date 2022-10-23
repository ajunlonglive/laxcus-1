/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.front.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 图形界面日志可显示数目调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopMaxLogElementsInvoker extends DesktopInvoker {

	/**
	 * 构造图形界面日志可显示数目调用器，指定命令
	 * @param cmd 可显示数目
	 */
	public DesktopMaxLogElementsInvoker(MaxLogElements cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#isDistributed()
	 */
	@Override
	public boolean isDistributed() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxLogElements getCommand() {
		return (MaxLogElements) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 不是FRONT.DESKTOP，拒绝执行
		if (!isDesktop()) {
			faultX(FaultTip.COMMAND_REFUSED);
			return useful(false);
		}
		
		MaxLogElements cmd = getCommand();
		FrontLauncher launcher = getLauncher();
		int size = launcher.setMaxLogs(cmd.getElements());
		print(size);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 显示数字
	 * @param elements
	 */
	private void print(int elements) {
		// 标题
		createShowTitle(new String[] { "MAX-LOG-ELEMENTS/ELEMENTS" });
		ShowItem item = new ShowItem();
		item.add(new ShowIntegerCell(0, elements));
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

}