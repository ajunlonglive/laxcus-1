/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 消息拒止。<br>
 * 
 * 拒绝处理某些节点或者全部节点的消息
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class NoticeMuffler extends MutexHandler {

	/** 判断是全部 **/
	private boolean all;

	/** 被拒绝的节点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认的消息拒止
	 */
	public NoticeMuffler() {
		super();
		all = false;
	}
	
	/**
	 * 重置参数
	 */
	public void reset() {
		all = false;
		sites.clear();
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
		super.lockSingle();
		try {
			return sites.add(e);
		} finally {
			super.unlockSingle();
		}
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
	public boolean remove(Node e) {
		Laxkit.nullabled(e);
		super.lockSingle();
		try {
			return sites.remove(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除一批节点
	 * @param a 节点地址
	 * @return 返回删除数目
	 */
	public int removeAll(Collection<Node> a) {
		int count = sites.size();
		for (Node e : a) {
			remove(e);
		}
		return count - sites.size();
	}
	
	/**
	 * 清除保存的全部节点
	 */
	public void clear() {
		super.lockSingle();
		try {
			sites.clear();
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 判断是拒绝
	 * @param e 节点地址
	 * @return 返回真或者假
	 */
	public boolean isRefuse(Node e) {
		boolean refuse = all;
		super.lockMulti();
		try {
			if (!refuse) {
				refuse = sites.contains(e);
			}
		} finally {
			super.unlockMulti();
		}
		return refuse;
	}

}
