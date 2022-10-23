/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.meta;

import java.io.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 元数据标识。由调用器编号、阶段命名、迭代编号组成，表示一批元数据的唯一性。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public final class MetaTag implements Classable, Cloneable, Serializable, Comparable<MetaTag> {

	private static final long serialVersionUID = 3170851602536433870L;

	/** 调用器编号 **/
	private long invokerId;
	
	/** 使用者 **/
	private Siger issuer;

	/** 阶段命名 **/
	private Phase phase;
	
	/** 关联对象的迭代编号 **/
	private int iterateIndex;

	/**
	 * 构造默认和私有的元数据标识
	 */
	private MetaTag() {
		super();
		invokerId = InvokerIdentity.INVALID;
		iterateIndex = 0;
	}

	/**
	 * 根据传入的元数据标识，生成它的浅层数据副本。
	 * @param that MetaTag实例
	 */
	private MetaTag(MetaTag that) {
		this();
		invokerId = that.invokerId;
		issuer = that.issuer;
		phase = that.phase;
		iterateIndex = that.iterateIndex;
	}

	/**
	 * 构造元数据标识，指定参数
	 * @param invokerId 调用器编号
	 * @param issuer 用户签名
	 * @param phase 阶段命名
	 * @param iterateIndex 迭代编号
	 */
	public MetaTag(long invokerId, Siger issuer, Phase phase, int iterateIndex) {
		this();
		setInvokerId(invokerId);
		setIssuer(issuer);
		setPhase(phase);
		setIterateIndex(iterateIndex);
	}

	/**
	 * 从可类化数据读取器中解析元数据标识
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public MetaTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置阶段命名
	 * @param e Phase实例
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);
		phase = e;
	}

	/**
	 * 返回阶段命名
	 * @return Phase实例
	 */
	public Phase getPhase() {
		return phase;
	}
	
	/**
	 * 设置用户签名，不允许空指针
	 * @param e 用户签名实例
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);
		issuer = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置调用器编号。编号必须是>=0的数字
	 * @param id 调用器编号
	 */
	public void setInvokerId(long id) {
		if (InvokerIdentity.isInvalid(id)) {
			throw new IllegalValueException("illegal invoker identity: %d", id);
		}
		invokerId = id;
	}

	/**
	 * 返回调用器编号
	 * @return 调用器编号
	 */
	public long getInvokerId() {
		return invokerId;
	}
	
	/**
	 * 设置当前对象的迭代编号（系统不检查编号，正确由用户把握）
	 * @param index 迭代编号
	 */
	protected void setIterateIndex(int index) {
		iterateIndex = index;
	}

	/**
	 * 返回当前对象的迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 生成当前对象新的数据副本
	 * @return MetaTag实例
	 */
	public MetaTag duplicate() {
		return new MetaTag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != MetaTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((MetaTag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (invokerId ^ issuer.hashCode() ^ phase.hashCode() ^ iterateIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d#%s#%d", invokerId, phase, iterateIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MetaTag that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(invokerId, that.invokerId);
		if (ret == 0) {
			ret = Laxkit.compareTo(issuer, that.issuer);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(phase, that.phase);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(iterateIndex, that.iterateIndex);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 调用器编号
		writer.writeLong(invokerId);
		// 用户签名
		writer.writeObject(issuer);
		// 阶段命名
		writer.writeInstance(phase);
		// 迭代编号
		writer.writeInt(iterateIndex);
		// 保存数据的字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 调用器编号
		invokerId = reader.readLong();
		// 用户签名
		issuer = new Siger(reader);
		// 阶段命名
		phase = reader.readInstance(Phase.class);
		// 迭代编号
		iterateIndex = reader.readInt();
		// 解析的字节长度
		return reader.getSeek() - seek;
	}

}