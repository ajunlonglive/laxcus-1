/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import com.laxcus.command.*;
import com.laxcus.util.*;

/**
 * 前端任务处理结果<br><br>
 * 
 * FrontResult只保存原始数据，它有多个子类，这些子类由各种驱动程序调用器构造产生，驱动程序调用器会把处理结果保存到内存或者磁盘。子类或者其它外部接口通过“getInputStream”获得数据的读取流。对原始数据的解析和解释由子类们去实现。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public abstract class MissionResult {
	
	/** 前端任务返回结果类型 **/
	private byte family;

	/** 被投递的命令 **/
	private Command command;

	/** 返回类定义 **/
	private Class<?> thumb;

	/**
	 * 构造默认的前端任务处理结果
	 */
	protected MissionResult(byte family) {
		super();
		setFamily(family);
	}
	
	/**
	 * 设置前端任务返回结果类型
	 * @param who 结果类型
	 */
	private void setFamily(byte who) {
		if (!MissionResultTag.isFamily(who)) {
			throw new IllegalValueException("illegal type:%d", who);
		}
		family = who;
	}

	/**
	 * 返回前端任务返回结果类型
	 * @return 前端任务返回结果类型
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 判断是缓存类型
	 * @return 返回真或者假
	 */
	public boolean isBuffer() {
		return MissionResultTag.isBuffer(family);
	}

	/**
	 * 判断是文件类型
	 * @return 返回真或者假
	 */
	public boolean isFile() {
		return MissionResultTag.isFile(family);
	}

	/**
	 * 判断是报告类型
	 * @return 返回真或者假
	 */
	public boolean isProduct() {
		return MissionResultTag.isProduct(family);
	}

	/**
	 * 判断是对象类型
	 * @return 返回真或者假
	 */
	public boolean isObject() {
		return MissionResultTag.isObject(family);
	}

	/**
	 * 设置命令
	 * @param e Command子类实例
	 */
	public void setCommand(Command e) {
		command = e;
	}

	/**
	 * 返回命令
	 * @return Command子类实例
	 */
	public Command getCommand() {
		return command;
	}
	
	/**
	 * 设置输出类定义。FrontInvoker子类定义，调用FrontInvoker.setThumb实现。
	 * @param e 类定义
	 */
	public void setThumb(java.lang.Class<?> e) {
		thumb = e;
	}

	/**
	 * 返回输出类定义
	 * @return 类定义
	 */
	public java.lang.Class<?> getThumb() {
		return thumb;
	}

	/**
	 * 判断是匹配的输出类定义
	 * @param e 类定义
	 * @return 返回真或者假
	 */
	public boolean isThumb(java.lang.Class<?> e) {
		return thumb != null && thumb == e;
	}

	/**
	 * 返回内部包含的类对象
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public abstract <T> T getObject(Class<T> clazz);

	/**
	 * 销毁处理结果的资源数据 <br>
	 * 特别注意：子类实现自己的“destroy”方法时，必须加入“super.destroy();”，否则上级资源数据不能被释放。<br>
	 * 释放资源数据规则：子类先释放自己的资源，再最后调用“super.destroy();”，释放父类资源数据。<br>
	 */
	protected void destroy() {
		command = null;
		thumb = null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}
}