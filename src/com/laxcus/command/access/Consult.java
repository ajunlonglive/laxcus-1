/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布数据处理协商命令。<br><br>
 * 
 * 根据集群的多节点数据处理特点，通过三段协商处理机制，针对数据的写入、删除、更新操作，在数据处理节点和数据请求端节点进行。<br><br>
 * 
 * 流程：<br>
 * 1. 数据处理节点提交自己的数据处理结果（多点提交）。<br>
 * 2. 数据请求节点汇总（一个节点全部接收后，会出确认/取消两种动作）。<br>
 * 3. 数据处理节点收到后，做出确认反馈，或者取消动作（取消动作又会为取消成功和取消失败两种可能）。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 5/25/2015
 * @since laxcus 1.0
 */
public abstract class Consult extends Command {

	private static final long serialVersionUID = 6084627081689866599L;

	/** 表名 **/
	private Space space;

	/** 协商状态码 **/
	private byte status;

	/**
	 * 构造默认的协商命令
	 */
	protected Consult() {
		super();
		status = 0;
	}

	/**
	 * 根据传入的协商命令，生成它的数据副本
	 * @param that Consult实例
	 */
	protected Consult(Consult that) {
		super(that);
		space = that.space;
		status = that.status;
	}

	/**
	 * 设置数据表名，不允许空值。
	 * @param e Space实例
	 * @throws NullPointerException
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置协商状态，见ConsultStatus定义
	 * @param who 协商状态
	 */
	public void setStatus(byte who) {
		if (!ConsultStatus.isConsult(who)) {
			throw new IllegalValueException("illegal code %d", who);
		}
		status = who;
	}

	/**
	 * 返回协商状态
	 * @return 协商状态的字节描述
	 */
	public final byte getStatus() {
		return status;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", space, ConsultStatus.translate(status));
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 协商状态
		writer.write(status);
		// 写表名
		writer.writeObject(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 协商状态
		status = reader.read();
		// 读表名
		space = new Space(reader);
	}

}