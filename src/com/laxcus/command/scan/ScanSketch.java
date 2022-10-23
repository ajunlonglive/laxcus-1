/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分析表分布数据容量。<br><br>
 * 
 * 数据容量内容包括：数据块数目、数据块文件尺寸、全部行数、有效行数。<br>
 * 通过数据容量分析，用户决定使用MODULATE、REGULATE来优化数据。<br><br>
 * 
 * 此命令由FRONT节点发出，具有强一致和实时状态检查的特点。任何一个节点出现错误，都会返回失败。
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public final class ScanSketch extends Command {

	private static final long serialVersionUID = 8723161901886340210L;

	/** 表名 **/
	private Space space;
	
	/**
	 * 构造分析表分布数据容量
	 */
	public ScanSketch() {
		super();
	}

	/**
	 * 根据传入分析表分布数据容量，生成它的数据副本
	 * @param that ScanSketch实例
	 */
	private ScanSketch(ScanSketch that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造分析表分布数据容量，指定数据表名
	 * @param space 数据表名
	 */
	public ScanSketch(Space space) {
		this();
		setSpace(space);
	}
	
	/**
	 * 从可类化读取器中解析分析表分布数据容量
	 * @param reader 可类化数据读取器
	 */
	public ScanSketch(ClassReader reader) {
		this();
		resolve(reader);
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

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanSketch duplicate() {
		return new ScanSketch(this);
	}

	/**
	 * 将被处理的表名写入可类化存储器
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/**
	 * 从可类化读取器中解析被处理的表名
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}
}