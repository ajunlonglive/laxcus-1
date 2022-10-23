/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 获取数据块编号命令。<br><br>
 * 
 * 这个命令由DATA/BUILD站点发出，通过HOME站点转发给TOP站点，TOP站点负责分配数据块编号。
 * 
 * @author scott.liang
 * @version 1.1 05/07/2015
 * @since laxcus 1.0
 */
public final class TakeStub extends Command {

	private static final long serialVersionUID = -5819804911308232936L;

	/** 申请的数据块申请数量 **/
	private int size;
	
	/**
	 * 构造默认和私有的获取数据块编号命令
	 */
	private TakeStub() {
		super();
		size = 10;
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that TakeStub实例
	 */
	private TakeStub(TakeStub that) {
		super(that);
		size = that.size;
	}
	
	/**
	 * 构造获取数据块编号命令，指定要求的申请数量
	 * @param size 申请数量
	 */
	public TakeStub(int size) {
		this();
		setSize(size);
	}

	/**
	 * 从可类化读取器中解析获取数据块编号命令
	 * @param reader - 可类化读取器
	 * @since 1.1
	 */
	public TakeStub(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置申请数量
	 * @param i 申请数量
	 */
	public void setSize(int i) {
		if (i < 1) {
			throw new IllegalArgumentException("cannot be " + i);
		}
		size = i;
	}

	/**
	 * 返回申请数量
	 * @return 申请数量
	 */
	public int getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeStub duplicate() {
		return new TakeStub(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(size);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		size = reader.readInt();
	}

}