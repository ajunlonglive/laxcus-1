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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据导出结果 <br>
 * 是“EXPORT ENTITY”命令处理结果。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2019
 * @since laxcus 1.0
 */
public class ExportEntityProduct extends DefaultTableProduct {
	
	private static final long serialVersionUID = -3740305361146445402L;

	/** 数组**/
	private ArrayList<SingleExportEntityResult> array = new ArrayList<SingleExportEntityResult>();

	/**
	 * 构造默认和私有的数据导出结果
	 */
	private ExportEntityProduct() {
		super();
	}

	/**
	 * 构造数据导出结果，指定表名
	 * @param space 表名 
	 */
	public ExportEntityProduct(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 生成数据导出结果的数据副本
	 * @param that 数据副本
	 */
	private ExportEntityProduct(ExportEntityProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析数据导出结果
	 * @param reader 可类化数据读取器
	 */
	public ExportEntityProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存结果
	 * @param file 处理结果
	 * @return 返回真或者假
	 */
	public boolean add(SingleExportEntityResult file) {
		Laxkit.nullabled(file);
		if (array.contains(file)) {
			return false;
		}
		return array.add(file);
	}

	/**
	 * 保存全部
	 * @param a
	 * @return
	 */
	public int addAll(Collection<SingleExportEntityResult> a) {
		int size = array.size();
		for (SingleExportEntityResult e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出处理结果
	 * @return
	 */
	public List<SingleExportEntityResult> list() {
		return new ArrayList<SingleExportEntityResult>(array);
	}

	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ExportEntityProduct duplicate() {
		return new ExportEntityProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for(SingleExportEntityResult e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for(int i =0; i < size; i++) {
			SingleExportEntityResult e = new SingleExportEntityResult(reader);
			add(e);
		}
	}
	
}
