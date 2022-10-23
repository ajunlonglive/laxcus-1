/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 构造默认的用户阶段命名检索结果单元
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserTaskItem extends SeekUserSiteItem {

	private static final long serialVersionUID = 775601714958271733L;

	/** 阶段命名数组 **/
	private TreeSet<Phase> array = new TreeSet<Phase>();

	/**
	 * 构造默认的用户阶段命名检索结果单元
	 */
	public SeekUserTaskItem() {
		super();
	}

	/**
	 * 生成用户阶段命名检索结果单元的数据副本
	 * @param that 用户阶段命名检索结果单元
	 */
	private SeekUserTaskItem(SeekUserTaskItem that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造用户阶段命名检索结果单元，指定用户基点
	 * @param seat 用户基点
	 */
	public SeekUserTaskItem(Seat seat) {
		super(seat);
	}
	
	/**
	 * 构造用户阶段命名检索结果单元，指定基础参数
	 * @param siger 用户签名
	 * @param site 站点地址
	 */
	public SeekUserTaskItem(Siger siger, Node site) {
		super(siger, site);
	}

	/**
	 * 从可类化数据读取器中解析用户阶段命名检索结果单元
	 * @param reader 可类化数据读取器
	 */
	public SeekUserTaskItem(ClassReader reader) {
		this();
		super.resolve(reader);
	}

	/**
	 * 保存阶段命名，不允许空指针
	 * @param e 阶段命名实例
	 * @return 成功返回真，否则假
	 */
	public boolean addPhase(Phase e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批阶段命名
	 * @param a 阶段命名列表
	 * @return 返回新增成员数目
	 */
	public int addPhases(Collection<Phase> a) {
		int size = array.size();
		for (Phase e : a) {
			addPhase(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部阶段命名
	 * @return 阶段命名列表
	 */
	public List<Phase> getPhases() {
		return new ArrayList<Phase>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.SeekUserItem#duplicate()
	 */
	@Override
	public SeekUserTaskItem duplicate() {
		return new SeekUserTaskItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 被检索站点标记
		writer.writeInt(array.size());
		for (Phase e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 被检索站点标记
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			array.add(e);
		}
	}

}