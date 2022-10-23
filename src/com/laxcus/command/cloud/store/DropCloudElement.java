/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 删除云存储资源
 * 资源位置是CALL节点，是云存储服务的资源
 * 
 * @author scott.liang
 * @version 1.0 1/22/2022
 * @since laxcus 1.0
 */
public abstract class DropCloudElement extends Command {
	
	private static final long serialVersionUID = 7086607079520769683L;
	
	/** 存储资源定位器 **/
	private SRL srl;

	/**
	 * 构造默认的删除云存储资源命令
	 */
	protected DropCloudElement() {
		super();
	}

	/**
	 * 生成删除云存储资源副本
	 * @param that
	 */
	protected DropCloudElement(DropCloudElement that) {
		super(that);
		srl = that.srl;
	}
	
	/**
	 * 设置存储资源定位器
	 * @param l
	 */
	public void setSRL(SRL l) {
		srl = l;
	}
	
	/**
	 * 返回存储资源定位器
	 * @return
	 */
	public SRL getSRL(){
		return srl;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(srl);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = new SRL(reader);
	}

}