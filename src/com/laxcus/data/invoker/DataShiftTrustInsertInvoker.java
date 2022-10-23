/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.*;
import com.laxcus.access.casket.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.trust.*;
import com.laxcus.data.slider.*;
import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;

/**
 * INSERT代理转发调用器。<br>
 * INSERT代理命令是主DATA站点的调用器，通过FromTrustor发出和使用，不需要做锁定操作。
 * 
 * @author scott.liang
 * @version 9/15/2017
 * @since laxcus 1.0
 */
public class DataShiftTrustInsertInvoker extends DataShiftTrustAccessInvoker {

	/**
	 * 构造INSERT代理转发调用器，指定命令
	 * @param cmd INSERT代理转发命令
	 */
	public DataShiftTrustInsertInvoker(ShiftTrustInsert cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTrustInsert getCommand() {
		return (ShiftTrustInsert) super.getCommand();
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

		// 取出INSERT命令
		ShiftTrustInsert shift = getCommand();
		Insert cmd = shift.getCommand().getInsert();
		// 向JNI写入INSERT数据，输出写入原语
		byte[] result = AccessTrustor.insert(cmd);

		// 如果写入失败，撤销已经写入数据
		if(!isSuccessfulStack(result)) {
			leave(result);
			return useful(false);
		}

		// 备份到从站点
		int value = backup(result);
		// 判断成功
		success = (value > 0);

		Logger.debug(this, "launch", success, "insert %s", cmd.getSpace());

		// 如果成功，把INSERT命令写入码位统计池
		if (success) {
			boolean into = DataSliderPool.getInstance().insert(cmd);
			Logger.debug(this, "launch", into, "write '%s' scale pool", cmd.getSpace());
		}
		// 设置行数或者错误码
		shift.getHook().setResult(new Integer(value));

		// 退出
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
	 * 在插入成功之后，把数据块备份到从站点
	 * @param result 从JNI返回的原始数据
	 * @return 返回行数或者错误码
	 */
	private int backup(byte[] result) {
		ClassReader reader = new ClassReader(result);
		int count = 0; // 成功统计
		int items = 0; // 单元统计
		// 行数
		int rows = 0;

		// 循环读取参数，任何一个出错，即退出
		while(reader.hasLeft()) {
			AccessStack stack = new AccessStack(reader);
			items++; // 单元统计

			// 如果发生错误，忽略它
			if (stack.isFault()) {
				Logger.error(this, "insert", "insert failed, code:%d", stack.getFault());
				continue;
			}

			// 统计行数
			rows += stack.getRows();

			Space space = stack.getSpace();
			// 如果写满，删除从站点的CACHE数据块，将CHUNK数据块备份过去
			boolean success = false;
			if (stack.isInsertFull()) {
				success = doReplaceCache(space, stack.getStub());
			} else {
				// 如果没有写满，取出CACHE映像数据，备份到从站点。
				success = doBackupCache(stack);
			}
			if(success) count++;
		}

		// 判断成功
		boolean success = (count == items);
		return (success ? rows : FaultCode.REMOTE_BACKUP_FAILED);
	}

	/**
	 * 撤销INSERT数据
	 * @param result
	 * @return 撤销成功返回真，否则假
	 */
	private boolean leave(byte[] result) {
		ClassReader reader = new ClassReader(result);
		int count = 0; // 撤销成功数目
		int items = 0; // 成功单元数目

		while (reader.hasLeft()) {
			AccessStack stack = new AccessStack(reader);
			// 发生错误的忽略，只找到写入成功的数据做撤销
			if (stack.isFault()) {
				continue;
			}
			// 统计成功单元
			items++;

			// 删除写入的数据
			long stub = stack.getStub();
			Space space = stack.getSpace();
			// 映像数据
			byte[] reflex = stack.getReflex();

			// 调用JNI.leave函数，撤销已经成功写入的数据（即删除数据），返回Leave堆栈数据
			byte[] primitive = AccessTrustor.leave(space, stub, reflex);

			// 删除数据保存到堆栈和解析
			AccessStack sta = new AccessStack(primitive);
			// 回滚不成功，是严重错误！！！（这一块的处理现在仍然没有确定！！！）
			boolean success = sta.isSuccessful();

			Logger.debug(this, "leave", success, "cannot be leave! %s - %x, fault code:%d",
					space, stub, sta.getFault());
			// 撤销成功，统计它
			if(success) {
				count++;
			}
		}
		// 判断成功
		boolean success = (count == items);

		int value = (success ? FaultCode.INSERT_FAILED : FaultCode.INSERT_LEAVE_FAILED);
		ShiftTrustInsert shift = getCommand();
		shift.getHook().setResult(new Integer(value));

		return success;
	}

}