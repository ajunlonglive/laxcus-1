/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.login;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 注册站点。<br>
 * 
 * 这个命令避开SiteLauncher.login, 子站点采用INVOKE/PRODUCE模式，向父站点注册。
 * 
 * @author scott.liang
 * @version 1.0 12/03/2017
 * @since laxcus 1.0
 */
public class LoginSite extends Command {

	private static final long serialVersionUID = 8003185435123172680L;

	/** 所属站点资源 **/
	private Site site;
	
	/**
	 * 构造默认的注册站点命令
	 */
	private LoginSite() {
		super();
	}

	/**
	 * 构造注册站点命令，指定站点实例
	 * @param site 站点实例
	 */
	public LoginSite(Site site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化读取器中取出注册站点命令
	 * @param reader
	 */
	public LoginSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成注册站点命令的数据副本
	 * @param that 注册站点命令
	 */
	private LoginSite(LoginSite that) {
		super(that);
		site = that.site;
	}

	/**
	 * 设置站点实例，不允许空指针
	 * @param e 站点实例
	 */
	public void setSite(Site e) {
		Laxkit.nullabled(e);
		site = e;
	}
	
	/**
	 * 返回站点实例
	 * @return 站点实例
	 */
	public Site getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public LoginSite duplicate() {
		return new LoginSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeDefault(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = (Site) reader.readDefault();
	}

}