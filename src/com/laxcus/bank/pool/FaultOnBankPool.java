/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;

/**
 * BANK故障命令管理池。<br><br>
 * 
 * 记录发生故障的命令，并且没有正确释放资源前，锁定这些记录。
 * 
 * @author scott.liang
 * @version 1.2 10/11/2013
 * @since laxcus 1.0
 */
public class FaultOnBankPool extends DiskPool {

	/** 资源管理池句柄 **/
	private static FaultOnBankPool selfHandle = new FaultOnBankPool();
	
	/** 故障命令 **/
	private Class<?>[] faults = new Class<?>[] { CreateUser.class, DropUser.class, DropSchema.class,
		CreateTable.class, DropTable.class	};
	
	/** 故障命令数组 **/
	private ArrayList<Command> array = new ArrayList<Command>();

	/**
	 * 构造BANK故障命令管理池
	 */
	private FaultOnBankPool() {
		super();
		setSleepTime(20);
	}

	/**
	 * 返回BANK故障命令管理池句柄
	 * @return 故障命令管理池实例
	 */
	public static FaultOnBankPool getInstance() {
		return FaultOnBankPool.selfHandle;
	}
	
	/**
	 * 判断是规定的故障命令
	 * @param cmd 命令实例
	 * @return 匹配返回真，否则假
	 */
	private boolean isFaultCommand(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < faults.length; i++) {
			if (cmd.getClass() == faults[i]) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 延时等待退出
		while (!isInterrupted()) {
			check();
			sleep();
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {

	}
	
	/**
	 * 判断签名在故障池中
	 * @param siger 账号签名
	 * @return 返回真或者假
	 */
	public boolean hasAccount(Siger siger) {
		return false;
	}

	/**
	 * 判断有数据库在故障池中
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean hasSchema(Fame fame) {
		return false;
	}

	/**
	 * 判断有数据表在故障池中
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space space) {
		return false;
	}
	
	/**
	 * 检查资源
	 */
	private void check() {
		
	}
	
	/**
	 * 资源执行过程中发生故障 ，记录这些命令
	 * @param cmd 命令实例
	 * @return 如果接受并且保存了这个命令，返回真，否则假。
	 */
	public boolean push(Command cmd) {
		boolean success = isFaultCommand(cmd);
		if (success) {
			success = array.add(cmd);
		}

		Logger.note(this, "push", success, "保存故障命令:%s", cmd.getClass().getName());

		return success;
	}

}