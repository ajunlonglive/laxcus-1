/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 转发检查被WATCH站点监视的站点状态。<br>
 * 命令只在WATCH站点使用，只限管理员使用。
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public class ShiftSeekSiteRuntime extends ShiftCommand {

	private static final long serialVersionUID = 7208377295174908229L;

	/** 全部被监视的站点 **/ 
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 根据传入的转发检查被WATCH站点监视的站点状态，生成数据副本
	 * @param that ShiftSeekSiteRuntime实例
	 */
	private ShiftSeekSiteRuntime(ShiftSeekSiteRuntime that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的转发检查被WATCH站点监视的站点状态
	 */
	public ShiftSeekSiteRuntime() {
		super();
	}

	/**
	 * 构造转发检查被WATCH站点监视的站点状态，指定节点
	 * @param e 节点
	 */
	public ShiftSeekSiteRuntime(Node e) {
		this();
		add(e);
	}

	/**
	 * 构造转发检查被WATCH站点监视的站点状态，保存一组记录
	 * @param a Node数组
	 */
	public ShiftSeekSiteRuntime(List<Node> a) {
		this();
		addAll(a);
	}

	/**
	 * 保存一个节点地址
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存全部节点地址
	 * @param a Node列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Node> a) {
		int size = array.size();
		for (Node e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部节点地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 判断地址包含
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean contains(Node e) {
		return array.contains(e);
	}

	/**
	 * 统计全部节点地址数目
	 * @return 地址数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断全部节点地址数目空值
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSeekSiteRuntime duplicate() {
		return new ShiftSeekSiteRuntime(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}

}