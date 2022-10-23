/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.cyber;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 一个账号的云端空间
 * 
 * @author scott.liang
 * @version 1.0 7/27/2022
 * @since laxcus 1.0
 */
public final class CloudField implements Classable, Serializable, Cloneable, Comparable<CloudField> {

	private static final long serialVersionUID = 7358916406887584285L;

	/** 云端空间节点 **/
	private Node site;

	/** 用户签名 **/
	private Siger siger;

	/** 最大尺寸 **/
	private long maxCapacity;

	/** 已经使用的尺寸 **/
	private long usedCapacity;
	
	/** 目录数目 **/
	private int directories;
	
	/** 文件数目 **/
	private int files;

	/**
	 * 构造默认的账号的云端空间
	 */
	public CloudField() {
		super();
		maxCapacity = usedCapacity = 0L;
		directories = files = 0;
	}

	/**
	 * 设置账号的云端空间
	 * @param siger 签名
	 */
	public CloudField(Siger siger) {
		this();
		setSiger(siger);
	}

	
	/**
	 * 生成账号的云端空间副本
	 * @param that 账号的云端空间
	 */
	private CloudField(CloudField that) {
		this();
		site = that.site;
		siger = that.siger;
		maxCapacity = that.maxCapacity;
		usedCapacity = that.usedCapacity;
		directories = that.directories;
		files = that.files;
	}

	/**
	 * 从可类化读取器解析账号的云端空间
	 * @param reader 可类化数据读取器
	 */
	public CloudField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造账号的云端空间，指定参数
	 * @param maxCapacity 最大空间数
	 * @param usedCapacity 已经使用的容量
	 */
	public CloudField(long maxCapacity, long usedCapacity) {
		this();
		setMaxCapacity(maxCapacity);
		setUsedCapacity(usedCapacity);
	}

	/**
	 * 设置节点地址
	 * @param e
	 */
	public void setSite(Node e){
		site = e;
	}

	/**
	 * 返回节点地址
	 * @return
	 */
	public Node getSite(){
		return site;
	}

	/**
	 * 设置站点成员签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回站点成员签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 可以承载的最大磁盘容量。如果是0表示没有限制
	 * @param more 磁盘容量
	 */
	public void setMaxCapacity(long more) {
		maxCapacity = more;
	}

	/**
	 * 返回最大磁盘容量
	 * @return 磁盘容量
	 */
	public long getMaxCapacity() {
		return maxCapacity;
	}

	/**
	 * 设置已经使用的磁盘容量
	 * @param more 磁盘容量
	 */
	public void setUsedCapacity(long more) {
		usedCapacity = more;
	}

	/**
	 * 返回磁盘容量
	 * @return 磁盘容量
	 */
	public long getUsedCapacity() {
		return usedCapacity;
	}

	/**
	 * 判断达到“满员”状态
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return maxCapacity > 0 && usedCapacity >= maxCapacity;
	}

	//	/**
	//	 * 判断达到“空间不足”状态
	//	 * @return 返回真或者假
	//	 */
	//	public boolean isMissing() {
	//		boolean success = (maxPersons > 0 && threshold > 0.0f);
	//		if (success) {
	//			double rate = ((double) realPersons / (double) maxPersons) * 100.0f;
	//			success = (rate >= threshold);
	//		}
	//		return success;
	//	}
	
	public void setDirectires(int i){
		directories = i;
	}
	public int getDirectires(){
		return directories;
	}
	
	public void setFiles(int i){
		files = i;
	}
	
	public int getFiles(){
		return files;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d # %d", maxCapacity, usedCapacity);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		// 类属性必须一致
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CloudField) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (site != null && siger != null) {
			return site.hashCode() ^ siger.hashCode();
		} else if (site != null) {
			return site.hashCode();
		} else if (siger != null) {
			return siger.hashCode();
		}
		return 0;
	}

	/**
	 * 生成副本
	 * @return CloudField副本
	 */
	public CloudField duplicate() {
		return new CloudField(this);
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
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInstance(site);
		writer.writeInstance(siger);
		// 最大磁盘容量
		writer.writeLong(maxCapacity);
		// 已经使用容量
		writer.writeLong(usedCapacity);
		// 目录和文件数目
		writer.writeInt(directories);
		writer.writeInt(files);
		// 返回写入的字节数
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		site = reader.readInstance(Node.class);
		siger = reader.readInstance(Siger.class);
		// 最大磁盘容量
		maxCapacity = reader.readLong();
		// 已经使用容量
		usedCapacity = reader.readLong();
		// 目录和文件数目
		directories = reader.readInt();
		files = reader.readInt();
		// 返回读取的字节数
		return reader.getSeek() - seek;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CloudField that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(site, that.site);
		if (ret == 0) {
			ret = Laxkit.compareTo(siger, that.siger);
		}
		return ret;
	}

}