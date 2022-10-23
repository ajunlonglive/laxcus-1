/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.site.*;

/**
 * 设置虚拟空间参数应答报告
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class UserCyberProduct extends EchoProduct {

	private static final long serialVersionUID = -7064495987851051213L;

	/** 单元数组 **/
	private TreeSet<UserCyberItem> array = new TreeSet<UserCyberItem>();

	/**
	 * 构造默认的设置虚拟空间参数应答报告
	 */
	public UserCyberProduct() {
		super();
	}

	/**
	 * 构造设置虚拟空间参数应答报告，指定单元
	 * @param item 单元
	 */
	public UserCyberProduct(UserCyberItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析设置虚拟空间参数应答报告
	 * @param reader 可类化数据读取器
	 */
	public UserCyberProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造设置虚拟空间参数应答报告的数据副本
	 * @param that 设置虚拟空间参数应答报告
	 */
	private UserCyberProduct(UserCyberProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理单元，不允许空指针
	 * @param e UserCyberItem实例
	 * @return 返回真或者假
	 */
	public boolean add(UserCyberItem e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}
	
	/**
	 * 保存一个处理单元
	 * @param node Node实例
	 * @param moment 瞬时记录
	 * @return 返回真或者假
	 */
	public boolean add(Node node, Moment moment) {
		return add(new UserCyberItem(node, moment));
	}

	/**
	 * 保存一批处理单元
	 * @param a UserCyberItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<UserCyberItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (UserCyberItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e UserCyberProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(UserCyberProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e UserCyberItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(UserCyberItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e UserCyberItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(UserCyberItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部处理单元列表
	 * @return UserCyberItem列表
	 */
	public List<UserCyberItem> list() {
		return new ArrayList<UserCyberItem>(array);
	}

	/**
	 * 统计处理单元成员数目
	 * @return 处理单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public UserCyberProduct duplicate() {
		return new UserCyberProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (UserCyberItem e : array) {
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
			UserCyberItem item = new UserCyberItem(reader);
			array.add(item);
		}
	}

}