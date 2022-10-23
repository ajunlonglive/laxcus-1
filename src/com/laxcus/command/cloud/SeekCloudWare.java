/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 检索分布的云应用服务 <br>
 * 由FRONT用户发出，通过CALL节点，检索CALL/DATA/BUILD/WORK节点上的分布式应用。
 * 
 * @author scott.liang
 * @version 1.0 2/5/2020
 * @since laxcus 1.0
 */
public class SeekCloudWare extends Command {

	private static final long serialVersionUID = -9085440569670417290L;

	/** 基础字集合 **/
	private TreeSet<Sock> socks = new TreeSet<Sock>();

	/**
	 * 构造默认的检索分布的云应用服务
	 */
	public SeekCloudWare() {
		super();
	}

	/**
	 * 从可类化读取器中解析检索分布的云应用服务
	 * @param reader 可类化读取器
	 */
	public SeekCloudWare(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检索分布的云应用服务的副本
	 * @param that 检索分布的云应用服务
	 */
	private SeekCloudWare(SeekCloudWare that) {
		super(that);
		socks.addAll(that.socks);
	}

	/**
	 * 保存基础字
	 * @param e 基础字
	 * @return 返回真或者假
	 */
	public boolean add(Sock e) {
		Laxkit.nullabled(e);
		return socks.add(e);
	}
	
	/**
	 * 判断包含
	 * @param e
	 * @return 返回真或者假
	 */
	public boolean contains(Sock e) {
		Laxkit.nullabled(e);
		return socks.contains(e);
	}
	
	/**
	 * 保存一批基础字
	 * @param a 基础字数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Sock> a) {
		int size = socks.size();
		socks.addAll(a);
		return socks.size() - size;
	}

	/**
	 * 输出全部基础字
	 * @return 基础字列表
	 */
	public List<Sock> list() {
		return new ArrayList<Sock>(socks);
	}

	/**
	 * 统计基础字数目
	 * @return 基础字数目
	 */
	public int size() {
		return socks.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * 要求显示全部
	 * @return 真或者假
	 */
	public boolean isAll() {
		return isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekCloudWare duplicate() {
		return new SeekCloudWare(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(socks.size());
		for (Sock e : socks) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Sock e = new Sock(reader);
			socks.add(e);
		}
	}

}