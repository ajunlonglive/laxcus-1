/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.gate;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * GATE注册用户与GATE站点编号一致性检查单元
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
public class GateUserConsistencyItem implements Classable, Serializable, Cloneable, Comparable<GateUserConsistencyItem> {

	private static final long serialVersionUID = -6722082556549547637L;

	/** GATE站点地址 **/
	private Node site;
	
	/** 全部签名数目 **/
	private int users;
	
	/** 登录数目，一个用户账号可以有多个登录地址。 **/
	private int members;
	
	/** 匹配数目 **/
	private int matchs;

	/**
	 * 构造默认和私有的GATE注册用户与GATE站点编号一致性检查单元
	 */
	private GateUserConsistencyItem() {
		super();
	}

	/**
	 * 生成GATE注册用户与GATE站点编号一致性检查单元的数据副本
	 * @param that GateUserConsistencyItem实例
	 */
	private GateUserConsistencyItem(GateUserConsistencyItem that) {
		this();
		site = that.site;
		users = that.users;
		members = that.members;
		matchs = that.matchs;
	}

	/**
	 * 构造GATE注册用户与GATE站点编号一致性检查单元，指定参数
	 * @param node 站点地址
	 * @param users 在线用户数目
	 * @param members 在线登录数目
	 * @param matchs 匹配数目
	 */
	public GateUserConsistencyItem(Node node, int users, int members, int matchs) {
		this();
		setSite(node);
		setUsers(users);
		setMembers(members);
		setMatchs(matchs);
	}

	/**
	 * 从可类化数据读取器中解析GATE注册用户与GATE站点编号一致性检查单元
	 * @param reader 可类化数据读取器
	 */
	public GateUserConsistencyItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置GATE站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}
	
	/**
	 * 返回GATE站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}
	
	/**
	 * 设置用户数目
	 * @param i 用户数目
	 */
	public void setUsers(int i) {
		users = i;
	}

	/**
	 * 返回用户数目
	 * @return 用户数目
	 */
	public int getUsers() {
		return users;
	}	
	
	/**
	 * 设置登录数目。一个用户账号可以有多个登录数目
	 * @param i 用户数目
	 */
	public void setMembers(int i) {
		members = i;
	}

	/**
	 * 返回登录数目
	 * @return 用户数目
	 */
	public int getMembers() {
		return members;
	}	
	
	/**
	 * 设置用户签名与GATE站点匹配数目
	 * @param i 匹配数目
	 */
	public void setMatchs(int i) {
		matchs = i;
	}

	/**
	 * 返回用户签名与GATE站点匹配数目
	 * @return 匹配数目
	 */
	public int getMatchs() {
		return matchs;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return GateUserConsistencyItem实例
	 */
	public GateUserConsistencyItem duplicate() {
		return new GateUserConsistencyItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != GateUserConsistencyItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((GateUserConsistencyItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s # %d # %d # %d", site, users, members, matchs);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GateUserConsistencyItem that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(site);
		writer.writeInt(users);
		writer.writeInt(members);
		writer.writeInt(matchs);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		users = reader.readInt();
		members = reader.readInt();
		matchs = reader.readInt();
		return reader.getSeek() - seek;
	}

}