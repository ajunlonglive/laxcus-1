/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.util.display.show.*;

/**
 * 显示数据重组时间调用器。
 * 
 * 这个调用器在本地处理。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopPrintRegulateTimeInvoker extends DesktopInvoker {

	/**
	 * 构造显示数据重组时间调用器，指定命令
	 * @param cmd 显示数据重组时间
	 */
	public DesktopPrintRegulateTimeInvoker(PrintRegulateTime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintRegulateTime getCommand(){
		return (PrintRegulateTime)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintRegulateTime cmd = getCommand();
		List<SwitchTime> times = getStaffPool().getSwitchTimes();
		
		// 设置标题
		createShowTitle(new String[] { "PRINT-REGULATE-TIME/DATABASE",
				"PRINT-REGULATE-TIME/TABLE", "PRINT-REGULATE-TIME/COLUMN",
				"PRINT-REGULATE-TIME/FAMILY", "PRINT-REGULATE-TIME/INTERVAL" });

		// 显示
		for (SwitchTime time : times) {
			Space space = time.getSpace();
			boolean success = (cmd.isAll() || cmd.contains(space));
			if (!success) {
				continue;
			}
			
			// 数据表名
			Table table = getStaffPool().findTable(space);
			if (table == null) {
				continue;
			}
			// 取出属性
			ColumnAttribute attribute = table.find(time.getColumnId());
			if (attribute == null) {
				attribute = table.pid(); // 如果没有定义，是主键
			}
			
			ShowItem item = new ShowItem();
			// 数据库和表
			item.add(new ShowStringCell(0, space.getSchemaText()));
			item.add(new ShowStringCell(1, space.getTableText()));
			// 列名
			item.add(new ShowStringCell(2, attribute.getNameText()));
			// 类型
			String family = SwitchTimeTag.translate(time.getFamily());
			item.add(new ShowStringCell(3, family));
			// 时间间隔描述
			item.add(new ShowStringCell(4, time.getIntervalText()));
			// 增加一行
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
		
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

}