/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 传输数据块命令 <br>
 * 
 * 将一个节点上的数据块信息，传输到另一个节点上。
 * 被传输的数据块包括CHUNK/CACHE两种，如果是CACHE数据块，将标明它为缓存映像块（CACHE REFLEX）。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public abstract class TransferMass extends Command {

	private static final long serialVersionUID = 5367965697748607335L;

	/** 数据块标识 **/
	private StubFlag flag;

	/** 缓存映像数据块 **/
	private boolean cacheReflex;

	/**
	 * 构造默认和私有的传输数据块命令
	 */
	protected TransferMass() {
		super();
		cacheReflex = false;
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that TransferMass实例
	 */
	protected TransferMass(TransferMass that) {
		super(that);
		flag = that.flag;
		cacheReflex = that.cacheReflex;
	}

	/**
	 * 设置数据块标识
	 * @param e StubFlag实例
	 */
	public void setFlag(StubFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}

	/**
	 * 设置数据块标识
	 * @param space 数据表名
	 * @param stub 数据块编号
	 */
	public void setFlag(Space space, long stub) {
		flag = new StubFlag(space, stub);
	}

	/**
	 * 返回数据块标识
	 * @return StubFlag实例
	 */
	public StubFlag getFlag() {
		return flag;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return flag.getSpace();
	}

	/**
	 * 返回数据块编号
	 * @return long类型
	 */
	public long getStub() {
		return flag.getStub();
	}

	/**
	 * 判断是缓存映像数据块
	 * @return 返回真或者假
	 */
	public boolean isCacheReflex() {
		return cacheReflex;
	}

	/**
	 * 设置数据块为缓存映像数据，或者否。
	 * 这个参数由DATA主站点设置
	 * 
	 * @param b 属性标识
	 */
	public void setCacheReflex(boolean b) {
		cacheReflex = b;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(flag);
		writer.writeBoolean(cacheReflex);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		flag = new StubFlag(reader);
		cacheReflex = reader.readBoolean();
	}

}