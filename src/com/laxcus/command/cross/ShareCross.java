/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 授权共享资源 <br><br>
 * 
 * 数据持有人（授权人）操作。<br>
 * 
 * 被共享的资源包括数据库和数据库两种，由数据持有人提供给被分享的用户（被授权人）。<br>
 * 当发起人提交命令成功后， 被授权人需要重新启动FRONT站点，才能启用新的共享数据资源。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public abstract class ShareCross extends Command {

	private static final long serialVersionUID = 6080257719905377534L;

	/** 被授权人签名集合 **/
	private Set<Siger> conferrers = new TreeSet<Siger>();

	/** 用户明文。不做可类化处理，不在网络间传输 **/
	private TreeSet<Naming> texts = new TreeSet<Naming>();

	/** 共享操作符 **/
	private int operator;

	/**
	 * 构造默认的授权共享资源
	 */
	protected ShareCross() {
		super();
		operator = CrossOperator.NONE;
	}

	/**
	 * 生成授权共享资源的数据副本
	 * @param that ShareCross实例
	 */
	protected ShareCross(ShareCross that) {
		super(that);
		operator = that.operator;
		conferrers.addAll(that.conferrers);
		texts.addAll(that.texts);
	}

	/**
	 * 设置共享资源操作符
	 * @param who 共享资源操作符
	 */
	public void setOperator(int who) {
		if (!CrossOperator.isOperator(who)) {
			throw new IllegalValueException("illegal cross operator:%d", who);
		}
		operator = who;
	}

	/**
	 * 返回共享资源操作符
	 * @return 共享资源操作符
	 */
	public int getOperator() {
		return operator;
	}

	/**
	 * 保存一个被授权人签名
	 * @param e Siger实例
	 */
	public void addConferrer(Siger e) {
		Laxkit.nullabled(e);
		
		conferrers.add(e);
	}

	/**
	 * 保存一批被授权人签名
	 * @param a 被授权人签名数组
	 * @return 新增成员数目
	 */
	public int addConferrers(Collection<Siger> a) {
		int size = conferrers.size();
		for (Siger e : a) {
			addConferrer(e);
		}
		return conferrers.size() - size;
	}

	/**
	 * 输出全部被授权人签名
	 * @return 返回Siger列表
	 */
	public List<Siger> getConferrers() {
		return new ArrayList<Siger>(conferrers);
	}

	/**
	 * 保存一个被授权人明文
	 * @param e 被授权人明文
	 * @return 返回真或者假
	 */
	public boolean addText(String e) {
		Laxkit.nullabled(e);

		return texts.add(new Naming(e));
	}

	/**
	 * 根据被授权人数字签名，查找匹配的被授权人明文
	 * @param conferrer 被授权人数字签名
	 * @return 返回用户明文，没有返回用户数字签名
	 */
	public String findText(Siger conferrer) {
		for (Naming e : texts) {
			String name = e.toString();
			// 判断是SHA256码，或者明文
			if (Siger.validate(name)) {
				if (Laxkit.compareTo(name, conferrer.toString(), false) == 0) {
					return name.toString();
				}
			} else {
				Siger siger = SHAUser.doUsername(name);
				if (siger.equals(conferrer)) {
					return name;
				}
			}
		}
		return conferrer.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(operator);
		writer.writeInt(conferrers.size());
		for (Siger e : conferrers) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		operator = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			conferrers.add(e);
		}
	}

}