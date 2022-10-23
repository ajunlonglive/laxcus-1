/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 删除云端文件
 * 目录位置是CALL节点，是云存储服务的目录
 * 
 * @author scott.liang
 * @version 1.0 1/22/2022
 * @since laxcus 1.0
 */
public class DropCloudFile extends DropCloudElement {
	
	private static final long serialVersionUID = -6439155737833899998L;

	/**
	 * 构造默认的删除云端文件命令
	 */
	public DropCloudFile() {
		super();
	}

	/**
	 * 构造默认的删除云端文件命令
	 * @param l 存储资源定位器
	 */
	public DropCloudFile(SRL l) {
		this();
		setSRL(l);
	}
	
	/**
	 * 生成删除云端文件副本
	 * @param that
	 */
	private DropCloudFile(DropCloudFile that) {
		super(that);
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropCloudFile duplicate() {
		return new DropCloudFile(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}