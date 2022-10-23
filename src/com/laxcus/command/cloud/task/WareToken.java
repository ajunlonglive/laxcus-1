/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import com.laxcus.access.util.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 软件令牌
 * 
 * @author scott.liang
 * @version 1.0 8/8/2020
 * @since laxcus 1.0
 */
public class WareToken extends CloudToken implements  Comparable<WareToken> {

	/** 软件命名 **/
	private String naming;
	
	/** 版本号  **/
	private String version;
	
	/** 软件产品名称 **/
	private String productName;
	
	/** 产品发布日期 **/
	private String productDate;
	
	/** 生产/开发者 **/
	private String maker;

	/** 介绍 **/
	private String comment;
	
//	/** 自用 **/
//	private boolean selfly;
	
	/**
	 * 判断数据有效
	 * @return 真或者假
	 */
	public boolean isEnabled() {
		boolean success = isEnabled(naming);
		if (success) {
			success = isEnabled(version);
		}
		if (success) {
			success = isEnabled(productName);
		}
		if (success) {
			success = isEnabled(productDate);
			if (success) {
				// 解析日期
				try {
					CalendarGenerator.splitDate(productDate);
				} catch (Throwable e) {
					success = false;
				}
			}
		}
		if (success) {
			success = isEnabled(maker);
		}
		if (success) {
			success = isEnabled(comment);
		}
		return success;
	}

	/**
	 * 判断一个参数有效
	 * @param e 参数
	 * @return 真或者假
	 */
	private boolean isEnabled(String e) {
		return e != null && e.trim().length() > 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected  void buildSuffix(ClassWriter writer){
		writer.writeString(naming);
		writer.writeString(version);
		writer.writeString(productName);
		writer.writeString(productDate);
		writer.writeString(maker);
		writer.writeString(comment);
//		writer.writeBoolean(selfly);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected  void resolveSuffix(ClassReader reader) {
		naming = reader.readString();
		version = reader.readString();
		productName = reader.readString();
		productDate = reader.readString();
		maker = reader.readString();
		comment = reader.readString();
//		selfly = reader.readBoolean();
	}

	/**
	 * 构造默认的软件令牌
	 */
	public WareToken() {
		super();
//		selfly = false; // 非自用
	}
	
	/**
	 * 构造默认的软件令牌
	 * @param naming 命名
	 */
	public WareToken(String naming) {
		this();
		setNaming(naming);
	}
	
	/**
	 * 构造默认的软件令牌
	 */
	public WareToken(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成软件令牌
	 * @param that 传入类
	 */
	private WareToken(WareToken that) {
		this();
		naming = that.naming;
		version = that.version;
		productName = that.productName;
		productDate = that.productDate;
		maker = that.maker;
		comment =that.comment;
//		selfly = that.selfly;
	}
	
	/**
	 * 设置命名
	 * @param e
	 */
	public void setNaming(String e) {
		Laxkit.nullabled(e);
		naming = e.trim();
	}
	
	/**
	 * 返回命名
	 * @return
	 */
	public String getNaming() {
		return naming;
	}
	
	/**
	 * 设置引导类
	 * @param e
	 */
	public void setVersion(String e) {
		Laxkit.nullabled(e);
		version = e;
	}
	
	/**
	 * 返回引导类
	 * @return
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * 设置图标路径
	 * @param e
	 */
	public void setProductName(String e) {
		Laxkit.nullabled(e);
		productName = e;
	}
	
	/**
	 * 返回图标路径
	 * @return
	 */
	public String getProductName() {
		return productName;
	}
	
	/**
	 * 设置标题
	 * @param e
	 */
	public void setProductDate(String e) {
		Laxkit.nullabled(e);
		productDate = e;
	}
	
	/**
	 * 返回标题
	 * @return
	 */
	public String getProductDate() {
		return productDate;
	}
	
	/**
	 * 设置生产/开发者
	 * @param e
	 */
	public void setMaker(String e) {
		maker = e;
	}
	
	/**
	 * 返回生产/开发者
	 * @return
	 */
	public String getMaker(){
		return maker;
	}
	
	/**
	 * 设置介绍
	 * @param e
	 */
	public void setComment(String e) {
		comment = e;
	}

	/**
	 * 返回介绍
	 * @return
	 */
	public String getComment() {
		return comment;
	}

//	/**
//	 * 设置自用
//	 * @param b
//	 */
//	public void setSelfly(boolean b) {
//		selfly = b;
//	}
//
//	/**
//	 * 判断是自用
//	 * @return
//	 */
//	public boolean isSelfly() {
//		return selfly;
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#buildXML()
	 */
	@Override
	public String buildXML() {
		StringBuilder ware = new StringBuilder();
		ware.append(formatXML_CDATA(WareMark.WARE_NAMING, getNaming()));
		ware.append(formatXML_CDATA(WareMark.WARE_VERSION, getVersion()));
		ware.append(formatXML_CDATA(WareMark.WARE_PRODUCT_NAME, getProductName()));
		ware.append(formatXML_CDATA(WareMark.WARE_PRODUCT_DATE, getProductDate()));
		ware.append(formatXML_CDATA(WareMark.WARE_MAKER, getMaker()));
		ware.append(formatXML_CDATA(WareMark.WARE_COMMENT, getComment()));
//		ware.append(formatXML_CDATA(WareMark.WARE_SELFLY, (isSelfly() ? "Yes" : "No")));
		return formatXML(WareMark.WARE, ware.toString());
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.command.cloud.task.CloudToken#buildXML()
//	 */
//	@Override
//	public String buildXML() {
//		StringBuilder ware = new StringBuilder();
//		ware.append(formatXML_CDATA("naming", getNaming()));
//		ware.append(formatXML_CDATA("version", getVersion()));
//		ware.append(formatXML_CDATA("product-name", getProductName()));
//		ware.append(formatXML_CDATA("product-date", getProductDate()));
//		ware.append(formatXML_CDATA("maker", getMaker()));
//		ware.append(formatXML_CDATA("comment", getComment()));
//		ware.append(formatXML_CDATA("selfly", (isSelfly() ? "Yes" : "No")));
//		return formatXML("ware", ware.toString());
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.task.CloudToken#duplicate()
	 */
	@Override
	public WareToken duplicate() {
		return new WareToken(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return naming;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		}
		return compareTo((WareToken) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return naming.toLowerCase().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WareToken that) {
		if( that == null ) {
			return 1;
		}
		
		return naming.compareToIgnoreCase(that.naming);
	}


}