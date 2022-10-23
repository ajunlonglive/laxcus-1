/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.cross;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 共享资源域 <br>
 * 
 * 由授权人、被授权人、被共享共享标识集合3个部分组成。 <br>
 * 共享资源权限由管理员控制，开放/撤销共享资源的决定由数据持有人操作。<br><br>
 * 
 * 操作语法：<br>
 * 
 * OPEN  SHARE DATABASE [ALL | schema name, ...] ON [READ,WRITE] TO [username, SIGN SHA256 code ...]<br>
 * CLOSE SHARE DATABASE [ALL | schema name, ...] ON [READ,WRITE] FROM [username, SIGN SHA256 code ...] <br><br>
 * 
 * OPEN SHARE TABLE [ALL | table name, ...] ON [READ,WRITE] TO [username, SIGN SHA256 code ...] <br>
 * CLOSE SHARE TABLE [ALL | table name, ...] ON [READ,WRITE] FROM [username, SIGN SHA256 code ...]<br><br>
 * 
 * SHOW OPEN RESOURCE FROM [ALL | username, SIGN sha256 code, ...]
 * 
 * SHOW PASSIVE RESOURCE FROM [ALL | username, SIGN sha256 code, ...]
 * 
 * @author scott.liang
 * @version 1.0 7/4/2017
 * @since laxcus 1.0
 */
public class CrossField implements Classable, Cloneable, Serializable, Comparable<CrossField> {

	private static final long serialVersionUID = 383727734198257000L;

	/** 授权人签名 **/
	private Siger authorizer;

	/** 被授权人签名 **/
	private Siger conferrer;
	
	/** 被分享的共享标识集合 **/
	private TreeSet<CrossFlag> flags = new TreeSet<CrossFlag>();
	
	/**
	 * 构造默认和私有的共享资源域
	 */
	private CrossField() {
		super();
	}

	/**
	 * 生成共享资源域的数据副本
	 * @param that CrossField实例
	 */
	private CrossField(CrossField that) {
		this();
		authorizer = that.authorizer;
		conferrer = that.conferrer;
		flags.addAll(that.flags);
	}

	/**
	 * 构造共享资源域，指定授权人和被授权人
	 * @param authorizer 授权人签名
	 * @param conferrer 被授权人签名
	 */
	public CrossField(Siger authorizer, Siger conferrer) {
		this();
		setAuthorizer(authorizer);
		setConferrer(conferrer);
	}

	/**
	 * 从可类化数据读取器中解析共享资源域
	 * @param reader 可类化数据读取器
	 */
	public CrossField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置授权人签名
	 * @param e Siger实例
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);

		authorizer = e;
	}

	/**
	 * 返回授权人签名
	 * @return Siger实例
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}

	/**
	 * 设置被授权人签名
	 * @param e Siger实例
	 */
	public void setConferrer(Siger e) {
		Laxkit.nullabled(e);

		conferrer = e;
	}

	/**
	 * 返回被授权人签名
	 * @return Siger实例
	 */
	public Siger getConferrer() {
		return conferrer;
	}

	/**
	 * 保存一个共享标识
	 * @param e 共享标识
	 * @return 返回真或者假
	 */
	public boolean add(CrossFlag e) {
		Laxkit.nullabled(e);

		return flags.add(e);
	}

	/**
	 * 保存一批共享标识
	 * @param a 共享标识
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<CrossFlag> a) {
		int size = flags.size();
		for (CrossFlag e : a) {
			add(e);
		}
		return flags.size() - size;
	}

	/**
	 * 删除一个共享标识
	 * @param e 共享标识
	 * @return 返回真或者假
	 */
	public boolean remove(CrossFlag e) {
		Laxkit.nullabled(e);

		return flags.remove(e);
	}

	/**
	 * 删除一批共享标识
	 * @param a 共享标识列表
	 * @return 返回被删除的成员数目
	 */
	public int removeAll(Collection<CrossFlag> a) {
		int size = flags.size();
		for (CrossFlag e : a) {
			remove(e);
		}
		return size - flags.size();
	}

	/**
	 * 返回全部共享标识
	 * @return CrossFlag列表
	 */
	public List<CrossFlag> list() {
		return new ArrayList<CrossFlag>(flags);
	}

	/**
	 * 返回成员数目
	 * @return 成员数目的整型值
	 */
	public int size() {
		return flags.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 生成授权单元
	 * @return ActiveItem列表
	 */
	public List<ActiveItem> createActiveItems() {
		ArrayList<ActiveItem> a = new ArrayList<ActiveItem>();
		for (CrossFlag flag : flags) {
			ActiveItem item = new ActiveItem(conferrer, flag);
			a.add(item);
		}
		return a;
	}

	/**
	 * 生成被授权单元
	 * @return PassiveItem列表
	 */
	public List<PassiveItem> createPassiveItems() {
		ArrayList<PassiveItem> a = new ArrayList<PassiveItem>();
		for (CrossFlag e : flags) {
			PassiveItem item = new PassiveItem(authorizer, e);
			a.add(item);
		}
		return a;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return CrossField实例
	 */
	public CrossField duplicate() {
		return new CrossField(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		// 比较参数完成一致
		return compareTo((CrossField) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return conferrer.hashCode() ^ authorizer.hashCode();
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
	public int compareTo(CrossField that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(conferrer, that.conferrer);
		if (ret == 0) {
			ret = Laxkit.compareTo(authorizer, that.authorizer);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(conferrer);
		writer.writeObject(authorizer);
		writer.writeInt(flags.size());
		for (CrossFlag e : flags) {
			writer.writeObject(e);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		conferrer = new Siger(reader);
		authorizer = new Siger(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CrossFlag e = new CrossFlag(reader);
			flags.add(e);
		}
		// 返回解析字节长度
		return reader.getSeek() - seek;
	}

}