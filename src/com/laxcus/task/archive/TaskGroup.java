/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.hash.*;

/**
 * 分布任务组件群，由任意多个组件组成。
 * 
 * @author scott.liang
 * @version 1.0 6/17/2020
 * @since laxcus 1.0
 */
public class TaskGroup implements Comparable<TaskGroup>, Cloneable {

	/** 组件标记 **/
	private TaskTag tag;

	/** 所在目录 **/
	private File root;

	/** 组件成员 **/
	private TreeSet<TaskElement> elements = new TreeSet<TaskElement>();

	/**
	 * 构造默认的分布任务组件群
	 */
	public TaskGroup() {
		super();
	}

	/**
	 * 生成当前分布任务组件群的数据副本
	 * @param that 当前分布任务组件群
	 */
	private TaskGroup(TaskGroup that) {
		this();
		tag = that.tag;
		root = that.root;
		elements.addAll(that.elements);
	}

	/**
	 * 构造分布任务组件群，指定组件标记和磁盘目录
	 * @param tag 组件标记
	 * @param root 磁盘根目录
	 */
	public TaskGroup(TaskTag tag, File root) {
		this();
		setTag(tag);
		setRoot(root);
	}

	/**
	 * 构造分布任务组件群，指定组件标记和磁盘目录
	 * @param part 组件部件
	 * @param sign 组件的MD5签名
	 * @param root 磁盘根目录
	 */
	public TaskGroup(TaskPart part, MD5Hash sign, File root) {
		this(new TaskTag(part, sign), root);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * 设置分布任务组件群所在的磁盘目录，不允许空指针！！！
	 * @param e 磁盘目录
	 */
	public void setRoot(File e) {
		Laxkit.nullabled(e);
		root = e;
	}

	/**
	 * 返回分布任务组件群所在的磁盘目录
	 * @return 磁盘目录
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * 设置分布任务组件组件标记，不允许空指针!!!
	 * @param e TaskTag实例
	 */
	public void setTag(TaskTag e) {
		Laxkit.nullabled(e);
		tag = e;
	}

	/**
	 * 返回分布任务组件组件标记
	 * @return TaskTag实例
	 */
	public TaskTag getTag() {
		return tag;
	}

	/**
	 * 返回工作部件
	 * @return TaskPart实例
	 */
	public TaskPart getPart() {
		return tag.getPart();
	}

	/**
	 * 返回签名
	 * @return MD5Hash实例
	 */
	public MD5Hash getSign() {
		return tag.getSign();
	}

	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getIssuer() {
		if (tag == null) {
			return null;
		}
		return tag.getIssuer();
	}

	/**
	 * 保存分布任务组件成员，不允许空指针
	 * @param e 分布任务组件成员
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(TaskElement e) {
		Laxkit.nullabled(e);
		return elements.add(e);
	}

	/**
	 * 删除分布任务组件成员，不允许空指针
	 * @param e 分布任务组件成员
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(TaskElement e) {
		Laxkit.nullabled(e);
		return elements.remove(e);
	}

	/**
	 * 输出包
	 * @return TaskElement列表
	 */
	public List<TaskElement> list() {
		return new ArrayList<TaskElement>(elements);
	}

	/**
	 * 统计成员数目
	 * @return 整数
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * 清除全部旧的内容
	 */
	public void clear() {
		elements.clear();
	}

	/**
	 * 生成数据副本
	 * @return 数据副本
	 */
	public TaskGroup duplicate() {
		return new TaskGroup(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

//	/**
//	 * 判断对象匹配
//	 * @param that 传入实例
//	 * @return 一致返回真，否则假
//	 */
//	public boolean match(TaskGroup that) {
//		if (that == null) {
//			return false;
//		}
//		// 标记
//		int ret = Laxkit.compareTo(tag, that.tag);
//		System.out.printf("%s -> %s: %d\n", tag, that.tag, ret);
//		if (ret == 0) {
//			ret = Laxkit.compareTo(root, that.root);
//			System.out.printf("%s -> %s: %d\n", root, that.root, ret);
//		}
//		// 成员数目一致！
//		if (ret == 0) {
//			ret = Laxkit.compareTo(elements.size(), that.elements.size());
//			System.out.printf("%d -> %d: %d\n", elements.size(), that.elements.size(), ret);
//		}
//		// 比较成员
//		if (ret == 0) {
//			for (TaskElement key : elements) {
//				for (TaskElement key2 : that.elements) {
//					boolean match = key.match(key2);
//					if (!match) {
//						System.out.printf("%s -> %s, not match!\n", key, key2);
//						return false;
//					}
////					ret = (match ? 0 : -1);
////					if (ret != 0) break;
//				}
////				if (ret != 0) break;
//			}
//		}
//		// 判断一致！
//		return ret == 0;
//	}
	
//	/**
//	 * 判断对象匹配
//	 * @param that 传入实例
//	 * @return 一致返回真，否则假
//	 */
//	public boolean match(TaskGroup that) {
//		if (that == null) {
//			return false;
//		}
//		// 标记
//		int ret = Laxkit.compareTo(tag, that.tag);
////		System.out.printf("\n%s -> %s: %d\n", tag, that.tag, ret);
//		if (ret == 0) {
//			ret = Laxkit.compareTo(root, that.root);
////			System.out.printf("%s -> %s: %d\n", root, that.root, ret);
//		}
//		// 成员数目一致！
//		if (ret == 0) {
//			ret = Laxkit.compareTo(elements.size(), that.elements.size());
////			System.out.printf("%d -> %d: %d\n", elements.size(), that.elements.size(), ret);
//		}
//		// 比较成员
//		if (ret == 0) {
////			System.out.println("check TaskElement!");
//			for (TaskElement key : elements) {
//				int count = 0;
//				for (TaskElement key2 : that.elements) {
//					if (key.match(key2)) {
//						count++;
//					}
//				}
//				// 必须有且只有一个匹配的，否则是错误！
//				if (count != 1) {
////					System.out.println("not match!");
//					return false;
//				}
//			}
//		}
//		// 判断一致！
//		return ret == 0;
//	}

	/**
	 * 判断对象匹配
	 * @param that 传入实例
	 * @return 一致返回真，否则假
	 */
	public boolean match(TaskGroup that) {
		if (that == null) {
			return false;
		}
		// 标记
		int ret = Laxkit.compareTo(tag, that.tag);
		if (ret == 0) {
			ret = Laxkit.compareTo(root, that.root);
		}
		// 成员数目一致！
		if (ret == 0) {
			ret = Laxkit.compareTo(elements.size(), that.elements.size());
		}
		// 比较成员
		if (ret == 0) {
			for (TaskElement key : elements) {
				int count = 0;
				for (TaskElement key2 : that.elements) {
					if (key.match(key2)) {
						count++;
					}
				}
				// 必须有且只有一个匹配的，否则是错误！
				if (count != 1) {
					return false;
				}
			}
		}
		// 判断一致！
		return ret == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskGroup that) {
		if (that == null) {
			return 1;
		}
		// 只比较路径
		return Laxkit.compareTo(root, that.root);
	}

}