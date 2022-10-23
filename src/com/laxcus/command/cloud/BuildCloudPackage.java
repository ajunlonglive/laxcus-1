/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 生成云应用软件包
 * 
 * @author scott.liang
 * @version 1.0 2/13/2020
 * @since laxcus 1.0
 */
public abstract class BuildCloudPackage extends Command {

	private static final long serialVersionUID = 8904939744707934640L;

	/** 写入的磁盘文件 **/
	private File disk;
	
	/** 如果文件存在，覆盖它！默认是假**/
	private boolean override;
	
	/** 自读成员 **/
	private ReadmePackageElement readme;
	
	/** 引导成员，负责分配参数和生成命令 **/
	private CloudPackageElement guide;

	/**
	 * 构造生成云应用软件包
	 */
	protected BuildCloudPackage() {
		super();
		override = false;
	}

	/**
	 * 生成生成云应用软件包副本
	 * @param that 生成云应用软件包
	 */
	protected BuildCloudPackage(BuildCloudPackage that) {
		super(that);
		disk = that.disk;
		override = that.override;
		readme = that.readme;
		guide = that.guide;
	}
	
	/**
	 * 覆盖
	 * @param b
	 */
	public void setOverride(boolean b) {
		override = b;
	}

	/**
	 * 判断覆盖
	 * @return 真或者假
	 */
	public boolean isOverride() {
		return override;
	}

	/**
	 * 设置写入文件，不允许空指针
	 * @param e File实例
	 */
	public void setWriter(File e) {
		Laxkit.nullabled(e);
		disk = e;
	}

	/**
	 * 返回写入文件
	 * @return File实例
	 */
	public File getWriter() {
		return disk;
	}

	/**
	 * 设置自读文件成员
	 * @param e ReadmePackageElement实例
	 */
	public void setReadmeElement(ReadmePackageElement e) {
		readme = e;
	}

	/**
	 * 返回自读文件成员
	 * @return ReadmePackageElement实例，或者空指针
	 */
	public ReadmePackageElement getReadmeElement() {
		return readme;
	}
	
	/**
	 * 设置引导成员
	 * @param e
	 */
	public void setGuideElement(CloudPackageElement e) {
		guide = e;
	}

	/**
	 * 返回引导成员
	 * @return CloudPackageElement实例
	 */
	public CloudPackageElement getGuideElement() {
		return guide;
	}
	
	/**
	 * 判断符合“完全”状态，全部参数齐备
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return readme != null && guide != null;
	}
	
	/**
	 * 输出除自读文件包的阶段单元包单元
	 * @return CloudPackageElement 数组
	 */
	public abstract CloudPackageElement[] elements();

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeFile(disk);
		writer.writeBoolean(override);
		writer.writeInstance(readme);
		writer.writeInstance(guide);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		disk = reader.readFile();
		override = reader.readBoolean();
		readme = reader.readInstance(ReadmePackageElement.class);
		guide = reader.readInstance(CloudPackageElement.class);
	}

}
