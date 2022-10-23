/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * DELETE删除报告。<br><br>
 * 
 * 在DELETE删除之后产生。
 * 
 * @author scott.liang
 * @version 1.1 7/17/2015
 * @since laxcus 1.0
 */
public final class DeleteResult extends Command {

	private static final long serialVersionUID = 9132874580814672261L;

	/** 数据表名 **/
	private Space space;

	/** 列数目 **/
	private long rows;

	/** 行数目 **/
	private int columns;

	/**
	 * 根据传入的DELETE删除报告，生成它的数据副本
	 * @param that DeleteResult实例
	 */
	private DeleteResult(DeleteResult that) {
		super(that);
		setSpace(that.space);
		setRows(that.rows);
		setColumns(that.columns);
	}

	/**
	 * 构造DELETE删除报告
	 */
	public DeleteResult() {
		super();
		rows = 0L;
		columns = 0;
	}

	/**
	 * 构造DELETE删除报告，指定数据表名
	 * @param space
	 */
	public DeleteResult(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析DELETE删除报告
	 * @param reader 可类化数据读取器
	 * @since l.1
	 */
	public DeleteResult(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空指针
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置行数
	 * @param i 行数
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回行数
	 * @return 行数
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return rows == 0L;
	}

	/**
	 * 设置列数
	 * @param i 列数
	 */
	public void setColumns(int i) {
		columns = i;
	}

	/**
	 * 返回列数
	 * @return 列数
	 */
	public int getColumns() {
		return columns;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %d %d", space, rows, columns);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#duplicate()
	 */
	@Override
	public DeleteResult duplicate() {
		return new DeleteResult(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(space);
		writer.writeLong(rows);
		writer.writeInt(columns);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = reader.readInstance(Space.class);
		rows = reader.readLong();
		columns = reader.readInt();
	}

}