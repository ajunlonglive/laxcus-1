/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 诊断分布任务组件所在的ACCOUNT站点地址
 * 
 * @author scott.liang
 * @version 1.0 3/12/2017
 * @since laxcus 1.0
 */
public class AssertTaskHub extends Command {
	
	private static final long serialVersionUID = -7309732561100075939L;
	
	/** 签名集合 **/
	private Set<TaskPart> array = new TreeSet<TaskPart>();

	/**
	 * 构造默认的诊断分布任务组件所在的ACCOUNT站点地址
	 */
	public AssertTaskHub() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析诊断分布任务组件所在的ACCOUNT站点地址
	 * @param reader 可类化数据读取器
	 */
	public AssertTaskHub(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成诊断分布任务组件所在的ACCOUNT站点地址数据副本
	 * @param that AssertTaskHub实例
	 */
	private AssertTaskHub(AssertTaskHub that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存分布任务组件的工作部件，不允许空指针
	 * @param e TaskPart实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(TaskPart e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批分布任务组件的工作部件
	 * @param a TaskPart数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<TaskPart> a) {
		int size = array.size();
		for (TaskPart e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 删除一个分布任务组件的工作部件
	 * @param e TaskPart实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(TaskPart e) {
		return array.remove(e);
	}

	/**
	 * 输出全部分布任务组件的工作部件
	 * @return TaskPart列表
	 */
	public List<TaskPart> list() {
		return new ArrayList<TaskPart>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回分布任务组件的工作部件成员数目
	 * @return int值
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertTaskHub duplicate() {
		return new AssertTaskHub(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (TaskPart e : array) {
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
			TaskPart e = new TaskPart(reader);
			array.add(e);
		}
	}

}
