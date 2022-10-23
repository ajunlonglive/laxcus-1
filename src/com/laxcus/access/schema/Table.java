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

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 数据表 <br>
 * 表由注册用户定义和发布，包括分布存储的参数和列属性集合。
 *
 * @author scott.liang 
 * @version 1.3 9/12/2013
 * @since laxcus 1.0
 */
public final class Table implements Serializable, Cloneable, Markable, Classable, Comparable<Table> {

	private static final long serialVersionUID = 8514725241720722032L;

	/** 表的版本号(通过版本号做不同的数据流生成和解析) **/
	private static final int VERSION = 0x100;

	/** 数据表的持有人，这个参数必须有 **/
	private Siger issuer = new Siger();

	/** 表的存储模式(物理内模式)， 默认是行存储(NSM)。定义见Types类 */
	private byte storage;
	
	/** DSM压缩倍数，默认是3 **/
	private int multiple;

	/** 数据表名 **/
	private Space space;

	/** 指定DATA主节点数目(一个表可以在多个DATA主节点上存在，默认是1) **/
	private int primeSites;

	/** 表在DATA节点上的存在模式 (共享/独享磁盘空间，share or exclusive, 默认是 share) */
	private int siteMode;

	/** 数据块备份数目 (默认是 1，多则不限，由用户确定) **/
	private int chunkCopy;

	/** 数据块文件尺寸 (默认是 64M，以32的倍数扩展，由用户决定) **/
	private int chunkSize;

	/** 列属性集合 **/
	private ArrayList<ColumnAttribute> array = new ArrayList<ColumnAttribute>();

	/** 数据表建立时间，由TOP站点设置 **/
	private long createTime;

	/**
	 * 根据传入的数据表，生成它的数据副本
	 * @param that Table实例
	 */
	private Table(Table that) {
		this();
		// 表持有人
		issuer = that.issuer.duplicate();
		// 存储模型和DSM压缩倍数
		storage = that.storage;
		multiple = that.multiple;
		// 表空间
		space = that.space.duplicate();

		// 其它参数要求
		primeSites = that.primeSites;
		siteMode = that.siteMode;
		chunkCopy = that.chunkCopy;
		chunkSize = that.chunkSize;
		// 数据表建立时间
		createTime = that.createTime;

		// 全部列属性
		for (ColumnAttribute e : that.array) {
			array.add(e.duplicate());
		}
	}

	/**
	 * 构造一个默认的数据表
	 */
	public Table() {
		super();
		// 默认行存储模式
		setStorage(StorageModel.NSM);
		// 3倍压缩倍数
		setMultiple(3);
		setPrimeSites(1);
		setChunkCopy(1);
		// 默认共享模式
		setSiteMode(TableMode.SHARE);
		// 默认数据块为64M
		setChunkSize(0x4000000);
		// 默认数据表建立时间
		setCreateTime(SimpleTimestamp.currentTimeMillis());
	}

	/**
	 * 构造数据表，指定表的名称
	 * @param space 表名
	 */
	public Table(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 构造数据表，指定列单元数目
	 * @param capacity 列单元数目
	 */
	public Table(int capacity) {
		this();
		array.ensureCapacity(capacity);
	}

	/**
	 * 构造构造数据表，指定表名和列单元数目
	 * @param space 表名
	 * @param capacity 列单元数目
	 */
	public Table(Space space, int capacity) {
		this(capacity);
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析表的参数数据
	 * @param reader 可类化读取器
	 * @since 1.3
	 */
	public Table(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据表参数
	 * @param reader 标记化读取器
	 */
	public Table(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置表持有人的用户名称。<br>
	 * 用户名称确定表的归属，是必须参数，在终端建表时设置。用户名称一经定义不可以修改。
	 * @param e 用户签名
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);

		issuer = e;
	}

	/**
	 * 返回表持有人的用户名称。
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置存储模型(行存储或者列存储)，见StorageModel定义
	 * @param who 存储模型
	 */
	public void setStorage(byte who) {
		// 如果不合法，弹出异常
		if(!StorageModel.isFamily(who)) {
			throw new IllegalValueException("illegal storage model:%d", who);
		}
		storage = who;
	}

	/**
	 * 返回存储模型定义
	 * @return 存储模型
	 */
	public byte getStorage() {
		return storage;
	}

	/**
	 * 判断是行存储模型
	 * @return 返回真或者假
	 */
	public boolean isNSM() {
		return StorageModel.isNSM(storage);
	}

	/**
	 * 判断是列存储模型
	 * @return 返回真或者假
	 */
	public boolean isDSM() {
		return StorageModel.isDSM(storage); 
	}
	
	/**
	 * 设置DSM压缩倍数。<br>
	 * 声明：只对DSM表有效，并且压缩倍数受到系统和实际数据量的限制。
	 * 
	 * @param who DSM压缩倍数
	 */
	public void setMultiple(int who) {
		if (who > 0) {
			multiple = who;
		}
	}

	/**
	 * 返回DSM压缩倍数
	 * @return DSM压缩倍数
	 */
	public int getMultiple() {
		return multiple;
	}

	/**
	 * 设置数据表名称
	 * @param e 表名
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名称
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置表分配到多少个DATA主站点
	 * 
	 * @param num DATA主站点数目
	 */
	public void setPrimeSites(int num) {
		if (num < 1) {
			throw new IllegalValueException("illegal prime sites: %d", num);
		}
		primeSites = num;
	}

	/**
	 * 返回表在DATA主站点数目
	 * @return DATA主站点数目的整型值
	 */
	public int getPrimeSites() {
		return primeSites;
	}

	/**
	 * 设置数据块的复制数量(默认是3)
	 * @param num 数据块复制量
	 */
	public void setChunkCopy(int num) {
		if (num < 1) {
			throw new IllegalValueException("illegal copy num: %d", num);
		}
		chunkCopy = num;
	}

	/**
	 * 返回数据块的分配数量
	 * @return 数据块复制量的整型值
	 */
	public int getChunkCopy() {
		return chunkCopy;
	}

	/**
	 * 表在DATA节点上的存在模式(共享或者独占)，见TableMode定义
	 * @param who 存在模式
	 */
	public void setSiteMode(int who) {
		if (!TableMode.isFamily(who)) {
			throw new IllegalValueException("illegal table mode: %d", who);
		}
		siteMode = who;
	}

	/**
	 * 返回表在DATA节点上的存在模式
	 * @return 存在模式的整型值
	 */
	public int getSiteMode() {
		return siteMode;
	}

	/**
	 * 判断是共享模式
	 * @return 返回真或者假
	 */
	public boolean isShare() {
		return TableMode.isShare(siteMode);
	}

	/**
	 * 判断是独亨模式
	 * @return 返回真或者假
	 */
	public boolean isExclusive() {
		return TableMode.isExclusive(siteMode);
	}

	/**
	 * 设置数据块尺寸
	 * @param size 数据块尺寸
	 */
	public void setChunkSize(int size) {
		if (size < 1024 * 1024) {
			throw new IllegalArgumentException("invalid chunk size:" + size);
		}
		chunkSize = size;
	}

	/**
	 * 返回数据块尺寸
	 * @return 数据块尺寸的整型值
	 */
	public int getChunkSize() {
		return chunkSize;
	}
	
	/**
	 * 设置数据表建立时间
	 * @param i 数据表建立时间
	 */
	public void setCreateTime(long i){
		createTime = i;
	}
	
	/**
	 * 返回数据表建立时间
	 * @return 数据表建立时间
	 */
	public long getCreateTime(){
		return createTime;
	}

	/**
	 * 保存列属性，按照列编号顺序存储
	 * @param e 列属性
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ColumnAttribute e) {
		short columnId = e.getColumnId();
		if (columnId > array.size()) {
			array.add(e);
		} else {
			array.add(columnId - 1, e);
		}
		return true;
	}

	/**
	 * 保存全部列属性
	 * @param a 列属性集合
	 * @return 返回新增列属性数目
	 */
	public int addAll(Collection<ColumnAttribute> a) {
		int size = array.size();
		for (ColumnAttribute e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 根据属性名，删除一个列属性
	 * @param name 名称
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(String name) {
		for (int index = 0; index < array.size(); index++) {
			ColumnAttribute e = array.get(index);
			// 比较名称一致
			if (Laxkit.compareTo(name, e.getNameText(), false) == 0) {
				return array.remove(index) != null;
			}
		}
		return false;
	}

	/**
	 * 根据列编号，删除一个列属性
	 * 
	 * @param columnId 列编号
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(short columnId) {
		if (columnId <= array.size()) {
			ColumnAttribute attribute = array.get(columnId - 1);
			if (attribute.getColumnId() == columnId) {
				return array.remove(columnId - 1) != null;
			}
		}
		for (int index = 0; index < array.size(); index++) {
			ColumnAttribute attribute = array.get(index);
			if (attribute.getColumnId() == columnId) {
				return array.remove(index) != null;
			}
		}
		return false;
	}

	/**
	 * 返回列属性名称集合
	 * @return String列表
	 */
	public Set<String> nameSet() {
		TreeSet<String> set = new TreeSet<String>();
		for(ColumnAttribute attribute : array) {
			set.add(attribute.getNameText());
		}
		return set;
	}

	/**
	 * 返回列编号列表
	 * @return 短整型列表
	 */
	public List<Short> getColumnIdentityList() {
		ArrayList<Short> a = new ArrayList<Short>();
		for (int i = 0; i < array.size(); i++) {
			a.add(array.get(i).getColumnId());
		}
		return a;
	}

	/**
	 * 按照列的从小到大顺序排序，返回列编号数组
	 * @return short数组
	 */
	public short[] getColumnIdentities() {
		short[] a = new short[array.size()];
		for (int i = 0; i < array.size(); i++) {
			a[i] = array.get(i).getColumnId();
		}
		return a;
	}

	/**
	 * 按照列的排序顺序，返回全部列的数据类型
	 * @return byte数组
	 */
	public byte[] getColumnTypes() {
		byte[] a = new byte[array.size()];
		for (int i = 0; i < array.size(); i++) {
			a[i] = array.get(i).getType();
		}
		return a;
	}

	/**
	 * 返回全部列属性
	 * @return 列属性集合
	 */
	public Collection<ColumnAttribute> list() {
		return new ArrayList<ColumnAttribute>(array);
	}

	/**
	 * 查找表的主键属性(是prime key，非primary key)
	 * 
	 * @return 返回相关列属性，没有返回空值
	 */
	public ColumnAttribute pid() {
		for (ColumnAttribute attribute : array) {
			if (attribute.isPrimeKey()) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * 根据列名称查找关联列属性
	 * @param title 列名称
	 * @return 返回列属性实例，没有找到返回空指针
	 */
	public ColumnAttribute find(String title) {
		Naming naming = new Naming(title);
		for (ColumnAttribute that : array) {
			if (that.getName().compareTo(naming) == 0) {
				return that;
			}
		}
		return null;
	}

	/**
	 * 根据列编号查找对应的列属性
	 * 
	 * @param columnId 列编号。有效的列编号从1开始，小于1是无效编号。
	 * @return 列属性实例
	 */
	public ColumnAttribute find(short columnId) {
		if (columnId > 0 && columnId <= array.size()) {
			ColumnAttribute attribute = array.get(columnId - 1);
			if (attribute.getColumnId() == columnId) {
				return attribute;
			}
		}
		for (int index = 0; index < array.size(); index++) {
			ColumnAttribute attribute = array.get(index);
			if (attribute.getColumnId() == columnId) {
				return attribute;
			}
		}
		return null;
	}

	/**
	 * 返回指定下标的列属性
	 * 
	 * @param index 索引下标
	 * @return 返回指定下标的列属性，不匹配返回空指针
	 */
	public ColumnAttribute get(int index) {
		if (index < 0 || index >= array.size()) {
			return null;
		}
		return array.get(index);
	}

	/**
	 * 根据列标识号数组，生成一个列属性顺序表：Sheet
	 * 
	 * @param columnIds 列属性数组
	 * @return 返回列属性顺序表
	 */
	public Sheet getSheet(short[] columnIds) {
		int index = 0;
		Sheet sheet = new Sheet();
		for(short columnId : columnIds) {
			ColumnAttribute attribute = find(columnId);
			if(attribute == null) {
				throw new ColumnAttributeException("not found %d", columnId);
			}
			sheet.add(index++, (ColumnAttribute) attribute.clone());
		}
		return sheet;
	}

	/**
	 * 根据列编号顺序，生成一个列属性顺序表
	 * @return 返回Sheet实例
	 */
	public Sheet getSheet() {
		short[] columnIds = new short[array.size()];
		for (int index = 0; index < columnIds.length; index++) {
			columnIds[index] = array.get(index).getColumnId();
		}
		Arrays.sort(columnIds);
		return getSheet(columnIds);
	}

	/**
	 * 将列属性数组空间调整为实际大小(删除多余空间)
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 清除全部列属性
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 返回列属性成员数目
	 * @return 列属性的整型值
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Table.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Table) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode();
	}

	/**
	 * 生成当前数据表的数据副本
	 * @return Table实例
	 */
	public Table duplicate() {
		return new Table(this);
	}

	/**
	 * 根据当前实例，克隆它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个数据表名一致
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Table that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(space, that.space);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return space.toString();
	}

	/**
	 * 将表的参数配置写入可类化存储器，兼容C接口。
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		// 表数据写入器
		ClassWriter buff = new ClassWriter();
		// 版本号
		buff.writeInt(Table.VERSION);
		// 表持有人
		byte[] hash = issuer.binary();
		buff.writeInt(hash.length);
		buff.write(hash, 0, hash.length);
		// 物理存储模型和DSM压缩倍数
		buff.write(storage);
		buff.writeInt(multiple);
		// 数据表名
		space.build(buff);
		// 前缀参数
		byte[] flags = buff.effuse();

		// 重置，写入全部列属性
		buff.reset();
		for (ColumnAttribute attribute : array) {
			attribute.build(buff);
		}
		byte[] mateBytes = buff.effuse();
		buff.reset();

		// 列属性成员数
		buff.writeShort((short) array.size());
		// 全部列属性字节数
		buff.writeInt(mateBytes.length);
		// 列数据
		buff.write(mateBytes, 0, mateBytes.length);

		// "数据块复制数量"标记
		buff.writeInt(chunkCopy);		
		// 数据存在模式(共享/独享)
		buff.writeInt(siteMode);
		// 数据块尺寸
		buff.writeInt(chunkSize);
		// 被分配到多少个主节点上
		buff.writeInt(primeSites);
		// 建立时间
		buff.writeLong(createTime);

		// flush all
		mateBytes = buff.effuse();
		// 重置
		buff.reset();
		// 表数据的最大尺寸
		int maxsize = 4 + flags.length + mateBytes.length;
		// 分三部分输入
		buff.writeInt(maxsize);
		buff.write(flags, 0, flags.length);
		buff.write(mateBytes, 0, mateBytes.length);

		// 表数据写入缓存
		final int scale = writer.size();
		writer.write(buff.effuse());
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析表的数据，兼容C接口。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		// 表数据的有效尺寸
		int maxsize = reader.readInt();
		if (seek + maxsize > reader.getEnd()) {
			throw new IndexOutOfBoundsException();
		}
		// 版本号
		int version = reader.readInt();
		if (version != Table.VERSION) {
			throw new IllegalValueException("illegal version:%d", version);
		}

		// 持有人签名
		int hashSize = reader.readInt();
		if (hashSize > 0) {
			byte[] hash = reader.read(hashSize);
			issuer = new Siger(hash);
		}

		// 物理存储模型 (DSM or NSM)
		storage = reader.read();
		// DSM压缩倍数
		multiple = reader.readInt();

		// 数据表名
		space = new Space(reader);

		// 列属性成员数和字节长度
		short elements = reader.readShort(); 
		int mateSize = reader.readInt(); 
		
		// 解析列属性成员
		final int attributeSeek = reader.getSeek();
		for (short i = 0; i < elements; i++) {
			byte family = reader.current(); //列的数据类型
			ColumnAttribute attribute = ColumnAttributeCreator.create(family);
			attribute.resolve(reader); //解析列属性
			add(attribute);
		}
		// 检查尺寸
		if (reader.getSeek() - attributeSeek != mateSize) {
			throw new IndexOutOfBoundsException("column attribute size error!");
		}

		// 数据块的复制数
		chunkCopy = reader.readInt();
		// 表在节点的存在模式
		siteMode = reader.readInt();
		// 数据块尺寸
		chunkSize = reader.readInt();
		// 被分配的主节点数量
		primeSites = reader.readInt();
		// 建立时间
		createTime = reader.readLong();

		// 检查尺寸
		if (reader.getSeek() - seek != maxsize) {
			throw new IndexOutOfBoundsException("table resolve error!");
		}

		// 减去数组剩余空间
		trim();

		return reader.getSeek() - seek;
	}

	/**
	 * 生成表的数据流并且返回字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter(10240);
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从输入的字节数组中解析表的参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}