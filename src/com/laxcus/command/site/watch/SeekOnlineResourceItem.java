/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 站点在线资源单元。<br>
 * 
 * 基于一个账号的数据单元。
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class SeekOnlineResourceItem implements Classable, Serializable, Cloneable, Comparable<SeekOnlineResourceItem> {

	private static final long serialVersionUID = 5266417608277125531L;

	/** 用户签名 **/
	private Siger siger;

	/** 用户资源引用 **/
	private Refer refer;

	/** 阶段命名 **/
	private TreeSet<Phase> phases = new TreeSet<Phase>();
	
	/**
	 * 构造默认和私有的站点在线资源单元
	 */
	public SeekOnlineResourceItem() {
		super();
	}

	/**
	 * 生成站点在线资源单元的副本
	 * 
	 * @param that SeekOnlineResourceItem实例
	 */
	private SeekOnlineResourceItem(SeekOnlineResourceItem that) {
		this();
		refer = that.refer;
		siger = that.siger;
		phases.addAll(that.phases);
	}
	
	/**
	 * 构造站点在线资源单元，指定用户签名
	 * 
	 * @param siger 用户签名
	 */
	public SeekOnlineResourceItem(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 构造站点在线资源单元，指定用户资源引用
	 * 
	 * @param refer 用户资源引用
	 */
	public SeekOnlineResourceItem(Refer refer) {
		this();
		setRefer(refer);
	}

	/**
	 * 从可类化数据读取器中解析站点在线资源单元
	 * @param reader 可类化数据读取器
	 */
	public SeekOnlineResourceItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * 
	 * @param e Refer实例
	 */
	public void setRefer(Refer e) {
		Laxkit.nullabled(e);

		if (siger == null) {
			siger = e.getUsername();
		} else if (Laxkit.compareTo(siger, e.getUsername()) != 0) {
			throw new IllegalValueException("cannot be match:%s - %s", siger,
					e.getUsername());
		}

		refer = e;
	}

	/**
	 * 返回站点地址
	 * 
	 * @return Refer实例
	 */
	public Refer getRefer() {
		return refer;
	}

	/**
	 * 设置用户签名
	 * 
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		if (siger == null) {
			siger = e;
		} else if (refer != null
				&& Laxkit.compareTo(refer.getUsername(), siger) != 0) {
			throw new IllegalValueException("cannot be match:%s - %s", siger,
					refer.getUsername());
		}
	}

	/**
	 * 返回用户签名
	 * 
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 保存阶段命名
	 * @param phase
	 * @return
	 */
	public boolean addPhase(Phase phase) {
		Laxkit.nullabled(phase);
		
		// 不允许是系统组件
		if (phase.isSystemLevel()) {
			throw new IllegalPhaseException("canot be system phase!");
		}
		
		// 如果不匹配，弹出错误
		if (Laxkit.compareTo(siger, phase.getIssuer()) != 0) {
			throw new IllegalPhaseException("cannot be match:%s - %s", siger, phase.getIssuer());
		}
		return phases.add(phase);
	}

	/**
	 * 保存一批阶段命名
	 * @param a
	 * @return
	 */
	public int addPhases(List<Phase> a) {
		int size = phases.size();
		for (Phase e : a) {
			addPhase(e);
		}
		return phases.size() - size;
	}
	
	/**
	 * 输出全部阶段命名
	 * @return 阶段命名列表
	 */
	public List<Phase> getPhases() {
		return new ArrayList<Phase>(phases);
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return SeekOnlineResourceItem实例
	 */
	public SeekOnlineResourceItem duplicate() {
		return new SeekOnlineResourceItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SeekOnlineResourceItem that) {
		if (that == null) {
			return 1;
		}

		// 比较参数
		return Laxkit.compareTo(siger, that.siger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(siger);
		writer.writeInstance(refer);
		// 阶段命名
		writer.writeInt(phases.size());
		for (Phase e : phases) {
			writer.writeObject(e);
		}
		
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		siger = new Siger(reader);
		refer = reader.readInstance(Refer.class);
		// 阶段命名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			phases.add(e);
		}
		return reader.getSeek() - seek;
	}

}