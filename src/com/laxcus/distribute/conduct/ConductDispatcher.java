/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct;

import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;

/**
 * CONDUCT命令的任务分派器。有FROM/TO两个阶段的子类分派器<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public abstract class ConductDispatcher extends DistributedDispatcher {

	private static final long serialVersionUID = 4797264009223023160L;

	/** 数据索引分区。在INIT/BALANCE阶段产生，是FROM/TO阶段计算分割数据的依据。**/
	private ColumnSector sector;

	/*
	 * 将任务输出器参数写入可类化存储器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 保存上级数据信息
		super.buildSuffix(writer);
		// 分区记录
		writer.writeDefault(sector);
	}

	/*
	 * 从可类化读取器中读取任务输出器的参数
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析上级信息
		super.resolveSuffix(reader);
		// 解析分区
		sector = (ColumnSector) reader.readDefault();
	}

	/**
	 * 构造一个默认的任务输出器
	 */
	protected ConductDispatcher() {
		super();
	}

	/**
	 * 根据传入的任务输出器，复制它的成员参数
	 * @param that ConductDispatcher实例
	 */
	protected ConductDispatcher(ConductDispatcher that) {
		super(that);
		// 分区实例的副本
		sector = that.sector;
	}

	/**
	 * 设置数据分区，在INIT/BALANCE阶段设置，在FROM/TO阶段结合实际数据使用
	 * @param e IndexSector实例
	 */
	public void setIndexSector(ColumnSector e) {
		sector = e;
	}

	/**
	 * 返回数据分区
	 * @return IndexSector实例
	 */
	public ColumnSector getIndexSector() {
		return sector;
	}

	/**
	 * 返回数据分区的列空间
	 * @return Dock实例
	 */
	public Dock getIndexDock() {
		if (sector != null) {
			return sector.getDock();
		}
		return null;
	}

	/**
	 * 任务处理完成后，是否需要再分区。<br>
	 * FROM/TO阶段任务完成前，调用此方法，判断是否需要为后续TO阶段做数据分片。<br>
	 * 分片后，数据将以元信息(FluxArea)返回，否则就是实际数据。
	 * 
	 * @return 返回真或者假
	 */
	public boolean hasIndexSector() {
		return sector != null;
	}

}