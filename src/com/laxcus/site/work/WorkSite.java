/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.work;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * WORK站点运行配置。<br>
 * 
 * 记录WORK站点运行过程中被分配的阶段命名。
 * 
 * @author scott.liang
 * @version 1.1 10/23/2015
 * @since laxcus 1.0
 */
public final class WorkSite extends Site {

	private static final long serialVersionUID = 505371836459946358L;

	/** 账号签名 -> WORK站点成员 **/
	private Map<Siger, WorkMember> members = new TreeMap<Siger, WorkMember>();

	/** 用户阶段命名 -> WORK站点成员 **/
	private Map<Phase, WorkMember> phases = new TreeMap<Phase, WorkMember>();

	/** 数据表名 -> WORK站点成员 **/
	private Map<Space, WorkMember> spaces = new TreeMap<Space, WorkMember>();

	/**
	 * 将WORK站点属性写入可类化写入器
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写入参数
		writer.writeInt(members.size());
		for(WorkMember e : members.values()) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器中解析WORK站点属性信息
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 读参数
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			WorkMember member = new WorkMember(reader);
			Siger siger = member.getSiger();
			// 保存WORK站点成员
			members.put(siger, member);
			// 保存数据表名
			for (Space space : member.getTables()) {
				spaces.put(space, member);
			}
			// 保存阶段命名
			for (Phase phase : member.getPhases()) {
				phases.put(phase, member);
			}
		}
	}

	/**
	 * 根据传入的实例，生成它的数据副本
	 * @param that WorkSite实例
	 */
	private WorkSite(WorkSite that) {
		super(that);
		members.putAll(that.members);
		phases.putAll(that.phases);
		spaces.putAll(that.spaces);
	}

	/**
	 * 生成一个默认的WORK节点地址
	 */
	public WorkSite() {
		super(SiteTag.WORK_SITE);
	}

	/**
	 * 从可类化数据读取器中解析WORK节点地址信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public WorkSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据用户签名分配一个WORK站点成员。<br>
	 * 如果WORK站点成员存在，返回它；否则建立新的WORK站点成员实例并且返回它。
	 * 
	 * @param siger 用户签名
	 * @return WorkMember实例
	 * @throws NullPointerException - 如果用户签名是空值是
	 */
	private WorkMember allocate(Siger siger) {
		Laxkit.nullabled(siger);
		
		WorkMember member = members.get(siger);
		if (member == null) {
			member = new WorkMember(siger);
			members.put(siger, member);
		}
		return member;
	}

	/**
	 * 建立一个WORK站点成员。这项操作在启动或者分配新账号时设置
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean create(Siger siger) {
		Laxkit.nullabled(siger);
		
		return allocate(siger) != null;
	}

	/**
	 * 根据用户签名查找WORK站点成员
	 * @param siger 用户签名
	 * @return WorkMember实例
	 */
	public WorkMember find(Siger siger) {
		Laxkit.nullabled(siger);
		return members.get(siger);
	}

	/**
	 * 判断WORK站点成员存在
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

		WorkMember member = members.remove(siger);
		boolean success = (member != null);
		if (success) {
			for (Phase e : member.getPhases()) {
				phases.remove(e);
			}
			for(Space e : member.getTables()) {
				spaces.remove(e);
			}
		}
		return success;
	}

	/**
	 * 输出全部注册用户
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(members.keySet());
	}

	/**
	 * 输出全部WORK成员
	 * @return WorkMember列表
	 */
	public List<WorkMember> getMembers() {
		return new ArrayList<WorkMember>(members.values());
	}

	/**
	 * 重置全部参数，但是不能够清除用户名称
	 */
	public void reset() {
		members.clear();
		phases.clear();
		spaces.clear();
	}
	
	/**
	 * 增加一个注册账号下的表名
	 * @param siger 账号签名
	 * @param space 数据表名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addSpace(Siger siger, Space space) {
		WorkMember member = allocate(siger);
		// 保存签名
		boolean success = member.addTable(space);
		if (success) {
			spaces.put(space, member);
		}
		return success;
	}

	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(spaces.keySet());
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
		WorkMember member = allocate(phase.getIssuer());
		boolean success = member.addPhase(phase);
		if (success) {
			phases.put(phase, member);
		}
		return success;
	}

	/**
	 * 删除阶段命名
	 * @param phase 阶段命名
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean removePhase(Phase phase) {
		Laxkit.nullabled(phase);
		
		// 阶段命名必须有持有人签名
		Siger siger = phase.getIssuer();
		if (!contains(siger)) {
			return false;
		}
		WorkMember member = allocate(siger);
		boolean success = (member != null);
		if (success) {
			member.removePhase(phase);
		}
		phases.remove(phase);
		
		return success;
	}

	/**
	 * 输出全部阶段命名
	 * @return Phase列表
	 */
	public List<Phase> getPhases() {
		return new ArrayList<Phase>(phases.keySet());
	}

	/**
	 * 根据用户签名查找全部阶段命名
	 * @param siger 用户签名
	 * @return Phase列表
	 */
	public List<Phase> findPhase(Siger siger) {
		WorkMember member = this.find(siger);
		if (member != null) {
			return member.getPhases();
		}
		return new ArrayList<Phase>();
	}

	/**
	 * 统计当前阶段命名数目
	 * @return 阶段命名数目
	 */
	public int getPhaseCount() {
		int count = 0;
		for (WorkMember e : members.values()) {
			count += e.getPhaseSize();
		}
		return count;
	}

	/**
	 * 统计阶段命名持有人数目
	 * @return 阶段命名持有人数目
	 */
	public int size() {
		return members.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public WorkSite duplicate() {
		return new WorkSite(this);
	}

}