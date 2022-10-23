/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 阶段命名成员<br><br>
 * 
 * HOME集群下面有四个阶段命名站点：CALL/DATA/WORK/BUILD，阶段命名成员在它们中存在和运行。
 * 
 * @author scott.liang
 * @version 1.0 7/17/2012
 * @since laxcus 1.0
 */
public abstract class PhaseMember extends SiteMember { 

	private static final long serialVersionUID = 2512786969401948931L;

	/** 阶段命名集合 **/
	private TreeSet<Phase> phases = new TreeSet<Phase>();

	/**
	 * 根据传入阶段命名成员参数，生成它的数据副本
	 * @param that PhaseMember实例
	 */
	protected PhaseMember(PhaseMember that) {
		super(that);
		phases.addAll(that.phases);
	}

	/**
	 * 构造默认的阶段命名成员
	 */
	protected PhaseMember() {
		super();
	}

	/**
	 * 保存阶段命名
	 * @param e Phase实例
	 * @return 成功返回真，否则假
	 */
	public boolean addPhase(Phase e) {
		Laxkit.nullabled(e);

		return phases.add(e);
	}

	/**
	 * 删除阶段命名
	 * @param e Phase实例
	 * @return 成功返回真，否则假
	 */
	public boolean removePhase(Phase e) {
		Laxkit.nullabled(e);
		return phases.remove(e);
	}

	/**
	 * 返回阶段命名集合
	 * @return Phase列表
	 */
	public List<Phase> getPhases() {
		return new ArrayList<Phase>(phases);
	}

	/**
	 * 判断存在阶段命名
	 * @param e Phase实例
	 * @return 返回真或者假
	 */
	public boolean contains(Phase e) {
		return phases.contains(e);
	}

	/**
	 * 判断集合是空
	 * @return 返回真或者假
	 */
	public boolean isPhaseEmpty() {
		return phases.isEmpty();
	}
	
	/**
	 * 统计阶段命名成员
	 * @return 统计数目
	 */
	public int getPhaseSize() {
		return phases.size();
	}

	/**
	 * 重置数据
	 */
	public void reset() {
		phases.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 阶段命名集合
		writer.writeInt(phases.size());
		for (Phase e : phases) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			phases.add(e);
		}
	}

}