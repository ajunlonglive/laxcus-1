/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.tub.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 显示边缘容器调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopShowTubContainerInvoker extends DesktopInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 显示边缘容器
	 */
	public DesktopShowTubContainerInvoker(ShowTubContainer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowTubContainer getCommand() {
		return (ShowTubContainer) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 管理员不允许执行这种操作
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
			return useful(false);
		}

		// 找到结果
		List<TubTag> tubs = TubPool.getInstance().getTags();
		print(tubs);
		
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
	 * 判断匹配
	 * @param tag
	 * @param names
	 * @return
	 */
	private boolean matchs(TubTag tag, Naming[] names) {
		for (Naming e : names) {
			if (Laxkit.compareTo(tag.getNaming(), e) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 打印结果
	 * @param e
	 */
	private void print(List<TubTag> tubs) {
		// 设置标题
		createShowTitle(new String[] { "SHOW-TUB-CONTAINER/NAMING",
				"SHOW-TUB-CONTAINER/CLASS" });
		
		ShowTubContainer cmd = getCommand();
		Naming[] names = cmd.getNamings();
		
		// 显示结果
		for (TubTag e : tubs) {
			// 检查匹配
			boolean success = cmd.isAll();
			if (!success) {
				success = matchs(e, names);
			}
			if (!success) {
				continue;
			}
			
			ShowItem item = new ShowItem();
			// 站点地址
			item.add(new ShowStringCell(0, e.getNaming()));
			// 返回码
			item.add(new ShowStringCell(1, e.getClassName()));

			// 显示
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}