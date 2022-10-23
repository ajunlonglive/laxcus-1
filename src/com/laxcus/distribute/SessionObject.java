/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 阶段会话对象。<br>
 * 
 * 阶段会话对象相对于动态的“分布任务会话（DistributeSession）”，它是“静态”对象。
 * 由用户/驱动程序在前端站点（FRONT SITE）输入和产生，被投递到目标站点上。
 * 配置在各站点上的分布任务组件根据阶段会话对象产生“分布任务会话”，然后交给指定的站点处理。<br><br>
 * 
 * 阶段会话对象包括CONDUCT命令的FROM/TO会话对象，和ESTABLISH命令的SCAN/SIFT/RISE会话对象。 <br>
 * 
 * @author scott.liang
 * @version 1.1 12/7/2011
 * @since laxcus 1.0
 */
public abstract class SessionObject extends AccessObject {

	private static final long serialVersionUID = 4854847981377680406L;

	/** 会话执行阶段，包括CONDUCT语句的FROM/TO，和ESTABLISH语句的SCAN/SIFT/RISE。**/
	private int family;
	
	/** 会话对象的迭代编号，默认是-1，无迭代。正式的迭代编号从0开始，依次递增1。迭代编号在分布任务初始化时定义，以后不再改变。 **/
	private int iterateIndex;

	/**
	 * 构造会话对象，并且指定执行阶段
	 * @param family 会话执行阶段，包括CONDUCT语句的FROM/TO，和ESTABLISH语句的SCAN/SIFT/RISE
	 */
	protected SessionObject(int family) {
		super();
		setFamily(family);		
		// 默认是-1，无迭代。根迭代编号从0开始，在INIT初始化时设置
		setIterateIndex(-1);
	}

	/**
	 * 根据传入的会话执行阶段，生成它的副本
	 * @param that SessionObject实例
	 */
	protected SessionObject(SessionObject that) {
		super(that);
		setFamily(that.family);
		setIterateIndex(that.iterateIndex);
	}

	/**
	 * 设置执行阶段。必须是CONDUCT/ESTABLISH的会话属性
	 * @param who 执行阶段
	 */
	private void setFamily(int who) {
		if (!PhaseTag.isSession(who)) {
			throw new IllegalPhaseException("illegal session family %d", who);
		}
		family = who;
	}

	/**
	 * 返回执行阶段
	 * @return 执行阶段
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断是否CONDUCT.FROM阶段会话对象
	 * @return 返回真或者假
	 */
	public boolean isFrom() {
		return PhaseTag.isFrom(family);
	}

	/**
	 * 判断是否CONDUCT.TO阶段会话对象
	 * @return 返回真或者假
	 */
	public boolean isTo() {
		return PhaseTag.isTo(family);
	}

	/**
	 * 判断是否ESTABLISH.SCAN阶段会话对象
	 * @return 返回真或者假
	 */
	public boolean isScan() {
		return PhaseTag.isScan(family);
	}

	/**
	 * 判断是否ESTABLISH.SIFT阶段会话对象
	 * @return 返回真或者假
	 */
	public boolean isSift() {
		return PhaseTag.isSift(family);
	}

	/**
	 * 设置当前对象的迭代编号
	 * @param index 迭代编号
	 */
	protected void setIterateIndex(int index) {
		iterateIndex = index;
	}

	/**
	 * 返回当前对象的迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 判断是子级对象 <br>
	 * 判断子级对象的依据是迭代编号，根迭代编号是0，大于0是子级对象。
	 * @return 返回真或者假
	 */
	public boolean isSubObject() {
		return getIterateIndex() > 0;
	}

	/*
	 * 将当前类的数据信息写入可类化写入器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 1.前缀
		super.buildSuffix(writer);
		// 2.会话执行阶段
		writer.writeInt(family);
		// 3.迭代编号
		writer.writeInt(iterateIndex);
	}

	/*
	 * 从可类化读取器中解析当前类的数据信息
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. 前缀
		super.resolveSuffix(reader);
		// 2.会话执行阶段
		setFamily(reader.readInt());
		// 3. 迭代编号
		iterateIndex = reader.readInt();
	}
}