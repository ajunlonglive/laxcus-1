/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.missing;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 内存空间不足。<br><br>
 * 
 * <b>注意！这是计算机整体环境内存不足，不是虚拟机内存不足！！！</b>
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class MemoryMissing extends Command {
	
	private static final long serialVersionUID = 7753398157894830249L;

	/** 内存所在的站点地址 **/
	private Node site;

	/**
	 * 构造默认和私有的内存空间不足命令
	 */
	public MemoryMissing() {
		super();
	}

	/**
	 * 生成内存空间不足命令的数据副本
	 * @param that MemoryMissing实例
	 */
	private MemoryMissing(MemoryMissing that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造内存空间不足，指定站点地址和数据表名
	 * @param site 站点地址
	 */
	public MemoryMissing(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中解析内存空间不足命令
	 * @param reader 可类化数据读取器
	 */
	public MemoryMissing(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MemoryMissing duplicate() {
		return new MemoryMissing(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		if (site != null) {
			return String.format("%s memory missing", site);
		}
		// 调用上层
		return super.toString();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = reader.readInstance(Node.class);
	}

}