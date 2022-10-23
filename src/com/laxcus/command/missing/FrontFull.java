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
 * FRONT用户虚拟空间耗尽。<br>
 * 涉及用户登录使用数据的节点，包括：GATE/CALL/DATA/WORK/BUILD。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class FrontFull extends Command {

	private static final long serialVersionUID = 521601515040181311L;

	/** 用户所在的站点地址 **/
	private Node site;

	/** 最大规定数 **/
	private int maxPersons;

	/** 实际用户数 **/
	private int realPersons;

	/**
	 * 构造默认和私有的FRONT用户虚拟空间耗尽命令
	 */
	public FrontFull() {
		super();
	}

	/**
	 * 构造FRONT用户虚拟空间不足命令
	 * @param maxPersons 最大用户数
	 * @param realPersons 当前是用户数
	 */
	public FrontFull(int maxPersons, int realPersons) {
		this();
		setMaxPersons(maxPersons);
		setRealPersons(realPersons);
	}
	
	/**
	 * 生成FRONT用户虚拟空间耗尽命令的数据副本
	 * @param that FrontFull实例
	 */
	private FrontFull(FrontFull that) {
		super(that);
		site = that.site;
		maxPersons = that.maxPersons;
		realPersons = that.realPersons;
	}

	/**
	 * 构造FRONT用户虚拟空间耗尽，指定站点地址和数据表名
	 * @param site 站点地址
	 */
	public FrontFull(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中解析FRONT用户虚拟空间耗尽命令
	 * @param reader 可类化数据读取器
	 */
	public FrontFull(ClassReader reader) {
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

	/**
	 * 设置可以支持的最多用户数目。<br>
	 * 
	 * @param more 用户数目
	 */
	public void setMaxPersons(int more) {
		maxPersons = more;
	}

	/**
	 * 返回可以支持的最多用户数目
	 * @return 整数
	 */
	public int getMaxPersons() {
		return maxPersons;
	}

	/**
	 * 设置当前用户数目。<br>
	 * 
	 * @param more 用户数目
	 */
	public void setRealPersons(int more) {
		realPersons = more;
	}

	/**
	 * 返回可以支持的最多用户数目
	 * @return 整数
	 */
	public int getRealPersons() {
		return realPersons;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FrontFull duplicate() {
		return new FrontFull(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		if (site != null) {
			return String.format("%s, %d#%d", site, maxPersons, realPersons);
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
		writer.writeInt(maxPersons);
		writer.writeInt(realPersons);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = reader.readInstance(Node.class);
		maxPersons = reader.readInt();
		realPersons = reader.readInt();
	}

}