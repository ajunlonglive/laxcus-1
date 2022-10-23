/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 数据块索引库。<br><br>
 * 
 * 数据块索引库是记录一个DATA站点下，全部数据块索引表的集合。
 * 这些数据块索引表可以分别属于不同的用户。<br>
 * 
 * @author scott.liang 
 * @version 1.1 6/28/2015
 * @since laxcus 1.0
 */
public final class StubSchema implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -1158448280282042011L;
	
	/** 数据表名 -> 数据块索引表 **/
	private Map<Space, StubTable> tables = new TreeMap<Space, StubTable>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入表的数目
		writer.writeInt(tables.size());
		// 写入每一个表的配置
		for(StubTable e : tables.values()) {
			writer.writeObject(e);
		}
		// 返回写入字节长度
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读取表的数目
		int size = reader.readInt();
		// 读取每一个表的配置和保存它
		for (int i = 0; i < size; i++) {
			StubTable e = new StubTable(reader);
			this.tables.put(e.getSpace(), e);
		}
		// 返回解析的字节尺寸
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的数据块索引库参数，生成它的副本
	 * @param that StubSchema实例
	 */
	private StubSchema(StubSchema that) {
		super();
		this.tables.putAll(that.tables);
	}

	/**
	 * 构造一个默认的数据块索引库
	 */
	public StubSchema() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public StubSchema(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 增加数据块索引表
	 * @param table 数据块索引表
	 * @return 新增加的索引表返回“真”，否则“假”。
	 */
	public boolean add(StubTable table) {
		return tables.put(table.getSpace(), table) == null;
	}
	
	/**
	 * 使用传入的数据块索引库，重置当前索引表集合
	 * @param e 数据块索引库
	 */
	public void reset(StubSchema e) {
		tables.clear();
		tables.putAll(e.tables);
	}

	/**
	 * 增加一个空的表索引集合
	 * @param space 表名
	 * @return 返回真或者假
	 */
	public boolean add(Space space) {
		StubTable table = tables.get(space);
		if (table == null) {
			table = new StubTable(space);
			tables.put(space, table);
			return true;
		}
		return false;
	}
	/**
	 * 根据表名和数据块编号，增加一个记录
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 增加成功返回真，否则假
	 */
	public boolean add(Space space, long stub) {
		StubTable table = tables.get(space);
		if (table == null) {
			table = new StubTable(space);
			tables.put(space, table);
		}
		return table.add(stub);
	}

	/**
	 * 根据表名和数据块标识号，删除一个数据块编号记录
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Space space, long stub) {
		boolean success = false;
		StubTable table = tables.get(space);
		if (table != null) {
			success = table.remove(stub);
			if (table.isEmpty()) {
				tables.remove(space);
			}
		}
		return success;
	}

	/**
	 * 删除一个表的全部数据块编号记录
	 * @param space  表名
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Space space) {
		return tables.remove(space) != null;
	}

	/**
	 * 根据表名和数据块标识号，检索这个数据块编号记录是否存在
	 * @param space 表名
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	public boolean contains(Space space, long stub) {
		StubTable table = tables.get(space);
		if (table != null) {
			return table.contains(stub);
		}
		return false;
	}

	/**
	 * 检查某个表的数据块编号是否存在
	 * @param space 表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space space) {
		return tables.get(space) != null;
	}
	
	/**
	 * 返回数据表名集合
	 * @return 表名集合
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(tables.keySet());
	}

	/**
	 * 根据表名，查找对应的索引表
	 * @param space 表名
	 * @return StubTable实例
	 */
	public StubTable find(Space space) {
		return tables.get(space);
	}

	/**
	 * 返回全部索引表
	 * @return StubTable列表
	 */
	public List<StubTable> list() {
		return new ArrayList<StubTable>(tables.values());
	}

	/**
	 * 统计全部数据块的数目
	 * @return 数据块统计值
	 */
	public int getChunkCount() {
		int count = 0;
		for (StubTable table : tables.values()) {
			count += table.size();
		}
		return count;
	}

	/**
	 * 清空记录
	 */
	public void clear() {
		tables.clear();
	}

	/**
	 * 索引表成员数目
	 * @return StubTable的成员数目
	 */
	public int size() {
		return tables.size();
	}

	/**
	 * 参数成员集合是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return tables.isEmpty();
	}
	
	/**
	 * 生成当前对象的数据副本
	 * @return StubSchema实例
	 */
	public StubSchema duplicate() {
		return new StubSchema(this);
	}

	/**
	 * 输入当前实例的浅层数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

}