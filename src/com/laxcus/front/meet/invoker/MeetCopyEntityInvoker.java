/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 数据块导出命令异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/10/2018
 * @since laxcus 1.0
 */
public class MeetCopyEntityInvoker extends MeetRuleInvoker {

	/** 步骤 **/
	private int step;

	/**
	 * 构造数据块导出命令异步调用器，指定数据块导出命令
	 * @param cmd 数据块导出命令
	 */
	public MeetCopyEntityInvoker(CopyEntity cmd) {
		super(cmd);
		initRule();
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CopyEntity getCommand() {
		return (CopyEntity) super.getCommand();
	}

	/**
	 * 初始化事务规则，采用排它写操作规则，即在处理这个数据块中的过程中，不允许有用户使用“写操作”。
	 */
	private void initRule() {
		CopyEntity cmd = getCommand();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch(step) {
		case 1 :
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}
		step++;

		// 不成功或者大于2时，退出
		if (!success || step > 2) {
			return true;
		}
		return false;
	}

	/**
	 * 检查和发送命令
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 检查注册用户有删除的权限
		CopyEntity cmd = getCommand();
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
		// 不能操作共享表
		if (getStaffPool().isPassiveTable(space)) {
			faultX(FaultTip.PERMISSION_MISSING_X, space);
			return false;
		}
		// 发送命令给CALL节点
		NodeSet set = getStaffPool().findTableSites(space);
		Node node = (set != null ? set.next() : null);
		if (node == null) {
			faultX(FaultTip.SITE_MISSING);
			return false;
		}
		// 发送给CALL节点
		return launchTo(node, cmd);
	}

	/**
	 * 接收返回结果
	 * @return
	 */
	private boolean receive() {
		CopyEntityProduct product = null;
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(CopyEntityProduct.class, index);
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
		return success;
	}

	/**
	 * 显示反馈结果
	 * @param a
	 */
	private void print(List<CopyEntityItem> a) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "COPY-ENTITY/STATUS", "COPY-ENTITY/STUB" });
		// 处理单元
		for (CopyEntityItem e : a) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			String s = String.format("0x%X", e.getStub());
			item.add(new ShowStringCell(1, s));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}
	
	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	//	 */
	//	@Override
	//	protected boolean process() {
	//		// 检查注册用户有删除的权限
	//		CopyEntity cmd = getCommand();
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
	//		// 2. 判断有下载数据块的权限
	//		success = getStaffPool().canTable(space, ControlTag.EXPORT_ENTITY);
	//		if (!success) {
	//			faultX(FaultTip.PERMISSION_MISSING_X, space);
	//			return false;
	//		}
	//
	//		// 生成命令
	//		ArrayList<SingleCopyEntity> subs = new ArrayList<SingleCopyEntity>();
	//		for(CopyEntityItem item : cmd.list() ) {
	//			SingleCopyEntity sub = new SingleCopyEntity(space);
	//			sub.setStub(item.getStub());
	//			sub.setFilename(item.getFile().toString());
	//			sub.setCharset(cmd.getCharset());
	//			sub.setType(cmd.getType());
	//			subs.add(sub);
	//		}
	//
	//		ArrayList<SingleCopyEntityResult> array = new ArrayList<SingleCopyEntityResult>();
	//
	//		// 逐一下载
	//		for (SingleCopyEntity sub : subs) {
	//			// 生成钩子和转发命令
	//			SingleCopyEntityHook hook = new SingleCopyEntityHook();
	//			ShiftSingleCopyEntity shift = new ShiftSingleCopyEntity(sub, hook);
	//			shift.setIssuer(getIssuer());
	//
	//			// 交给句柄处理
	//			success = press(shift);
	//			if (!success) {
	//				SingleCopyEntityResult res = new SingleCopyEntityResult(
	//						false, sub.getFile(), sub.getStub(), 0);
	//				array.add(res);
	//				continue;
	//			}
	//			// 进行等待
	//			hook.await();
	//			// 取出结果
	//			SingleCopyEntityResult res = hook.getProduct();
	//			if (res != null) {
	//				array.add(res);
	//			}
	//		}
	//
	//		// 打印结果
	//		print(array);
	//
	//		// 结束，退出！
	//		return true;
	//	}

}