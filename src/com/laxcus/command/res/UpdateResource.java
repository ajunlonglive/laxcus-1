/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.res;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 更新资源。<br>
 * 这个命令由HOME发出，通知下属的CALL/WORK，从指定的DATA站点上获取新的数据。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2015
 * @since laxcus 1.0
 */
public class UpdateResource extends Command {
	
	private static final long serialVersionUID = 6899128709554838148L;
	
	/** 目标地址，一定是DATA站点 **/
	private Node target;

	/**
	 * 根据传入的命令，生成它的数据副本
	 * @param that UpdateResource实例
	 */
	private UpdateResource(UpdateResource that) {
		super(that);
		target = that.target;
	}
	
	/**
	 * 构造默认的更新资源命令
	 */
	public UpdateResource() {
		super();
	}

	/**
	 * 构造更新资源命令，指定目标地址。
	 * @param node 节点地址
	 */
	public UpdateResource(Node node) {
		this();
		setTarget(node);
	}
	
	/**
	 * 设置目标地址
	 * @param e Node实例
	 */
	public void setTarget(Node e) {
		Laxkit.nullabled(e);

		target = e;
	}
	
	/**
	 * 返回目标地址
	 * @return Node实例
	 */
	public Node getTarget() {
		return target;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public UpdateResource duplicate() {
		return new UpdateResource(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(target);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		target = new Node(reader);
	}

}
