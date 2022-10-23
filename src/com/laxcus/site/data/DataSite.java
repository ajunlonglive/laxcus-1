/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.data;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * DATA站点配置。<br>
 * 
 * 数据存储节点元信息
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class DataSite extends Site {

	private static final long serialVersionUID = 3615003516691033760L;

	/** 磁盘已经占用尺寸 */
	private long usedDiskCapacity;

	/** 未使用尺寸 */
	private long freeDiskCapacity;

	/** 用户签名 -> DATA站点成员 **/
	private Map<Siger, DataMember> members = new TreeMap<Siger, DataMember>();

	/** 数据表名  -> DATA站点成员 **/
	private Map<Space, DataMember> spaces = new TreeMap<Space, DataMember>();

	/** 用户阶段命名 **/
	private Map<Phase, DataMember> phases = new TreeMap<Phase, DataMember>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 已用空间
		writer.writeLong(usedDiskCapacity);
		// 自由空间
		writer.writeLong(freeDiskCapacity);
		// 注册成员 
		writer.writeInt(members.size());
		for(DataMember e : members.values()) {
			writer.writeObject(e);
		}
	}

	/*
	 * s(non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

		// 磁盘已用空间
		usedDiskCapacity = reader.readLong();
		// 磁盘自由空间
		freeDiskCapacity = reader.readLong();

		// 成员数目
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			// DATA站点成员
			DataMember e = new DataMember(reader);
			members.put(e.getSiger(), e);
			// 数据表名
			for (Space space : e.getSpaces()) {
				spaces.put(space, e);
			}
			// 阶段命名
			for (Phase phase : e.getPhases()) {
				phases.put(phase, e);
			}
		}
	}

	/**
	 * 根据传入的实例，生成它的副本
	 * @param that DataSite实例
	 */
	private DataSite(DataSite that) {
		super(that);
		usedDiskCapacity = that.usedDiskCapacity;
		freeDiskCapacity = that.freeDiskCapacity;
		members.putAll(that.members);
		spaces.putAll(that.spaces);
		phases.putAll(that.phases);
	}

	/**
	 * 构造默认的DATA站点实例
	 */
	public DataSite() {
		super(SiteTag.DATA_SITE);
	}

	/**
	 * 构造DATA节点记录，并且设置它的级别(主从关系，PRIME/SLAVE)
	 * @param rank 站点级别
	 */
	public DataSite(byte rank) {
		this();
		setRank(rank);
	}

	/**
	 * 从可类化读取器中解析数据节点参数
	 * @param reader 可类化写入器
	 * @since 1.1
	 */
	public DataSite(ClassReader reader) {
		this();
		resolve(reader);
	}


	/**
	 * 设置DATA节点上已经占用的磁盘空间尺寸
	 * @param i 已经占用的磁盘空间尺寸
	 */
	public void setDiskUsedCapacity(long i) {
		usedDiskCapacity = i;
	}

	/**
	 * 返回DATA节点已经占用的磁盘空间尺寸
	 * @return 已经占用的磁盘空间尺寸
	 */
	public long getDiskUsedCapacity() {
		return usedDiskCapacity;
	}

	/**
	 * 设置DATA节点没有使用的磁盘空间尺寸
	 * @param i 没有使用的磁盘空间尺寸
	 */
	public void setDiskFreeCapacity(long i) {
		freeDiskCapacity = i;
	}

	/**
	 * 返回DATA节点没有使用的磁盘空间尺寸
	 * @return 没有使用的磁盘空间尺寸
	 */
	public long getDiskFreeCpacity() {
		return freeDiskCapacity;
	}

	/**
	 * 返回注册用户
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(members.keySet());
	}

	/**
	 * 输出全部单元
	 * @return 数据单元
	 */
	public List<DataMember> getMembers() {
		return new ArrayList<DataMember>(members.values());
	}

	/**
	 * 根据用户签名分配一个DATA站点成员。<br>
	 * 如果DATA站点成员存在，返回它；否则建立新的DATA站点成员实例并且返回它。
	 * 
	 * @param siger 用户签名
	 * @return 返回DataMember实例
	 * @throws NullPointerException - 如果用户签名是空值是
	 */
	private DataMember allocate(Siger siger) {
		Laxkit.nullabled(siger);

		DataMember member = members.get(siger);
		if (member == null) {
			member = new DataMember(siger);
			members.put(siger, member);
		}
		return member;
	}

	/**
	 * 建立一个DATA站点成员。这项操作在启动或者分配新账号时设置
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean create(Siger siger) {
		Laxkit.nullabled(siger);

		return allocate(siger) != null;
	}

	/**
	 * 根据用户签名查找DATA站点成员
	 * @param siger 用户签名
	 * @return 返回DataMember实例
	 */
	public DataMember find(Siger siger) {
		Laxkit.nullabled(siger);

		return members.get(siger);
	}

	/**
	 * 判断DATA站点成员存在
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		return find(siger) != null;
	}

	/**
	 * 增加一个数据块空间域
	 * @param siger 用户签名
	 * @param reflex 数据块空间域
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addStubReflex(Siger siger, StubReflex reflex) {
		// 分配DATA站点成员
		DataMember member = allocate(siger);
		boolean success = member.addStubReflex(reflex);
		if (success) {
			spaces.put(reflex.getSpace(), member);
		}
		return success;
	}

	/**
	 * 增加阶段命名，拒绝系统级阶段命名。
	 * @param phase 阶段命名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addPhase(Phase phase) {
		// 拒绝系统级阶段命名，或者没有签名时
		if (phase.isSystemLevel()) {
			return false;
		} else if (phase.getIssuer() == null) {
			Logger.error(this, "addPhase", "illegal user phase %s", phase);
			return false;
		}
		// 保存阶段命名
		DataMember member = allocate(phase.getIssuer());
		boolean success = member.addPhase(phase);
		if (success) {
			phases.put(phase, member);
		}
		return success;
	}

	/**
	 * 释放全部参数
	 */
	public void reset() {
		setMoment(null);
		usedDiskCapacity = freeDiskCapacity = 0;
		members.clear();
		spaces.clear();
		phases.clear();
	}

	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(spaces.keySet());
	}

	/**
	 * 返回全部阶段命名
	 * @return Phase列表
	 */
	public List<Phase> getPhases() {
		return new ArrayList<Phase>(phases.keySet());
	}

	/**
	 * 返回账号数目 
	 * @return
	 */
	public int size() {
		return members.size();
	}

	/**
	 * 根据账号签名查找它下属表空间集合
	 * @param siger 账号签名
	 * @return 返回表空间列表，没有返回null
	 */
	public List<Space> findSpace(Siger siger) {
		DataMember member = find(siger);
		if (member != null) {
			return member.getSpaces();
		}
		return null;
	}

	/**
	 * 根据表名，取它的磁盘数据块总长度
	 * @param space 数据表名
	 * @return 磁盘数据块总长度
	 */
	public long findDiskCapacity(Space space) {
		DataMember member = spaces.get(space);
		if (member != null) {
			return member.findDiskCapacity(space);
		}
		return 0;
	}

	/**
	 * 根据表名，取它的内存数据块总长度
	 * @param space 数据表名
	 * @return 内存数据块总长度
	 */
	public long findMemoryCapacity(Space space) {
		DataMember member = spaces.get(space);
		if (member != null) {
			return member.findMemoryCapacity(space);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public DataSite duplicate() {
		return new DataSite(this);
	}

}