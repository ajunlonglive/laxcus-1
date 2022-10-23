/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.command;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 释放ESTABLISH.SIFT产生的数据资源。<br>
 * 
 * 这个操作发生在ESTABLISH命令执行完成后，由CALL.END阶段发出，目标是BUILD站点。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2015
 * @since laxcus 1.0
 */
public final class ReleaseSiftSource extends Command {

	private static final long serialVersionUID = -6769861205895550499L;

	/** 数据表名 **/
	private Set<Space> array = new TreeSet<Space>();
	
	/**
	 * 构造默认的释放SIFT资源命令
	 */
	public ReleaseSiftSource() {
		super();
	}

	/**
	 * 构造释放SIFT资源命令，指定数据表名
	 * @param e 数据表名
	 */
	public ReleaseSiftSource(Space e) {
		this();
		add(e);
	}

	/**
	 * 构造释放SIFT资源命令，指定一批表
	 * @param a 数据表名数组
	 */
	public ReleaseSiftSource(Collection<Space> a) {
		this();
		addAll(a);
	}
	
	/**
	 * 构造一个释放SIFT资源命令实例浅层数据副本
	 * @param that 传入实例
	 */
	private ReleaseSiftSource(ReleaseSiftSource that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 从可类化数据读取器中解析释放SIFT资源命令参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ReleaseSiftSource(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存一个数据表名，不允许空指针。
	 * @param e 数据表名实例
	 * @return 成功返回真，否则假
	 */
	public boolean add(Space e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 删除一个数据表名
	 * @param e Space实例
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Space e) {
		return array.remove(e);
	}

	/**
	 * 保存一批数据表名
	 * @param a 数据表名数组
	 * @return 返回新增的表
	 */
	public int addAll(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部数据表名
	 * @return 数据表名列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 统计数据表名数目
	 * @return 数据表名数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 条件成立返回真，否则假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReleaseSiftSource duplicate() {
		return new ReleaseSiftSource(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Space e : array) {
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
			Space e = new Space(reader);
			array.add(e);
		}
	}

}