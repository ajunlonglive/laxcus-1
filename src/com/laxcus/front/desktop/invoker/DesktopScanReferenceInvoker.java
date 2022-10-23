/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描用户资源调用器
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public abstract class DesktopScanReferenceInvoker extends DesktopInvoker {

	/**
	 * 构造扫描用户资源调用器，指定命令
	 * @param cmd 扫描用户资源命令
	 */
	protected DesktopScanReferenceInvoker(ScanReference cmd) {
		super(cmd);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanVolumeProduct product = new ScanVolumeProduct();

		// 取出参数
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					ScanVolumeProduct e = getObject(ScanVolumeProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 显示处理记录
		print(product.list());

		return useful();
	}

	/**
	 * 显示处理结果
	 * @param array
	 */
	private void print(List<ScanVolumeItem> array) {
		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SCAN-TABLE/DATABASE", "SCAN-TABLE/TABLE", "SCAN-TABLE/STUBS",
				"SCAN-TABLE/ROWS", "SCAN-TABLE/AROWS" });

		for (ScanVolumeItem item : array) {
			ShowItem e = new ShowItem();
			Space space = item.getSpace();
			e.add(new ShowStringCell(0, space.getSchemaText()));
			e.add(new ShowStringCell(1, space.getTableText()));
			e.add(new ShowIntegerCell(2, item.getStubs()));
			e.add(new ShowLongCell(3, item.getRows()));
			e.add(new ShowLongCell(4, item.getAvailableRows()));
			addShowItem(e);
		}
		
		// 输出全部记录
		flushTable();
	}

	/**
	 * 根据输入的表名，将命令分发给关联的CALL站点
	 * @param array 数据表名数组
	 * @return 发送成功返回真，否则假
	 */
	protected boolean distribute(List<Space> array) {
		if (array.isEmpty()) {
			faultX(FaultTip.EMPRY_LIST);
			return false;
		}
		
		TreeMap<Node, ScanTable> cmds = new TreeMap<Node, ScanTable>();
		for (Space space : array) {
			// 根据表名，查找对应的CALL站点地址
			NodeSet set = getStaffPool().findTableSites(space);
			// 返回一个CALL节点地址
			Node node = ((set == null || set.isEmpty()) ? null : set.next());
			// 出错
			if(node == null) {
				faultX(FaultTip.NOTFOUND_X, space);
				return useful(false);
			}
			ScanTable sub = cmds.get(node);
			if (sub == null) {
				sub = new ScanTable();
				cmds.put(node, sub);
			}
			// 保存一个表名
			sub.add(space);
		}

		// 生成命令队列
		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, ScanTable>> iterator = cmds.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, ScanTable> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			items.add(item);
		}

		// 以容错误模式发送到CALL站点
		boolean success = (items.size() > 0);
		if (success) {
			int count = incompleteTo(items);
			success = (count > 0);
		}
		// 发送失败
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT);
		}

		Logger.debug(this, "distribute", success, "send size %d", items.size());

		return success;
	}
}