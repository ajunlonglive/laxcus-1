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
 * INSERT前导命令。<br><br>
 * 
 * 这个命令是客户机和服务器之间执行数据写入操作的许可判断，用于INSERT数据传输前。<br><br>
 * 
 * 数据写入处理流程：<br>
 * 1. 客户端向服务器传输“INSERT”前，发送一个前导命令。<br>
 * 2. 服务器返回“受理/不受理”的布值。<br>
 * 3. 如果受理成功，返回一个新的前导命令（InsertHint），否则是错误。<br>
 * 4. 客户端根据服务端的前导命令中的回显地址，向服务器发送数据。<br>
 * 5. 发送完成，客户端异步等待服务器返回写入记录数（大于0的正整数），负数是失败。数据写入处理完成。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/25/2015
 * @since laxcus 1.0
 */
public final class InsertGuide extends Command {

	private static final long serialVersionUID = -2387100668659818608L;

	/** 数据表名 **/
	private Space space;
	
	/** 磁盘数据容量 **/
	private long capacity;

	/**
	 * 构造默认的INSERT前导命令
	 */
	private InsertGuide() {
		super();
		capacity = 0;
	}

	/**
	 * 根据传入的INSERT前导命令，生成它的数据副本
	 * @param that InsertHint实例
	 */
	private InsertGuide(InsertGuide that) {
		super(that);
		space = that.space;
		capacity = that.capacity;
	}

	/**
	 * 构造INSERT前导命令，指定数据表名
	 * @param space 数据表名
	 */
	public InsertGuide(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 构造INSERT前导命令，指定全部参数
	 * @param space 数据表名
	 * @param capacity INSERT数据的字节长度
	 */
	public InsertGuide(Space space, long capacity) {
		this(space);
		setCapacity(capacity);
	}
	
	/**
	 * 从可类化数据读取器中解析INSERT前导命令。
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public InsertGuide(ClassReader reader) {
		this();
		resolve(reader);
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
	 * 设置磁盘数据容量
	 * @param i 磁盘数据容量
	 */
	public void setCapacity(long i) {
		capacity = i;
	}

	/**
	 * 返回磁盘数据容量
	 * @return 磁盘数据容量
	 */
	public long getCapacity() {
		return capacity;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%d", space, capacity);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public InsertGuide duplicate() {
		return new InsertGuide(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写数据表名
		writer.writeObject(space);
		// 磁盘数据容量
		writer.writeLong(capacity);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 读数据表名
		space = new Space(reader);
		// 磁盘数据容量
		capacity = reader.readLong();
	}

}