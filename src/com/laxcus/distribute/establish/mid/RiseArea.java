/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH.RISE阶段的数据扫描区域。<br>
 * 
 * 在DATA.RISE生成，返回给CALL/ESTABLISH调用器，每个站点生成一个。在FRONT.END阶段显示。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public final class RiseArea extends EstablishArea {

	private static final long serialVersionUID = 4690756494924276896L;

	/** 新的数据块  **/
	private Map<EstablishFlag, RiseNewField> updates = new TreeMap<EstablishFlag, RiseNewField>();

	/** 旧的数据块 **/
	private Map<EstablishFlag, RiseOldField> deletes = new TreeMap<EstablishFlag, RiseOldField>();

	/**
	 * 构造一个默认的数据扫描区域。
	 */
	private RiseArea() {
		super();
	}

	/**
	 * 根据传入的数据扫描区域，生成它的数据副本
	 * @param that RiseArea实例
	 */
	private RiseArea(RiseArea that) {
		super(that);
		updates.putAll(that.updates);
		deletes.putAll(that.deletes);
	}

	/**
	 * 构造数据扫描区域，设置它的源主机地址。
	 * @param source 源主机地址(一定是数据节点地址)
	 */
	public RiseArea(Node source) {
		this();
		 setSource(source);
	}

	/**
	 * 从可类化读取器中解析数据扫描区域
	 * @param reader
	 */
	public RiseArea(ClassReader reader) {
		this();
		 resolve(reader);
	}

	/**
	 * 保存一个表下的被更新数据域
	 * @param field 被更新数据域
	 * @return 成功返回true，失败返回false。
	 */
	public boolean addNewField(RiseNewField field) {
		return updates.put(field.getFlag(), field) == null;
	}

	/**
	 * 输出全部被更新数据域
	 * @return RiseNewField列表
	 */
	public List<RiseNewField> getNewFields() {
		return new ArrayList<RiseNewField>(updates.values());
	}

	/**
	 * 保存一个表下的被删除数据域
	 * @param field 被删除数据域
	 * @return 成功返回true，失败返回false。
	 */
	public boolean addOldField(RiseOldField field) {
		return deletes.put(field.getFlag(), field) == null;
	}

	/**
	 * 输出全部被删除数据域
	 * @return RiseOldField列表
	 */
	public List<RiseOldField> getOldFields() {
		return new ArrayList<RiseOldField>(deletes.values());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.establish.util.EstabArea#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 更新数据成员数目
		writer.writeInt(updates.size());
		// 写入数据成员
		for (RiseNewField e : updates.values()) {
			writer.writeObject(e);
		}
		// 删除数据成员数目
		writer.writeInt(deletes.size());
		// 写入数据成员
		for (RiseOldField e : deletes.values()) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.establish.util.EstabArea#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 成员数目
		int size = reader.readInt();
		// 每一个成员
		for (int i = 0; i < size; i++) {
			RiseNewField field = new RiseNewField(reader);
			updates.put(field.getFlag(), field);
		}
		// 被删除
		size = reader.readInt();
		// 每一个成员
		for (int i = 0; i < size; i++) {
			RiseOldField field = new RiseOldField(reader);
			deletes.put(field.getFlag(), field);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public RiseArea duplicate() {
		return new RiseArea(this);
	}

}