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
 * 建立云存储目录
 * 目录位置是CALL节点，是云存储服务的目录
 * 
 * @author scott.liang
 * @version 1.0 10/23/2021
 * @since laxcus 1.0
 */
public class CreateCloudDirectory extends Command {
	
	private static final long serialVersionUID = 1015605228851777932L;

	/** 存储资源定位器 **/
	private SRL srl;

	/**
	 * 构造默认的建立云存储目录命令
	 */
	public CreateCloudDirectory() {
		super();
	}

	/**
	 * 构造默认的建立云存储目录命令
	 * @param l 存储资源定位器
	 */
	public CreateCloudDirectory(SRL l) {
		this();
		setSRL(l);
	}
	
	/**
	 * 生成建立云存储目录副本
	 * @param that
	 */
	private CreateCloudDirectory(CreateCloudDirectory that) {
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
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateCloudDirectory duplicate() {
		return new CreateCloudDirectory(this);
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

