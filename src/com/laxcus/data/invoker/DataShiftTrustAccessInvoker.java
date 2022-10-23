/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.access.casket.*;
import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * JNI存取命令代理转发调用器
 * 
 * @author scott.liang
 * @version 9/15/2017
 * @since laxcus 1.0
 */
public abstract class DataShiftTrustAccessInvoker extends DataSerialWriteInvoker {

	/**
	 * JNI存取命令代理转发调用器，指定转发命令
	 * @param cmd 转发命令
	 */
	protected DataShiftTrustAccessInvoker(ShiftCommand cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCommand getCommand() {
		return (ShiftCommand) super.getCommand();
	}

	/**
	 * 根据错误码，产生一个错误的数据流
	 * @param error 错误码
	 * @return 字节数组
	 */
	protected byte[] faulty(int error) {
		// 生成AccessStack的错误标识
		ClassWriter writer = new ClassWriter();
		writer.write(AccessState.FAULT);
		writer.writeInt(error); // 通用错误（或者换成一个专用错误！！！）
		// 输出
		return writer.effuse();
	}

	/**
	 * 解析字节数组，判断INSERT/DELETE处理全部成功
	 * @param b 字节数组
	 * @return 完全成功返回真，否则假
	 */
	protected boolean isSuccessfulStack(byte[] b) {
		// 解析返回结果，判断成功
		ClassReader reader = new ClassReader(b);
		int items = 0;
		int count = 0;
		// 循环
		while (reader.hasLeft()) {
			AccessStack result = new AccessStack(reader);
			items++;
			// 成功，统计值增1
			if (result.isSuccessful()) count++;
		}

		// 判断成功
		boolean success = (items > 0 && items == count);

		return success;
	}

	/**
	 * 执行存取操作前的检查工作，包括：
	 * 1. 判断当前DATA站点级别，只允许DATA主站点
	 * @return 检查通过返回真，否则假
	 */
	protected boolean check() {
		// 向命令钩子输出一个错误
		ShiftCommand shift = getCommand();
		CommandHook hook = shift.getHook();
		// 判断是主站点
		boolean success = isMaster();
		// 如果出错，返回权限不足故障
		if (!success) {
			hook.setResult(new Integer(FaultCode.PERMISSION_FAILED));
			return false;
		}

		// 以上成功
		return true;
	}
}