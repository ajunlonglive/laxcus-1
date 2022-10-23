/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据节点图谱。<br><br>
 * 
 * 包含数据块节点主机地址和数据块元信息的集合
 * 
 * @author scott.liang
 * @version 1.0 11/11/2020
 * @since laxcus 1.0
 */
public class PrintStubsDiagramItem implements Classable, Serializable, Cloneable, Comparable<PrintStubsDiagramItem> {

	private static final long serialVersionUID = -2895175300136366085L;

	/** 节点地址 **/
	private Node site;

	/** 缓存数据块集合 **/
	private TreeSet<PrintStubsDiagramCell> cacheStubs = new TreeSet<PrintStubsDiagramCell>();

	/** 缓存映像数据块集合 **/
	private TreeSet<PrintStubsDiagramCell> reflexStubs = new TreeSet<PrintStubsDiagramCell>();

	/** 存储数据块集合 **/
	private TreeSet<PrintStubsDiagramCell> chunkStubs = new TreeSet<PrintStubsDiagramCell>();

	/**
	 * 构造打印数据块分布图谱处理结果
	 */
	public PrintStubsDiagramItem() {
		super();
	}

	/**
	 * 构造打印数据块分布图谱处理结果，设置表名
	 * @param space 节点地址
	 */
	public PrintStubsDiagramItem(Node space) {
		this();
		setSite(space);
	}

	/**
	 * 从可类化数据读取器中解析打印数据块分布图谱处理结果
	 * @param reader 可类化数据读取器
	 */
	public PrintStubsDiagramItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成打印数据块分布图谱处理结果的数据副本
	 * @param that 打印数据块分布图谱处理结果
	 */
	private PrintStubsDiagramItem(PrintStubsDiagramItem that) {
		this();
		site = that.site;
		cacheStubs.addAll(that.cacheStubs);
		reflexStubs.addAll(that.reflexStubs);
		chunkStubs.addAll(that.chunkStubs);
	}

	/**
	 * 设置节点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回节点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 保存一个缓存数据块编号
	 * @param e 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean addCacheStub(PrintStubsDiagramCell e) {
		Laxkit.nullabled(e);
		return cacheStubs.add(e);
	}

	/**
	 * 保存一批缓存数据块编号
	 * @param a 数据块编号集合
	 * @return 返回增加的数据块编号数目
	 */
	public int addCacheStubs(Collection<PrintStubsDiagramCell> a) {
		int size = cacheStubs.size();
		for (PrintStubsDiagramCell e : a) {
			addCacheStub(e);
		}
		return cacheStubs.size() - size;
	}

	/**
	 * 返回缓存块编号集合
	 * @return 长整型集合
	 */
	public List<PrintStubsDiagramCell> getCacheStubs(){
		return new ArrayList<PrintStubsDiagramCell>(cacheStubs);
	}

	/**
	 * 保存一个缓存映像数据块编号
	 * @param e 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean addReflexStub(PrintStubsDiagramCell e) {
		Laxkit.nullabled(e);
		return reflexStubs.add(e);
	}

	/**
	 * 保存一批缓存映像数据块编号
	 * @param a 数据块编号集合
	 * @return 返回增加的数据块编号数目
	 */
	public int addReflexStubs(Collection<PrintStubsDiagramCell> a) {
		int size = reflexStubs.size();
		for (PrintStubsDiagramCell e : a) {
			addReflexStub(e);
		}
		return reflexStubs.size() - size;
	}

	/**
	 * 返回缓存映像块编号集合
	 * @return 长整型集合
	 */
	public List<PrintStubsDiagramCell> getReflexStubs(){
		return new ArrayList<PrintStubsDiagramCell>(reflexStubs);
	}

	/**
	 * 保存一个存储数据块编号
	 * @param e 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean addChunkStub(PrintStubsDiagramCell e) {
		Laxkit.nullabled(e);
		return chunkStubs.add(e);
	}

	/**
	 * 保存一批数据块编号
	 * @param a 数据块编号集合
	 * @return 返回增加的数据块编号数目
	 */
	public int addChunkStubs(Collection<PrintStubsDiagramCell> a) {
		int size = chunkStubs.size();
		for (PrintStubsDiagramCell e : a) {
			addChunkStub(e);
		}
		return chunkStubs.size() - size;
	}

	/**
	 * 返回存储块编号集合
	 * @return 长整型集合
	 */
	public List<PrintStubsDiagramCell> getChunkStubs() {
		return new ArrayList<PrintStubsDiagramCell>(chunkStubs);
	}

	/**
	 * 保存全部
	 * @param that
	 */
	public void addAll(PrintStubsDiagramItem that) {
		if (site == null) {
			site = that.site;
		} else if (Laxkit.compareTo(site, that.site) != 0) {
			throw new IllegalValueException("cannot be match! %s - %s", site, that.site);
		}
		cacheStubs.addAll(that.cacheStubs);
		reflexStubs.addAll(that.reflexStubs);
		chunkStubs.addAll(that.chunkStubs);
	}

	/**
	 * 生成副本
	 * @return
	 */
	public PrintStubsDiagramItem duplicate() {
		return new PrintStubsDiagramItem(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PrintStubsDiagramItem that) {
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		
		writer.writeObject(site);
		// 缓存块
		writer.writeInt(cacheStubs.size());
		for (PrintStubsDiagramCell e : cacheStubs) {
			writer.writeObject(e);
		}
		// 缓存映像块
		writer.writeInt(reflexStubs.size());
		for (PrintStubsDiagramCell e : reflexStubs) {
			writer.writeObject(e);
		}
		// 存储块
		writer.writeInt(chunkStubs.size());
		for (PrintStubsDiagramCell e : chunkStubs) {
			writer.writeObject(e);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		
		site = new Node(reader);
		// 缓存块
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PrintStubsDiagramCell e = new PrintStubsDiagramCell(reader);
			addCacheStub(e);
		}
		// 缓存映像块
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PrintStubsDiagramCell e = new PrintStubsDiagramCell(reader);
			addReflexStub(e);
		}
		// 存储块
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PrintStubsDiagramCell e = new PrintStubsDiagramCell(reader);
			addChunkStub(e);
		}
		return reader.getSeek() - seek;
	}



}

//public final class PrintStubsDiagramItem implements Classable, Serializable, Cloneable, Comparable<PrintStubsDiagramItem> {
//
//	private static final long serialVersionUID = 8704387981310205789L;
//	
//	/** DATA节点主机地址 **/
//	private Node site;
//
//	/** 数据节点图谱 */
//	private TreeSet<PrintStubsDiagramCell> array = new TreeSet<PrintStubsDiagramCell>();
//
//	/**
//	 * 将数据节点图谱写入可类化存储器
//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
//	 */
//	@Override
//	public int build(ClassWriter writer) {
//		final int size = writer.size();
//		// 节点
//		writer.writeObject(site);
//		// 写入成员数目
//		writer.writeInt(array.size());
//		// 写入每一个成员
//		for (PrintStubsDiagramCell e : array) {
//			writer.writeObject(e);
//		}
//		// 返回写入的字节长度
//		return writer.size() - size;
//	}
//
//	/**
//	 * 从可类化读取器中解析数据节点图谱
//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
//	 */
//	@Override
//	public int resolve(ClassReader reader) {
//		final int seek = reader.getSeek();
//		// 节点
//		site = new Node(reader);
//		// 读成员数目
//		int size = reader.readInt();
//		// 读每个成员和保存它
//		for (int i = 0; i < size; i++) {
//			PrintStubsDiagramCell e = new PrintStubsDiagramCell(reader);
//			array.add(e);
//		}
//		// 返回读取的字节长度
//		return reader.getSeek() - seek;
//	}
//
//	/**
//	 * 根据传入的数据节点图谱实例，生成它的副本
//	 * @param that StubResult实例
//	 */
//	private PrintStubsDiagramItem(PrintStubsDiagramItem that) {
//		this();
//		site = that.site;
//		array.addAll(that.array);
//	}
//
//	/**
//	 * 构造一个空的数据节点图谱
//	 */
//	public PrintStubsDiagramItem() {
//		super();
//	}
//
//	/**
//	 * 构造数据节点图谱
//	 * @param site
//	 */
//	public PrintStubsDiagramItem(Node site) {
//		this();
//		setSite(site);
//	}
//
//	/**
//	 * 构造数据节点图谱，保存数据块编号数组
//	 * @param a 数据块编号数组
//	 */
//	public PrintStubsDiagramItem(PrintStubsDiagramCell[] a) {
//		this();
//		addAll(a);
//	}
//
//	/**
//	 * 构造数据节点图谱
//	 * @param a 数据节点图谱
//	 */
//	public PrintStubsDiagramItem(Collection<PrintStubsDiagramCell> a) {
//		this();
//		addAll(a);
//	}
//
//	/**
//	 * 从可类化数据读取器中解析数据节点图谱
//	 * @param reader 可类化数据读取器
//	 * @since 1.1
//	 */
//	public PrintStubsDiagramItem(ClassReader reader) {
//		this();
//		resolve(reader);
//	}
//	
//	/**
//	 * 设置节点地址
//	 * @param e
//	 */
//	public void setSite(Node e) {
//		site = e;
//	}
//	
//	/**
//	 * 返回节点地址
//	 * @return
//	 */
//	public Node getSite() {
//		return site;
//	}
//
//	/**
//	 * 保存一个数据块编号
//	 * @param e 数据块编号
//	 * @return 返回真或者假
//	 */
//	public boolean add(PrintStubsDiagramCell e) {
//		if (e != null) {
//			return array.add(e);
//		}
//		return false;
//	}
//
//	/**
//	 * 删除一个数据块编号
//	 * @param e 数据块编号
//	 * @return 返回真或者假
//	 */
//	public boolean remove(PrintStubsDiagramCell e) {
//		return array.remove(e);
//	}
//
//	/**
//	 * 保存另一个数据节点图谱的全部编号
//	 * @param that 另一个数据节点图谱
//	 * @return 返回新加入的数据块编号数目
//	 */
//	public int addAll(PrintStubsDiagramItem that) {
//		int size = array.size();
//		if (that != null) {
//			array.addAll(that.array);
//		}
//		return array.size() - size;
//	}
//
//	/**
//	 * 保存一批数据块编号
//	 * @param a 数据块编号数组
//	 * @return 已经保存的成员数目
//	 */
//	public int addAll(PrintStubsDiagramCell[] a) {
//		int size = array.size();
//		for (int i = 0; a != null && i < a.length; i++) {
//			add(a[i]);
//		}
//		return array.size() - size;
//	}
//
//	/**
//	 * 保存一批数据块编号
//	 * @param that 数据节点图谱
//	 * @return 返回新增加的成员数目
//	 */
//	public int addAll(Collection<PrintStubsDiagramCell> a) {
//		int size = array.size();
//		for (PrintStubsDiagramCell e : a) {
//			add(e);
//		}
//		return array.size() - size;
//	}
//
//	/**
//	 * 判断数据块编号存在
//	 * @param e 数据块编号
//	 * @return 返回真或者假
//	 */
//	public boolean contains(PrintStubsDiagramCell e) {
//		if (e != null) {
//			return array.contains(e);
//		}
//		return false;
//	}
//
//	/**
//	 * 以非锁定方式和复制数据副本方式，输出全部数据块编号
//	 * @return 数据块编号列表
//	 */
//	public List<PrintStubsDiagramCell> list() {
//		return new ArrayList<PrintStubsDiagramCell>(array);
//	}
//
//	/**
//	 * 清除全部数据块编号
//	 */
//	public void clear() {
//		array.clear();
//	}
//
//	/**
//	 * 统计数据块编号的数目
//	 * @return 编号数目
//	 */
//	public int size() {
//		return array.size();
//	}
//
//	/**
//	 * 检测集合是空
//	 * @return 返回真或者假
//	 */
//	public boolean isEmpty() {
//		return size() == 0;
//	}
//
//	/**
//	 * 以非锁定方式输出全部数据块编号数组
//	 * @return 数据块编号数组
//	 */
//	public PrintStubsDiagramCell[] toArray() {
//		PrintStubsDiagramCell[] a = new PrintStubsDiagramCell[array.size()];
//		return array.toArray(a);
//	}
//	
//	/**
//	 * 克隆当前数据节点图谱的数据副本
//	 * @see java.lang.Object#clone()
//	 */
//	@Override
//	public Object clone() {
//		return new PrintStubsDiagramItem(this);
//	}
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(PrintStubsDiagramItem that) {
//		return Laxkit.compareTo(site, that.site);
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		return site.hashCode();
//	}
//}