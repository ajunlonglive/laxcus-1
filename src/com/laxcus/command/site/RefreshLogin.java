/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 强制站点重新注册 <br>
 * 这是一个WATCH命令，强制指定的站点重新注册到管理站点。<br>
 * 在这些被强制的站点中，不包括FRONT站点。<br><br>
 * 
 * 这个命令从WATCH站点发送出来，只通知本集群的节点重新注册。<br>
 * 即WATCH发送到TOP站点，TOP只分发到本集群内，下级HOME子集群不在范围内。<br>
 * 如果发送到HOME站点，HOME只通知本集群子站点，上级TOP集群不在范围内。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/12/2017
 * @since laxcus 1.0
 */
public class RefreshLogin extends Command {

	private static final long serialVersionUID = -4928565644378855775L;

	/** 被强制注册站点 **/
	private TreeSet<Node> sites = new TreeSet<Node>();
	
	/**
	 * 构造默认的强制站点注册
	 */
	public RefreshLogin() {
		super();
	}

	/**
	 * 生成强制站点注册的数据副本
	 * @param that RefreshLogin实例
	 */
	private RefreshLogin(RefreshLogin that) {
		super(that);
		sites.addAll(that.sites);
	}

	/**
	 * 从可类化数据读取器中解析强制站点注册命令
	 * @param reader 可类化数据读取器
	 */
	public RefreshLogin(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存一个站点地址
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 保存一批站点地址
	 * @param a 站点集合
	 * @return 新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 返回站点地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 判断是全部重新注册
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshLogin duplicate() {
		return new RefreshLogin(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 站点地址
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 站点地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}