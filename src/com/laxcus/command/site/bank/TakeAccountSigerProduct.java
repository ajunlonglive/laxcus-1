/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得坐标范围内账号签名结果。<br>
 * HASH站点，ACCOUNT站点回应结果。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeAccountSigerProduct extends EchoProduct {

	private static final long serialVersionUID = 6738948156054497065L;
	
	/** ACCOUNT站点地址 **/
	private Node local;

	/** 单元数组 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();
	
	/**
	 * 构造默认的获得坐标范围内账号签名结果命令
	 */
	public TakeAccountSigerProduct() {
		super();
	}

	/**
	 * 生成获得坐标范围内账号签名结果的数据副本
	 * @param that 获得坐标范围内账号签名结果
	 */
	private TakeAccountSigerProduct(TakeAccountSigerProduct that) {
		super(that);
		local = that.local;
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析获得坐标范围内账号签名结果
	 * @param reader 可类化数据读取器
	 */
	public TakeAccountSigerProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置ACCOUNT站点地址，不允许空指针
	 * @param e ACCOUNT站点地址
	 */
	public void setLocal(Node e) {
		Laxkit.nullabled(e);
		local = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return ACCOUNT站点地址
	 */
	public Node getLocal() {
		return local;
	}
	
	/**
	 * 保存一个账号签名
	 * @param e 账号签名
	 * @return 注册成功返回真，否则假
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批账号签名
	 * @param a 账号签名列表
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Siger> a) {
		int size = array.size();
		for (Siger e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 输出全部账号签名
	 * @return 账号签名列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计账号签名数目
	 * @return 成员数目
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeAccountSigerProduct duplicate() {
		return new TakeAccountSigerProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// ACCOUNT站点
		writer.writeObject(local);
		// 保存全部账号签名
		writer.writeInt(array.size());
		for (Siger e : array) {
			writer.writeDefault(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// ACCOUNT站点
		local = new Node(reader);
		// 取出全部账号签名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = (Siger) reader.readDefault();
			add(e);
		}
	}

}