/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 数据库配置。<br>
 * 
 * 数据库规则定义：<br>
 * 1. 每个数据库的名称都是全局唯一，不允许重复。<br>
 * 2. 数据库名称忽略大小写。<br>
 * 3. 数据库包含任意多个数据表和数据优化器。<br>
 * 4. 数据库有最大空间尺寸定义。<br>
 * 
 * @author scott.liang
 * @version 1.1 03/12/2015
 * @since laxcus 1.0
 */
public final class Schema implements Classable, Markable, Serializable, Cloneable, Comparable<Schema> {

	private static final long serialVersionUID = 7174541410589951141L;

	/** 数据库名称，忽略大小写。名称长度不得超过20字节，此定义见Space类 */
	private Fame fame;

	/** 数据库空间的最大允许字节量。默认是0，不定义 */
	private long maxsize;
	
	/** 数据库建立时间，由TOP站点设置 **/
	private long createTime;

	/** 数据表名 -> 数据表 */
	private TreeMap<Space, Table> tables = new TreeMap<Space, Table>();

	/** 数据表名 -> 数据表优化触发器（达到触发时间，启动数据表的优化工作） */
	private TreeMap<Space, SwitchTime> switches = new TreeMap<Space, SwitchTime>();

	/**
	 * 将数据库参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int seek = writer.size();
		// 写入数据库名称
		writer.writeObject(fame);
		// 最大空间
		writer.writeLong(maxsize);
		// 建立时间
		writer.writeLong(createTime);
		// 表成员记录
		writer.writeInt(tables.size());
		for (Table e : tables.values()) {
			writer.writeObject(e);
		}
		// 定时触发器
		writer.writeInt(switches.size());
		for (SwitchTime e : switches.values()) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - seek;
	}

	/**
	 * 从可类化读取器中解析数据库参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 数据库名称
		fame = new Fame(reader);
		// 最大空间
		maxsize = reader.readLong();
		// 数据库建立时间
		createTime = reader.readLong();
		// 表成员数目
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Table e = new Table(reader);
			tables.put(e.getSpace(), e);
		}
		// 定时触发器
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SwitchTime e = new SwitchTime(reader);
			switches.put(e.getSpace(), e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入参数，生成它的副本
	 * @param that Schema实例
	 */
	private Schema(Schema that) {
		this();
		fame = that.fame;
		maxsize = that.maxsize;
		createTime = that.createTime;
		tables.putAll(that.tables);
		switches.putAll(that.switches);
	}

	/**
	 * 构造一个默认的数据库
	 */
	public Schema() {
		super();
		maxsize = 0L;
		setCreateTime(SimpleTimestamp.currentTimeMillis());
	}

	/**
	 * 从可类化读取器中解析数据库的参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Schema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据库参数
	 * @param reader 标记化读取器
	 */
	public Schema(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 构造一个数据库，同时指定它的名称
	 * @param name 数据库名称
	 */
	public Schema(String name) {
		this();
		setFame(new Fame(name));
	}

	/**
	 * 构造一个数据库，同时指定它的名称
	 * @param name 数据库名称
	 */
	public Schema(Naming name) {
		this();
		setFame(new Fame(name));
	}

	/**
	 * 设置数据库名称
	 * @param e 数据库名
	 */
	public void setFame(Fame e) {
		Laxkit.nullabled(e);

		fame = e;
	}

	/**
	 * 返回数据库名称
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/**
	 * 设置数据库最大空间尺寸
	 * @param size 空间尺寸
	 */
	public void setMaxSize(long size) {
		if (size < 0L) {
			throw new IllegalValueException("illegal maxsize %d", size);
		}
		maxsize = size;
	}

	/**
	 * 返回数据库最大空间尺寸
	 * 
	 * @return 长整型空间尺寸
	 */
	public long getMaxSize() {
		return maxsize;
	}
	
	/**
	 * 设置数据库建立时间
	 * @param i 数据库建立时间
	 */
	public void setCreateTime(long i){
		createTime = i;
	}
	
	/**
	 * 返回数据库建立时间
	 * @return 数据库建立时间
	 */
	public long getCreateTime(){
		return createTime;
	}

	/**
	 * 判断数据表是否存在
	 * 
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space space) {
		return find(space) != null;
	}

	/**
	 * 返回数据库下的全部数据表名
	 * 
	 * @return 数据表名列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(tables.keySet());
	}

	/**
	 * 输出全部时间触发器
	 * @return 时间触发器列表
	 */
	public List<SwitchTime> getSwitchTimes() {
		return new ArrayList<SwitchTime>(switches.values());
	}

	/**
	 * 根据数据表名查找对应的数据块尺寸.没找到返回-1
	 * 
	 * @param space 数据表名
	 * @return 返回数据块尺寸，或者-1
	 */
	public int findChunkSize(Space space) {
		Table table = tables.get(space);
		if (table != null) {
			return table.getChunkSize();
		}
		return -1;
	}

	/**
	 * 设置数据表的数据块尺寸
	 * 
	 * @param space 数据表名
	 * @param size 数据块尺寸
	 * @return 成功返回真，否则假
	 */
	public boolean setChunkSize(Space space, int size) {
		Table table = tables.get(space);
		if (table != null) {
			table.setChunkSize(size);
			return true;
		}
		return false;
	}

	/**
	 * 建立数据优化器
	 * @param time 时间触发器
	 * @return 成功返回真，否则假
	 */
	public boolean createSwitchTime(SwitchTime time) {	
		// 判断表存在
		boolean success = tables.containsKey(time.getSpace());
		// 设置触发器时间
		if (success) {
			// 保存到内存
			switches.put(time.getSpace(), time);
		}
		
		return success;
	}

	/**
	 * 撤销数据表触发器
	 * @param space 数据表名
	 * @return 成功返回真，否则假
	 */
	public boolean dropSwitchTime(Space space) {	
		// 判断表存在
		boolean success = tables.containsKey(space);
		// 撤销触发器
		if (success) {
			SwitchTime time = switches.remove(space);
			success = (time != null);
		}
		return success;
	}

	/**
	 * 查找数据表优化触发器
	 * @param space 数据表名
	 * @return 返回数据表优化触发器，没有返回空指针
	 */
	public SwitchTime findSwitchTime(Space space) {
		// 判断表存在
		boolean success = tables.containsKey(space);
		if (success) {
			return switches.get(space);
		}
		return null;
	}

	/**
	 * 增加一个数据表
	 * 
	 * @param table 数据表实例
	 * @return 增加成功返回真，否则假
	 */
	public boolean add(Table table) {
		Space space = table.getSpace();
		Table that = tables.get(space);
		boolean success = (that == null);
		if (success) {
			success = (tables.put(space, table) == null);
		}
		return success;
	}

	/**
	 * 根据数据表名，删除数据表
	 * @param space 数据表名
	 * @return 返回删除的数据表实例，或者空指针
	 */
	public Table remove(Space space) {
		Table table = tables.remove(space);
		if (table != null) {
			switches.remove(space);
		}
		return table;
	}

	/**
	 * 根据数据表名查找一个数据表
	 * 
	 * @param space 数据表名
	 * @return Table实例
	 */
	public Table find(Space space) {
		return tables.get(space);
	}

	/**
	 * 返回全部数据表
	 * @return Table列表
	 */
	public List<Table> list() {
		return new ArrayList<Table>(tables.values());
	}

	/**
	 * 统计表的数目
	 * @return 返回表数目
	 */
	public int size() {
		return tables.size();
	}

	/**
	 * 判断两个数据库一致.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Schema) that) == 0;
	}

	/**
	 * 返回数据库散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (fame == null) {
			return 0;
		}
		return fame.hashCode();
	}

	/**
	 * 返回当前实例的浅层副本（参数只赋值，而不是生成新的数据对象，目标是节约内存）
	 * @return Schema实例
	 */
	public Schema duplicate() {
		return new Schema(this);
	}

	/**
	 * 返回当前实例的浅层副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回数据库名称
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (fame == null) {
			return "";
		}
		return fame.toString();
	}

	/**
	 * 比较名称是否一致(忽略大小写)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Schema that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(fame, that.fame);
	}

}