/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得用户签名应答报告。<br>
 * 
 * TOP站点根据AID站点的TakeSiger命令要求，返回给的用户签名集合。
 * 
 * @author scott.liang
 * @version 1.1 06/02/2015
 * @since laxcus 1.0
 */
public final class TakeSigerProduct extends EchoProduct {

	private static final long serialVersionUID = -4124450778849479820L;

	/** 获得用户签名集合 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 根据传入的获得用户签名应答报告实例，生成它的数据副本
	 * @param that TakeSigerProduct实例
	 */
	private TakeSigerProduct(TakeSigerProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的获得用户签名应答报告
	 */
	public TakeSigerProduct() {
		super();
	}

	/**
	 * 构造获得用户签名应答报告，指定一批签名
	 * @param a 用户签名集合
	 */
	public TakeSigerProduct(Collection<Siger> a) {
		this();
		addAll(a);
	}
	
	/**
	 * 从可类化数据读取器中解析获得用户签名应答报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeSigerProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加一个获得用户签名，不允许空指针
	 * @param e 获得用户签名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}

	/**
	 * 增加一批用户签名。
	 * @param a 用户签名集合
	 * @return 返回新增签名数目
	 */
	public int addAll(Collection<Siger> a) {
		int size = array.size();
		for (Siger e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 输出全部获得用户签名
	 * @return 获得用户签名列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计获得用户签名数目
	 * @return 返回获得用户签名数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeSigerProduct duplicate() {
		return new TakeSigerProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for(Siger e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
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