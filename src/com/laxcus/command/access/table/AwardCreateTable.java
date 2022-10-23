/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 授权建表命令。<br>
 * 此命令是强制目标站点接受一个表。
 * 
 * @author scott.liang
 * @version 1.1 11/09/2015
 * @since laxcus 1.0
 */
public class AwardCreateTable extends Command {
	
	private static final long serialVersionUID = 938672534972326674L;

	/** 用户资源引用 **/
	private Refer refer;
	
	/** 数据表 **/
	private Table table;

	/**
	 * 构造默认的授权建表命令
	 */
	private AwardCreateTable() {
		super();
	}

	/**
	 * 根据传入的授权建表命令实例，生成它的数据副本
	 * @param that AwardCreateTable实例
	 */
	private AwardCreateTable(AwardCreateTable that) {
		super(that);
		refer = that.refer;
		table = that.table;
	}

	/**
	 * 构造授权建表命令，指定全部参数
	 * @param refer 用户资源引用
	 * @param table 数据表
	 */
	public AwardCreateTable(Refer refer, Table table) {
		this();
		setRefer(refer);
		setTable(table);
	}

	/**
	 * 从可类化数据读取器中解析授权建表命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AwardCreateTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回用户资源引用
	 * @return Refer实例
	 */
	public Refer getRefer() {
		return refer;
	}

	/**
	 * 设置用户资源引用，允许空值
	 * @param e Refer实例
	 */
	public void setRefer(Refer e) {
		refer = e;
	}

	/**
	 * 返回数据表
	 * @return Table实例
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 设置数据表，不允许空值
	 * @param e Table实例
	 */
	public void setTable(Table e) {
		Laxkit.nullabled(e);
		table = e;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardCreateTable duplicate() {
		return new AwardCreateTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(refer);
		writer.writeObject(table);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		refer = reader.readInstance(Refer.class);
		table = new Table(reader);
	}

}