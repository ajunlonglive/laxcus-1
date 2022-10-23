/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.forbid;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.law.forbid.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递禁止操作命令
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public abstract class PostForbid extends Command {
	
	private static final long serialVersionUID = -3032788878072443286L;
	
	/** 禁止操作单元集合 **/
	private TreeSet<ForbidItem> array = new TreeSet<ForbidItem>();

	/**
	 * 构造默认的投递禁止操作命令
	 */
	protected PostForbid() {
		super();
	}

	/**
	 * 生成投递禁止操作命令数据副本
	 * @param that PostForbid实例
	 */
	protected PostForbid(PostForbid that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个禁止操作单元，不允许空指针
	 * @param e ForbidItem实例
	 * @return 返回真或者假
	 */
	public boolean add(ForbidItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批禁止操作单元 
	 * @param a ForbidItem数组
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<ForbidItem> a) {
		int size = array.size();
		for (ForbidItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 删除一个禁止操作单元
	 * @param e ForbidItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(ForbidItem e) {
		return array.remove(e);
	}

	/**
	 * 输出全部禁止操作单元
	 * @return ForbidItem列表
	 */
	public List<ForbidItem> list() {
		return new ArrayList<ForbidItem>(array);
	}

	/**
	 * 判断包含一个禁止操作单元
	 * @param e ForbidItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ForbidItem e) {
		return array.contains(e);
	}

	/**
	 * 统计禁止操作单元成员数目
	 * @return ForbidItem成员数目
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

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ForbidItem e : array) {
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
			ForbidItem e = ForbidItemCreator.resolve(reader);
			array.add(e);
		}
	}

}