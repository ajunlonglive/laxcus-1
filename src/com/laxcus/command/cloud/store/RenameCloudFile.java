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
 * 修改云端文件
 * 文件位置是CALL节点，是云存储服务的文件
 * 
 * @author scott.liang
 * @version 1.0 1/26/2022
 * @since laxcus 1.0
 */
public class RenameCloudFile extends RenameCloudElement {
	
	private static final long serialVersionUID = -8663087136608334001L;

	/**
	 * 构造默认的修改云端文件命令
	 */
	public RenameCloudFile() {
		super();
	}

	/**
	 * 构造默认的修改云端文件命令
	 * @param srl 存储资源定位器
	 * @param name 名称
	 */
	public RenameCloudFile(SRL srl, String name) {
		this();
		setSRL(srl);
		setName(name);
	}
	
	/**
	 * 生成修改云端文件副本
	 * @param that
	 */
	private RenameCloudFile(RenameCloudFile that) {
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RenameCloudFile duplicate() {
		return new RenameCloudFile(this);
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