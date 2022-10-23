/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得授权人账号注册地址。<br>
 * 
 * 命令从FRONT站点发出，作用到ENTRANCE站点，查询授权人的注册的GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public class TakeAuthorizerSite extends Command {
	
	private static final long serialVersionUID = 4698046986305794269L;
	
	/** 授权人签名集合 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 构造默认的获得授权人账号注册地址
	 */
	public TakeAuthorizerSite() {
		super();
	}

	/**
	 * 构造获得授权人账号注册地址，保存一批授权人签名
	 * @param a 授权人签名数组
	 */
	public TakeAuthorizerSite(Collection<Siger> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析获得授权人账号注册地址
	 * @param reader 可类化数据读取器
	 */
	public TakeAuthorizerSite(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成获得授权人账号注册地址的数据副本
	 * @param that TakeAuthorizerSite实例
	 */
	private TakeAuthorizerSite(TakeAuthorizerSite that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个授权人签名
	 * @param e 授权人签名
	 * @return 返回真或者假
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批授权人签名
	 * @param a 授权人签名
	 * @return 返回真增成员数目
	 */
	public int addAll(Collection<Siger> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 删除一个授权人签名
	 * @param e 授权人签名
	 * @return 返回真或者假
	 */
	public boolean remove(Siger e) {
		return array.remove(e);
	}

	/**
	 * 检查一个账号用户名是否在允许范围内。
	 * @param e 账号用户名。
	 * @return 允许返回true，否则返回false。
	 */
	public boolean contains(Siger e) {		
		return array.contains(e);
	}

	/**
	 * 返回授权人签名列表
	 * @return Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计授权人签名的数量
	 * @return 统计数量
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 如果没有授权人签名，返回true；否则返回false。
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeAuthorizerSite duplicate() {
		return new TakeAuthorizerSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Siger e : array) {
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
			Siger e = new Siger(reader);
			array.add(e);
		}
	}

}