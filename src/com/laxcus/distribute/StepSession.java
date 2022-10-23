/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;
import com.laxcus.site.Node;

/**
 * 分布阶段会话 <br>
 * 
 * StepSession是“动态”的运行实例。在命令的运行过程中产生，通过网络传输到目标站点上，携带着目标站点需要的运行数据。
 * ESTABLISH和CONDUCT的StepSession从它派生。<br>
 * 
 * @author scott.liang
 * @version 1.3 4/2/2015
 * @since laxcus 1.0
 */
public abstract class StepSession extends AccessObject implements Comparable<StepSession> {

	private static final long serialVersionUID = -6336015456682775786L;

	/** 分布会话执行阶段，包括FROM/TO(SUBTO)，和SCAN/SIFT/RISE，见PhaseTag中的定义 **/
	private int family;

	/** 目标站点地址。FROM/SCAN/RISE阶段指向DATA节点，TO阶段指向WORK节点，SIFT阶段指向BUILD节点。**/
	private Node remote;

	/** 同级关联站点地址 **/
	private TreeSet<Node> buddies = new TreeSet<Node>();

	/** 回显地址的数字签名，由任务发起方设置。一般是CALL站点。**/
	private SHA256Hash master;

	/** 会话实例在所属集合的编号，编号从0开始，逐次递增1。无效值是-1。**/
	private int number;

	/** 已经使用标记 **/
	private boolean used;

	/**
	 * 将StepSession参数写入可类化存储器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 分布会话执行阶段
		writer.writeInt(family);
		// CALL节点回显地址的数字签名
		writer.writeObject(master);
		// 目标站点地址
		writer.writeObject(remote);
		// 关联节点
		writer.writeInt(buddies.size());
		for (Node e : buddies) {
			writer.writeObject(e);
		}
		// 会话编号
		writer.writeInt(number);
		// 使用标记
		writer.writeBoolean(used);
	}

	/**
	 * 从可类化读取器中解析StepSession参数
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
		// 分布会话执行阶段
		family = reader.readInt();
		// CALL节点回显地址的数字签名
		master = new SHA256Hash (reader);
		// 目标站点地址
		remote = new Node(reader);
		// 关联节点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			buddies.add(e);
		}
		// 会话编号
		number = reader.readInt();
		// 已经使用标记
		used = reader.readBoolean();
	}

	/**
	 * 构造StepSession实例，同时指定它的分布会话执行阶段
	 * @param family 分布会话执行阶段，包括FROM/TO(SUBTO)，和SCAN/SIFT/RISE，见PhaseTag中的定义
	 */
	protected StepSession(int family) {
		super();
		setFamily(family);
		number = -1; // -1是无定义。
		used = false; // 默认是没有使用
	}

	/**
	 * 根据传入的StepSession实例，构造它的浅层数据副本
	 * @param that StepSession实例
	 */
	protected StepSession(StepSession that) {
		super(that);
		family = that.family;
		number = that.number;
		remote = that.remote;
		master = that.master;
		used = that.used;
	}

	/**
	 * 设置会话阶段类型
	 * @param who 分布会话执行阶段
	 */
	public void setFamily(int who) {
		if (!PhaseTag.isSession(who)) {
			throw new IllegalValueException("illegal family %d", who);
		}
		family = who;
	}

	/**
	 * 返回会话阶段类型
	 * @return 分布会话执行阶段
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断是“CONDUCT.FROM”阶段会话
	 * @return 返回真或者假
	 */
	public boolean isFrom() {
		return PhaseTag.isFrom(family);
	}

	/**
	 * 判断是“CONDUCT.TO”阶段会话
	 * @return 返回真或者假
	 */
	public boolean isTo() {
		return PhaseTag.isTo(family);
	}

	/**
	 * 判断是“ESTABLISH.SCAN”阶段会话
	 * @return 返回真或者假
	 */
	public boolean isScan() {
		return PhaseTag.isScan(family);
	}

	/**
	 * 判断是“ESTABLISH.SIFT”阶段会话
	 * @return 返回真或者假
	 */
	public boolean isSift() {
		return PhaseTag.isSift(family);
	}
	
	/**
	 * 判断是“ESTABLISH.RISE”阶段会话
	 * @return 返回真或者假
	 */
	public boolean isRise() {
		return PhaseTag.isRise(family);
	}
	
	/**
	 * 判断是“CONTACT.DISTANT”阶段会话
	 * @return 返回真或者假
	 */
	public boolean isDistant() {
		return PhaseTag.isDistant(family);
	}

	/**
	 * 设置回显地址的数字签名
	 * @param e SHA256Hash 实例
	 */
	public void setMaster(SHA256Hash  e) {
		Laxkit.nullabled(e);
		master = e;
	}

	/**
	 * 返回回显地址的数字签名
	 * @return 返回SHA256Hash 实例
	 */
	public SHA256Hash  getMaster() {
		return master;
	}

	/**
	 * 设置目标节点地址（DATA/WORK/BUILD三种）
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回目标节点地址
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}
	
	/**
	 * 保存一个关联节点地址，不能是自己的目标节点
	 * @param e 节点地址
	 */
	public void addBuddy(Node e) {
		Laxkit.nullabled(e);
		if (Laxkit.compareTo(remote, e) != 0) {
			buddies.add(e);
		}
	}

	/**
	 * 保存一批关联节点地址
	 * @param a 节点数组
	 */
	public void addBuddies(Collection<Node> a) {
		for (Node e : a) {
			addBuddy(e);
		}
	}

	/**
	 * 输出全部关联节点地址，不包括它自己
	 * @return 返回节点列表
	 */
	public List<Node> getBuddies() {
		return new ArrayList<Node>(buddies);
	}

	/**
	 * 从传入的StepSession对象集中找到最大的编号，然后在此基础上加1，即是当前会话的编号。
	 * @param array 传入的对象，必须是会话对象的子类。
	 */
	public void doNumber(Object[] array) {
		int max = -1;
		int size = (array == null ? 0 : array.length);
		for (int i = 0; i < size; i++) {
			if (array[i] instanceof StepSession) {
				StepSession session = (StepSession) array[i];
				if (session.getNumber() > max) {
					max = session.getNumber();
				}
			}
		}
		number = max + 1;
	}

	/**
	 * 设置会话在集合中的序列编号
	 * @param i 序列编号
	 */
	public void setNumber(int i) {
		number = i;
	}

	/**
	 * 返回会话在集合中的序列编号
	 * @return 序列编号
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * 设置会话运用标记
	 * @param b 运用标记
	 */
	public void setUsed(boolean b) {
		used = b;
	}

	/**
	 * 判断会话是否已经使用
	 * @return 返回真或者假
	 */
	public boolean isUsed() {
		return used;
	}

	/**
	 * 计算StepSession实例的排列位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StepSession that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = remote.compareTo(that.remote);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(number, that.number);
		}
		return ret;
	}

}