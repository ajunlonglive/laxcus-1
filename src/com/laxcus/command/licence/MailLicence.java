/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.licence;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 投递新的许可证 <br><br>
 * 
 * 这个命令由WATCH节点投递给服务端节点<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/21/2020
 * @since laxcus 1.0
 */
public class MailLicence extends Command {

	private static final long serialVersionUID = -4342747106849770483L;

	/** 许可证内容  **/
	private byte[] content;
	
	/** 节点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/** 立即执行 **/
	private boolean immediate;
	
	/**
	 * 构造默认的重新设置节点的安全策略命令
	 */
	public MailLicence() {
		super();
		immediate = false;
	}

	/**
	 * 从可类化数据读取器中解析重新设置节点的安全策略命令
	 * @param reader 可类化数据读取器
	 */
	public MailLicence(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成重新设置节点的安全策略命令的数据副本
	 * @param that MailLicence实例
	 */
	private MailLicence(MailLicence that) {
		super(that);
		content = that.content;
		sites.addAll(that.sites);
		immediate = that.immediate;
	}
	
	/**
	 * 设置许可证内容
	 * @param b 字节数组
	 */
	public void setContent(byte[] b) {
		Laxkit.nullabled(b);
		content = b;
	}

	/**
	 * 返回许可证内容
	 * @return 字节数组
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 保存一个节点地址，不允许空指针
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 保存一批节点
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
	 * 地址成员数目
	 * @return 成员数目
	 */
	public int size() {
		return sites.size();
	}
	
	/**
	 * 清除全部!
	 */
	public void clear() {
		sites.clear();
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
	public MailLicence duplicate() {
		return new MailLicence(this);
	}


	/**
	 * 设置执行操作
	 * @param b
	 */
	public void setImmediate(boolean b) {
		immediate = b;
	}

	/**
	 * 判断要执行操作
	 * @return 返回真或者假
	 */
	public boolean isImmediate() {
		return immediate;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeByteArray(content);
		// 节点地址
		writer.writeInt(sites.size());
		for (Node node : sites) {
			writer.writeObject(node);
		}
		writer.writeBoolean(immediate);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		content = reader.readByteArray();
		// 节点地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
		immediate = reader.readBoolean();
	}

}