/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 打印站点检测目录报告
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public class CheckSitePathProduct extends EchoProduct {

	private static final long serialVersionUID = -2734903819938985694L;

	/** 单元数组 **/
	private TreeSet<CheckSitePathItem> array = new TreeSet<CheckSitePathItem>();

	/**
	 * 构造默认的打印站点检测目录报告
	 */
	public CheckSitePathProduct() {
		super();
	}

	/**
	 * 构造打印站点检测目录报告，指定单元
	 * @param item 单元
	 */
	public CheckSitePathProduct(CheckSitePathItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析打印站点检测目录报告
	 * @param reader 可类化数据读取器
	 */
	public CheckSitePathProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造打印站点检测目录报告的数据副本
	 * @param that 打印站点检测目录报告
	 */
	private CheckSitePathProduct(CheckSitePathProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理单元，不允许空指针
	 * @param e CheckSitePathItem实例
	 * @return 返回真或者假
	 */
	public boolean add(CheckSitePathItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理单元
	 * @param node Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node node) {
		return add(new CheckSitePathItem(node));
	}

	/**
	 * 保存一批处理单元
	 * @param a CheckSitePathItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<CheckSitePathItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (CheckSitePathItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e CheckSitePathProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CheckSitePathProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e CheckSitePathItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(CheckSitePathItem e) {
		return array.remove(e);
	}
	
	/**
	 * 找到匹配的单元
	 * @param node 节点
	 * @return 返回实例，没有是空指针
	 */
	public CheckSitePathItem find(Node node) {
		for (CheckSitePathItem e : array) {
			boolean b = (Laxkit.compareTo(e.getSite(), node) == 0);
			if (b) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 判断存在
	 * @param e CheckSitePathItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(CheckSitePathItem e) {
		return array.contains(e);
	}

	/**
	 * 输出全部处理单元列表
	 * @return CheckSitePathItem列表
	 */
	public List<CheckSitePathItem> list() {
		return new ArrayList<CheckSitePathItem>(array);
	}

	/**
	 * 统计处理单元成员数目
	 * @return 处理单元成员数目
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 返回全部节点
	 * @return Node列表
	 */
	public List<Node> getSites() {
		ArrayList<Node> a = new ArrayList<Node>();
		for (CheckSitePathItem e : array) {
			a.add(e.getSite());
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckSitePathProduct duplicate() {
		return new CheckSitePathProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CheckSitePathItem e : array) {
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
			CheckSitePathItem item = new CheckSitePathItem(reader);
			array.add(item);
		}
	}


	//	public void test(CheckSitePathProduct that) throws Exception {
	//		Node node = new Node("TOP://12.12.23.90:89_89");
	//		that.add(node);
	//		
	//		CheckSitePathItem item = new CheckSitePathItem(new Node("toP://12.12.23.90:89_89"));
	//		item.setOS("Winddows");
	////		if(that.contains(item)) {
	////			that.remove(item);
	////		}
	//		that.add(item);
	//		
	//		for(CheckSitePathItem e : that.list()) {
	//			System.out.printf("%s - %s\n", e.getSite(),e.getOS());
	//		}
	//	}
	//	
	//	public static void main(String[] args) {
	//		CheckSitePathProduct e = new CheckSitePathProduct();
	//		try {
	//			e.test(e);
	//		} catch (Exception s) {
	//			s.printStackTrace();
	//		}
	//	}

}