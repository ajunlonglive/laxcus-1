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
 * 生成CONDUCT分布计算应用软件包。<br>
 * 对应命令：“CREATE CONDUCT PACKAGE”。<br>
 * 只在FRONT本地执行。
 * 
 * 包括：
 * 图标文件：可表达应用软件包涵义的图标，必须是图片文件且必须存在，文件路径格式适配所属操作系统。
 * 许可证文件：软件包声明许可，要求第三方接受后才能使用。
 * 引导文件：带“.dtc”后缀的组件包磁盘文件，文件路径格式适配所属操作系统。是必选项。
 * 其它文件：带“.jar”后缀的附件包，或者是“.so”、“.dll”后缀的动态链接库文件。文件路径格式适配所属操作系统，动态链接库适配云端操作系统。
 * 
 * @author scott.liang
 * @version 1.0 2/13/2020
 * @since laxcus 1.0
 */
public class BuildConductPackage extends BuildCloudPackage {
	
	private static final long serialVersionUID = -8844960092677936394L;
	
	/** 文件后缀, conduct package kit **/
	public final static String SUFFIX = ".cpk";

	/**  正则表达式 **/
	public final static String SUFFIX_REGEX = "^\\s*([\\w\\W]+)(?i)(\\.CPK)\\s*$";

	/** INIT阶段应用包 **/
	private CloudPackageElement initElement;
	
	/** BALANCE阶段应用包 **/
	private CloudPackageElement balanceElement;
	
	/** FROM阶段应用包 **/
	private CloudPackageElement fromElement;
	
	/** TO阶段应用包 **/
	private CloudPackageElement toElement;
	
	/** PUT阶段应用包 **/
	private CloudPackageElement putElement;
	
	/**
	 * 构造默认的CONDUCT分布计算应用软件包
	 */
	public BuildConductPackage() {
		super();
	}

	/**
	 * 从可类化读取器中解析CONDUCT分布计算应用软件包
	 * @param reader 可类化读取器
	 */
	public BuildConductPackage(ClassReader reader) {
		super();
		resolve(reader);
	}
	
	/**
	 * 生成CONDUCT分布计算应用软件包副本
	 * @param that CONDUCT分布计算应用软件包实例
	 */
	private BuildConductPackage(BuildConductPackage that) {
		super(that);
		initElement = that.initElement;
		balanceElement = that.balanceElement;
		fromElement = that.fromElement;
		toElement = that.toElement;
		putElement = that.putElement;
	}
	
	/**
	 * 判断符合“完全”状态，全部参数齐备
	 * @return 返回真或者假
	 */
	@Override
	public boolean isFull() {
		boolean full = super.isFull();
		if (full) {
			full = (initElement != null && balanceElement != null
					&& fromElement != null && toElement != null
					&& putElement != null);
		}
		return full;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.cloud.BuildCloudPackage#elements()
	 */
	@Override
	public CloudPackageElement[] elements() {
		return new CloudPackageElement[] { initElement, balanceElement,
				fromElement, toElement, putElement };
	}
	
	/**
	 * 设置INIT云应用软件包成员
	 * @param e InitElement实例
	 */
	public void setInitElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		initElement = e;
	}

	/**
	 * 返回INIT云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getInitElement() {
		return initElement;
	}

	/**
	 * 设置BALANCE云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setBalanceElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		balanceElement = e;
	}

	/**
	 * 返回BALANCE云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getBalanceElement() {
		return balanceElement;
	}
	
	/**
	 * 设置FROM云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setFromElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		fromElement = e;
	}

	/**
	 * 返回FROM云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getFromElement() {
		return fromElement;
	}

	/**
	 * 设置TO云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setToElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		toElement = e;
	}

	/**
	 * 返回开始的TO云应用软件包成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getToElement() {
		return toElement;
	}

	/**
	 * 设置PUT云应用软件包成员
	 * @param e CloudPackageElement实例
	 */
	public void setPutElement(CloudPackageElement e) {
		Laxkit.nullabled(e);
		putElement = e;
	}

	/**
	 * 返回PUT云应用软件包成员
	 * @return PutElement实例
	 */
	public CloudPackageElement getPutElement() {
		return putElement;
	}

	/**
	 * 判断INIT有效
	 * @return 返回真或者假
	 */
	public boolean hasInitElement() {
		return initElement != null;
	}

	/**
	 * 判断BALANCE有效
	 * @return 返回真或者假
	 */
	public boolean hasBalanceElement() {
		return balanceElement != null;
	}
	
	/**
	 * 判断FROM有效
	 * @return 返回真或者假
	 */
	public boolean hasFromElement() {
		return fromElement != null;
	}
	/**
	 * 判断TO有效
	 * @return 返回真或者假
	 */
	public boolean hasToElement() {
		return toElement != null;
	}
	/**
	 * 判断PUT有效
	 * @return 返回真或者假
	 */
	public boolean hasPutElement() {
		return putElement != null;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BuildConductPackage duplicate() {
		return new BuildConductPackage(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);

		writer.writeObject(initElement);
		writer.writeObject(balanceElement);
		writer.writeObject(fromElement);
		writer.writeObject(toElement);
		writer.writeObject(putElement);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);

		initElement = new CloudPackageElement(reader);
		balanceElement = new CloudPackageElement(reader);
		fromElement = new CloudPackageElement(reader);
		toElement = new CloudPackageElement(reader);
		putElement = new CloudPackageElement(reader);
	}

}