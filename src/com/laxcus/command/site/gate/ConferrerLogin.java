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
 * 被授权FRONT账号登录。
 * 
 * @author scott.liang
 * @version 1.0 7/25/2018
 * @since laxcus 1.0
 */
public class ConferrerLogin extends Command {
	
	private static final long serialVersionUID = -6289547354634107337L;

	/** 被授权FRONT站点 **/
	private ConferrerSite site;

	/**
	 * 构造默认的被授权FRONT账号登录
	 */
	public ConferrerLogin() {
		super();
	}

	/**
	 * 构造被授权FRONT账号登录，指定参数
	 * @param site 被授权FRONT站点
	 */
	public ConferrerLogin(ConferrerSite site) {
		super();
		setSite(site);
	}

	/**
	 * 生成被授权FRONT账号登录的数据副本
	 * @param that 被授权FRONT账号登录
	 */
	private ConferrerLogin(ConferrerLogin that) {
		super(that);
		site = that.site;
	}

	/**
	 * 设置被授权FRONT站点
	 * @param e
	 */
	public void setSite(ConferrerSite e) {
		Laxkit.nullabled(e);
		site = e;
	}
	
	/**
	 * 返回被授权FRONT站点
	 * @return
	 */
	public ConferrerSite getSite(){
		return site;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ConferrerLogin duplicate() {
		return new ConferrerLogin(this);
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
		site = new ConferrerSite(reader);
	}

}
