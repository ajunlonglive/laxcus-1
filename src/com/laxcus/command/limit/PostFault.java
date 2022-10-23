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
 * 投递故障锁定命令
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public abstract class PostFault extends Command {
	
	private static final long serialVersionUID = -3032788878072443286L;
	
	/** 故障锁定单元集合 **/
	private TreeSet<FaultItem> array = new TreeSet<FaultItem>();

	/**
	 * 构造默认的投递故障锁定命令
	 */
	protected PostFault() {
		super();
	}

	/**
	 * 生成投递故障锁定命令数据副本
	 * @param that PostFault实例
	 */
	protected PostFault(PostFault that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个故障锁定单元，不允许空指针
	 * @param e FaultItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(FaultItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批故障锁定单元 
	 * @param a FaultItem数组
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<FaultItem> a) {
		int size = array.size();
		for (FaultItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 删除一个故障锁定单元
	 * @param e FaultItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(FaultItem e) {
		if (e != null) {
			return array.remove(e);
		}
		return false;
	}
	
	/**
	 * 删除全部故障锁定单元
	 * @param a 故障锁定单元数组
	 * @return 返回被删除的成员数目
	 */
	public int removeAll(Collection<FaultItem> a) {
		int size = array.size();
		for (FaultItem e : a) {
			remove(e);
		}
		return size - array.size();
	}

	/**
	 * 输出全部故障锁定单元
	 * @return FaultItem列表
	 */
	public List<FaultItem> list() {
		return new ArrayList<FaultItem>(array);
	}

	/**
	 * 判断包含一个故障锁定单元
	 * @param e FaultItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(FaultItem e) {
		return array.contains(e);
	}

	/**
	 * 统计故障锁定单元成员数目
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
		for (FaultItem e : array) {
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
			FaultItem e = FaultItemCreator.resolve(reader);
			array.add(e);
		}
	}

}