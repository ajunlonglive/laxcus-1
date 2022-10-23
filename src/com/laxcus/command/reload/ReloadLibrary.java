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
 * 重新加载各节点上的本地动态链接库。<br>
 * 根据JVM提示，只对初次加载起作用，二次或以后加载不起作用。<br>
 * 这个方法是在不停机情况下，载入新的动态链接库。<br><br>
 * 
 * 这个命令只能由WATCH站点发起，分到不同的站点去执行。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class ReloadLibrary extends Command {

	private static final long serialVersionUID = 3055415239876983191L;

	/** 站点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认的重新加载动态链接库命令
	 */
	public ReloadLibrary() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析重新加载动态链接库命令
	 * @param reader 可类化数据读取器
	 */
	public ReloadLibrary(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成重新加载动态链接库命令的数据副本
	 * @param that ReloadLibrary实例
	 */
	private ReloadLibrary(ReloadLibrary that) {
		super(that);
		sites.addAll(that.sites);
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
	 * 清除地址
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
	public ReloadLibrary duplicate() {
		return new ReloadLibrary(this);
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