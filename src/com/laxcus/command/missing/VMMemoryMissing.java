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
 * JVM虚拟机JVM虚拟机内存空间不足。<br><br>
 *
 * @author scott.liang
 * @version 1.0 10/29/2019
 * @since laxcus 1.0
 */
public class VMMemoryMissing extends Command {
	
	private static final long serialVersionUID = 7753398157894830249L;

	/** 内存所在的站点地址 **/
	private Node site;

	/**
	 * 构造默认的JVM虚拟机内存空间不足命令
	 */
	public VMMemoryMissing() {
		super();
	}

	/**
	 * 生成JVM虚拟机内存空间不足命令的数据副本
	 * @param that VMMemoryMissing实例
	 */
	private VMMemoryMissing(VMMemoryMissing that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造JVM虚拟机内存空间不足，指定站点地址和数据表名
	 * @param site 站点地址
	 */
	public VMMemoryMissing(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中解析JVM虚拟机内存空间不足命令
	 * @param reader 可类化数据读取器
	 */
	public VMMemoryMissing(ClassReader reader) {
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
	public VMMemoryMissing duplicate() {
		return new VMMemoryMissing(this);
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