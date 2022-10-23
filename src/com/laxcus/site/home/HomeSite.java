/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.home;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * HOME站点资源。<br>
 * 记录一个HOME集群的全部元数据。是一组用户账号下的资源配置。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public final class HomeSite extends Site {

	private static final long serialVersionUID = 3174237320062236760L;

	/** 管理站点(只能有一个) **/
	private boolean manager;

	/** 账号签名 -> HOME站点成员 **/
	private TreeMap<Siger, HomeMember> members = new TreeMap<Siger, HomeMember>();

	/** 数据表名 **/
	private TreeMap<Space, HomeMember> spaces = new TreeMap<Space, HomeMember>();

	/**
	 * 将HOME站点属性写入可类化写入器
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 运行状态
		writer.writeBoolean(manager);
		// 写入参数
		writer.writeInt(members.size());
		for(HomeMember e : members.values()) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器中解析HOME站点属性信息
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 运行状态
		manager = reader.readBoolean();
		// 读参数
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			HomeMember member = new HomeMember(reader);
			Siger siger = member.getSiger();
			// 保存HOME站点成员
			members.put(siger, member);
			// 保存数据表名
			for (Space space : member.getSpaces()) {
				spaces.put(space, member);
			}
		}
	}

	/**
	 * 根据传入的HOME站点实例，生成它的副本
	 * @param that HomeSite实例
	 */
	private HomeSite(HomeSite that) {
		super(that);
		manager = that.manager;
		members.putAll(that.members);
		spaces.putAll(that.spaces);
	}

	/**
	 * 构造一个默认的HOME站点
	 */
	public HomeSite() {
		super(SiteTag.HOME_SITE);
		manager = false;
	}

	/**
	 * 从可类化读取器中解析HOME站点地址
	 * @param reader 可类化读取器
	 * @since 1.3
	 */
	public HomeSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置管理站点标记
	 * @param b 管理站点标记
	 */
	public void setManager(boolean b) {
		manager = b;
	}

	/**
	 * 检查是管理站点（否则即是监视站点）
	 * @return 返回真或者假
	 */
	public boolean isManager() {
		return manager;
	}

	/**
	 * 判断是监视站点
	 * @return 返回真或者假
	 */
	public boolean isMonitor() {
		return !isManager();
	}

	/**
	 * 清除全部参数
	 */
	public void reset() {
		members.clear();
		spaces.clear();
	}

	/**
	 * 根据用户签名分配一个HOME站点成员。<br>
	 * 如果HOME站点成员存在，返回它；否则建立新的HOME站点成员实例并且返回它。
	 * 
	 * @param siger 用户签名
	 * @return HomeMember HOME成员
	 * @throws NullPointerException - 如果用户签名是空值是
	 */
	private HomeMember alloc(Siger siger) {
		Laxkit.nullabled(siger);

		HomeMember member = members.get(siger);
		if (member == null) {
			member = new HomeMember(siger);
			members.put(siger, member);
		}
		return member;
	}

	/**
	 * 建立一个HOME站点成员。这项操作在启动或者分配新账号时设置
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean create(Siger siger) {
		Laxkit.nullabled(siger);
		return alloc(siger) != null;
	}

	/**
	 * 根据用户签名查找HOME站点成员
	 * @param siger 用户签名
	 * @return HomeMember实例
	 */
	public HomeMember find(Siger siger) {
		Laxkit.nullabled(siger);

		return members.get(siger);
	}

	/**
	 * 判断HOME站点成员存在
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		return find(siger) != null;
	}

	/**
	 * 删除一个账号及它的全部记录
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Siger siger) {
		Laxkit.nullabled(siger);

		HomeMember member = members.remove(siger);
		boolean success = (member != null);
		if (success) {
			for (Space space : member.getSpaces()) {
				spaces.remove(space);
			}
		}
		return success;
	}

	/**
	 * 返回全部注册用户签名
	 * @return Siger列表
	 */
	public List<Siger> getSigers() {
		return new ArrayList<Siger>(members.keySet());
	}

	/**
	 * 增加一个注册账号下的表名
	 * @param siger 账号签名
	 * @param space 数据表名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addSpace(Siger siger, Space space) {
		HomeMember member = alloc(siger);
		// 保存签名
		boolean success = member.addSpace(space);
		if (success) {
			spaces.put(space, member);
		}
		return success;
	}

	/**
	 * 判断数据表名存在
	 * @param space 数据表名
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean contains(Space space) {
		return spaces.get(space) != null;
	}

	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(spaces.keySet());
	}

	/**
	 * 统计全部账号数目 
	 * @return 注册账号数目
	 */
	public int size() {
		return members.size();
	}

	/**
	 * 判断账号集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public HomeSite duplicate() {
		return new HomeSite(this);
	}

}