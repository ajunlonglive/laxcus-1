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
 * 设置应答包尺寸。<br>
 * 
 * 对应ReplyTransfer.defaultPacketSize 和 ReplyTransfer.defaultSubPacketSize <br>
 * 
 * 命令只能由WATCH站点发起，分到不同的站点去执行。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public class ReplyPacketSize extends Command {

	private static final long serialVersionUID = 7793191611452163024L;

	/** FIXP包集尺寸 **/
	private int packetSize;
	
	/** FIXP子包尺寸 **/
	private int subPacketSize;
	
	/** 公网 **/
	private boolean wide;
	
	/** 站点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();
	
	/** 判断在本地执行，释放本地内存 **/
	private boolean local;

	/**
	 * 构造默认的设置应答包尺寸命令
	 */
	public ReplyPacketSize() {
		super();
		local = false;
		wide = false;
	}

	/**
	 * 从可类化数据读取器中解析设置应答包尺寸命令
	 * @param reader 可类化数据读取器
	 */
	public ReplyPacketSize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成设置应答包尺寸命令的数据副本
	 * @param that ReplyPacketSize实例
	 */
	private ReplyPacketSize(ReplyPacketSize that) {
		super(that);
		sites.addAll(that.sites);
		local = that.local;
		wide = that.wide;
		packetSize = that.packetSize;
		subPacketSize = that.subPacketSize;
	}

	/**
	 * 设置FIXP包集尺寸
	 * @param len
	 */
	public void setPacketSize(int len) {
		packetSize = len;
	}

	/**
	 * 返回FIXP包集尺寸
	 * @return
	 */
	public int getPacketSize() {
		return packetSize;
	}
	
	/**
	 * 设置FIXP子包尺寸
	 * @param len
	 */
	public void setSubPacketSize(int len) {
		subPacketSize = len;
	}

	/**
	 * 返回FIXP子包尺寸
	 * @return
	 */
	public int getSubPacketSize() {
		return subPacketSize;
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
	
	public void setWide(boolean b) {
		wide = b;
	}
	
	public boolean isWide() {
		return wide;
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
	 * 清除节点
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
	public ReplyPacketSize duplicate() {
		return new ReplyPacketSize(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(local);
		writer.writeBoolean(wide);
		writer.writeInt(packetSize);
		writer.writeInt(subPacketSize);
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
		local = reader.readBoolean();
		wide = reader.readBoolean();
		packetSize = reader.readInt();
		subPacketSize = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}