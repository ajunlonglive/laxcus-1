/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 上传和部署系统应用软件包。
 * 用户每次只能上传和部署一个软件包！
 * 
 * @author scott.liang
 * @version 1.0 2/17/2020
 * @since laxcus 1.0
 */
public abstract class DeploySystemPackage extends Command {

	private static final long serialVersionUID = 5413557053278206443L;

	/** 执行组件分发 **/
	private boolean publish;
	
	/**
	 * 构造上传和部署系统应用软件包
	 */
	protected DeploySystemPackage() {
		super();
		publish = false;
	}

	/**
	 * 上传和部署上传和部署系统应用软件包副本
	 * @param that 上传和部署系统应用软件包
	 */
	protected DeploySystemPackage(DeploySystemPackage that) {
		super(that);
		publish = that.publish;
	}

	/**
	 * 设置执行组件分发
	 * @param b 真或者假
	 */
	public void setPublish(boolean b) {
		publish = b;
	}
	
	/**
	 * 判断执行组件分发
	 * @return 真或者假
	 */
	public boolean isPublish() {
		return publish;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(publish);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		publish = reader.readBoolean();
	}

}