/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

import java.util.*;

/**
 * 数据库权限表
 * 
 * @author scott.liang
 * @version 1.1 7/23/2015
 * @since laxcus 1.0
 */
public final class SchemaPermit extends Permit {

	private static final long serialVersionUID = -4800222839506485082L;
	
	/** 数据库名 -> 操作项集合 **/
	private TreeMap<Fame, Control> set = new TreeMap<Fame, Control>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 成员数目
		writer.writeInt(set.size());
		// 写入每一组成员参数
		Iterator<Map.Entry<Fame, Control>> iterator = set.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Fame, Control> entry = iterator.next();
			writer.writeObject(entry.getKey());
			writer.writeObject(entry.getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// 读成员数目
		int size = reader.readInt();
		// 解析每一组成员，保存它
		for (int i = 0; i < size; i++) {
			Fame key = new Fame(reader);
			Control value = new Control(reader);
			set.put(key, value);
		}
	}

	/**
	 * 根据传入的数据库权限表实例，生成它的副本
	 * @param that 数据库权限表实例
	 */
	private SchemaPermit(SchemaPermit that) {
		super(that);
		set.putAll(that.set);
	}

	/**
	 * 构造默认的数据库权限表 
	 */
	public SchemaPermit() {
		super(PermitTag.SCHEMA_PERMIT);
	}

	/**
	 * 从可类化读取器中解析数据库权限表参数
	 * @param reader  可类化读取器
	 * @since 1.1
	 */
	public SchemaPermit(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出数据库权限表参数
	 * @param reader 标记化读取器
	 */
	public SchemaPermit(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 返回数据库命名称集合
	 * @return 数据库名称集合
	 */
	public Set<Fame> keys() {
		return new TreeSet<Fame>(set.keySet());
	}

	/**
	 * 查找数据库资源控制
	 * @param fame 数据库名称
	 * @return Control实例
	 */
	public Control find(Fame fame) {
		return set.get(fame);
	}

	/**
	 * 保存数据库资源控制
	 * @param fame 数据库名称
	 * @param ctrl 数据库资源控制
	 */
	public void add(Fame fame, Control ctrl) {
		Control that = set.get(fame);
		if (that == null) {
			set.put(fame, ctrl);
		} else {
			that.addAll(ctrl);
		}
	}

	/**
	 * 删除数据库资源控制
	 * @param fame 数据库名称
	 * @return 删除成功返回，否则假
	 */
	public boolean remove(Fame fame) {
		return set.remove(fame) != null;
	}

	/**
	 * 返回成员数目
	 * @return 返回数据库关联控制数目
	 */
	public int size() {
		return set.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * 允许数据库下的操作选项
	 * @param fame  数据库名
	 * @param who  操作选项
	 * @return  返回真或者假
	 */
	public boolean allow(Fame fame, short who) {
		Control that = set.get(fame);
		boolean success = (that != null);
		if (success) {
			success = that.allow(who);
		}
		return success;
	}

	/**
	 * 判断允许一个数据库下的多个操作选项
	 * @param fame  数据库名
	 * @param all  全部操作选项
	 * @return  返回真或者假
	 */
	public boolean allow(Fame fame, short[] all) {
		int count = 0;
		for (short who : all) {
			boolean success = allow(fame, who);
			if (success) count++;
		}
		return count == all.length;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#duplicate()
	 */
	@Override
	public SchemaPermit duplicate() {
		return new SchemaPermit(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#add(com.laxcus.access.diagram.Permit)
	 */
	@Override
	public boolean add(Permit e) {
		if (e == null || e.getClass() != SchemaPermit.class) {
			return false;
		}
		SchemaPermit permit = (SchemaPermit) e;
		for (Fame fame : permit.set.keySet()) {
			Control ctrl = permit.set.get(fame);
			Control that = set.get(fame);
			if (that == null) {
				set.put(fame, ctrl);
			} else {
				that.addAll(ctrl);
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#remove(com.laxcus.access.diagram.Permit)
	 */
	@Override
	public boolean remove(Permit e) {
		if (e == null || e.getClass() != SchemaPermit.class) {
			return false;
		}
		SchemaPermit permit = (SchemaPermit) e;
		for (Fame fame : permit.set.keySet()) {
			Control ctrl = permit.set.get(fame);
			Control that = set.get(fame);
			if (that != null) {
				that.removeAll(ctrl);
				if (that.isEmpty()) {
					set.remove(fame);
				}
			}
		}
		return true;
	}

}