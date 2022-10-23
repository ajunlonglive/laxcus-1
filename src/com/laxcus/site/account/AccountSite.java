/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.account;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * ACCOUNT站点。站点信息注册到BANK站点
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class AccountSite extends Site {

	private static final long serialVersionUID = 5880615319865068292L;

	/** 无效的编号 **/
	public final static int INVALID_NO = -1;

	/** 编号，从0开始，默认是-1。 **/
	private int no;
	
	/** 注册成员数目 **/
	private int members;

	/** 分布任务组件数目 **/
	private int tasks;

	/**
	 * 根据传入的AIT站点实例，生成它的数据副本
	 * @param that AccountSite实例
	 */
	private AccountSite(AccountSite that) {
		super(that);
		no = that.no;
		members = that.members;
		tasks = that.tasks;
	}

	/**
	 * 构造一个默认的ACCOUNT站点地址
	 */
	public AccountSite() {
		super(SiteTag.ACCOUNT_SITE);
		setNo(AccountSite.INVALID_NO); // 默认是-1
	}

	/**
	 * 从可类化读取器中解析ACCOUNT站点地址
	 * @param reader 可类化数据读取器
	 */
	public AccountSite(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置编号
	 * @param who
	 */
	public void setNo(int who) {
		no = who;
	}
	
	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return no;
	}
	
	/**
	 * 判断是有效的编号
	 * @return 返回真或者假
	 */
	public boolean isValidNo() {
		return no > AccountSite.INVALID_NO;
	}

	/**
	 * 设置注册成员数目
	 * @param i
	 */
	public void setMembers(int i) {
		members = i;
	}

	/**
	 * 返回注册成员数目
	 * @return 注册成员数目
	 */
	public int getMembers() {
		return members;
	}

	/**
	 * 设置分布任务组件数目
	 * @param i 分布任务组件数目
	 */
	public void setTasks(int i) {
		tasks = i;
	}

	/**
	 * 返回分布任务组件数目
	 * @return 分布任务组件数目
	 */
	public int getTasks() {
		return tasks;
	}


	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public AccountSite duplicate() {
		return new AccountSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(no);
		writer.writeInt(members);
		writer.writeInt(tasks);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		no = reader.readInt();
		members = reader.readInt();
		tasks = reader.readInt();
	}

}