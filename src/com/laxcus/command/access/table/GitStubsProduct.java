/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 表数据块编号处理结果
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class GitStubsProduct extends EchoProduct {
	
	private static final long serialVersionUID = 5739643595677566140L;

	/** 数据表名 **/
	private Space space;
	
	/** 缓存状态数据块集合 **/
	private TreeSet<GitStubsItem> cacheStubs = new TreeSet<GitStubsItem>();
	
	/** 存储状态数据块集合 **/
	private TreeSet<GitStubsItem> chunkStubs = new TreeSet<GitStubsItem>();
	
	/**
	 * 构造表数据块编号处理结果
	 */
	public GitStubsProduct() {
		super();
	}

	/**
	 * 构造表数据块编号处理结果，设置表名
	 * @param space 数据表名
	 */
	public GitStubsProduct(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析表数据块编号处理结果
	 * @param reader 可类化数据读取器
	 */
	public GitStubsProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成表数据块编号处理结果的数据副本
	 * @param that 表数据块编号处理结果
	 */
	private GitStubsProduct(GitStubsProduct that) {
		super(that);
		space = that.space;
		cacheStubs.addAll(that.cacheStubs);
		chunkStubs.addAll(that.chunkStubs);
	}
	
	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}
	
	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 保存一个缓存数据块编号
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean addCacheStub(GitStubsItem stub) {
		return cacheStubs.add(stub);
	}

	/**
	 * 保存一批缓存数据块编号
	 * @param a 数据块编号集合
	 * @return 返回增加的数据块编号数目
	 */
	public int addCacheStubs(Collection<GitStubsItem> a) {
		int size = cacheStubs.size();
		for (GitStubsItem e : a) {
			addCacheStub(e);
		}
		return cacheStubs.size() - size;
	}
	
	/**
	 * 返回缓存块编号集合
	 * @return 长整型集合
	 */
	public List<GitStubsItem> getCacheStubs(){
		return new ArrayList<GitStubsItem>(cacheStubs);
	}

	/**
	 * 保存一个存储数据块编号
	 * @param e 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean addChunkStub(GitStubsItem e) {
		return chunkStubs.add(e);
	}

	/**
	 * 保存一批数据块编号
	 * @param a 数据块编号集合
	 * @return 返回增加的数据块编号数目
	 */
	public int addChunkStubs(Collection<GitStubsItem> a) {
		int size = chunkStubs.size();
		for (GitStubsItem e : a) {
			addChunkStub(e);
		}
		return chunkStubs.size() - size;
	}

	/**
	 * 返回存储块编号集合
	 * @return 长整型集合
	 */
	public List<GitStubsItem> getChunkStubs() {
		return new ArrayList<GitStubsItem>(chunkStubs);
	}

	/**
	 * 保存全部
	 * @param that
	 */
	public void addAll(GitStubsProduct that) {
		if (space == null) {
			space = that.space;
		} else if (Laxkit.compareTo(space, that.space) != 0) {
			throw new IllegalValueException("cannot be match! %s - %s", space, that.space);
		}
		cacheStubs.addAll(that.cacheStubs);
		chunkStubs.addAll(that.chunkStubs);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public GitStubsProduct duplicate() {
		return new GitStubsProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
		// 缓存块
		writer.writeInt(cacheStubs.size());
		for (GitStubsItem e : cacheStubs) {
			writer.writeObject(e);
		}
		// 存储块
		writer.writeInt(chunkStubs.size());
		for (GitStubsItem e : chunkStubs) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
		// 缓存块
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			GitStubsItem e = new GitStubsItem(reader);
			addCacheStub(e);
		}
		// 存储块
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			GitStubsItem e = new GitStubsItem(reader);
			addChunkStub(e);
		}
	}

}

///**
// * 表数据块编号处理结果
// * 
// * @author scott.liang
// * @version 1.1 5/23/2015
// * @since laxcus 1.0
// */
//public class GitStubsProduct extends EchoProduct {
//	
//	private static final long serialVersionUID = 5739643595677566140L;
//
//	/** 数据表名 **/
//	private Space space;
//	
//	/** 缓存状态数据块集合 **/
//	private StubSet cacheStubs = new StubSet();
//	
//	/** 存储状态数据块集合 **/
//	private StubSet chunkStubs = new StubSet();
//
//	/**
//	 * 构造表数据块编号处理结果
//	 */
//	public GitStubsProduct() {
//		super();
//	}
//
//	/**
//	 * 构造表数据块编号处理结果，设置表名
//	 * @param space 数据表名
//	 */
//	public GitStubsProduct(Space space) {
//		this();
//		setSpace(space);
//	}
//
//	/**
//	 * 从可类化数据读取器中解析表数据块编号处理结果
//	 * @param reader 可类化数据读取器
//	 */
//	public GitStubsProduct(ClassReader reader) {
//		this();
//		resolve(reader);
//	}
//
//	/**
//	 * 生成表数据块编号处理结果的数据副本
//	 * @param that 表数据块编号处理结果
//	 */
//	private GitStubsProduct(GitStubsProduct that) {
//		super(that);
//		space = that.space;
//		cacheStubs.addAll(that.cacheStubs);
//		chunkStubs.addAll(that.chunkStubs);
//	}
//	
//	/**
//	 * 设置数据表名
//	 * @param e Space实例
//	 */
//	public void setSpace(Space e) {
//		Laxkit.nullabled(e);
//
//		space = e;
//	}
//	
//	/**
//	 * 返回数据表名
//	 * @return Space实例
//	 */
//	public Space getSpace() {
//		return space;
//	}
//
//	/**
//	 * 保存一个缓存数据块编号
//	 * @param stub 数据块编号
//	 * @return 成功返回真，否则假
//	 */
//	public boolean addCacheStub(long stub) {
//		return cacheStubs.add(stub);
//	}
//
//	/**
//	 * 保存一批缓存数据块编号
//	 * @param a 数据块编号集合
//	 * @return 返回增加的数据块编号数目
//	 */
//	public int addCacheStubs(Collection<java.lang.Long> a) {
//		return cacheStubs.addAll(a);
//	}
//	
//	/**
//	 * 返回缓存块编号集合
//	 * @return 长整型集合
//	 */
//	public List<Long> getCacheStubs(){
//		return cacheStubs.list();
//	}
//
//	/**
//	 * 保存一个存储数据块编号
//	 * @param stub 数据块编号
//	 * @return 成功返回真，否则假
//	 */
//	public boolean addChunkStub(long stub) {
//		return chunkStubs.add(stub);
//	}
//
//	/**
//	 * 保存一批数据块编号
//	 * @param a 数据块编号集合
//	 * @return 返回增加的数据块编号数目
//	 */
//	public int addChunkStubs(Collection<java.lang.Long> a) {
//		return chunkStubs.addAll(a);
//	}
//
//	/**
//	 * 返回存储块编号集合
//	 * @return 长整型集合
//	 */
//	public List<Long> getChunkStubs(){
//		return chunkStubs.list();
//	}
//	
//	/**
//	 * 保存全部
//	 * @param that
//	 */
//	public void addAll(GitStubsProduct that) {
//		if (space == null) {
//			space = that.space;
//		} else if (Laxkit.compareTo(space, that.space) != 0) {
//			throw new IllegalValueException("cannot be match! %s - %s", space, that.space);
//		}
//		cacheStubs.addAll(that.cacheStubs);
//		chunkStubs.addAll(that.chunkStubs);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
//	 */
//	@Override
//	public GitStubsProduct duplicate() {
//		return new GitStubsProduct(this);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
//	 */
//	@Override
//	protected void buildSuffix(ClassWriter writer) {
//		writer.writeObject(space);
//		writer.writeObject(cacheStubs);
//		writer.writeObject(chunkStubs);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
//	 */
//	@Override
//	protected void resolveSuffix(ClassReader reader) {
//		space = new Space(reader);
//		cacheStubs.resolve(reader);
//		chunkStubs.resolve(reader);
//	}
//
//}