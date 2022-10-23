/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.access.schema.*;
import com.laxcus.site.Node;

/**
 * 数据块索引分区。<br>
 * 
 * 数据块索引分区是平均分配集群数据块的记录。
 * 
 * @author scott.liang
 * @version 1.1 11/12/2015
 * @since laxcus 1.0
 */
public final class StubSector extends StubNote implements Comparable<StubSector> {

	private static final long serialVersionUID = -3725646233886259236L;

	/** 目标站点地址 **/
	private Node remote;

	/**
	 * 根据传入的数据块索引分区，生成它的数据副本
	 * @param that StubSector实例
	 */
	private StubSector(StubSector that) {
		super(that);
		setRemote(that.remote);
	}

	/**
	 * 构造默认的数据块索引分区
	 */
	public StubSector() {
		super();
	}

	/**
	 * 构造数据块索引分区，指定数据表名和目标地址
	 * @param space 表名
	 * @param remote 目标站点地址
	 */
	public StubSector(Space space, Node remote) {
		this();
		setSpace(space);
		setRemote(remote);
	}

	/**
	 * 从可类化数据读取器中解析数据块索引分区
	 * @param reader 可类化数据读取器
	 * @since l.1
	 */
	public StubSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置目标站点地址，不允许空值
	 * @param e 站点地址
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);

		remote = e;
	}

	/**
	 * 返回目标站点地址
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", getSpace(), remote);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubSector.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubSector) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSpace().hashCode() ^ remote.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StubSector that) {
		int ret = Laxkit.compareTo(getSpace(), that.getSpace());
		if (ret == 0) {
			ret = Laxkit.compareTo(remote, that.remote);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.StubNote#duplicate()
	 */
	@Override
	public StubNote duplicate() {
		return new StubSector(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.StubNote#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(remote);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.StubNote#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		remote = new Node(reader);
	}

}