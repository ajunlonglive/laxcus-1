/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检测多个节点之间的传输流量
 * 
 * @author scott.liang
 * @version 1.0 8/20/2018
 * @since laxcus 1.0
 */
public class MultiGust extends Command {
	
	private static final long serialVersionUID = -4969544908951240415L;
	
	/** 编号，来自Parallel Multi Swarm命令，默认是0，无定义！ **/
	private int serial;
	
	/** 流量测试命令 **/
	private ArrayList<Gust> array = new ArrayList<Gust>();

	/**
	 * 构造默认的多节点流量检测命令
	 */
	public MultiGust() {
		super();
		serial =0;
	}

	/**
	 * 生成多节点流量检测命令副本
	 * @param that 多节点流量检测命令
	 */
	private MultiGust(MultiGust that) {
		super(that);
		serial = that.serial;
		array.addAll(that.array);
	}
	
	/**
	 * 从可类化数据读取器中解析多节点流量检测命令
	 * @param reader 可类化数据读取器
	 */
	public MultiGust(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置序列号
	 * @param who
	 */
	public void setSerial(int who) {
		serial = who;
	}
	
	/**
	 * 返回序列号
	 * @return
	 */
	public int getSerial() {
		return serial;
	}
	
	/**
	 * 保存命令
	 * @param e
	 */
	public void add(Gust e) {
		Laxkit.nullabled(e);
		array.add(e);
	}

	/**
	 * 输出命令
	 * @return
	 */
	public List<Gust> list() {
		return new ArrayList<Gust>(array);
	}
	
	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 命令数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MultiGust duplicate() {
		return new MultiGust(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(serial);
		writer.writeInt(array.size());
		for (Gust e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		serial = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Gust e = new Gust(reader);
			array.add(e);
		}
	}

}