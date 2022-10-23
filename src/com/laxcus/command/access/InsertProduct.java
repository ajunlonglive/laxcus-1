/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * INSERT处理结果。<br>
 * 这个INSERT处理结果是最终由CALL站点提交给FRONT站点。
 *  
 * @author scott.liang
 * @version 1.3 8/17/2015
 * @since laxcus 1.0
 */
public final class InsertProduct extends EchoProduct {
	
	private static final long serialVersionUID = -362941937586598342L;

	/** 成功或者否  **/
	private boolean successful;

	/** 数据表名 **/
	private Space space;
	
	/** 写入行数 **/
	private long rows;

	/**
	 * 构造私有和默认的INSERT处理结果
	 */
	private InsertProduct() {
		super();
		successful = false;
		rows = 0;
	}

	/**
	 * 根据传入的INSERT处理结果，生成它的数据副本
	 * @param that 传入实例
	 */
	private InsertProduct(InsertProduct that) {
		super(that);
		successful = that.successful;
		space = that.space;
		rows = that.rows;
	}

	/**
	 * 构造INSERT处理结果，指定参数
	 * @param success 成功或者否
	 * @param space 数据表名
	 */
	public InsertProduct(boolean success, Space space) {
		this();
		setSuccessful(success);
		setSpace(space);
	}

	/**
	 * 构造INSERT处理结果，指定参数
	 * @param space 数据表名
	 * @param rows 写入行数
	 */
	public InsertProduct(boolean success, Space space, int rows) {
		this(success, space);
		setRows(rows);
	}

	/**
	 * 从可类化数据读取器中解析INSERT处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.3
	 */
	public InsertProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置成功或者否
	 * @param b 真或者假
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断成功
	 * @return 真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}
	
	
	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

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
	 * 设置写入行数
	 * @param i 写入行数
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回写入行数
	 * @return 写入行数
	 */
	public long getRows() {
		return rows;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%d", space, rows);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public InsertProduct duplicate() {
		return new InsertProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(successful);
		writer.writeObject(space);
		writer.writeLong(rows);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		successful = reader.readBoolean();
		space = new Space(reader);
		rows = reader.readLong();
	}

}