/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置对称密钥长度
 * 
 * 对称密钥包括客户机和服务器两种。
 * 
 * @author scott.liang
 * @version 1.0 2/27/2021
 * @since laxcus 1.0
 */
public class SetSecureSize extends Command {

	private static final long serialVersionUID = 2312354891009239387L;

	/** 本地执行 **/
	private boolean local;
	
	/** 节点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();
	
	/** 客户机对称密钥长度 **/
	private int clientBits;

	/** 服务器对称密钥长度 **/
	private int serverBits;

	/**
	 * 构造默认的设置对称密钥长度命令
	 */
	public SetSecureSize() {
		super();
		local = false;
		clientBits = serverBits = 0;
	}

	/**
	 * 从可类化数据读取器中解析设置对称密钥长度命令
	 * @param reader 可类化数据读取器
	 */
	public SetSecureSize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成设置对称密钥长度命令的数据副本
	 * @param that SetSecureSize实例
	 */
	private SetSecureSize(SetSecureSize that) {
		super(that);
		local = that.local;
		clientBits = that.clientBits;
		serverBits = that.serverBits;
		sites.addAll(that.sites);
	}

	/**
	 * 设置本地执行
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 返回本地执行
	 * @return 真或者假
	 */
	public boolean isLocal() {
		return local;
	}
	
	/**
	 * 设置客户机对称密钥长度
	 * @param bits 以数位为单位的密钥长度，必须是8的倍数
	 */
	public void setClientBits(int bits) {
		clientBits = bits;
	}
	
	/**
	 * 返回客户机对称密钥长度
	 * @return 以数位为单位的密钥长度
	 */
	public int getClientBits() {
		return clientBits;
	}

	/**
	 * 设置服务器对称密钥长度
	 * @param bits 以数位为单位的密钥长度，必须是8的倍数
	 */
	public void setServerBits(int bits) {
		serverBits = bits;
	}
	
	/**
	 * 返回服务器对称密钥长度
	 * @return 以数位为单位的密钥长度
	 */
	public int getServerBits() {
		return serverBits;
	}
	
	/**
	 * 保存一个节点地址，不允许空指针
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		// 如果存在，返回假
		if (sites.contains(e)) {
			return false;
		}

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
	 * 输出全部节点地址
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(local);
		writer.writeInt(clientBits);
		writer.writeInt(serverBits);
		// 节点地址
		writer.writeInt(sites.size());
		for (Node node : sites) {
			writer.writeObject(node);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		local = reader.readBoolean();
		clientBits = reader.readInt();
		serverBits = reader.readInt();
		// 节点地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSecureSize duplicate() {
		return new SetSecureSize(this);
	}

}