/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.mid;

import java.util.*;

import com.laxcus.distribute.mid.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * DIFFUSE/CONVERGE分布计算区 <br><br>
 * 
 * FluxArea是一个FROM/TO计算实例处理过程中，基于一个节点产生的数据映像。它的参数包括：站点地址、任务编号、超时时间，一组FluxField。<br>
 * 
 * 除了这些固定参数，用户也可以在FluxArea中自定义数据参数，相应的，这些自定义数据的处理也由用户的分布组件自行解释和处理。<br>
 * 
 * <b>注意：FluxArea只在DATA/WORK站点产生，且每个DATA/WORK站点只能产生一个FluxArea。</b><br>
 * 
 * 
 * @author scott.liang
 * @version 1.1 12/03/2015
 * @since laxcus 1.0
 */
public final class FluxArea extends SiteArea implements Comparable<FluxArea> {

	private static final long serialVersionUID = -2043123489585481570L;

	/** 任务编号，对应中间数据。系统分配，保证唯一 **/
	private long taskId;

	/** DATA/WORK节点数据等待时间，单位：秒 (超过指定时间，DATA将删除存储记录) **/
	private int timeout;

	/** DATA/WORK节点上的实体数据的元信息子区域集合  **/
	private ArrayList<FluxField> array = new ArrayList<FluxField>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.SiteArea#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 参数
		writer.writeLong(taskId);
		writer.writeInt(timeout);
		// 成员参数
		writer.writeInt(array.size());
		for (FluxField field : array) {
			writer.writeObject(field);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.SiteArea#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 参数	
		taskId = reader.readLong();
		timeout = reader.readInt();
		// 成员数据
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FluxField field = new FluxField(reader);
			array.add(field);
		}
	}

	/**
	 * 根据传入参数生成它的副本
	 * @param that FluxArea实例
	 */
	private FluxArea(FluxArea that) {
		super(that);
		taskId = that.taskId;
		timeout = that.timeout;
		for (FluxField field : that.array) {
			array.add(field.duplicate());
		}
	}

	/**
	 * 初始化磁盘文件数据集
	 */
	public FluxArea() {
		super();
		taskId = 0L;
		timeout = 0;
	}
	
	/**
	 * 构造DIFFUSE/CONVERGE分布计算区，包括数据源站点地址，任务编号，超时时间
	 * @param source 数据源站点地址
	 * @param taskId 任务编号
	 * @param timeout 存取超时
	 */
	public FluxArea(Node source, long taskId, int timeout) {
		this();
		setSource(source);
		setTaskId(taskId);
		setTimeout(timeout);
	}

//	/**
//	 * 初始化并且任务工作号、站点地址，数据超时时间
//	 * @param taskId 任务编号
//	 * @param source 数据源站点地址
//	 * @param timeout 存取超时
//	 */
//	public FluxArea(long taskId, Node source, int timeout) {
//		this();
//		setTaskId(taskId);
//		super.setSource(source);
//		setTimeout(timeout);
//	}

	/**
	 * 从可类化数据读取器中解析磁盘数据图谱
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FluxArea(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置任务编号
	 * @param i 任务编号
	 */
	public void setTaskId(long i) {
		taskId = i;
	}

	/**
	 * 返回任务编号
	 * @return 任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 设置数据超时时间
	 * @param i 数据超时时间
	 */
	public void setTimeout(int i) {
		timeout = i;
	}

	/**
	 * 返回数据超时时间
	 * 
	 * @return 数据超时时间
	 */
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * 设置迭代编号。<br>
	 * 这个操作应该发生在“TO”阶段的“effuse”方法中。是FluxArea在输出之前，给每个FluxField子集设置编号。编号通过ToSession获得<br>
	 * 通过迭代编号，映像数据与实体数据形成关联。<br>
	 * FROM阶段不需要迭代，默认是-1。可以不设置。<br>
	 * @param index
	 */
	public void setIterateIndex(int index) {
		for(FluxField field : array) {
			field.setIterateIndex(index);
		}
	}

	/**
	 * 保存一项磁盘数据记录，每个分布信息具有唯一性
	 * @param e FluxField实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(FluxField e) {
		if (e == null || array.contains(e)) {
			return false;
		}
		return array.add((FluxField) e.clone());
	}

	/**
	 * 保存一组磁盘数据记录
	 * @param a FluxField集合
	 * @return 返回新增成员数目
	 */
	public int add(Collection<FluxField> a) {
		int size = array.size();
		for (FluxField field : a) {
			add(field);
		}
		return array.size() - size;
	}

	/**
	 * 删除一项磁盘数据记录
	 * @param e FluxField实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(FluxField e) {
		return array.remove(e);
	}

	/**
	 * 返回磁盘数据记录集合
	 * @return FluxField列表
	 */
	public List<FluxField> list() {
		return new ArrayList<FluxField>(array);
	}

	/**
	 * 收缩内存占用
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 统计磁盘文件的总长度
	 * 
	 * @return 文件总长度的长整型值
	 */
	public long length() {
		long len = 0L;
		for (FluxField field : array) {
			len += field.length();
		}
		return len;
	}

	/**
	 * 判断是否空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 返回集合中的FluxField成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%d/%d", super.getSource(), taskId, array.size());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FluxArea.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FluxArea) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (taskId >>> 32 & taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FluxArea that) {
		// 空对象排在前面
		if(that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(taskId, that.taskId);
		if (ret == 0) {
			ret = Laxkit.compareTo(getSource(), that.getSource());
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public FluxArea duplicate() {
		return new FluxArea(this);
	}

	
}