/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 生成ESTABLISH分布计算应用软件包。<br>
 * 只在FRONT本地执行。
 * 
 * 命令格式：
 * 
 * BUILD ESTABLISH PACKAGE 本地磁盘文件名 README LOGO 图标文件,LICENCE 许可证文件,其它文件 ISSUE 引导文件,其它文件 ASSIGN 引导文件,其它文件 SCAN 引导文件,其它文件 SIFT 引导文件,其它文件 RISE 引导文件,其它文件 END 引导文件,其它文件
 * 
 * 
本地磁盘文件 : 保证文件在磁盘不存在，文件路径格式适配所属操作系统。
图标文件：可表达应用软件包涵义的图标，必须是图片文件且必须存在，文件路径格式适配所属操作系统。
许可证文件：软件包声明许可，要求第三方接受后才能使用。
引导文件：带“.dtc”后缀的组件包磁盘文件，文件路径格式适配所属操作系统。是必选项。
其它文件：带“.jar”后缀的附件包，或者是“.so”、“.dll”后缀的动态链接库文件。文件路径格式适配所属操作系统，动态链接库适配云端操作系统。
 * 
 * @author scott.liang
 * @version 1.0 2/13/2020
 * @since laxcus 1.0
 */
public class BuildEstablishPackage extends BuildCloudPackage {
	
	private static final long serialVersionUID = 5948758683001591369L;

	/** 文件后缀, establish package kit **/
	public final static String SUFFIX = ".epk";

	/**  正则表达式 **/
	public final static String SUFFIX_REGEX = "^\\s*([\\w\\W]+)(?i)(\\.EPK)\\s*$";

	/** ISSUE阶段应用包 **/
	private CloudPackageElement issueElement;
	
	/** ASSIGN阶段应用包 **/
	private CloudPackageElement assignElement;
	
	/** SCAN阶段应用包 **/
	private CloudPackageElement scanElement;
	
	/** SIFT阶段应用包 **/
	private CloudPackageElement siftElement;
	
	/** RISE阶段应用包 **/
	private CloudPackageElement riseElement;
	
	/** END阶段应用包 **/
	private CloudPackageElement endElement;
	
	/**
	 * 构造默认的ESTABLISH分布计算应用软件包
	 */
	public BuildEstablishPackage() {
		super();
	}

	/**
	 * 构造默认的ESTABLISH分布计算应用软件包
	 */
	public BuildEstablishPackage(ClassReader reader) {
		super();
		resolve(reader);
	}
	
	/**
	 * 生成ESTABLISH分布计算应用软件包副本
	 * @param that CreateEstablishPackage实例
	 */
	private BuildEstablishPackage(BuildEstablishPackage that) {
		super(that);
		issueElement = that.issueElement;
		assignElement = that.assignElement;
		scanElement = that.scanElement;
		siftElement = that.siftElement;
		riseElement = that.riseElement;
		endElement = that.endElement;
	}
	
	/**
	 * 判断符合“完全”状态，全部参数齐备
	 * @return 返回真或者假
	 */
	@Override
	public boolean isFull() {
		boolean full = super.isFull();
		if (full) {
			full = (issueElement != null && assignElement != null
					&& scanElement != null && siftElement != null
					&& riseElement != null && endElement != null);
		}
		return full;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.BuildCloudPackage#elements()
	 */
	@Override
	public CloudPackageElement[] elements() {
		return new CloudPackageElement[] { issueElement, assignElement,
				scanElement, siftElement, riseElement, endElement };
	}

	/**
	 * 设置ISSUE云应用软件包成员
	 * @param e IssueElement实例
	 */
	public void setIssueElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		issueElement = e;
	}

	/**
	 * 返回ISSUE云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getIssueElement() {
		return issueElement;
	}

	/**
	 * 设置ASSIGN云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setAssignElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		assignElement = e;
	}

	/**
	 * 返回ASSIGN云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getAssignElement() {
		return assignElement;
	}
	
	/**
	 * 设置SCAN云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setScanElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		scanElement = e;
	}

	/**
	 * 返回SCAN云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getScanElement() {
		return scanElement;
	}

	/**
	 * 设置SIFT云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setSiftElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		siftElement = e;
	}

	/**
	 * 返回开始的SIFT云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getSiftElement() {
		return siftElement;
	}

	/**
	 * 设置END云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setEndElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		endElement = e;
	}

	/**
	 * 返回END云应用软件包成员
	 * @return EndElement实例
	 */
	public CloudPackageElement getEndElement() {
		return endElement;
	}

	/**
	 * 判断ISSUE有效
	 * @return 返回真或者假
	 */
	public boolean hasIssueElement() {
		return issueElement != null;
	}

	/**
	 * 判断ASSIGN有效
	 * @return 返回真或者假
	 */
	public boolean hasAssignElement() {
		return assignElement != null;
	}
	
	/**
	 * 判断SCAN有效
	 * @return 返回真或者假
	 */
	public boolean hasScanElement() {
		return scanElement != null;
	}
	
	/**
	 * 判断SIFT有效
	 * @return 返回真或者假
	 */
	public boolean hasSiftElement() {
		return siftElement != null;
	}
	
	/**
	 * 判断END有效
	 * @return 返回真或者假
	 */
	public boolean hasEndElement() {
		return endElement != null;
	}
	
	/**
	 * 判断RISE有效
	 * @return 返回真或者假
	 */
	public boolean hasRiseElement() {
		return riseElement != null;
	}
	
	/**
	 * 设置RISE云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setRiseElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		riseElement = e;
	}

	/**
	 * 返回开始的RISE云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getRiseElement() {
		return riseElement;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BuildEstablishPackage duplicate() {
		return new BuildEstablishPackage(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);

		writer.writeObject(issueElement);
		writer.writeObject(assignElement);
		writer.writeObject(scanElement);
		writer.writeObject(siftElement);
		writer.writeObject(riseElement);
		writer.writeObject(endElement);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);

		issueElement = new CloudPackageElement(reader);
		assignElement = new CloudPackageElement(reader);
		scanElement = new CloudPackageElement(reader);
		siftElement = new CloudPackageElement(reader);
		riseElement = new CloudPackageElement(reader);
		endElement = new CloudPackageElement(reader);
	}

}