/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.shutdown;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 远程关闭报告
 * 
 * @author scott.liang
 * @version 1.0 9/8/2019
 * @since laxcus 1.0
 */
public class ShutdownProduct extends EchoProduct {

	private static final long serialVersionUID = 798814580531257245L;

	/** 单元数组 **/
	private TreeSet<ShutdownItem> array = new TreeSet<ShutdownItem>();

	/**
	 * 构造默认的远程关闭报告
	 */
	public ShutdownProduct() {
		super();
	}

	/**
	 * 构造远程关闭报告，指定单元
	 * @param item 单元
	 */
	public ShutdownProduct(ShutdownItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析远程关闭报告
	 * @param reader 可类化数据读取器
	 */
	public ShutdownProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造远程关闭报告的数据副本
	 * @param that 远程关闭报告
	 */
	private ShutdownProduct(ShutdownProduct that) {
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
	private boolean push(ShutdownItem that) {
		Laxkit.nullabled(that);

		ShutdownItem next = null;
		for (ShutdownItem old : array) {
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
	 * 保存一个处理单元，不允许空指针
	 * @param e ShutdownItem实例
	 * @return 返回真或者假
	 */
	public boolean add(ShutdownItem e) {
		Laxkit.nullabled(e);
		
		return push(e);
	}
	
	/**
	 * 保存一个处理单元
	 * @param node Node实例
	 * @param successful 成功标记
	 * @return 返回真或者假
	 */
	public boolean add(Node node, boolean successful) {
		return add(new ShutdownItem(node, successful));
	}

	/**
	 * 保存一批处理单元
	 * @param a ShutdownItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ShutdownItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (ShutdownItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e ShutdownProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ShutdownProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e ShutdownItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(ShutdownItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ShutdownItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ShutdownItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部处理单元列表
	 * @return ShutdownItem列表
	 */
	public List<ShutdownItem> list() {
		return new ArrayList<ShutdownItem>(array);
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
	public ShutdownProduct duplicate() {
		return new ShutdownProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ShutdownItem e : array) {
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
			ShutdownItem item = new ShutdownItem(reader);
			array.add(item);
		}
	}

}


///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license GNU Lesser General Public License (LGPL)
// */
//package com.laxcus.command.shutdown;
//
//import java.util.*;
//
//import com.laxcus.echo.product.*;
//import com.laxcus.site.*;
//import com.laxcus.util.*;
//import com.laxcus.util.classable.*;
//
///**
// * 远程关闭命令处理结果<br>
// * 
// * @author scott.liang
// * @version 1.1 5/19/2015
// * @since laxcus 1.0
// */
//public final class ShutdownProduct extends EchoProduct {
//
//	private static final long serialVersionUID = 5874441444492967643L;
//
//	/** 站点地址 **/
//	private TreeSet<Node> array = new TreeSet<Node>();
//
//	/**
//	 * 构造默认的远程关闭命令处理结果
//	 */
//	public ShutdownProduct() {
//		super();
//	}
//
//	/**
//	 * 生成一个远程关闭命令处理结果数据副本
//	 * @param that ShutdownProduct实例
//	 */
//	private ShutdownProduct(ShutdownProduct that) {
//		super(that);
//		array.addAll(that.array);
//	}
//
//	/**
//	 * 构造远程关闭命令处理结果，指定参数
//	 * @param node 站点地址
//	 */
//	public ShutdownProduct(Node node) {
//		this();
//		add(node);
//	}
//
//	/**
//	 * 从可类化数据读取器中解析远程关闭命令处理结果
//	 * @param reader 可类化数据读取器
//	 * @since 1.1
//	 */
//	public ShutdownProduct(ClassReader reader) {
//		this();
//		resolve(reader);
//	}
//
//	/**
//	 * 保存一个站点地址
//	 * @param e Node实例
//	 * @return 保存成功返回真，否则假
//	 */
//	public boolean add(Node e) {
//		Laxkit.nullabled(e);
//
//		return array.add(e);
//	}
//
//	/**
//	 * 保存一批站点地址
//	 * @param a Node数组
//	 * @return 返回新增成员数目
//	 */
//	public int addAll(List<Node> a) {
//		int size = array.size();
//		for (Node e : a) {
//			add(e);
//		}
//		return array.size() - size;
//	}
//
//	/**
//	 * 输出全部站点地址
//	 * @return Node列表
//	 */
//	public List<Node> list() {
//		return new ArrayList<Node>(array);
//	}
//
//	/**
//	 * 统计成员数目
//	 * @return 成员数目
//	 */
//	public int size() {
//		return array.size();
//	}
//
//	/**
//	 * 判断是空集合
//	 * @return 返回真或者假
//	 */
//	public boolean isEmpty() {
//		return size() == 0;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
//	 */
//	@Override
//	protected void buildSuffix(ClassWriter writer) {
//		writer.writeInt(array.size());
//		for (Node node : array) {
//			writer.writeObject(node);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
//	 */
//	@Override
//	protected void resolveSuffix(ClassReader reader) {
//		int size = reader.readInt();
//		for (int i = 0; i < size; i++) {
//			Node e = new Node(reader);
//			array.add(e);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
//	 */
//	@Override
//	public ShutdownProduct duplicate() {
//		return new ShutdownProduct(this);
//	}
//}