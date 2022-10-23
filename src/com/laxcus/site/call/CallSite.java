/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.call;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;

/**
 * CALL站点。<br><br>
 * CALL站点是网关站点，注册到多个HOME集群，其中一个本集群，还有多个辅集群。
 * 
 * 记录它可以受理的用户账号，以及本地的阶段任务命名。<br>
 * 
 * @author scott.liang
 * @version 1.3 4/15/2015
 * @since laxcus 1.0
 */
public final class CallSite extends GatewaySite {

	private static final long serialVersionUID = 3174237320062236760L;

	/** 账号签名 -> CALL站点成员 **/
	private Map<Siger, CallMember> members = new TreeMap<Siger, CallMember>();

	/** 数据表名 -> CALL站点成员 **/
	private Map<Space, CallMember> spaces = new TreeMap<Space, CallMember>();

	/** 阶段命名 -> CALL站点成员 **/
	private Map<Phase, CallMember> phases = new TreeMap<Phase, CallMember>();

	/**
	 * 将CALL站点属性写入可类化写入器
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 写入参数
		writer.writeInt(members.size());
		for(CallMember e : members.values()) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器中解析CALL站点属性信息
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 读参数
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CallMember member = new CallMember(reader);
			Siger siger = member.getSiger();
			// 保存CALL站点成员
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
	 * 根据传入的CALL站点实例，生成它的副本
	 * @param that CallSite实例
	 */
	private CallSite(CallSite that) {
		super(that);
		members.putAll(that.members);
		spaces.putAll(that.spaces);
		phases.putAll(that.phases);
	}

	/**
	 * 构造一个默认的CALL站点
	 */
	public CallSite() {
		super(SiteTag.CALL_SITE);
	}

	/**
	 * 从可类化读取器中解析CALL站点地址
	 * @param reader 可类化读取器
	 * @since 1.3
	 */
	public CallSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 重置全部参数，但是不能够清除用户名称
	 */
	public void reset() {
		setMoment(null);
		members.clear();
		spaces.clear();
		phases.clear();
	}

	/**
	 * 根据用户签名分配一个CALL站点成员。<br>
	 * 如果CALL站点成员存在，返回它；否则建立新的CALL站点成员实例并且返回它。
	 * 
	 * @param siger 用户签名
	 * @return CallMember实例
	 * @throws NullPointerException - 如果用户签名是空值是
	 */
	private CallMember allocate(Siger siger) {
		Laxkit.nullabled(siger);

		CallMember member = members.get(siger);
		if (member == null) {
			member = new CallMember(siger);
			members.put(siger, member);
		}
		return member;
	}

	/**
	 * 建立一个CALL站点成员。这项操作在启动或者分配新账号时设置
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean create(Siger siger) {
		Laxkit.nullabled(siger);

		return allocate(siger) != null;
	}

	/**
	 * 根据用户签名查找CALL站点成员
	 * @param siger 用户签名
	 * @return CallMember实例
	 */
	public CallMember find(Siger siger) {
		Laxkit.nullabled(siger);

		return members.get(siger);
	}

	/**
	 * 判断CALL站点成员存在
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

		CallMember member = members.remove(siger);
		boolean success = (member != null);
		if (success) {
			for (Space space : member.getTables()) {
				spaces.remove(space);
			}
			for (Phase phase : member.getPhases()) {
				phases.remove(phase);
			}
		}
		return success;
	}

	/**
	 * 返回全部注册用户
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(members.keySet());
	}

	/**
	 * 输出CALL节点成员
	 * @return CallMember列表
	 */
	public List<CallMember> getMembers() {
		return new ArrayList<CallMember>(members.values());
	}

	/**
	 * 设置云端空间
	 * @param field
	 * @return
	 */
	public boolean setCloudField(CloudField field) {
		Siger username = field.getSiger();
		if (username == null) {
			return false;
		}
		// 生成单元
		CallMember member = allocate(username);
		// 设置公网地址
		field.setSite(getGatewayNode().getPublic());
		// 设置云端空间
		member.setCloudField(field);
		return true;
	}

	/**
	 * 设置云端空间
	 * @param siger
	 * @param maxCapacity
	 * @param usedCapacity
	 * @return 设置成功返回真，否则假
	 */
	public boolean setCloudField(Siger siger, long maxCapacity, long usedCapacity) {
		CloudField field = new CloudField(maxCapacity, usedCapacity);
		field.setSiger(siger);
		return setCloudField(field);
	}

	/**
	 * 查找云端空间
	 * @param siger 签名
	 * @return 返回CloudField实例，没有是空指针
	 */
	public CloudField findCloudField(Siger siger) {
		CallMember member = findMember(siger);
		if (member != null) {
			return member.getCloudField();
		}
		return null;
	}

	/**
	 * 增加一个注册账号下的表名
	 * @param siger 账号签名
	 * @param space 数据表名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addSpace(Siger siger, Space space) {
		CallMember member = allocate(siger);
		// 保存签名
		boolean success = member.addTable(space);
		if (success) {
			spaces.put(space, member);
		}
		return success;
	}

	/**
	 * 保存一批表名
	 * @param siger 账号签名
	 * @param spaces 表名列表
	 * @return 返回保存的表名数目
	 */
	public int addSpaces(Siger siger, Collection<Space> spaces) {
		int count = 0;
		for (Space space : spaces) {
			boolean success = addSpace(siger, space);
			if (success) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 返回全部数据表名
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(spaces.keySet());
	}

	/**
	 * 增加阶段命名。拒绝系统级阶段命名
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
		// 分配成员
		CallMember member = allocate(phase.getIssuer());
		boolean success = member.addPhase(phase);
		if (success) {
			phases.put(phase, member);
		}
		return success;
	}

	/**
	 * 根据签名查找CALL站点成员
	 * @param siger 用户签名
	 * @return CALL站点成员
	 */
	public CallMember findMember(Siger siger) {
		return members.get(siger);
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
	 * 判断阶段命名存在
	 * @param phase 阶段命名
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean contains(Phase phase) {
		return phases.get(phase) != null;
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
	 * @return 账号数目
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
	public CallSite duplicate() {
		return new CallSite(this);
	}

}