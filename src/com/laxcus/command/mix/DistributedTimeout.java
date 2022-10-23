/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布处理超时命令。<br><br>
 * 
 * 处理对象包括命令和调用器，它们在内存中的驻留时间，一旦超过，将从所在管理池中删除，并超时信息给调用者。<br><br>
 * 这个参数在节点在“local.xml”的对应标签是“member-timeout”。
 * 
 * FRONT站点操作此命令，只修改自己节点的成员超时时间。<br>
 * WATCH节点操作此命令，可以修改自己的超时时间，或者调整集群或者某个站点的成员超时时间。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/15/2019
 * @since laxcus 1.0
 */
public class DistributedTimeout extends Command {

	private static final long serialVersionUID = -4226322327203184193L;
	
	/** 命令模式或者否 **/
	private boolean command;

	/** 成员超时时间 **/
	private long interval;
	
	/** 本地执行 **/
	private boolean local;

	/** 目标站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造默认的成员超时命令
	 */
	public DistributedTimeout() {
		super();
		command = true;
		interval = 0; // 默认是0，无限制
	}

	/**
	 * 生成成员超时命令的数据副本
	 * @param that DistributedTimeout实例
	 */
	private DistributedTimeout(DistributedTimeout that) {
		super(that);
		command = that.command;
		local = that.local;
		interval = that.interval;
		sites.addAll(that.sites);
	}
	
	/**
	 * 解析FIXP密文
	 * @param reader 可类化数据读取器
	 */
	public DistributedTimeout(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置为命令模式
	 * @param b 是或者否
	 */
	public void setCommand(boolean b) {
		command = b;
	}

	/**
	 * 判断是命令模式
	 * @return 是或者否
	 */
	public boolean isCommand() {
		return command;
	}

	/**
	 * 设置为调用器模式
	 * @param b 是或者否
	 */
	public void setInvoker(boolean b) {
		command = !b;
	}

	/**
	 * 判断是调用器模式
	 * @return 是或者否
	 */
	public boolean isInvoker() {
		return !command;
	}

	/**
	 * 设置本地执行
	 * @param b
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 判断是本地执行
	 * @return
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * 设置FIXP成员超时时间
	 * @param ms 最长操作时间
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回FIXP成员超时时间
	 * @return 最长操作时间
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * 保存一个站点地址，不允许空指针
	 * @param e Node实例
	 * @return 返回新增成员数目
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		// 存在，忽略它
		if (sites.contains(e)) {
			return false;
		}
		
		return sites.add(e);
	}

	/**
	 * 返回站点地址
	 * @return 节点列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}
	
	/**
	 * 清除全部
	 */
	public void clear() {
		sites.clear();
	}

	/**
	 * 设置一批站点地址
	 * @param a Node集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 返回站点数目
	 * @return 站点数目
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断站点是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 判断处理全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(command);
		writer.writeBoolean(local);
		writer.writeLong(interval);
		// 地址成员数目
		writer.writeInt(sites.size());
		// 保存地址
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		command = reader.readBoolean();
		local = reader.readBoolean();
		interval = reader.readLong();
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DistributedTimeout duplicate() {
		return new DistributedTimeout(this);
	}

}