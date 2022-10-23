/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 数据表权限表
 * 
 * @author scott.liang
 * @version 1.1 7/23/2015
 * @since laxcus 1.0
 */
public final class TablePermit extends Permit {

	private static final long serialVersionUID = 277240050550758035L;

	/** 数据表名 -> 操作项集合 **/
	private TreeMap<Space, Control> set = new TreeMap<Space, Control>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 成员数目
		writer.writeInt(set.size());
		// 写入每一组成员参数
		Iterator<Map.Entry<Space, Control>> iterator = set.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Space, Control> entry = iterator.next();
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
			Space key = new Space(reader);
			Control value = new Control(reader);
			set.put(key, value);
		}
	}

	/**
	 * 根据传入的数据表权限表实例，生成它的副本
	 * @param that 数据表权限表实例
	 */
	private TablePermit(TablePermit that) {
		super(that);
		set.putAll(that.set);
	}

	/**
	 * 构造默认的数据表权限表
	 */
	public TablePermit() {
		super(PermitTag.TABLE_PERMIT);
	}

	/**
	 * 从可类化读取器中解析数据表权限表参数
	 * @param reader  可类化读取器
	 * @since 1.1
	 */
	public TablePermit(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据表权限表参数
	 * @param reader 标记化读取器
	 */
	public TablePermit(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 返回数据表名集合
	 * @return 数据表名集合
	 */
	public Set<Space> keys() {
		return new TreeSet<Space>(set.keySet());
	}

	/**
	 * 查找数据表资源控制
	 * @param space 数据表名
	 * @return Control实例
	 */
	public Control find(Space space) {
		return set.get(space);
	}
	
	/**
	 * 判断数据表权限表是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * 返回数据表权限表的成员数目
	 * @return 成员数目
	 */
	public int size() {
		return set.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#duplicate()
	 */
	@Override
	public TablePermit duplicate() {
		return new TablePermit(this);
	}

	/**
	 * 保存一个数据表权限
	 * @param space 表名
	 * @param ctrl 资源控制
	 * @return 返回真或者假
	 */
	public boolean add(Space space, Control ctrl) {
		Control that = set.get(space);
		if (that == null) {
			set.put(space, ctrl);
		} else {
			that.addAll(ctrl);
		}
		return true;
	}

	/**
	 * 允许一个表下的资源控制选项
	 * @param space  表名
	 * @param who  资源控制选项
	 * @return  返回真或者假
	 */
	public boolean allow(Space space, short who) {
		Control that = set.get(space);
		boolean success = (that != null);
		if (success) {
			success = that.allow(who);
		}
		return success;
	}

	/**
	 * 判断允许一个表的多个资源控制选项
	 * @param space  表名 
	 * @param all  资源控制选项数组
	 * @return  返回真或者假
	 */
	public boolean allow(Space space, short[] all) {
		int count = 0;
		for (short who : all) {
			boolean success = allow(space, who);
			if (success) count++;
		}
		return count == all.length;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#add(com.laxcus.access.diagram.Permit)
	 */
	@Override
	public boolean add(Permit e) {
		if (e == null || e.getClass() != TablePermit.class) {
			return false;
		}
		TablePermit permit = (TablePermit) e;
		for (Space space : permit.set.keySet()) {
			Control ctrl = permit.set.get(space);
			Control that = set.get(space);
			if (that == null) {
				set.put(space, ctrl);
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
		if (e == null || e.getClass() != TablePermit.class) {
			return false;
		}
		TablePermit permit = (TablePermit) e;
		for (Space space : permit.set.keySet()) {
			Control ctrl = permit.set.get(space);
			Control that = set.get(space);
			if (that != null) {
				that.removeAll(ctrl);
				if (that.isEmpty()) {
					set.remove(space);
				}
			}
		}
		return true;
	}


}