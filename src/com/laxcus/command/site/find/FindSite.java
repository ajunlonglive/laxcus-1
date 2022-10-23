/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.find;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 查询站点命令 <br>
 * 
 * 此命令由CALL/DATA/WORK/BUILD/WATCH/GATE等子级站点发出，目标是上级的TOP/HOME站点。命令中指定查询的站点类型，子类可以包含其它相关参数。
 * 
 * FRONT站点不具备查询这个命令的能力。
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public class FindSite extends Command {

	private static final long serialVersionUID = 8383140611098524765L;

	/** 查询站点标识 **/
	private FindSiteTag tag; 

	/**
	 * 构造默认的查询站点命令
	 */
	public FindSite() {
		super();
	}

	/**
	 * 通过传入实例，生成查询站点命令的数据副本
	 * @param that FindSite实例
	 */
	protected FindSite(FindSite that) {
		this();
		tag = that.tag;
	}

	/**
	 * 构造查询站点命令，指定站点类型
	 * @param family 站点类型
	 */
	public FindSite(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造解析查询站点命令，设置标识
	 * @param tag FindSiteTag实例
	 */
	public FindSite(FindSiteTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 从可类化数据读取器中解析查询站点命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置查询站点标识
	 * @param e FindSiteTag实例
	 */
	public void setTag(FindSiteTag e) {
		tag = e;
	}

	/**
	 * 返回查询站点标识
	 * @return FindSiteTag实例
	 */
	public FindSiteTag getTag() {
		return tag;
	}

	/**
	 * 设置站点类型
	 * @param who 站点类型
	 */
	public void setFamily(byte who) {
		if (tag == null) {
			tag = new FindSiteTag();
		}
		tag.setFamily(who);
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public byte getFamily() {
		return (tag != null ? tag.getFamily() : 0);
	}

	/**
	 * 设置站点级别
	 * @param who 站点级别
	 */
	public void setRank(byte who) {
		if (tag == null) {
			tag = new FindSiteTag();
		}
		tag.setRank(who);
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return (tag != null ? tag.getRank() : 0);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindSite duplicate() {
		return new FindSite(this);
	}

	/* (non-Javadoc)
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(tag);
	}

	/* (non-Javadoc)
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		tag = reader.readInstance(FindSiteTag.class);
	}

}