/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 授权删除数据库命令。<br>
 * 此命令是强制目标站点删除一个库下面的全部表。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2018
 * @since laxcus 1.0
 */
public class AwardDropSchema extends Command {
	
	private static final long serialVersionUID = -3587394569246427216L;

	/** 数据库名 **/
	private Fame fame;
	
	/**
	 * 构造默认的授权删除数据库命令
	 */
	private AwardDropSchema() {
		super();
	}
	
	/**
	 * 根据传入的授权删除数据库命令，生成它的数据副本
	 * @param that AwardDropSchema实例
	 */
	private AwardDropSchema(AwardDropSchema that) {
		super(that);
		fame = that.fame;
	}

	/**
	 * 构造授权删除数据库命令，指定数据库名
	 * @param fame 数据库名
	 */
	public AwardDropSchema(Fame fame) {
		this();
		setFame(fame);
	}
	
	/**
	 * 从可类化数据读取器中解析授权删除数据库命令
	 * @param reader 可类化数据读取器
	 */
	public AwardDropSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据库名
	 * @param e Fame实例
	 */
	public void setFame(Fame e) {
		Laxkit.nullabled(e);

		fame = e;
	}
	
	/**
	 * 返回数据库名
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardDropSchema duplicate() {
		return new AwardDropSchema(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(fame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fame = new Fame(reader);
	}

}