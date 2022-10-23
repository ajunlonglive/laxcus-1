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

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 云应用软件包自读成员<br><br>
 * 
 * 记录一个阶段的云应用，包括：<br>
 * 1. 标签 <br>
 * 2. LOGO图标，只允许一个 <br>
 * 3. 许可证文件，只允许一个，可选项 <br>
 * 4. 辅助文件，任意多个，如:“readme.txt, version.txt, ...” <br>
 * 
 * @author scott.liang
 * @version 1.0 2/15/2020
 * @since laxcus 1.0
 */
public class ReadmePackageElement implements Classable, Cloneable, Serializable , Comparable<ReadmePackageElement>{
	
	private static final long serialVersionUID = -3703121668124208661L;

	/** 标签名称 **/
	private String mark;

	/** LOGO图标文件 **/
	private FileKey logo;
	
	/** 许可证文件 **/
	private FileKey licence;

	/** 辅助文件 **/
	private TreeSet<FileKey> assists = new TreeSet<FileKey>();

	/**
	 * 构造默认的云应用软件包自读成员，指定标签名称
	 * @param tag 标签名称
	 */
	public ReadmePackageElement(String tag) {
		super();
		setMark(tag);
	}

	/**
	 * 构造默认的云应用软件包自读成员
	 */
	public ReadmePackageElement() {
		this("readme");
	}
	
	/**
	 * 生成云应用软件包自读成员副本
	 * @param that 云应用软件包自读成员
	 */
	private ReadmePackageElement(ReadmePackageElement that) {
		this();
		mark = that.mark;
		logo = that.logo;
		assists.addAll(that.assists);
	}

	/**
	 * 从可类化读取器中解析云应用软件包自读成员
	 * @param reader 可类化数据读取器
	 */
	public ReadmePackageElement(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置标签名称，不允许空指针
	 * @param e 字符串
	 */
	public void setMark(String e) {
		Laxkit.nullabled(e);
		mark = e;
	}
	
	/**
	 * 返回标签名称 
	 * @return 字符串
	 */
	public String getMark() {
		return mark;
	}
	
	/**
	 * 设置LOGO图标
	 * @param e
	 */
	public void setLogo(FileKey e) {
		Laxkit.nullabled(e);
		logo = e;
	}

	/**
	 * 返回LOGO图标
	 * @return LOGO图标
	 */
	public FileKey getLogo() {
		return logo;
	}
	
	/**
	 * 判断有LOGO图标
	 * @return 返回真或者假
	 */
	public boolean hasLogo() {
		return logo != null;
	}
	
	/**
	 * 设置许可证文件，允许置空
	 * @param e 许可证文件，在发布时显示！
	 */
	public void setLicence(FileKey e) {
		licence = e;
	}

	/**
	 * 返回许可证文件
	 * @return 许可证文件
	 */
	public FileKey getLicence() {
		return licence;
	}
	
	/**
	 * 判断有许可证文件
	 * @return 返回真或者假
	 */
	public boolean hasLicence() {
		return licence != null;
	}
	
	/**
	 * 保存JAR辅助文件标记
	 * @param key JAR标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean addAssist(FileKey key) {
		Laxkit.nullabled(key);
		return assists.add(key);
	}
	
	/**
	 * 删除JAR辅助文件标记
	 * @param key JAR标记
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeAssist(FileKey key) {
		Laxkit.nullabled(key);
		return assists.remove(key);
	}

	/**
	 * 输出JAR包
	 * @return
	 */
	public List<FileKey> getAssists() {
		return new ArrayList<FileKey>(assists);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		
		// 标签
		writer.writeString(mark);
		// LOGO图标
		writer.writeObject(logo);
		// 许可证
		writer.writeInstance(licence);
		// 附件
		writer.writeInt(assists.size());
		for(FileKey e : assists) {
			writer.writeObject(e);
		}
		
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		
		// 标签
		mark = reader.readString();
		// LOGO图标
		logo = new FileKey(reader);
		// 许可证文件
		licence = reader.readInstance(FileKey.class);
		// 附件
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FileKey e = new FileKey(reader);
			assists.add(e);
		}

		// 统计读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ReadmePacketElement实例
	 */
	public ReadmePackageElement duplicate() {
		return new ReadmePackageElement(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ReadmePackageElement.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ReadmePackageElement) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return logo.hashCode();
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
	public int compareTo(ReadmePackageElement that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(logo, that.logo);
	}

}