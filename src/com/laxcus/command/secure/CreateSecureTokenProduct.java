/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立密钥令牌执行结果
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class CreateSecureTokenProduct extends EchoProduct {

	private static final long serialVersionUID = -6240611532606647517L;

	/** 单元集合 **/
	private TreeSet<CreateSecureTokenItem> array = new TreeSet<CreateSecureTokenItem>();

	/**
	 * 构造默认的建立密钥令牌执行结果
	 */
	public CreateSecureTokenProduct() {
		super();
	}

	/**
	 * 构造建立密钥令牌执行结果，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public CreateSecureTokenProduct(Node node, boolean successful) {
		this();
		add(new CreateSecureTokenItem(node, successful));
	}

	/**
	 * 从可类化数据读取器解析建立密钥令牌执行结果
	 * @param reader 可类化数据读取器
	 */
	public CreateSecureTokenProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造建立密钥令牌执行结果的数据副本
	 * @param that ReloadSecureProduct实例
	 */
	private CreateSecureTokenProduct(CreateSecureTokenProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 有选择保存单元，判断过程：<br>
	 * 1. 两个节点地址必须一致。<br>
	 * 2. 同地址情况下，保存成功，丢弃失败的。<br><br>
	 * 
	 * @param that 传入的单元
	 * @return 保存成功返回真，否则假
	 */
	private boolean push(CreateSecureTokenItem that) {
		Laxkit.nullabled(that);

		CreateSecureTokenItem next = null;
		for (CreateSecureTokenItem old : array) {
			// 基于节点地址的比较
			boolean math = (Laxkit.compareTo(old.getSite(), that.getSite()) == 0);
			if (!math) {
				continue;
			}

			// 原有不成功，新的成功，把旧的删除，保存新的。退出
			if (!old.isSuccessful() && that.isSuccessful()) {
				next = old;
				break;
			} 
			// 旧的成功，新的不成功，退出不保存
			else if(old.isSuccessful() && that.isSuccessful()) {
				return false;
			}
		}
		// 保存!
		if (next != null) {
			array.remove(next);
		} 
		return	array.add(that);
	}
	
	/**
	 * 保存一个执行结果单元，不允许空指针
	 * @param e ReloadSecureItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CreateSecureTokenItem e) {
		Laxkit.nullabled(e);

		return push(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @param successful 成功标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, boolean successful) {
		return add(new CreateSecureTokenItem(node, successful));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReloadSecureItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<CreateSecureTokenItem> a) {
		int size = array.size();
		for(CreateSecureTokenItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReloadSecureProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CreateSecureTokenProduct e) {
		return addAll(e.list());
	}

	/**
	 * 返回节点
	 * @return
	 */
	public List<Node> getSites() {
		ArrayList<Node> a = new ArrayList<Node>();
		for (CreateSecureTokenItem e : array) {
			a.add(e.getSite());
		}
		return a;
	}

	/**
	 * 查找匹配的...
	 * @param node
	 * @return
	 */
	public CreateSecureTokenItem find(Node node) {
		for (CreateSecureTokenItem e : array) {
			if (Laxkit.compareTo(e.getSite(), node) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReloadSecureItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(CreateSecureTokenItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReloadSecureItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(CreateSecureTokenItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReloadSecureItem列表
	 */
	public List<CreateSecureTokenItem> list() {
		return new ArrayList<CreateSecureTokenItem>(array);
	}

	/**
	 * 统计执行结果单元成员数目
	 * @return 单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateSecureTokenProduct duplicate() {
		return new CreateSecureTokenProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CreateSecureTokenItem e : array) {
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
			CreateSecureTokenItem item = new CreateSecureTokenItem(reader);
			array.add(item);
		}
	}

}