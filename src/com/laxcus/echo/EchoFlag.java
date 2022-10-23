/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import java.io.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 回显标识。它是异步应答标识，配合回显地址使用。<br><br>
 * 
 * 回显标识用于异步应答的数据处理的一系列阶段，区分请求端发出的每一个子任务。<br><br>
 * 
 * 回显标识由请求端生成，放在命令（Command）中，发送至服务端。
 * 服务端将任务处理完成后，回显标识随应答数据包返回到异步数据代理（EchoAgent）。
 * 异步数据代理根据回显标识，找到请求端子任务（EchoReceiver的实现类：DoubleClient or EchoBuffer），
 * 将数据交给它完成具体的处理。<br><br>
 * 
 * 回显标识参数：<br>
 * 1. 调用器编号。<br>
 * 2. 索引号（子任务编号，或者是回显缓存编号）。<br><br>
 * 
 * 回显标识的生成规则：<br>
 * 1. 调用器编号由运行站点产生。<br>
 * 2. 如果是回显缓存（EchoBuffer），索引号在预分配缓存后产生，从0开始，逐次加1，与预分配的缓存数保持一致，即：预分配缓存数-1。<br>
 * 3. 如果是DoubleClient，因为只能是一个请求，索引号是0。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class EchoFlag implements Classable, Cloneable, Serializable, Comparable<EchoFlag> {

	private static final long serialVersionUID = -2063990422748904084L;

	/** 任务请求方分配的调用器编号，在0 - Long.MAX_SIZE之间。默认是-1，表示无定义。**/
	private long invokerId;

	/** 子任务在集合中的序列编号。下标从0开始，默认是-1，表示无定义。 **/
	private int index;

	/**
	 * 将回显标识参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 调用器编号
		writer.writeLong(invokerId);
		// 会话序列编号
		writer.writeInt(index);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析回显标识参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.0
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 调用器编号
		invokerId = reader.readLong();
		// 会话序列编号
		index = reader.readInt();
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认和私有的回显标识
	 */
	private EchoFlag() {
		super();
		invokerId = InvokerIdentity.INVALID;
		index = -1;
	}

	/**
	 * 根据传入的回显标识，生成它的数据副本
	 * @param that 回显标识
	 */
	private EchoFlag(EchoFlag that) {
		super();
		invokerId = that.invokerId;
		index = that.index;
	}

	/**
	 * 构造回显标识，指定调用器编号和索引编号
	 * @param invokerId 调用器编号
	 * @param index 索引编号
	 */
	public EchoFlag(long invokerId, int index) {
		this();
		setInvokerId(invokerId);
		setIndex(index);
	}

	/**
	 * 从可类化读取器中解析回显标识参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public EchoFlag(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置调用器编号。小于0时是错误
	 * @param id 调用器编号
	 */
	public void setInvokerId(long id) {
		if (InvokerIdentity.isInvalid(id)) {
			throw new IllegalValueException("illegal invoker id: %d", id);
		}
		invokerId = id;
	}

	/**
	 * 返回调用器编号
	 * @return 调用器编号
	 */
	public long getInvokerId() {
		return invokerId;
	}

	/**
	 * 设置会话序列编号
	 * @param i 会话序列编号
	 */
	public void setIndex(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal index %d", i);
		}
		index = i;
	}

	/**
	 * 返回会话序列编号
	 * @return 会话序列编号
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 判断无效索引（小于0无效）
	 * @return 返回真或者假
	 */
	public boolean isInvalidIndex() {
		return index < 0;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return EchoFlag实例
	 */
	public EchoFlag duplicate() {
		return new EchoFlag(this);
	}

	/**
	 * 比较两个命令源标识是否一致。
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != EchoFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EchoFlag) that) == 0;
	}

	/**
	 * 返回命令源标识散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (invokerId ^ index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回命令源标识的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d_%d", invokerId, index);
	}

	/**
	 * 比较两个回显标识的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EchoFlag that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(invokerId, that.invokerId);
		if (ret == 0) {
			ret = Laxkit.compareTo(index, that.index);
		}
		return ret;
	}

	/**
	 * 生成一个私有状态的回显标识，只需要指定调用器编号，不指定索引（索引值是-1）
	 * @param invokerId 调用器编号
	 * @return 回显标识实例
	 */
	public static EchoFlag doPrivate(long invokerId) {
		EchoFlag flag = new EchoFlag();
		flag.setInvokerId(invokerId);
		return flag;
	}
}