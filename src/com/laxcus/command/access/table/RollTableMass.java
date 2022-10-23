/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.stub.sign.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 修复表数据块。<br>
 * 这个命令是由CALL站点发出，目标是DATA从站点。要求从站点从主站点下载。
 * 
 * @author scott.liang
 * @version 1.0 4/12/2017
 * @since laxcus 1.0
 */
public class RollTableMass extends Command {

	private static final long serialVersionUID = 8330284129250040713L;

	/** 回滚表 **/
	private RollTable table;

	/**
	 * 构造默认的修复表数据块
	 */
	private RollTableMass() {
		super();
	}

	/**
	 * 生成修复表数据块的数据副本
	 * @param that RollTableMass实例
	 */
	private RollTableMass(RollTableMass that) {
		super(that);
		table = that.table;
	}

	/**
	 * 构造修复表数据块，指定回滚表
	 * @param table 回滚表
	 */
	public RollTableMass(RollTable table) {
		this();
		setTable(table);
	}

	/**
	 * 从可类化数据读取中解析修复表数据块
	 * @param reader 可类化数据读取器
	 */
	public RollTableMass(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置回滚表，不允许空值
	 * @param e RollTable实例
	 */
	public void setTable(RollTable e) {
		Laxkit.nullabled(e);

		table = e;
	}

	/**
	 * 返回回滚表
	 * @return RollTable实例
	 */
	public RollTable getTable() {
		return table;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RollTableMass duplicate() {
		return new RollTableMass(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(table);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		table = reader.readInstance(RollTable.class);
	}

}
