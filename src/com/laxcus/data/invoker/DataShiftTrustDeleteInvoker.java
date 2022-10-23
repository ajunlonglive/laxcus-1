/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.trust.*;
import com.laxcus.log.client.*;

/**
 * DELETE代理转发调用器
 * 
 * @author scott.liang
 * @version 9/15/2017
 * @since laxcus 1.0
 */
public class DataShiftTrustDeleteInvoker extends DataShiftTrustAccessInvoker {

	/**
	 * 构造DELETE代理转发调用器，指定命令
	 * @param cmd DELETE代理转发命令
	 */
	public DataShiftTrustDeleteInvoker(ShiftTrustDelete cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTrustDelete getCommand() {
		return (ShiftTrustDelete) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 在执行INSERT操作前，执行判断操作，不成功退出
		boolean success = check();
		if (!success) {
			return useful(false);
		}

		// 转发命令
		ShiftTrustDelete shift = getCommand();
		TrustDeleteHook hook = shift.getHook();
		TrustDelete trust = shift.getCommand();
		// 取出DELETE命令和数据块编号
		Delete cmd = trust.getDelete();
		long stub = trust.getStub();
		// 调用JNI接口，删除数据
		byte[] result = null;
		try {
			result = AccessTrustor.delete(cmd, stub);
		} catch (AccessException e) {
			Logger.error(e);
			return useful(false);
		}

		// 分析处理结果，取出数据内容
		AccessStack stack = new AccessStack(result);

		// 没有找到，返回成功，继续下一个数据块
		if (!stack.isSuccessful()) {
			int error = (stack.isNotFound() ? 0 : stack.getFault());
			// 设置错误码和退出
			hook.setResult(new java.lang.Integer(error));
			return useful(false);
		}
		// 删除行数
		int rows = stack.getRows();

		success = false;
		// 分为缓存块和固定块，备份到从站点
		if (stack.isCacheStub()) {
			success = doBackupCache(stack);
		} else if (stack.isChunkStub()) {
			success = doBackupChunk(stack);
		}
		
		// 判断是成功或者失败，选择返回码
		int value = (success ? rows : FaultCode.REMOTE_BACKUP_FAILED);

		// 如果不成功，把数据恢复到磁盘
		if (!success) {
			boolean ret = restore(stack);
			// 不成功，是重新插入故障
			if (!ret) {
				value = FaultCode.REINSERT_FAILED;
			}
		}

		// 设置删除结果
		hook.setResult(new java.lang.Integer(value));

		Logger.debug(this, "launch", success, "delete '%s/%x'", stack.getSpace(), stub );

		// 输出
		return useful(success);
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
	 * 把删除的数据恢复到磁盘。恢复以追加的方式写入
	 * @param stack 数据栈
	 * @return 恢复成功返回真，否则假
	 */
	private boolean restore(AccessStack stack) {
		// 提取数据表
		Space space = stack.getSpace();
		Table table = findTable(space);
		if (table == null) {
			Logger.error(this, "restore", "cannot not be find '%s'", space);
			return false;
		}
		// 按照显示列，生成列属性顺序表
		Sheet sheet = table.getSheet();

		byte[] content = stack.getContent();
		// 分析报头，确定全部数据流长度(标记头和检索数据)
		MassFlag flag = new MassFlag();
		int off = flag.resolve(content, 0, content.length);

		// 解析输出的结果，在原记录结果上产生新的记录，并且以字节数组输出到内存
		RowCracker cracker = new RowCracker(sheet);
		// 从报头之后开始解析
		cracker.split(content, off, content.length - off);
		List<Row> rows = cracker.flush();

		// 将数据重新写入硬盘
		Insert cmd = new Insert(space);
		cmd.addAll(rows);
		// 调用JNI接口，执行操作
		byte[] b = AccessTrustor.insert(cmd);
		// 判断成功
		boolean success = isSuccessfulStack(b);
		
		Logger.debug(this, "restore", success, "rewrite %s is:%d", space, rows.size());
		
		return success;
	}
	

}