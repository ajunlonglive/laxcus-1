/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建表命令。
 * 
 * @author scott.liang
 * @version 1.0 7/28/2009
 * @since laxcus 1.0
 */
public class CreateTable extends Command {

	private static final long serialVersionUID = -1371833847534450569L;

	/** 数据表 **/
	private Table table;

	/** HOME集群地址 */
	private Domain domain;

	/**
	 * 构造默认和私有的建表命令
	 */
	private CreateTable() {
		super();
	}

	/**
	 * 根据传入的建表命令，生成它的数据副本
	 * @param that CreateTable实例
	 */
	private CreateTable(CreateTable that) {
		super(that);
		table = that.table;
		domain = that.domain;
	}

	/**
	 * 构造建表命令，指定数据表
	 * @param table 数据表
	 */
	public CreateTable(Table table) {
		super();
		setTable(table);
	}

	/**
	 * 构造建表命令，指定全部参数
	 * @param table 数据表
	 * @param domain 集群参数
	 */
	public CreateTable(Table table, Domain domain) {
		this(table);
		setDomain(domain);
	}

	/**
	 * 根据传入的可类化读取器，解析建表参数
	 * @param reader
	 */
	public CreateTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger e) {
		table.setIssuer(e);
		super.setIssuer(e);
	}

	/**
	 * 设置数据表
	 * @param e Table实例
	 */
	public void setTable(Table e) {
		Laxkit.nullabled(e);
		table = e;
	}

	/**
	 * 返回数据表
	 * @return Table实例
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 设置集群
	 * @param e Domain实例
	 */
	public void setDomain(Domain e) {
		domain = e;
	}

	/**
	 * 返回集群
	 * @return Domain实例
	 */
	public Domain getDomain() {
		return domain;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		if (table == null) {
			return null;
		}
		return table.getSpace();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateTable duplicate() {
		return new CreateTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(table);
		writer.writeInstance(domain);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		table = reader.readInstance(Table.class);
		domain = reader.readInstance(Domain.class);
	}
}