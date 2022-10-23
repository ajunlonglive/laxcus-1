/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 云应用软件包单元<br><br>
 * 
 * 记录ZIP文件听一段应用包字节数据内容和名称
 * 
 * 
 * @author scott.liang
 * @version 1.0 4/1/2020
 * @since laxcus 1.0
 */
public class CloudPackageItem implements Classable, Cloneable, Serializable , Comparable<CloudPackageItem>{

	private static final long serialVersionUID = 1210400602903831946L;
	
	/** 软件名称 **/
	private Naming ware;

	/** ZIP包中的数据内容路径 **/
	private String name;
	
	/** 生成时间 **/
	private long time;

	/** 数据内容 **/
	private byte[] content;

	/**
	 * 构造默认的云应用软件包单元
	 */
	private CloudPackageItem() {
		super();
		time = -1;
	}

	/**
	 * 构造默认的云应用软件包单元，指定文件名名称和内容
	 * @param path 文件名名称
	 * @param time 生成时间
	 * @param b 数据内容
	 * @param off 下标
	 * @param len 长度
	 */
	public CloudPackageItem(String path, long time, byte[] b,int off, int len) {
		this();
		setName(path);
		setTime(time);
		setContent(b, off, len);
	}
	
	/**
	 * 构造默认的云应用软件包单元，指定文件名名称和内容
	 * @param path 文件名名称
	 * @param time 生成时间
	 * @param b 数据内容
	 */
	public CloudPackageItem(String path, long time, byte[] b) {
		this();
		setName(path);
		setTime(time);
		setContent(b);
	}
	
	/**
	 * 生成云应用软件包单元副本
	 * @param that 云应用软件包单元
	 */
	private CloudPackageItem(CloudPackageItem that) {
		this();
		ware = that.ware;
		name = that.name;
		content = that.content;
	}

	/**
	 * 从可类化读取器中解析云应用软件包单元
	 * @param reader 可类化数据读取器
	 */
	public CloudPackageItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置软件名称，允许空指针
	 * @param e 软件名称
	 */
	public void setWare(Naming e) {
		ware = e;
	}
	
	/**
	 * 返回软件名称
	 * @return 命名实例
	 */
	public Naming getWare() {
		return ware;
	}

	/**
	 * 设置文件名名称，不允许空指针
	 * @param e 字符串
	 */
	public void setName(String e) {
		Laxkit.nullabled(e);
		name = e;
	}

	/**
	 * 返回文件名名称
	 * @return 字符串
	 */
	public String getName() {
		return name;
	}

	/**
	 * 返回去掉路径的短名称
	 * @return
	 */
	public String getSimpleName() {
		final String regex = "^\\s*([\\w\\W]+)\\/([\\w\\W]+)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(name);
		if (matcher.matches()) {
			return matcher.group(2);
		}
		return name;
	}

	/**
	 * 设置生成时间，从ZipEntry得来 
	 * @param i
	 */
	public void setTime(long i) {
		time = i;
	}

	/**
	 * 返回生成时间
	 * @return
	 */
	public long getTime() {
		return time;
	}

//	/**
//	 * 判断是自有包文件
//	 * @return 返回真或者假
//	 */
//	public boolean isSelfly() {
//		String str = getSimpleName();
//		return str.equalsIgnoreCase(TF.SELFLY_FILE);
//	}

	/**
	 * 设置数据内容
	 * @param b 数据内容
	 */
	public void setContent(byte[] b) {
		Laxkit.nullabled(b);
		setContent(b, 0, b.length);
	}
	
	/**
	 * 设置数据内容
	 * @param b
	 */
	private void setContent(byte[] b, int off, int len) {
		// 判断是空指针
		if (Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}
		// 复制内容
		content = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 返回数据内容
	 * @return 数据内容
	 */
	public byte[] getContent() {
		return content;
	}
	
	/**
	 * 内容长度
	 * @return 整数
	 */
	public int getContentLength() {
		return content.length;
	}

	/**
	 * 判断有数据内容
	 * @return 返回真或者假
	 */
	public boolean hasContent() {
		return content != null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		// 软件名称
		writer.writeInstance(ware);
		// 文件名和生成时间
		writer.writeString(name);
		writer.writeLong(time);
		// 数据内容
		writer.writeByteArray(content);

		// 统计字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		// 文件名和生成时间
		ware = reader.readInstance(Naming.class);
		name = reader.readString();
		time = reader.readLong();
		// 数据内容
		content = reader.readByteArray();

		// 统计读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return CloudPacketElement实例
	 */
	public CloudPackageItem duplicate() {
		return new CloudPackageItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CloudPackageItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CloudPackageItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CloudPackageItem that) {
		if (that == null) {
			return 1;
		}
		
		int ret = Laxkit.compareTo(ware, that.ware);
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(time, that.time);
		}
		return ret;
	}

}