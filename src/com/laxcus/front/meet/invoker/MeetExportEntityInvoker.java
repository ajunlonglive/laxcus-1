/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 数据块导出命令异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/10/2018
 * @since laxcus 1.0
 */
public class MeetExportEntityInvoker extends MeetRuleInvoker {

	/**
	 * 构造数据块导出命令异步调用器，指定数据块导出命令
	 * @param cmd 数据块导出命令
	 */
	public MeetExportEntityInvoker(ExportEntity cmd) {
		super(cmd);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ExportEntity getCommand() {
		return (ExportEntity) super.getCommand();
	}

	/**
	 * 初始化事务规则，采用排它写操作规则，即在处理这个数据块中的过程中，不允许有用户使用“写操作”。
	 */
	private void initRule() {
		ExportEntity cmd = getCommand();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		addRule(rule);
	}

	/**
	 * 打印结果
	 * @param array
	 */
	private void print(List<SingleExportEntityResult> array) {
		// 本次运行时间
		printRuntime();

		// 生成表格标题
		createShowTitle(new String[] { "EXPORT-ENTITY/EXPORT/STATUS",
				"EXPORT-ENTITY/EXPORT/STUB", "EXPORT-ENTITY/EXPORT/ROWS",
				"EXPORT-ENTITY/EXPORT/FILE" });
		// 打印记录
		for (SingleExportEntityResult e : array) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowLongCell(1, e.getStub(), 16));
			item.add(new ShowIntegerCell(2, e.getRows()));
			item.add(new ShowStringCell(3, e.getFile()));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		// 检查注册用户有删除的权限
		ExportEntity cmd = getCommand();
		Space space = cmd.getSpace();
		// 1. 判断数据库存在
		boolean success = false;
		try {
			success = getStaffPool().hasTable(space);
		} catch (ResourceException e) {
			Logger.error(e);
		}
		if (!success) {
			faultX(FaultTip.NOTFOUND_X, space);
			return false;
		}
		// 2. 判断有下载数据块的权限
		success = getStaffPool().canTable(space, ControlTag.EXPORT_ENTITY);
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING_X, space);
			return false;
		}

		// 生成命令
		ArrayList<SingleExportEntity> subs = new ArrayList<SingleExportEntity>();
		for(ExportEntityItem item : cmd.list() ) {
			SingleExportEntity sub = new SingleExportEntity(space);
			sub.setStub(item.getStub());
			sub.setFilename(item.getFile().toString());
			sub.setCharset(cmd.getCharset());
			sub.setType(cmd.getType());
			subs.add(sub);
		}

		ArrayList<SingleExportEntityResult> array = new ArrayList<SingleExportEntityResult>();

		// 逐一下载
		for (SingleExportEntity sub : subs) {
			// 生成钩子和转发命令
			SingleExportEntityHook hook = new SingleExportEntityHook();
			ShiftSingleExportEntity shift = new ShiftSingleExportEntity(sub, hook);
			shift.setIssuer(getIssuer());

			// 交给句柄处理
			success = press(shift);
			if (!success) {
				SingleExportEntityResult res = new SingleExportEntityResult(
						false, sub.getFile(), sub.getStub(), 0);
				array.add(res);
				continue;
			}
			// 进行等待
			hook.await();
			// 取出结果
			SingleExportEntityResult res = hook.getProduct();
			if (res != null) {
				array.add(res);
			}
		}

		// 打印结果
		print(array);

		// 结束，退出！
		return true;
	}

}