/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 请求分配CALLK站点资源引用。
 * 
 * @author scott.liang
 * @version 1.1 05/17/2015
 * @since laxcus 1.0
 */
public final class RequestCallRefer extends RequestRefer {

	private static final long serialVersionUID = 6774296969985637778L;

	/** 忽略的签名 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 根据传入的实例，生成它的数据副本
	 * @param that 请求分配CALLK站点资源引用实例
	 */
	private RequestCallRefer(RequestCallRefer that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的请求分配CALLK站点资源引用命令
	 */
	public RequestCallRefer() {
		super();
	}

	/**
	 * 构造请求分配CALLK站点资源引用命令，指定内存空间
	 * @param size 内存空间
	 */
	public RequestCallRefer(long size) {
		super();
		setSize(size);
	}

	/**
	 * 从可类化数据读取器中解析请求分配CALLK站点资源引用命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RequestCallRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存忽略的签名
	 * @param siger
	 */
	public boolean addIgnore(Siger siger) {
		Laxkit.nullabled(siger);
		return array.add(siger);
	}

	/**
	 * 保存一批签名
	 * @param a
	 * @return
	 */
	public int addIgnores(Collection<Siger> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 返回忽略的签名
	 * @return
	 */
	public List<Siger> getIgnores() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 判断是忽略的签名
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean isIgnore(Siger siger) {
		return array.contains(siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RequestCallRefer duplicate() {
		return new RequestCallRefer(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 忽略的签名成员
		writer.writeInt(array.size());
		for (Siger siger : array) {
			writer.writeObject(siger);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 忽略的签名成员
		int members = reader.readInt();
		for (int i = 0; i < members; i++) {
			Siger siger = new Siger(reader);
			array.add(siger);
		}
	}

}