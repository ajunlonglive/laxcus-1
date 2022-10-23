/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * JAVA虚拟机信息单元
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class JREInfoItem implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = -6293384810197643693L;
	
	/** JAVA虚拟机提供商 **/
	private String vendor; // java.vm.vendor,  java.vendor
	
	/** JAVA虚拟机版本 **/
	private String version; // java.runtime.version, java.specification.version
	
	/** 虚拟机名称 **/
	private String vmname; // java.vm.name;
	
	/** 适配CPU架构 **/
	private String arch; // os.arch
	
	/** 寄居系统 **/
	private String osname; // os.name
	
	
	
//	/** 适配系统 **/
//	private String osname; // os.name - Windows XP
//	
//	/** 虚拟机名称 **/
//	private String vmname; //	java.vm.name;
	
	
	/**
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		
		writer.writeString(vendor);
		writer.writeString(version);
		writer.writeString(vmname);
		writer.writeString(arch);
		writer.writeString(osname);
		
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		vendor = reader.readString();
		version = reader.readString();
		vmname = reader.readString();
		arch = reader.readString();
		osname = reader.readString();
		
		
//		physicalId = reader.readString();
//		cores = reader.readString();
	}
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public JREInfoItem() {
		super();
//		osname = 0;
//		vmname = 0;
//		physicalId = 0;
//		cores = 0;
	}

	/**
	 * 根据传入实例，生成JAVA虚拟机信息单元的数据副本
	 * @param that JREInfoItem实例
	 */
	private JREInfoItem(JREInfoItem that) {
		super();
		osname = that.osname;
		vendor = that.vendor;
		version = that.version;
		arch = that.arch;
		vmname = that.vmname;
		
//		physicalId = that.physicalId;
//		cores = that.cores;
	}


	/**
	 * 从可类化数据读取器中JAVA虚拟机信息单元
	 * @param reader 可类化数据读取器
	 */
	public JREInfoItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 返回核心编号
	 * @param w
	 */
	public void setOsname(String w) {
		osname = w;
	}

	/**
	 * 设置核心编号
	 * @return
	 */
	public String getOsname() {
		return osname;
	}

	/**
	 * 设置制造商
	 * @param s
	 */
	public void setVendor(String s) {
		vendor = s;
	}

	/**
	 * 返回制造商
	 * @return
	 */
	public String getVendor() {
		return vendor;
	}

	public void setVersion(String s) {
		version = s;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setArch(String s) {
		arch = s;
	}
	
	public String getArch() {
		return arch;
	}

//	/**
//	 * 设置CPU物理数
//	 * @param what 虚拟机名称
//	 */
//	public void setPhysicalId(int what) {
//		physicalId = what;
//	}
//
//	/**
//	 * 返回FIXP失效时间
//	 * @return 虚拟机名称
//	 */
//	public int getPhysicalId() {
//		return physicalId;
//	}

//	/**
//	 * CPU核心数目
//	 * @param what
//	 */
//	public void setCores(int what) {
//		cores = what;
//	}
//
//	/**
//	 * 返回CPU核心数目
//	 * @return
//	 */
//	public int getCores() {
//		return cores;
//	}

	/**
	 * 设置虚拟机名称
	 * @param s 虚拟机名称
	 */
	public void setVmname(String s) {
		vmname = s;
	}

	/**
	 * 返回虚拟机名称
	 * @return 虚拟机名称
	 */
	public String getVmname() {
		return vmname;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return JREInfoItem实例
	 */
	public JREInfoItem duplicate() {
		return new JREInfoItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object that) {
//		if (that == null || getClass() != that.getClass()) {
//			return false;
//		} else if (that == this) {
//			return true;
//		}
//		// 比较
//		return compareTo((JREInfoItem ) that) == 0;
//	}

//	/* (non-Javadoc)
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(JREInfoItem that) {
//		if (that == null) {
//			return 1;
//		}
////		// 比较参数
////		int ret = Laxkit.compareTo(node, that.node);
////		if (ret == 0) {
////			ret = Laxkit.compareTo(successful, that.successful);
////		}
////		return ret;
//		
//		return 0;
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}


}