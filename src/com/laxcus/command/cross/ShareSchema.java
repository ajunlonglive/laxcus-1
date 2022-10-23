/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 共享数据库资源。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public abstract class ShareSchema extends ShareCross {
	
	private static final long serialVersionUID = 1495260198536838132L;
	
	/** 数据库名 **/
	private Set<Fame> array = new TreeSet<Fame>();

	/**
	 * 构造默认的共享数据库资源
	 */
	protected ShareSchema() {
		super();
	}

	/**
	 * 生成共享数据库资源的数据副本
	 * @param that ShareSchema实例
	 */
	protected ShareSchema(ShareSchema that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个数据库名
	 * @param e Fame实例
	 */
	public void addFame(Fame e) {
		Laxkit.nullabled(e);

		array.add(e);
	}

	/**
	 * 返回全部数据库名
	 * @return Fame实例
	 */
	public List<Fame> getFames() {
		return new ArrayList<Fame>(array);
	}
	
	/**
	 * 判断处理全部
	 * 
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (Fame e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Fame e = new Fame(reader);
			array.add(e);
		}
	}
}