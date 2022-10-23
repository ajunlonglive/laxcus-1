/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 虚拟路径
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public final class VPath implements Serializable, Cloneable, Classable, Comparable<VPath> {

	private static final long serialVersionUID = 4685082970369796854L;

	/** 类型 **/
	public static final byte DISK = 1;

	/** 目录 **/
	public static final byte DIRECTORY = 2;

	/** 文件 **/
	public static final byte FILE = 3;

	/** 路径层次，从0开始 **/
	private int level;

	/** 类型 **/
	private byte type;

	/** 局部路径 **/
	protected String path;

	/** 最后修改时间 **/
	protected long lastModified;

	/** 文件长度 **/
	protected long length;

	/** 子文件 **/
	private ArrayList<VPath> array = new ArrayList<VPath>();

	/**
	 * 构造虚拟路径
	 */
	public VPath() {
		super();
		level = 0;
		type = 0;
		lastModified = 0;
		length = 0;
	}

	/**
	 * 从可类化读取器中解析虚拟路径
	 * @param reader 可类化读取器
	 */
	public VPath(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造虚拟路径，指定类型
	 * @param type 类型
	 */
	public VPath(byte type) {
		this();
		setType(type);
	}

	/**
	 * 构造虚拟路径，指定类型和路径
	 * @param type 类型
	 * @param path 路径
	 */
	public VPath(byte type, String path) {
		this(type);
		setPath(path);
	}

	/**
	 * 构造虚拟路径，指定类型和路径
	 * @param level 层次
	 * @param type 类型
	 * @param path 路径
	 */
	public VPath(int level, byte type, String path) {
		this();
		setLevel(level);
		setType(type);
		setPath(path);
	}

	/**
	 * 生成虚拟路径副本
	 * @param that
	 */
	private VPath(VPath that) {
		this();
		copy(that);
	}

	/**
	 * 复制全部参数
	 * @param that
	 */
	public void copy(VPath that) {
		level = that.level;
		type = that.type;
		path = that.path;
		lastModified = that.lastModified;
		length = that.length;
		// 复制
		array.clear();
		array.addAll(that.array);
	}

	
	/**
	 * 设置类型
	 * @param who
	 */
	public void setType(byte who) {
		type = who;
	}

	/**
	 * 返回类型
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 判断是磁盘
	 * @return
	 */
	public boolean isDisk() {
		return type == VPath.DISK;
	}

	/**
	 * 判断是目录
	 * @return
	 */
	public boolean isDirectory() {
		return type == VPath.DIRECTORY;
	}

	/**
	 * 判断是文件
	 * @return
	 */
	public boolean isFile() {
		return type == VPath.FILE;
	}

	/**
	 * 设置路径的层次
	 * @param who
	 */
	public void setLevel(int who) {
		level = who;
	}

	/**
	 * 返回路径的层次
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * 设置路径
	 * @param s
	 */
	public void setPath(String s) {
		path = s;
	}

	/**
	 * 返回路径
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 输出名称
	 * @return 字符串
	 */
	public String getName() {
		if (path == null) {
			return null;
		}
		int lastIndex = path.lastIndexOf('/');
		if (lastIndex >= 0) {
			return path.substring(lastIndex + 1);
		}
		return null;
	}
	
	/**
	 * 返回父级路径
	 * @return 字符串
	 */
	public String getParent() {
		if (path == null || path.equals("/")) {
			return null;
		}
		int lastIndex = path.lastIndexOf('/');
		if (lastIndex == 0) {
			return path.substring(0, 1);
		} else if (lastIndex > 0) {
			return path.substring(0, lastIndex);
		}
		return null;
	}

	/**
	 * 设置文件长度
	 * @param l
	 */
	public void setLength(long l){
		length = l;
	}

	/**
	 * 返回文件长度
	 * @return
	 */
	public long getLength(){
		return length;
	}

	/**
	 * 设置最后修改时间
	 * @param date
	 */
	public void setLastModified(long date) {
		lastModified = date;
	}

	/**
	 * 返回最后修改时间
	 * @return
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * 判断传入的虚路径是当前的子路径
	 * @param child 子路径
	 * @return 返回真或者假
	 */
	public boolean isChild(VPath child) {
		return child.path.startsWith(path);
	}
	
	/**
	 * 判断有匹配的子级虚拟路径
	 * @param child 子路径
	 * @return 返回真或者假
	 */
	public boolean hasChild(VPath child) {
		for (VPath other : array) {
			// 如果匹配，更新它
			if (Laxkit.compareTo(other, child) == 0) {
				return true;
			}
			// 如果是目录，继续判断
			if (other.isDirectory()) {
				boolean success = other.hasChild(child);
				if (success) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 更新一个子级虚拟路径
	 * @param child
	 * @return 成功返回真，否则假
	 */
	public boolean replaceChild(VPath child) {
		for (VPath other : array) {
			// 如果匹配，更新它
			if (Laxkit.compareTo(other, child) == 0) {
				array.remove(other);
				array.add(child);
				return true;
			}
			// 如果是目录，继续判断
			if (other.isDirectory()) {
				boolean success = other.replaceChild(child);
				if (success) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断存在
	 * @param e
	 * @return
	 */
	public boolean contains(VPath e) {
		return array.contains(e);
	}

	/**
	 * 保存虚拟路径
	 * @param e
	 */
	public boolean add(VPath e) {
		if (e == null) {
			return false;
		}
		if (!array.contains(e)) {
			return array.add(e);
		}
		return false;
	}
	
	/**
	 * 删除虚拟路径
	 * @param e
	 * @return
	 */
	public boolean remove(VPath e) {
		if (e == null) {
			return false;
		}
		return array.remove(e);
	}

	/**
	 * 更换虚拟路径（先删除再增加）
	 * @param e 虚拟路径
	 * @return 返回真或者假
	 */
	public boolean replace(VPath e) {
		if (array.contains(e)) {
			remove(e);
			return add(e);
		}
		return false;
	}

	/**
	 * 显示虚拟路径
	 * @return VPath列表
	 */
	public List<VPath> list(){
		return new ArrayList<VPath>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成副本
	 * @return
	 */
	public VPath duplicate() {
		return new VPath(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return path; 
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((VPath) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return level ^ type ^ path.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VPath that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(level, that.level);
		if (ret == 0) {
			ret = Laxkit.compareTo(path, that.path);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(level);
		writer.write(type);
		writer.writeString(path);
		writer.writeLong(lastModified);
		writer.writeLong(length);
		// 子单元
		writer.writeInt(array.size());
		for (VPath child : array) {
			writer.writeObject(child);
		}
		// 尺寸
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		level = reader.readInt();
		type = reader.read();
		path = reader.readString();
		lastModified = reader.readLong();
		length = reader.readLong();
		// 成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			VPath child = new VPath(reader);
			array.add(child);
		}
		return reader.getSeek() - seek;
	}

//	public static void main(String[] args) {
//		VPath path = new VPath();
//		path.path = "/sites/common";
//		String parent = path.getParent();
//		System.out.printf("[%s] - [%s]\n", parent, path.getPath() );
//	}
}