/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.licence;

import java.io.*;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 发布许可证文件。<br>
 * 
 * 这个命令只能由WATCH站点发起，投递给集群服务器任何节点。
 * 
 * @author scott.liang
 * @version 1.0 7/21/2020
 * @since laxcus 1.0
 */
public class PublishLicence extends Command {

	private static final long serialVersionUID = 3055415239876983191L;
	
	/** 许可证文件 **/
	private File file;

	/** 站点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();
	
	/** 立即执行 **/
	private boolean immediate;

	/**
	 * 构造默认的重新加载动态链接库命令
	 */
	public PublishLicence() {
		super();
		immediate = false;
	}

	/**
	 * 从可类化数据读取器中解析重新加载动态链接库命令
	 * @param reader 可类化数据读取器
	 */
	public PublishLicence(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成重新加载动态链接库命令的数据副本
	 * @param that PublishLicence实例
	 */
	private PublishLicence(PublishLicence that) {
		super(that);
		file = that.file;
		sites.addAll(that.sites);
		immediate = that.immediate;
	}

	/**
	 * 保存磁盘文件
	 * @param e
	 */
	public void setFile(File e) {
		Laxkit.nullabled(e);
		file = e;
	}

	/**
	 * 返回磁盘文件
	 * @return
	 */
	public File getFile() {
		return file;
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
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PublishLicence duplicate() {
		return new PublishLicence(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeFile(file);
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
		file = reader.readFile();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
		immediate = reader.readBoolean();
	}

}