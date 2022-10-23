/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.site.*;

/**
 * 内网定位定时检测单元 <br>
 * 
 * LAXCUS大数据操作系统里的“shine”命令，网关检测内网节点在NAT设备上的地址，然后返回给内网节点。
 * 
 * @author scott.liang
 * @version 1.0 8/2/2018
 * @since laxcus 1.0
 */
public final class PockItem implements Cloneable, Comparable<PockItem> {
	
	/** 网关服务器 **/
	private Node hub;

	/** 网关主机地址 **/
	private SocketHost remote;

	/** 本地主机的NAT设备地址 **/
	private SocketHost localNAT;

	/** 刻度时间 **/
	private volatile long seekTime;

	/**
	 * 构造内网定位定时检测单元
	 * @param hub 服务器地址
	 * @param remote 网关主机地址
	 * @param local 本地主机的NAT设备地址
	 */
	public PockItem(Node hub, SocketHost remote, SocketHost local) {
		super();
		setHub(hub);
		setRemote(remote);
		setLocalNAT(local);
		refreshTime();
	}
	
	/**
	 * 构造内网定位定时检测单元
	 * @param remote 网关主机地址
	 * @param local 本地主机的NAT设备地址
	 */
	public PockItem(SocketHost remote, SocketHost local) {
		this(null, remote, local);
	}
	
	/**
	 * 构造内网定位定时检测单元副本
	 * @param that 内网定位定时检测单元
	 */
	private PockItem(PockItem that) {
		super();
		hub = that.hub;
		remote = that.remote;
		localNAT = that.localNAT;
		seekTime = that.seekTime;
	}

	/**
	 * 更新时间
	 */
	public void refreshTime() {
		seekTime = System.currentTimeMillis();
	}

	/**
	 * 判断达到超时删除时间<br><br>
	 * 
	 * @param timeout 超时时限
	 * @return 返回真或者假。
	 */
	public boolean isTimeout(long timeout) {
		return (System.currentTimeMillis() - seekTime >= timeout);
	}

	/**
	 * 设置目标节点服务器，允许空指针
	 * @param e 目标节点服务器
	 */
	public void setHub(Node e) {
		if (e == null) {
			hub = null;
		} else {
			hub = e.duplicate();
		}
	}

	/**
	 * 返回目标节点服务器
	 * @return
	 */
	public Node getHub() {
		return hub;
	}

	/**
	 * 设置网关主机地址
	 * @param e 网关主机地址实例
	 */
	public void setRemote(SocketHost e) {
		Laxkit.nullabled(e);
		remote = e.duplicate();
	}

	/**
	 * 输出网关主机地址
	 * @return 网关主机地址实例
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 设置本地主机的NAT设备地址
	 * @param e 本地主机的NAT设备地址实例
	 */
	public void setLocalNAT(SocketHost e) {
		Laxkit.nullabled(e);
		localNAT = e.duplicate();
	}

	/**
	 * 输出本地主机的NAT设备地址
	 * @return 本地主机的NAT设备地址实例
	 */
	public SocketHost getLocalNAT() {
		return localNAT;
	}

	/**
	 * 复制PockItem子类对象的浅层数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/**
	 * PockItem子类对象生成自己的浅层数据副本。<br>
	 * 浅层数据副本和标准数据副本的区别在于：浅层数据副本只赋值对象，而不是复制对象内容本身。
	 * @return PockItem子类实例
	 */
	public PockItem duplicate() {
		return new PockItem(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((PockItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return remote.hashCode() ^ localNAT.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PockItem that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(remote, that.remote);
		if (ret == 0) {
			ret = Laxkit.compareTo(localNAT, that.localNAT);
		}
		return ret;
	}
}