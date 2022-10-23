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
 * 授权删表命令。<br>
 * 此命令是强制目标站点删除一个表。
 * 
 * @author scott.liang
 * @version 1.1 11/09/2015
 * @since laxcus 1.0
 */
public class AwardDropTable extends Command {
	
	private static final long serialVersionUID = 8847565993781574593L;
	
	/** 数据表名 **/
	private Space space;
	
	/**
	 * 构造默认的授权删表命令
	 */
	private AwardDropTable() {
		super();
	}
	
	/**
	 * 根据传入的授权删表命令，生成它的数据副本
	 * @param that AwardDropTable实例
	 */
	private AwardDropTable(AwardDropTable that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造授权删表命令，指定数据表名
	 * @param space 数据表名
	 */
	public AwardDropTable(Space space) {
		this();
		setSpace(space);
	}
	
	/**
	 * 从可类化数据读取器中解析授权删表命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AwardDropTable(ClassReader reader) {
		this();
		resolve(reader);
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

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardDropTable duplicate() {
		return new AwardDropTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

}