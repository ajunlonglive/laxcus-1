/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 元数据处理命令。<br><br>
 * 
 * 元数据分为“查询/推送/释放”三组。在HOME/DATA/WORK/BUILD/CALL之间传递。<br>
 * CALL站点发起查询命令（FindField及子类），HOME/DATA/WORK/BUILD发送推送命令或者释放命令（PushField/DropField及子类）。<br>
 * 
 * 元数据为CALL站点的分布计算和统一协调提供数据定位依据。<br>
 * 
 * 一个CALL站点最少保存一个账号的全部元数据。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/12/2015
 * @since laxcus 1.0
 */
public abstract class ProcessField extends Command {

	private static final long serialVersionUID = -1867802559584104511L;

	/** 命令发起方站点地址 **/
	private Node node;

	/**
	 * 构造默认的元数据处理命令
	 */
	protected ProcessField() {
		super();
	}

	/**
	 * 根据传入的元数据处理命令实例，生成它的数据副本
	 * @param that FieldCommand实例。
	 */
	protected ProcessField(ProcessField that) {
		super(that);
		node = that.node;
	}

	/**
	 * 设置元数据源头地址
	 * @param e Node实例
	 */
	public final void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回数据来源地址
	 * @return Node实例
	 */
	public final Node getNode() {
		return node;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
	}

}