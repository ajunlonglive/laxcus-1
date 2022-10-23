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

/**
 * 分布任务组件成员，包括“引导包”、“JAR附件包”、“动态链接库”的属性类
 * 
 * @author scott.liang
 * @version 1.0 6/17/2020
 * @since laxcus 1.0
 */
public class TaskElement implements Comparable<TaskElement>, Cloneable {

	/** 工作分区 **/
	private TaskSection section;
	
	/** 所在目录 **/
	private File root;
	
	/** 引导类 **/
	private TaskElementBoot boot;

	/** 辅助附件包（*.jar格式）**/
	private TreeSet<FileKey> assists = new TreeSet<FileKey>();
	
	/** 动态链接库文件属性 **/
	private TreeSet<FileKey> links = new TreeSet<FileKey>();

	/**
	 * 构造默认的分布任务组件成员
	 */
	public TaskElement() {
		super();
	}
	
	/**
	 * 生成当前分布任务组件成员的数据副本
	 * @param that 当前分布任务组件成员
	 */
	private TaskElement(TaskElement that) {
		this();
		section = that.section;
		root = that.root;
		boot = that.boot;
		assists.addAll(that.assists);
		links.addAll(that.links);
	}

	/**
	 * 构造分布任务组件成员，指定工作部件和磁盘目录
	 * @param root 磁盘目录
	 * @param section
	 */
	public TaskElement(File root, TaskSection section) {
		this();
		setRoot(root);
		setSection(section);
	}

	/**
	 * 设置分布任务组件所在的磁盘目录，不允许空指针！！！
	 * @param e 磁盘目录
	 */
	public void setRoot(File e) {
		Laxkit.nullabled(e);
		root = e;
	}

	/**
	 * 返回分布任务组件群在的磁盘目录
	 * @return 磁盘目录
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * 设置分布任务组件工作部件，不允许空指针!!!
	 * @param e TaskSection实例
	 */
	public void setSection(TaskSection e) {
		Laxkit.nullabled(e);
		section = e;
	}

	/**
	 * 返回分布任务组件工作部件
	 * @return TaskSection实例
	 */
	public TaskSection getSection() {
		return section;
	}
	
	/**
	 * 返回执行部件
	 * @return TaskPart实例
	 */
	public TaskPart getTaskPart() {
		if (section == null) {
			return null;
		}
		return section.getTaskPart();
	}

	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getIssuer() {
		if (section == null) {
			return null;
		}
		return section.getIssuer();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return boot == null && assists.isEmpty() && links.isEmpty();
	}
	
	/**
	 * 返回当前成员数目
	 * @return 成员数目
	 */
	public int size() {
		return (boot != null ? 1 : 0) + assists.size() + links.size();
	}

	/**
	 * 设置分布计算组件引导包，不允许空指针！
	 * @param e 分布计算组件引导包
	 */
	public void setBoot(TaskElementBoot e) {
		Laxkit.nullabled(e);
		boot = e;
	}

	/**
	 * 返回分布计算组件引导包
	 * @return 分布计算组件引导包
	 */
	public TaskElementBoot getBoot() {
		return boot;
	}

	/**
	 * 保存JAR文件标记
	 * @param key JAR标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean addJAR(FileKey key) {
		Laxkit.nullabled(key);
		return assists.add(key);
	}
	
	/**
	 * 删除JAR文件标记
	 * @param key JAR标记
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeJAR(FileKey key) {
		Laxkit.nullabled(key);
		return assists.remove(key);
	}

	/**
	 * 输出JAR包
	 * @return FileKey列表
	 */
	public List<FileKey> getJARs() {
		return new ArrayList<FileKey>(assists);
	}

	/**
	 * 保存动态链接库文件标记
	 * @param key 动态链接库文件
	 * @return 保存成功返回真，否则假
	 */
	public boolean addLibrary(FileKey key) {
		Laxkit.nullabled(key);
		return links.add(key);
	}
	
	/**
	 * 删除动态链接库文件标记
	 * @param key 动态链接库文件
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeLibrary(FileKey key) {
		Laxkit.nullabled(key);
		return links.remove(key);
	}

	/**
	 * 输出动态链接库文件
	 * @return FileKey集合
	 */
	public List<FileKey> getLibraries() {
		return new ArrayList<FileKey>(links);
	}

	/**
	 * 生成数据副本
	 * @return 数据副本
	 */
	public TaskElement duplicate() {
		return new TaskElement(this);
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
//	 * 比较两个对象一致！
//	 * @param that 
//	 * @return 返回真或者假
//	 */
//	public boolean match(TaskElement that) {
//		if (that == null) {
//			return false;
//		}
//		// 逐个比较匹配
//		int ret = Laxkit.compareTo(section, that.section);
//		if (ret == 0) {
//			ret = Laxkit.compareTo(boot, that.boot);
//		}
//		// 附件文件
//		if (ret == 0) {
//			ret = Laxkit.compareTo(assists.size(), that.assists.size());
//		}
//		if (ret == 0) {
//			for (FileKey key : assists) {
//				for (FileKey key2 : that.assists) {
//					ret = Laxkit.compareTo(key, key2);
//					if (ret != 0) return false;
//				}
//			}
//		}
//		// 动态链接库
//		if (ret == 0) {
//			ret = Laxkit.compareTo(links.size(), that.links.size());
//		}
//		if (ret == 0) {
//			for (FileKey key : links) {
//				for (FileKey key2 : that.links) {
//					ret = Laxkit.compareTo(key, key2);
//					if (ret != 0) return false;
//				}
//			}
//		}
//
//		// 判断一致
//		return ret == 0;
//	}

//	/**
//	 * 比较两个对象一致！
//	 * @param that 
//	 * @return 返回真或者假
//	 */
//	public boolean match(TaskElement that) {
//		if (that == null) {
//			return false;
//		}
//		// 逐个比较匹配
//		int ret = Laxkit.compareTo(section, that.section);
////		System.out.printf("section check, %s -> %s, %d\n", section, that.section, ret);
//		if (ret == 0) {
//			ret = Laxkit.compareTo(boot, that.boot);
////			System.out.printf("boot: %s -> %s, %d\n", boot, that.boot, ret);
//		}
//		// 附件文件
//		if (ret == 0) {
//			ret = Laxkit.compareTo(assists.size(), that.assists.size());
////			System.out.printf("assists size: %d -> %d, %d\n", assists.size(), that.assists.size(), ret);
//		}
//		if (ret == 0) {
//			for (FileKey key : assists) {
//				int count = 0;
//				for (FileKey key2 : that.assists) {
//					if (Laxkit.compareTo(key, key2) == 0) {
//						count++;
//					}
//				}
//				// 必须有且只有一个，否则是错误
//				if (count != 1) {
////					System.out.printf("assists FileKey not match!\n");
//					return false;
//				}
//			}
//		}
//		// 动态链接库
//		if (ret == 0) {
//			ret = Laxkit.compareTo(links.size(), that.links.size());
////			System.out.printf("links size: %d -> %d, %d\n", links.size(), that.links.size(), ret);
//		}
//		if (ret == 0) {
//			for (FileKey key : links) {
//				int count = 0;
//				for (FileKey key2 : that.links) {
//					if (Laxkit.compareTo(key, key2) == 0) {
//						count++;
//					}
//				}
//				// 必须有且只有一个，否则是错误!
//				if (count != 1) {
////					System.out.printf("link FileKey not match!\n");
//					return false;
//				}
//			}
//		}
//
//		// 判断一致
//		return ret == 0;
//	}
	
	/**
	 * 比较两个对象一致！
	 * @param that 
	 * @return 返回真或者假
	 */
	public boolean match(TaskElement that) {
		if (that == null) {
			return false;
		}
		// 逐个比较匹配
		int ret = Laxkit.compareTo(section, that.section);
		if (ret == 0) {
			ret = Laxkit.compareTo(boot, that.boot);
		}
		// 附件文件
		if (ret == 0) {
			ret = Laxkit.compareTo(assists.size(), that.assists.size());
		}
		if (ret == 0) {
			for (FileKey key : assists) {
				int count = 0;
				for (FileKey key2 : that.assists) {
					if (Laxkit.compareTo(key, key2) == 0) {
						count++;
					}
				}
				// 必须有且只有一个，否则是错误
				if (count != 1) {
					return false;
				}
			}
		}
		// 动态链接库
		if (ret == 0) {
			ret = Laxkit.compareTo(links.size(), that.links.size());
		}
		if (ret == 0) {
			for (FileKey key : links) {
				int count = 0;
				for (FileKey key2 : that.links) {
					if (Laxkit.compareTo(key, key2) == 0) {
						count++;
					}
				}
				// 必须有且只有一个，否则是错误!
				if (count != 1) {
					return false;
				}
			}
		}

		// 判断一致
		return ret == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskElement that) {
		if (that == null) {
			return 1;
		}
		// 比较工作区和路径
		int ret = Laxkit.compareTo(section, that.section);
		if (ret == 0) {
			ret = Laxkit.compareTo(root, that.root);
		}
		return ret;
	}

}