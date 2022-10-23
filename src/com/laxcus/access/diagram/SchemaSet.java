/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.set.*;

/**
 * 数据库集合。<br>
 * 
 * 数据库集合保存数据库命名与数据表名集合的映射关系。
 * 
 * @author scott.liang
 * @version 1.1 5/21/2015
 * @since laxcus 1.0
 */
public final class SchemaSet implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = -361445889267964393L;

	/** 数据库名称 -> 数据表名集合 **/
	private TreeMap<Fame, SpaceSet> schemas = new TreeMap<Fame, SpaceSet>();
	
	/**
	 * 将数据库资源配置写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 写入成员数目
		writer.writeInt(schemas.size());
		// 写入每一组成员参数
		Iterator<Map.Entry<Fame, SpaceSet>> iterator = schemas.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Fame, SpaceSet> entry = iterator.next();
			writer.writeObject(entry.getKey());
			writer.writeObject(entry.getValue());
		}
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据库资源配置
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 读取成员数目
		int size = reader.readInt();
		// 读每一组成员参数
		for (int i = 0; i < size; i++) {
			Fame key = new Fame(reader);
			SpaceSet value = new SpaceSet(reader);
			schemas.put(key, value);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that
	 */
	private SchemaSet(SchemaSet that) {
		super();
		schemas.putAll(that.schemas);
	}

	/**
	 * 构造一个空的数据库集合
	 */
	public SchemaSet() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析数据库集合参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public SchemaSet(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 增加一个数据库命名
	 * @param e 数据库命名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean addSchema(Fame e) {
		SpaceSet set = schemas.get(e);
		// 如果集合存在是错误
		boolean success = (set != null);
		// 建立数据库命名和数据表名集合的映射关系
		if (success) {
			set = new SpaceSet();
			success = (schemas.put(e, set) == null);
		}
		// 返回结果
		return success;
	}
	
	/**
	 * 删除数据库及其属下的表
	 * @param e 数据库名称
	 * @return 成员返回真，或者假
	 */
	public boolean deleteSchema(Fame e) {
		return schemas.remove(e) != null;
	}
	
	/**
	 * 判断数据库存在
	 * @param e 数据库命名
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean hasSchema(Fame e) {
		return schemas.get(e) != null;
	}
	
	/**
	 * 增加一个数据表名，前提是数据库命名必须存在
	 * @param e 表名
	 * @return 增加成功返回“真”，否则“假“。
	 */
	public boolean addSpace(Space e) {
		SpaceSet set = schemas.get(e.getSchema());
		boolean success = (set == null);
		if (success) {
			success = set.add(e);
		}
		return success;
	}

	/**
	 * 删除一个数据库表，前提是数据库必须存在
	 * @param e 表名
	 * @return 删除成员返回真，否则假
	 */
	public boolean deleteSpace(Space e) {
		SpaceSet set = schemas.get(e.getSchema());
		if (set != null) {
			return set.remove(e);
		}
		return false;
	}

	/**
	 * 返回数据库名称集合
	 * @return 数据库名称集合
	 */
	public Set<Fame> keys() {
		return new TreeSet<Fame>(schemas.keySet());
	}
	
	/**
	 * 返回全部数据表名集合
	 * @return 数据表名称集合
	 */
	public Set<Space> values() {
		Set<Space> set = new TreeSet<Space>();
		for (SpaceSet e : schemas.values()) {
			set.addAll(e.list());
		}
		return set;
	}

	/**
	 * 查找数据库命名下的全部数据表名
	 * @param e 数据库命名
	 * @return 返回数据表名集合，如果没有找到，返回空集合
	 */
	public Set<Space> findSpaces(Fame e) {
		Set<Space> a = new TreeSet<Space>();
		SpaceSet set = schemas.get(e);
		if (set != null) {
			a.addAll(set.list());
		}
		return a;
	}
	
	/**
	 * 判断数据表名存在
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space e) {
		SpaceSet set = schemas.get(e.getSchema());
		boolean success = (set != null);
		if (success) {
			success = set.contains(e);
		}
		return success;
	}
	
	/**
	 * 返回集合成员数
	 * @return 数据库成员数目
	 */
	public int size() {
		return schemas.size();
	}
	
	/**
	 * 清除全部
	 */
	public void clear() {
		schemas.clear();
	}

	/**
	 * 判断集合是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 生成当前数据库集合的数据副本
	 * @return 返回新的数据库集合
	 */
	public SchemaSet duplicate() {
		return new SchemaSet(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
}