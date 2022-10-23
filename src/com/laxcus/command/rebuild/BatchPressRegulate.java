/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 批量处理数据优化命令。<br><br>
 * 
 * 包装多个数据优化命令，批量处理它们。<br>
 * 流程：ACCOUNT -> BANK -> GATE。<br>
 * 
 * @author scott.liang
 * @version 1.0 2018-7-20
 * @since laxcus 1.0
 */
public final class BatchPressRegulate extends Command {

	private static final long serialVersionUID = -6113946856529140981L;

	/** 数据表名 **/
	private ArrayList<PressRegulate> array = new ArrayList<PressRegulate>();

	/**
	 * 构造默认的批量处理数据优化命令
	 */
	public BatchPressRegulate() {
		super();
	}

	/**
	 * 构造批量处理数据优化命令，指定全部参数
	 * @param a 数据优化命令
	 */
	public BatchPressRegulate(List<PressRegulate> a) {
		this();
		addAll(a);
	}
	
	/**
	 * 从可类化数据读取器中解析批量处理数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public BatchPressRegulate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成批量处理数据优化命令的数据副本
	 * @param that 批量处理数据优化命令
	 */
	private BatchPressRegulate(BatchPressRegulate that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个数据优化命令
	 * @param e 数据优化命令实例
	 */
	public void add(PressRegulate e) {
		Laxkit.nullabled(e);
		array.add(e);
	}

	/**
	 * 保存一批数据优化命令
	 * @param a 数据优化命令数组
	 */
	public int addAll(List<PressRegulate> a) {
		int size = array.size();
		for (PressRegulate e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回全部数据优化命令
	 * @return 数据优化命令列表
	 */
	public List<PressRegulate> list() {
		return new ArrayList<PressRegulate>(array);
	}

	/**
	 * 确定数据优化命令成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BatchPressRegulate duplicate() {
		return new BatchPressRegulate(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (PressRegulate e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PressRegulate e = new PressRegulate(reader);
			array.add(e);
		}
	}

}