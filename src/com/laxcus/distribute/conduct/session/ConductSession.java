/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct.session;

import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.distribute.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布计算会话<br>
 * 
 * 根据阶段操作需求，包括三种类型：FROM/TO/SUBTO。(SUBTO包含在TO中）
 * 
 * @author scott.liang
 * @version 1.2 7/17/2015
 * @since laxcus 1.0
 */
public abstract class ConductSession extends StepSession {

	private static final long serialVersionUID = 7448635781417917821L;

	/**
	 * 会话对象的迭代编号，默认是-1，无迭代。正常迭代编号从0开始，依次增1。
	 * 对应SessionObject.iterateIndex
	 */
	private int iterateIndex;

	/** FROM/TO会话默认默认命令。通常会有一个，也可以没有，如果有多个命令就保存到自定义参数列表（由用户决定） */
	private Command command;

	/** 索引分区。在INIT/BALANCE阶段产生，是FROM/TO阶段分割数据的依据 **/
	private ColumnSector sector;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeSession#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀写入
		super.buildSuffix(writer);
		// 迭代编号
		writer.writeInt(iterateIndex);
		// 命令
		writer.writeDefault(command);
		// 索引分区
		writer.writeDefault(sector);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeSession#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
		// 迭代编号
		iterateIndex = reader.readInt();
		// 命令实例
		command = (Command) reader.readDefault();
		// 索引分区
		sector = (ColumnSector) reader.readDefault();
	}

	/**
	 * 构造一个任务会话实例，同时指定它的会话属性
	 * @param family 会话属性
	 */
	protected ConductSession(int family) {
		super(family);
		// 默认无迭代编号，是-1。
		setIterateIndex(-1);
	}

	/**
	 * 根据传入的ConductSession实例，构造它的副本
	 * @param that ConductSession实例
	 */
	protected ConductSession(ConductSession that) {
		super(that);
		// 设置会话迭代序号
		iterateIndex = that.iterateIndex;
		// 命令
		command = that.command;
		// 索引分区
		sector = that.sector;
	}

	/**
	 * 设置当前会话的迭代编号
	 * @param i 迭代编号
	 */
	public void setIterateIndex(int i) {
		iterateIndex = i;
	}

	/**
	 * 返回当前会话的迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 通过迭代编号判断是子阶段会话((SUBTO，大于0即子级)
	 * @return 返回真或者假
	 */
	public boolean isSubSession() {
		return getIterateIndex() > 0;
	}

	/**
	 * 设置命令
	 * @param e Command实例
	 */
	public void setCommand(Command e) {
		command = e;
	}

	/**
	 * 返回命令
	 * @return Command实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 判断是某个指定命令
	 * @param clazz 类类型
	 * @return 返回或者假
	 */
	public final boolean isCommand(Class<?> clazz) {
		// 判断命令有效
		if (command != null) {
			return Laxkit.isClassFrom(command, clazz);
		}
		return false;
	}

	/**
	 * 设置索引分区。在INIT/BALANCE阶段设置，在FROM/TO阶段结合实际数据使用
	 * @param e IndexSector实例
	 */
	public void setIndexSector(ColumnSector e) {
		sector = e;
	}

	/**
	 * 返回索引分区
	 * @return IndexSector实例
	 */
	public ColumnSector getIndexSector() {
		return sector;
	}

	/**
	 * 返回索引分区的列空间
	 * 
	 * @return 返回Dock实例或者空指针
	 */
	public Dock getIndexDock() {
		if (sector != null) {
			return sector.getDock();
		}
		return null;
	}

	/**
	 * 任务处理完成后，是否需要再分区。<br>
	 * FROM/TO阶段任务完成前，调用此方法，判断是否需要为后续TO阶段做数据分片。<br>
	 * 分片后，数据将以元信息(FluxArea)返回，否则就是实际数据。
	 * @return 返回真或者假
	 */
	public boolean hasIndexSector() {
		return sector != null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		// 给命令设置签名
		if (command != null) {
			command.setIssuer(username);
		}
	}

}