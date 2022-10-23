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
 * 切换通知的显示。<br>
 * WATCH节点收到“警告/错误”消息时，是不是显示出来。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public abstract class ProcessSiteNotice extends Command {

	private static final long serialVersionUID = 6862419217918421864L;

	/** 判断是全部 **/
	private boolean all;

	/** 被拒绝的节点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认的切换通知的显示
	 */
	protected ProcessSiteNotice() {
		super();
	}

	/**
	 * 生成切换通知的显示副本
	 * @param that 切换通知的显示
	 */
	protected ProcessSiteNotice(ProcessSiteNotice that) {
		super(that);
		all = that.all;
		sites.addAll(that.sites);
	}
	
	/**
	 * 全部限制
	 * @param b 真或者假
	 */
	public void setAll(boolean b) {
		all = b;
	}

	/**
	 * 判断是全部限制
	 * @return 真或者假
	 */
	public boolean isAll() {
		return all;
	}

	/**
	 * 保存节点
	 * @param e 节点地址
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		return sites.add(e);
	}
	
	/**
	 * 保存一批节点
	 * @param a 全部节点
	 * @return 返回新增数目
	 */
	public int addAll(Collection<Node> a) {
		int count = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - count;
	}

	/**
	 * 撤销节点
	 * @param e 节点地址
	 * @return 返回真或者假
	 */
	public boolean remote(Node e) {
		Laxkit.nullabled(e);
			return sites.remove(e);
	}
	
	/**
	 * 全部节点
	 * @return Node集合
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(all);
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		all = reader.readBoolean();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}