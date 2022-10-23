/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;

import com.laxcus.command.cyber.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;

/**
 * 检测MASSIVE MIMO调用器
 * 
 * @author scott.liang
 * @version 1.0 2/22/2022
 * @since laxcus 1.0
 */
public class CallCheckMassiveMimoInvoker extends CallInvoker {

	/**
	 * 构造检测MASSIVE MIMO调用器
	 * @param cmd
	 */
	public CallCheckMassiveMimoInvoker(CheckMassiveMimo cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckMassiveMimo getCommand() {
		return (CheckMassiveMimo) super.getCommand();
	}
	
	/**
	 * 返回当前MI接收器数目
	 * @return 数字
	 */
	private int getMISuckers() {
		return getLauncher().getMIMembers();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckMassiveMimo cmd = getCommand();
		// FRONT调用器监听地址
		Cabin source = cmd.getSource(); 
		
		Logger.debug(this, "launch", "cabin is [%s]", source);

		// 判断允许或者否
		int suckers = getMISuckers();
		boolean success = (suckers > 0);

		// 反馈给FRONT节点
		CheckMassiveMimoProduct product = new CheckMassiveMimoProduct(suckers);
		product.setSuccessful(success);// 大于0是成功

		// 如果不允许，返回一个错误
		if (!success) {
			replyProduct(source, product);
			return false;
		}

		// 强制设置为磁盘存储
		boolean ondisk = isDisk();
		setDisk(true);
		// 反馈结果
		success = replyTo(source, product);
		// 恢复原样
		setDisk(ondisk);

		// 以上不成功，向FRONT返回错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
		}
		
		Logger.debug(this, "launch", success, "reply to %s", source);

		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 如果接收FRONT站点数据失败，退出
		if (isFaultCompleted()) {
			super.replyFault();
			return false;
		}

		
		// 承接FIRST，第一个且只有一个索引编号
		int index = findEchoKey(0);
		// 统一用findItemCabin拿到FRONT的监听地址，（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
		Cabin cabin = findItemCabin(index);
		
		Logger.debug(this, "ending", "front hub: %s", cabin);
		
		// 选择是文件或者内存
		File file = null;
		byte[] memory = null;
		// 取出异步缓存
		EchoBuffer buff = findBuffer(index);
		if (buff.isDisk()) {
			file = buff.getFile();
			Logger.debug(this, "ending", "file is '%s', length: %d", file, file.length());
		} else if(buff.isMemory()){
			memory = buff.getMemory();
			Logger.debug(this, "ending", "memory length: %d", (memory != null ? memory.length : -1));
		}

		// 任何一个存在即有效
		boolean success = (file != null || memory !=null);
		
		// MI接收器数量
		int suckers = getMISuckers();

		CheckMassiveMimoProduct product = new CheckMassiveMimoProduct(suckers);
		product.setSuccessful(success);

		// 通知FRONT站点反馈结果
		success = replyProduct(cabin, product);

		Logger.debug(this, "ending", success, "reply to %s", cabin);

		// 结果
		return useful(success);
	}
	

}