/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.Node;

/**
 * 数据构建标识。<br>
 * 
 * 由数据表名和站点地址组成。
 * 
 * @author scott.liang
 * @version 1.1 9/19/2015
 * @since laxcus 1.0
 */
public final class EstablishFlag implements Classable, Cloneable, Serializable, Comparable<EstablishFlag> {

	private static final long serialVersionUID = -7760036930747883296L;

	/** 数据表名 **/
	private Space space;

	/** 数据源站点地址 **/
	private Node source;

	/**
	 * 将数据构建标识参数写入可类化写入器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(space);
		writer.writeObject(source);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中读取数据构建标识参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		space = new Space(reader);
		source = new Node(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认和私有的数据构建标识
	 */
	private EstablishFlag() {
		super();
	}

	/**
	 * 根据传入的数据构建标识，生成它的数据副本
	 * @param that EstablishFlag实例
	 */
	private EstablishFlag(EstablishFlag that) {
		this();
		space = that.space.duplicate();
		source = that.source.duplicate();
	}

	/**
	 * 构造数据构建标识，指定数据表名和源主机地址
	 * @param space 数据表名
	 * @param source 站点地址
	 */
	public EstablishFlag(Space space, Node source) {
		this();
		setSpace(space);
		setSource(source);
	}

	/**
	 * 从可类化读取器中解析数据构建的扫描标识
	 * @param reader 可类化读取器
	 * @since 1.1 
	 */
	public EstablishFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从字节数组中解析数据构建的扫描标识
	 * @param b 字节数组
	 * @param off 开始位置
	 * @param len 有效长度
	 */
	public EstablishFlag(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		// 不允许空值
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据源站点地址。DATA/BUILD站点地址
	 * @param e Node实例
	 */
	public void setSource(Node e) {
		// 不允许空值
		Laxkit.nullabled(e);

		source = e;
	}

	/**
	 * 返回数据源站点地址
	 * @return Node实例
	 */
	public Node getSource() {
		return source;
	}

	/**
	 * 产生一个当前实例的完整数据副本
	 * @return EstablishFlag实例
	 */
	public EstablishFlag duplicate() {
		return new EstablishFlag(this);
	}

	/**
	 * 检查两个数据构建的扫描标识实例是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != EstablishFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EstablishFlag) that) == 0;
	}

	/**
	 * 返回数据构建的扫描标识的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode() ^ source.hashCode();
	}

	/**
	 * 返回数据构建的扫描标识的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s$%s", space, source);
	}
	
	/**
	 * 根据当前实例，克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个数据构建标识的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EstablishFlag that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		int ret = space.compareTo(that.space);
		if (ret == 0) {
			ret = source.compareTo(that.source);
		}
		return ret;
	}

	/**
	 * 输出全部字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 解析字节数组
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}