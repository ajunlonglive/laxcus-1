/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据处理诊断命令。<br><br>
 * 
 * 当命令请求节点收到全部数据处理节点反馈的数据处理结果后，进行汇总诊断，做出确认、取消两种动作，发送到数据处理节点。<br<br
 * 
 * @author scott.liang
 * @version 1.1 5/25/2015
 * @since laxcus 1.0
 */
public abstract class AssertConsult extends Consult {

	private static final long serialVersionUID = -2158953299071549263L;

	/** 关联检测站点。用于各节点的相互确认 **/
	private TreeSet<Cabin> sites = new TreeSet<Cabin>();

	/**
	 * 构造默认的数据处理诊断命令
	 */
	protected AssertConsult() {
		super();
	}

	/**
	 * 生成数据处理诊断命令的数据副本
	 * @param that 数据处理诊断命令
	 */
	protected AssertConsult(AssertConsult that) {
		super(that);
		sites.addAll(that.sites);
	}

	/**
	 * 设置回复状态，见ConsultStatus定义。
	 * 
	 * @param who 回复状态
	 */
	public final void setStatus(byte who) {
		if (!ConsultStatus.isAssert(who)) {
			throw new IllegalValueException("illegal status %d", who);
		}
		super.setStatus(who);
	}

	/**
	 * 判断是CALL站点确认成功（在接收到DATA站点成功基础上的判断）
	 * @return  返回真或者假
	 */
	public boolean isConfirm() {
		return ConsultStatus.isConfirm(getStatus());
	}

	/**
	 * 判断是CALL站点确认取消（当任何一个节点执行失败，即产生取消动作）
	 * @return  返回真或者假
	 */
	public boolean isCancel() {
		return ConsultStatus.isCancel(getStatus());
	}

	/**
	 * 保存关联检测站点
	 * @param e 关联检测站点
	 * @return 成功返回真，否则假
	 */
	public boolean addSeekSite(Cabin e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 保存一批关联检测站点
	 * @param a 关联检测站点数组
	 * @return 返回新增成员数目
	 */
	public int addSeekSites(Collection<Cabin> a) {
		int size = sites.size();
		for (Cabin e : a) {
			addSeekSite(e);
		}
		return sites.size() - size;
	}

	/**
	 * 判断一个站点在关联检测站点集合里
	 * @param e 关联检测站点
	 * @return 返回真或者假
	 */
	public boolean isSeekSite(Cabin e) {
		return sites.contains(e);
	}

	/**
	 * 删除关联检测站点
	 * @param e 关联检测站点
	 * @return 成功返回真，否则假
	 */
	public boolean removeSeekSite(Cabin e) {
		return sites.remove(e);
	}

	/**
	 * 输出全部关联检测站点
	 * @return Cabin列表
	 */
	public List<Cabin> getSeekSites() {
		return new ArrayList<Cabin>(sites);
	}

	/**
	 * 返回关联检测站点数目
	 * @return 检测站点的整数
	 */
	public int getSeekSiteSize() {
		return sites.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 保存环形站点
		writer.writeInt(sites.size());
		for(Cabin node : sites) {
			writer.writeObject(node);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析上级参数
		super.resolveSuffix(reader);
		// 生成环形站点
		int size = reader.readInt();
		for(int i = 0; i < size; i++) {
			Cabin cabin = new Cabin(reader);
			sites.add(cabin);
		}
	}

}