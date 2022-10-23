/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递限制操作命令
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public abstract class PostLimit extends Command {
	
	private static final long serialVersionUID = -3032788878072443286L;
	
	/** 限制操作单元集合 **/
	private TreeSet<LimitItem> array = new TreeSet<LimitItem>();

	/**
	 * 构造默认的投递限制操作命令
	 */
	protected PostLimit() {
		super();
	}

	/**
	 * 生成投递限制操作命令数据副本
	 * @param that PostLimit实例
	 */
	protected PostLimit(PostLimit that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个限制操作单元，不允许空指针
	 * @param e LimitItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(LimitItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除一个限制操作单元
	 * @param e LimitItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(LimitItem e) {
		if (e != null) {
			return array.remove(e);
		}
		return false;
	}

	/**
	 * 输出全部限制操作单元
	 * @return LimitItem数组
	 */
	public List<LimitItem> list() {
		return new ArrayList<LimitItem>(array);
	}

	/**
	 * 判断包含一个限制操作单元
	 * @param e LimitItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(LimitItem e) {
		return array.contains(e);
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
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
		for (LimitItem e : array) {
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
			LimitItem e = LimitItemCreator.resolve(reader);
			array.add(e);
		}
	}

}
