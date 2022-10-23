/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reload;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 设置应答数据流量控制参数。<br>
 * 
 * 设置应答接收队列的成员数。设置连接和流量的一种方法，依此参数，系统为每个连接分配合适的带宽。功能类似于Socket Block。 <br>
 * 
 * 命令只能由WATCH站点发起，分到不同的站点去执行。
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public class ReplyFlowControl extends Command {

	private static final long serialVersionUID = -3561155213533814428L;

	/** 数据流接收队列成员 **/
	private int block;

	/** 分配SOCKET读取一个包的时间 **/
	private int timeslice;
	
	/** 子包内容尺寸 **/
	private int subPacketContentSize;

	/** 站点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/** 判断在本地执行，释放本地内存 **/
	private boolean local;

	/**
	 * 构造默认的设置应答数据流量控制参数命令
	 */
	public ReplyFlowControl() {
		super();
		local = false;
		block = 0;
		timeslice = -1;
		subPacketContentSize = 0;
	}

	/**
	 * 从可类化数据读取器中解析设置应答数据流量控制参数命令
	 * @param reader 可类化数据读取器
	 */
	public ReplyFlowControl(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成设置应答数据流量控制参数命令的数据副本
	 * @param that ReplyFlowControl实例
	 */
	private ReplyFlowControl(ReplyFlowControl that) {
		super(that);
		sites.addAll(that.sites);
		local = that.local;
		block = that.block;
		timeslice = that.timeslice;
		subPacketContentSize = that.subPacketContentSize;
	}

	/**
	 * 设置数据流成员
	 * @param len
	 */
	public void setBlock(int len) {
		if (len > 0)
			block = len;
	}

	/**
	 * 返回数据流成员
	 * @return
	 */
	public int getBlock() {
		return block;
	}

	/**
	 * 设置UDP SOCKET读一个UDP包和分发的处理时间
	 * @param ns 微秒级
	 */
	public void setTimeslice(int ns) {
		timeslice = ns;
	}

	/**
	 * 返回UDP SOCKET读一个UDP包和分发的处理时间
	 * @return 微秒级
	 */
	public int getTimeslice() {
		return timeslice;
	}

	/**
	 * 设置子包内容尺寸
	 * @param len
	 */
	public void setSubPacketContentSize(int len) {
		subPacketContentSize = len;
	}

	/**
	 * 返回子包内容尺寸
	 * @return 整数
	 */
	public int getSubPacketContentSize() {
		return subPacketContentSize;
	}

	/**
	 * 设置为本地执行
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 判断是本地执行
	 * @return 返回真或者假
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * 保存一个站点地址，不允许空指针
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 保存一批站点
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 输出全部站点地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 清除地址
	 */
	public void clear() {
		sites.clear();
	}

	/**
	 * 地址成员数目
	 * @return 成员数目
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断是全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReplyFlowControl duplicate() {
		return new ReplyFlowControl(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(block);
		writer.writeInt(timeslice);
		writer.writeInt(subPacketContentSize);
		writer.writeBoolean(local);
		writer.writeInt(sites.size());
		for (Node node : sites) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		block = reader.readInt();
		timeslice = reader.readInt();
		subPacketContentSize = reader.readInt();
		local = reader.readBoolean();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}