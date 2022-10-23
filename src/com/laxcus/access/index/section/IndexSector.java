/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import java.io.*;

import com.laxcus.access.index.slide.*;
import com.laxcus.util.classable.*;

/**
 * 分布数据的索引扇区 <br><br>
 * 
 * 索引扇区是集群中某一列同类型数据的全部索引值统计后的映像分割。它的索引值对应“列空间（Dock）”的索引，或者其它数据对象的索引集合。<br><br>
 * 
 * 索引扇区包含一组衔接和不会重叠的数值范围（xxxRange），这个值是：short、int、long、float、double类型。<br><br>
 * 
 * 索引扇区通过索引平衡器的“IndexBalancer.balacne”方法产生，通过IndexSector.indexOf(Object that)方法，计算出每个列值在分区中的下标位置。<br>
 * 
 * 直属子类包括：ShortSector, Bit32Sector, Bit64Sector, FloatSector, DoubleSector。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/4/2020
 * @since laxcus 1.0
 */
public abstract class IndexSector implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -1188753001227084546L;

	/** 对象定位器，运行过程中赋值，不做为可类化对象使用！ **/
	private Slider slider;

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		
		//		// 码位计算器部件
		//		writer.writeInstance(part);
		//		// 列空间
		//		writer.writeInstance(dock);

		// 子类参数
		buildSuffix(writer);
		// 返回新增字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		//		// 码位计算器部件和列空间
		//		part = reader.readInstance(ScalerPart.class);
		//		// 列标记
		//		dock = reader.readInstance(Dock.class);

		// 子类参数
		resolveSuffix(reader);
		// 读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的索引扇区
	 */
	protected IndexSector() {
		super();
	}

	/**
	 * 使用传入的索引扇区实例，生成它的浅层数据副本（只赋值，不克隆）
	 * @param that 传入的索引分区
	 */
	protected IndexSector(IndexSector that) {
		super();
	}
	

	/**
	 * 设置对象定位器。<br>
	 * 注意：对象定位器在运行过程中赋值，用完即弃！不做为可类化对象使用！！！
	 * 
	 * @param e 对象定位器
	 */
	public void setSlider(Slider e) {
		slider = e;
	}

	/**
	 * 返回对象定位器
	 * @return 对象定位器
	 */
	protected Slider getSlider() {
		return slider;
	}
	
	/**
	 * 参数检查，如果有错误弹出异常
	 */
	protected void check() {
		if (size() == 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	/**
	 * 调用子类实例，克隆它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成数据流
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 解析数据流，返回解析的字节流尺寸
	 * 
	 * @param b  字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
	
	/**
	 * 将索引扇区写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析索引扇区
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

	/**
	 * 由子类实现，生成类实例的数据副本
	 * @return IndexSector子类实例的数据副本
	 */
	public abstract IndexSector duplicate();

	/**
	 * 返回分区的成员数
	 * @return 成员整型值
	 */
	public abstract int size();

	/**
	 * 根据传入的对象（可以是“列值”或者其它数值型对象），定位它在分区数组的下标位置。<br><br>
	 * 
	 * 允许传入和支持的对象包括:<br>
	 * (1) com.laxcus.access.column.Column的子类。<br>
	 * (2) java.lang.Number的子类，及java.lang.String、java.lang.Character。<br>
	 * (3) JAVA的数据类型数组(byte,char,short,int,long,float,double)。<br>
	 * 
	 * @param that 数据对象，上述三种情况。
	 * @return 返回对象所在集合的下标位置。返回-1是出错。
	 */
	public abstract int indexOf(Object that);

}