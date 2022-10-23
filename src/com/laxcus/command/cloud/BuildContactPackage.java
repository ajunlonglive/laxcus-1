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
 * 生成CONTACT应用软件包。<br>
 * 只在FRONT本地执行。
 * 
 * @author scott.liang
 * @version 1.0 2/16/2020
 * @since laxcus 1.0
 */
public class BuildContactPackage extends BuildCloudPackage {

	private static final long serialVersionUID = -6533910100949106706L;

	/** 文件后缀, swift package kit **/
	public final static String SUFFIX = ".spk";

	/**  正则表达式 **/
	public final static String SUFFIX_REGEX = "^\\s*([\\w\\W]+)(?i)(\\.SPK)\\s*$";

	/** DISTANT阶段应用包 **/
	private CloudPackageElement distantElement;

	/** NEAR阶段应用包 **/
	private CloudPackageElement nearElement;

	/** FORK阶段应用包 **/
	private CloudPackageElement forkElement;

	/** MERGE阶段应用包 **/
	private CloudPackageElement mergeElement;

	//		/** PUT阶段应用包 **/
	//		private CloudPackageElement putElement;

	/**
	 * 构造默认的CONTACT分布计算应用软件包
	 */
	public BuildContactPackage() {
		super();
	}

	/**
	 * 从可类化读取器中解析CONTACT分布计算应用软件包
	 * @param reader 可类化读取器
	 */
	public BuildContactPackage(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 生成CONTACT分布计算应用软件包副本
	 * @param that CONTACT分布计算应用软件包实例
	 */
	private BuildContactPackage(BuildContactPackage that) {
		super(that);
		distantElement = that.distantElement;
		nearElement = that.nearElement;
		this.forkElement = that.forkElement;
		this.mergeElement = that.mergeElement;
	}

	/**
	 * 判断符合“完全”状态，全部参数齐备
	 * @return 返回真或者假
	 */
	@Override
	public boolean isFull() {
		boolean full = super.isFull();
		if (full) {
			full = (distantElement != null && nearElement != null
					&& this.mergeElement != null && this.forkElement != null);
		}
		return full;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.BuildCloudPackage#elements()
	 */
	@Override
	public CloudPackageElement[] elements() {
		return new CloudPackageElement[] { distantElement, nearElement, this.forkElement, this.mergeElement };
	}

	/**
	 * 设置DISTANT云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setDistantElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		distantElement = e;
	}

	/**
	 * 返回DISTANT云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getDistantElement() {
		return distantElement;
	}

	/**
	 * 设置NEAR云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setNearElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		nearElement = e;
	}

	/**
	 * 返回NEAR云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getNearElement() {
		return nearElement;
	}

	/**
	 * 设置FORK云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setForkElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		forkElement = e;
	}

	/**
	 * 返回FORK云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getForkElement() {
		return forkElement;
	}

	/**
	 * 设置MERGE云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setMergeElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		mergeElement = e;
	}

	/**
	 * 返回开始的MERGE云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getMergeElement() {
		return mergeElement;
	}

	//	/**
	//	 * 设置PUT云应用软件包成员
	//	 * @param e CloudPackageElement实例
	//	 */
	//	public void setPutElement(CloudPackageElement e) {
	//		Laxkit.nullabled(e);
	//		putElement = e;
	//	}
	//
	//	/**
	//	 * 返回PUT云应用软件包成员
	//	 * @return PutElement实例
	//	 */
	//	public CloudPackageElement getPutElement() {
	//		return putElement;
	//	}

	/**
	 * 判断DISTANT有效
	 * @return 返回真或者假
	 */
	public boolean hasDistantElement() {
		return distantElement != null;
	}

	/**
	 * 判断NEAR有效
	 * @return 返回真或者假
	 */
	public boolean hasNearElement() {
		return nearElement != null;
	}

	/**
	 * 判断FORK有效
	 * @return 返回真或者假
	 */
	public boolean hasForkElement() {
		return forkElement != null;
	}

	/**
	 * 判断MERGE有效
	 * @return 返回真或者假
	 */
	public boolean hasMergeElement() {
		return this.mergeElement != null;
	}

	//	/**
	//	 * 判断PUT有效
	//	 * @return 返回真或者假
	//	 */
	//	public boolean hasPutElement() {
	//		return putElement != null;
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BuildContactPackage duplicate() {
		return new BuildContactPackage(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);

		writer.writeObject(distantElement);
		writer.writeObject(nearElement);
		writer.writeObject(forkElement);
		writer.writeObject(mergeElement);


	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);

		distantElement = new CloudPackageElement(reader);
		nearElement = new CloudPackageElement(reader);
		forkElement = new CloudPackageElement(reader);
		mergeElement = new CloudPackageElement(reader);
	}

	//	/** CONTACT应用包 **/
	//	private CloudPackageElement swiftElement;
	//	
	//	/**
	//	 * 构造默认的CONTACT分布计算应用软件包
	//	 */
	//	public BuildSwiftPackage() {
	//		super();
	//	}
	//
	//	/**
	//	 * 从可类化读取器中解析CONTACT分布计算应用软件包
	//	 * @param reader 可类化读取器
	//	 */
	//	public BuildSwiftPackage(ClassReader reader) {
	//		super();
	//		resolve(reader);
	//	}
	//	
	//	/**
	//	 * 生成CONTACT分布计算应用软件包副本
	//	 * @param that CONTACT分布计算应用软件包实例
	//	 */
	//	private BuildSwiftPackage(BuildSwiftPackage that) {
	//		super(that);
	//		swiftElement = that.swiftElement;
	//	}
	//
	//	/**
	//	 * 设置CONTACT云应用软件包成员
	//	 * @param e InitElement实例
	//	 */
	//	public void setSwiftElement(CloudPackageElement e) {
	//		Laxkit.nullabled(e);
	//		swiftElement = e;
	//	}
	//
	//	/**
	//	 * 返回CONTACT云应用软件包成员
	//	 * @return CloudPackageElement实例
	//	 */
	//	public CloudPackageElement getSwiftElement() {
	//		return swiftElement;
	//	}
	//
	//	/**
	//	 * 判断CONTACT有效
	//	 * @return 返回真或者假
	//	 */
	//	public boolean hasSwiftElement() {
	//		return swiftElement != null;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.command.Command#duplicate()
	//	 */
	//	@Override
	//	public BuildSwiftPackage duplicate() {
	//		return new BuildSwiftPackage(this);
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	//	 */
	//	@Override
	//	protected void buildSuffix(ClassWriter writer) {
	//		super.buildSuffix(writer);
	//
	//		writer.writeObject(swiftElement);
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	//	 */
	//	@Override
	//	protected void resolveSuffix(ClassReader reader) {
	//		super.resolveSuffix(reader);
	//
	//		swiftElement = new CloudPackageElement(reader);
	//	}

}