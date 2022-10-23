/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 显示数据表优化时间 <br>
 * 
 * 这是局部命令，在FRONT站点处理和显示。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class PrintRegulateTime extends Command {

	private static final long serialVersionUID = 8944399783908361189L;

	/** 数据表名 **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认显示数据表优化时间
	 */
	public PrintRegulateTime() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示数据表优化时间
	 * @param reader 可类化数据读取器
	 */
	public PrintRegulateTime(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成显示数据表优化时间的数据副本
	 * @param that PrintRegulateTime实例
	 */
	private PrintRegulateTime(PrintRegulateTime that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 保存一个数据表名，不允许空指针
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean add(Space e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 设置一批数据表名
	 * @param a Space集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 判断参数存在
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space e) {
		Laxkit.nullabled(e);
		return array.contains(e);
	}

	/**
	 * 显示全部表名
	 * @return
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 成员数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 显示全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PrintRegulateTime duplicate() {
		return new PrintRegulateTime(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Space e : array) {
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
			Space e = new Space(reader);
			array.add(e);
		}
	}

}