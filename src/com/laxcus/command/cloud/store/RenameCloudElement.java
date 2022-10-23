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
 * 修改云端单元名称
 * 资源位置是CALL节点，是云存储服务的资源
 * 
 * @author scott.liang
 * @version 1.0 1/22/2022
 * @since laxcus 1.0
 */
public abstract class RenameCloudElement extends Command {
	
	private static final long serialVersionUID = 2091656695445660156L;

	/** 存储资源定位器 **/
	private SRL srl;
	
	/** 新的名称 **/
	private String name;

	/**
	 * 构造默认的修改云端单元名称命令
	 */
	protected RenameCloudElement() {
		super();
	}
	
	/**
	 * 生成修改云端单元名称副本
	 * @param that
	 */
	protected RenameCloudElement(RenameCloudElement that) {
		super(that);
		srl = that.srl;
		name = that.name;
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
	
	/**
	 * 设置新名称
	 * @param s
	 */
	public void setName(String s) {
		name = s;
	}

	/**
	 * 返回新名称
	 * @return
	 */
	public String getName() {
		return name;
	}


	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(srl);
		writer.writeString(name);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = new SRL(reader);
		name = reader.readString();
	}

}