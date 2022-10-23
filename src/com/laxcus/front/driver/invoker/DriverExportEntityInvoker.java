/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;

/**
 * 数据块导出命令异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/10/2018
 * @since laxcus 1.0
 */
public class DriverExportEntityInvoker extends DriverRuleInvoker {

	/**
	 * 构造数据块导出命令异步调用器，指定数据块导出命令
	 * @param cmd 数据块导出命令
	 */
	public DriverExportEntityInvoker(DriverMission mission) {
		super(mission);
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

//	/**
//	 * 打印结果
//	 * @param array
//	 */
//	private void print(List<SingleExportEntityResult> array) {
//		// 本次运行时间
//		printRuntime();
//
//		// 生成表格标题
//		createShowTitle(new String[] { "EXPORT-ENTITY/EXPORT/STATUS",
//				"EXPORT-ENTITY/EXPORT/STUB", "EXPORT-ENTITY/EXPORT/ROWS",
//				"EXPORT-ENTITY/EXPORT/FILE" });
//		// 打印记录
//		for (SingleExportEntityResult e : array) {
//			ShowItem item = new ShowItem();
//			item.add(createConfirmTableCell(0, e.isSuccessful()));
//			item.add(new ShowLongCell(1, e.getStub(), 16));
//			item.add(new ShowIntegerCell(2, e.getRows()));
//			item.add(new ShowStringCell(3, e.getFile()));
//			addShowItem(item);
//		}
//		// 输出全部记录
//		flushTable();
//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.invoker.DriverRuleInvoker#process()
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

			// 交给句柄处理
			success = getCommandPool().press(shift);
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

		// 保存参数
		ExportEntityProduct product = new ExportEntityProduct(cmd.getSpace());
		product.addAll(array);
		// 输出结果
		setProduct(product);

		// 结束，退出！
		return true;
	}

}

///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license GNU Lesser General Public License (LGPL)
// */
//package com.laxcus.front.driver.invoker;
//
//import java.io.*;
//
//import com.laxcus.access.diagram.*;
//import com.laxcus.access.parse.*;
//import com.laxcus.access.schema.*;
//import com.laxcus.command.access.table.*;
//import com.laxcus.front.driver.mission.*;
//import com.laxcus.law.rule.*;
//import com.laxcus.log.client.*;
//import com.laxcus.mission.*;
//import com.laxcus.site.*;
//import com.laxcus.util.set.*;
//import com.laxcus.util.tip.*;
//
///**
// * 获得数据块编号命令异步调用器。<br>
// * 
// * @author scott.liang
// * @version 1.0 2/10/2018
// * @since laxcus 1.0
// */
//public class DriverExportEntityInvoker extends DriverRuleInvoker {
//
//	/** 处理步骤 **/
//	private int step = 1;
//
//	/**
//	 * 构造获得数据块编号命令异步调用器
//	 * @param mission 驱动任务
//	 */
//	public DriverExportEntityInvoker(DriverMission mission) {
//		super(mission);
//		initRule();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
//	 */
//	@Override
//	public ExportEntity getCommand() {
//		return (ExportEntity) super.getCommand();
//	}
//
//	/**
//	 * 初始化事务规则
//	 */
//	private void initRule() {
//		ExportEntity cmd = getCommand();
//		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
//		rule.setSpace(cmd.getSpace());
//		addRule(rule);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.meet.invoker.DriverRuleInvoker#process()
//	 */
//	@Override
//	protected boolean process() {
//		boolean success = false;
//		switch (step) {
//		case 1:
//			success = send();
//			break;
//		case 2:
//			success = receive();
//			break;
//		}
//		// 自增1
//		step++;
//		// 大于2是完成，否则是没有完成
//		return (!success || step > 2);
//	}
//
//	/**
//	 * 处理第一阶段
//	 * @return 成功返回真，否则假
//	 */
//	private boolean send() {
//		// 检查注册用户有删除的权限
//		ExportEntity cmd = getCommand();
//		Space space = cmd.getSpace();
//		// 1. 判断数据库存在
//		boolean success = false;
//		try {
//			success = getStaffPool().hasTable(space);
//		} catch (ResourceException e) {
//			Logger.error(e);
//		}
//		if (!success) {
//			faultX(FaultTip.NOTFOUND_X, space);
//			return false;
//		}
//		// 2. 判断拥有获得数据块编号权限
//		success = getStaffPool().canTable(space, ControlTag.SELECT);
//		if (!success) {
//			faultX(FaultTip.PERMISSION_MISSING);
//			return false;
//		}
//
//		// 找到CALL站点
//		NodeSet set = getStaffPool().findTableSites(space);
//		// 顺序枚举一个CALL站点地址，保持调用平衡
//		Node hub = (set != null ? set.next() : null);
//		// 没有找到，弹出错误
//		if (hub == null) {
//			faultX(FaultTip.NOTFOUND_SITE_X, space);
//			return false;
//		}
//
//		// 发送到目标地址
//		success = completeTo(hub, cmd);
//		if(!success) {
//			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
//		}
//
//		Logger.debug(this, "send", success, "send to %s", hub);
//
//		return success;
//	}
//
//	/**
//	 * 处理第二阶段
//	 * @return 成功返回真，否则假
//	 */
//	private boolean receive() {
//		int index = findEchoKey(0);
//		// 不成功显示错误
//		if (!isSuccessCompleted(index)) {
//			faultX(FaultTip.FAILED_X, getCommand());
//			return false;
//		}
//
//		// 判断数据在磁盘文件中
//		boolean ondisk = isEchoFiles();
//		// 显示信息
//		boolean success = false;
//		try {
//			if (ondisk) {
//				File file = findFile(index);
//				File driver = rename(file);
//				setResult(new MissionFileResult(driver));
//			} else {
//				byte[] b = collect();
//				setResult(new MissionBufferResult(b));
//			}
//			success = true;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//			fault(e);
//		}
//
//		// 完成
//		return success;
//	}
//
//}