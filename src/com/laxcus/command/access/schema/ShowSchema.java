/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 显示数据库命令。<br><br>
 * 
 * 命令格式1：SHOW DATABASE 数据库, 数据库, ... <br>
 * 命令格式2: SHOW DATABASE ALL  <br>
 * 
 * SHOW DATABASE与FIND DATABASE不同之处在于，SHOW DATABASE返回一组数据库配置（多个）, FIND DATABASE只返回一个。<BR>
 * 
 * @author scott.liang
 * @version 1.1 03/02/2015
 * @since laxcus 1.0
 */
public class ShowSchema extends Command {

	private static final long serialVersionUID = -7383971617459966228L;

	/** 数据库名集合  **/
	private TreeSet<Fame> array = new TreeSet<Fame>();

	/**
	 * 构造默认的显示数据库命令。
	 */
	public ShowSchema() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示数据库
	 * @param reader 可类化数据读取器
	 */
	public ShowSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的显示数据库命令，生成它的数据副本
	 * @param that ShowSchema实例
	 */
	private ShowSchema(ShowSchema that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 判断显示全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/**
	 * 保存一个数据库名
	 * @param e Fame实例
	 * @return 返回真或者假
	 */
	public boolean add(Fame e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 保存一批数据库
	 * @param a 数据库集合
	 * @return 返回新增数目
	 */
	public int addAll(Collection<Fame> a) {
		int size = array.size();
		for (Fame e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回数据库名列表
	 * @return Fame列表
	 */
	public List<Fame> list() {
		return new ArrayList<Fame>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回数据库数目
	 * @return 数据库数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowSchema duplicate() {
		return new ShowSchema(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Fame e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Fame e = new Fame(reader);
			array.add(e);
		}
	}

}