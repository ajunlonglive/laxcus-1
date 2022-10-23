/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 判断一个站点存在于计算机集群中。<br>
 * 
 * 这个命令是子级站点发送，目标是TOP/HOME站点，子站向所属集群管理站点查询另一个站点存在。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public class AssertSite extends Command {

	private static final long serialVersionUID = 5329249581579337112L;

	/** 被检查的集群站点 **/
	private Node site;

	/**
	 * 构造默认和私有的查询站点存在命令。
	 */
	private AssertSite() {
		super();
	}

	/**
	 * 根据传入的查询站点存在命令实例，生成它的数据副本
	 * 
	 * @param that AssertSite实例
	 */
	private AssertSite(AssertSite that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造查询站点存在命令实例，指定被检查的集群站点
	 * 
	 * @param site 被检查的集群站点
	 */
	public AssertSite(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中解析查询站点存在命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AssertSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置被检查的集群站点
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回被检查的集群站点
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertSite duplicate() {
		return new AssertSite(this);
	}

}