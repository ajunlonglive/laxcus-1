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
 * 重新加载节点下的许可证。<br>
 * 许可证位于“conf/licence”，重新加载新的许可配置。 <br>
 * 
 * 这个命令只能由WATCH节点发起，分到服务端节点去执行，包括：TOP/HOME/BANK，LOG，DATA/WORK/BUILD/CALL, ACCOUNT/HASH/GATE/ENTRANCE。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class ReloadLicence extends Command {

	private static final long serialVersionUID = -3400731867372478408L;

	/** 节点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认的重新设置节点的安全策略命令
	 */
	public ReloadLicence() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析重新设置节点的安全策略命令
	 * @param reader 可类化数据读取器
	 */
	public ReloadLicence(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成重新设置节点的安全策略命令的数据副本
	 * @param that ReloadLicence实例
	 */
	private ReloadLicence(ReloadLicence that) {
		super(that);
		sites.addAll(that.sites);
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
	public ReloadLicence duplicate() {
		return new ReloadLicence(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
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
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}