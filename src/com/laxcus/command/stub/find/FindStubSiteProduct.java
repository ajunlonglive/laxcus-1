/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.set.*;
import com.laxcus.site.Node;

/**
 * 数据块编号关联站点查询结果。<br>
 * 
 * 这方法是CALL站点的数据块编号查询命令的反馈。其中包含了关联数据块编号的DATA站点地址。
 * 
 * @author scott.liang
 * @version 1.1 12/7/2013
 * @since laxcus 1.0
 */
public class FindStubSiteProduct extends EchoProduct {
	
	private static final long serialVersionUID = -6671551381107607958L;
	
	/** 子级站点地址 -> 数据块实体 **/
	private Map<Node, StubEntry> sites = new TreeMap<Node, StubEntry>();
	
	/**
	 * 构造默认的FindStubSiteProduct
	 */
	public FindStubSiteProduct() {
		super();
	}

	/**
	 * 根据传入的FindStubSiteProduct实例，生成它的数据副本
	 * @param that FindStubSiteProduct实例
	 */
	private FindStubSiteProduct(FindStubSiteProduct that) {
		super(that);
		sites.putAll(that.sites);
	}

	/**
	 * 输出全部数据块实体
	 * @return StubEntry列表
	 */
	public List<StubEntry> list() {
		return new ArrayList<StubEntry>(sites.values());
	}
	
	/**
	 * 统计数据块编号数目
	 * @return 数据块编号数目
	 */
	public int getStubSize() {
		TreeSet<Long> set = new TreeSet<Long>();
		for (StubEntry entry : sites.values()) {
			set.addAll(entry.list());
		}
		return set.size();
	}
	
	/**
	 * 保存站点地址和数据块编号
	 * @param node Node实例
	 * @param stub 数据块编号
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, long stub) {
		StubEntry entry = sites.get(node);
		if(entry == null) {
			entry = new StubEntry(node);
			sites.put(entry.getNode(), entry);
		}
		return entry.add(stub);
	}
	
	/**
	 * 保存一批参数
	 * @param that
	 * @return
	 */
	public int addAll(FindStubSiteProduct that) {
		int size = sites.size();

		Iterator<Map.Entry<Node, StubEntry>> iterator = that.sites.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, StubEntry> sub = iterator.next();
			StubEntry entry = sites.get(sub.getKey());
			if (entry == null) {
				sites.put(sub.getKey(), sub.getValue());
			} else {
				entry.addAll(sub.getValue().list());
			}
		}

		return sites.size() - size;
	}
	
	/**
	 * 输出全部站点地址集合
	 * @return Node列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(sites.keySet());
	}
	
	/**
	 * 根据站点地址，查询对应的站点记录
	 * @param node Node实例
	 * @return 返回数据块实体，或者空指针
	 */
	public StubEntry findStubEntry(Node node) {
		return sites.get(node);
	}
	
	/**
	 * 输出全部数据块编号
	 * @return 数据块编号列表（长整型）
	 */
	public List<Long> getStubs() {
		StubSet set = new StubSet();
		for (StubEntry e : sites.values()) {
			set.addAll(e.list());
		}
		return set.list();
	}
	
	/**
	 * 根据数据块编号，查找它的分布站点
	 * @param stub 数据块编号
	 * @return 分布站点地址列表
	 */
	public List<Node> findSites(long stub) {
		Set<Node> set = new TreeSet<Node>();

		Iterator<Map.Entry<Node, StubEntry>> iterator = sites.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, StubEntry> entry = iterator.next();
			if (entry.getValue().contains(stub)) {
				set.add(entry.getKey());
			}
		}
		// 输出全部
		return new ArrayList<Node>(set);
	}
	
	/**
	 * 统计数据块实体成员数目
	 * @return 数据块实体成员数目
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return sites.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindStubSiteProduct duplicate() {
		return new FindStubSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(sites.size());
		for (StubEntry e : sites.values()) {
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
			StubEntry e = new StubEntry(reader);
			sites.put(e.getNode(), e);
		}
	}

}