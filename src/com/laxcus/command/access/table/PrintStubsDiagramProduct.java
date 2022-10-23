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
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 打印数据块分布图谱处理结果
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class PrintStubsDiagramProduct extends EchoProduct {


	private static final long serialVersionUID = -4065364820963873132L;

	/** 数据表名 **/
	private Space space;

	/** 缓存数据块集合 **/
	private TreeSet<PrintStubsDiagramItem> array = new TreeSet<PrintStubsDiagramItem>();

	/**
	 * 构造打印数据块分布图谱处理结果
	 */
	public PrintStubsDiagramProduct() {
		super();
	}

	/**
	 * 构造打印数据块分布图谱处理结果，设置表名
	 * @param space 数据表名
	 */
	public PrintStubsDiagramProduct(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析打印数据块分布图谱处理结果
	 * @param reader 可类化数据读取器
	 */
	public PrintStubsDiagramProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成打印数据块分布图谱处理结果的数据副本
	 * @param that 打印数据块分布图谱处理结果
	 */
	private PrintStubsDiagramProduct(PrintStubsDiagramProduct that) {
		super(that);
		space = that.space;
		array.addAll(that.array);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 保存一个缓存数据块编号
	 * @param e 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean add(PrintStubsDiagramItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批缓存数据块编号
	 * @param a 数据块编号集合
	 * @return 返回增加的数据块编号数目
	 */
	public int addAll(Collection<PrintStubsDiagramItem> a) {
		int size = array.size();
		for (PrintStubsDiagramItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回缓存块编号集合
	 * @return 长整型集合
	 */
	public List<PrintStubsDiagramItem> list(){
		return new ArrayList<PrintStubsDiagramItem>(array);
	}

	/**
	 * 保存全部
	 * @param that
	 */
	public void addAll(PrintStubsDiagramProduct that) {
		if (space == null) {
			space = that.space;
		} else if (Laxkit.compareTo(space, that.space) != 0) {
			throw new IllegalValueException("cannot be match! %s - %s", space, that.space);
		}
		array.addAll(that.array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public PrintStubsDiagramProduct duplicate() {
		return new PrintStubsDiagramProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
		// 缓存块
		writer.writeInt(array.size());
		for (PrintStubsDiagramItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
		// 缓存块
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PrintStubsDiagramItem e = new PrintStubsDiagramItem(reader);
			add(e);
		}
	}

}