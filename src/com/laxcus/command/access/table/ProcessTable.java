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
 * 表处理命令 <br>
 * 
 * 基于单表的处理
 * 
 * @author scott.liang
 * @version 1.0 7/28/2012
 * @since laxcus 1.0
 */
public abstract class ProcessTable extends Command {

	private static final long serialVersionUID = 4189583345377620030L;

	/** 表名 **/
	private Space space;

	/**
	 * 构造表处理命令
	 */
	protected ProcessTable() {
		super();
	}

	/**
	 * 根据传入的对象实例，生成它的数据副本
	 * @param that ProcessTable对象
	 */
	protected ProcessTable(ProcessTable that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造表处理命令，指定数据表名
	 * @param space 数据表名
	 */
	protected ProcessTable(Space space) {
		this();
		setSpace(space);
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
	 * 将被处理的表名写入可类化存储器
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/**
	 * 从可类化读取器中解析被处理的表名
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

}