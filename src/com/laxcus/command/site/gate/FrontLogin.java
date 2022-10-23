/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.gate;

import com.laxcus.command.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT注册。
 * 
 * @author scott.liang
 * @version 1.0 7/14/2018
 * @since laxcus 1.0
 */
public class FrontLogin extends Command {
	
	private static final long serialVersionUID = -6289547354634107337L;

	/** FRONT站点 **/
	private FrontSite site;

	/**
	 * 构造默认的FRONT注册
	 */
	public FrontLogin() {
		super();
	}

	/**
	 * 构造FRONT注册，指定参数
	 * @param site FRONT登录站点
	 */
	public FrontLogin(FrontSite site) {
		super();
		setSite(site);
	}

	/**
	 * 生成FRONT注册的数据副本
	 * @param that FRONT注册
	 */
	private FrontLogin(FrontLogin that) {
		super(that);
		site = that.site;
	}

	/**
	 * 设置FRONT站点
	 * @param e
	 */
	public void setSite(FrontSite e) {
		Laxkit.nullabled(e);
		site = e;
	}
	
	/**
	 * 返回FRONT站点
	 * @return
	 */
	public FrontSite getSite(){
		return site;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FrontLogin duplicate() {
		return new FrontLogin(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new FrontSite(reader);
	}

}
