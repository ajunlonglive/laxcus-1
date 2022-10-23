/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 推送元数据命令。<br>
 * 
 * 这个命令由HOME/DATA/BUILD/WORK站点发出，目标是CALL站点。
 * 被接收的CALL站点保存DATA/WORK/BUILD站点的元数据。
 * 对于CALL站点，它需要删除旧的数据，才能保存新的数据。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public abstract class PushField extends ProcessField {

	private static final long serialVersionUID = -2991027919166463451L;

	/** 阶段命名集合 **/
	private TreeSet<Phase> phases = new TreeSet<Phase>();
	
	/** 用户签名集合 **/
	private TreeSet<Siger> sigers = new TreeSet<Siger>();

	/** 内存不足标记 **/
	private boolean memoryMissing;

	/** 磁盘不足标记 **/
	private boolean diskMissing;

	/**
	 * 构造默认的推送元数据命令。
	 */
	protected PushField() {
		super();
		// 默认是假，内存足够！
		memoryMissing = false;
		// 默认是假，磁盘空间足够！
		diskMissing = false;
	}

	/**
	 * 根据传入的推送命令，生成它的数据副本
	 * @param that PushField实例
	 */
	protected PushField(PushField that) {
		super(that);
		phases.addAll(that.phases);
		sigers.addAll(that.sigers);
		memoryMissing = that.memoryMissing;
		diskMissing = that.diskMissing;
	}
	
	/**
	 * 保存阶段命名，不允许空指针
	 * @param e Phase实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addPhase(Phase e) {
		Laxkit.nullabled(e);

		return phases.add(e);
	}

	/**
	 * 保存一批阶段命名
	 * @param a Phase数组
	 * @return 返回新增数目
	 */
	public int addPhases(Collection<Phase> a) {
		int size = phases.size();
		for(Phase e : a) {
			addPhase(e);
		}
		return phases.size() - size;
	}
	
	/**
	 * 输出全部阶段命名
	 * @return Phase列表
	 */
	public List<Phase> getPhases() {
		return new ArrayList<Phase>(phases);
	}
	
	/**
	 * 保存用户签名，不允许空指针
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSiger(Siger e) {
		Laxkit.nullabled(e);

		return sigers.add(e);
	}

	/**
	 * 保存一批用户签名
	 * @param a Siger数组
	 * @return 返回新增数目
	 */
	public int addSigers(Collection<Siger> a) {
		int size = sigers.size();
		for (Siger e : a) {
			addSiger(e);
		}
		return sigers.size() - size;
	}
	
	/**
	 * 输出全部用户签名
	 * @return Siger列表
	 */
	public List<Siger> getSigers() {
		return new ArrayList<Siger>(sigers);
	}

	/**
	 * 设置内存不足
	 * @param b 真或者假
	 */
	public void setMemoryMissing(boolean b) {
		memoryMissing = b;
	}
	
	/**
	 * 判断是内存不足
	 * @return 返回真或者假
	 */
	public boolean isMemoryMissing() {
		return memoryMissing;
	}

	/**
	 * 判断内存充裕
	 * @return 返回真或者假
	 */
	public boolean isMemoryPassed() {
		return !memoryMissing;
	}
	
	/**
	 * 设置磁盘空间不足
	 * @param b 真或者假
	 */
	public void setDiskMissing(boolean b) {
		diskMissing = b;
	}
	
	/**
	 * 判断是磁盘空间不足
	 * @return 返回真或者假
	 */
	public boolean isDiskMissing() {
		return diskMissing;
	}

	/**
	 * 判断磁盘空间充裕
	 * @return 返回真或者假
	 */
	public boolean isDiskPassed() {
		return !diskMissing;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 本级数据
		writer.writeInt(phases.size());
		for (Phase e : phases) {
			writer.writeObject(e);
		}
		// 用户签名
		writer.writeInt(sigers.size());
		for(Siger e : sigers) {
			writer.writeObject(e);
		}
		// 内存/磁盘空间不足
		writer.writeBoolean(memoryMissing);
		writer.writeBoolean(diskMissing);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 本级数据
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			phases.add(e);
		}
		// 用户签名
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			sigers.add(e);
		}
		// 内存/磁盘空间不足
		memoryMissing = reader.readBoolean();
		diskMissing = reader.readBoolean();
	}

}