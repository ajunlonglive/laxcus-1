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
 * 上传和部署云计算应用软件包。
 * 用户每次只能上传和部署一个软件包，且发布和部署过程中，其他资源全部停止！
 * 
 * @author scott.liang
 * @version 1.0 2/17/2020
 * @since laxcus 1.0
 */
public abstract class DeployCloudPackage extends Command {

	private static final long serialVersionUID = 5413557053278206443L;

	/** 准备上传的磁盘文件 **/
	private File file;
	
	/** 磁盘文件以字节数组形式存在 **/
	private byte[] content;

	/** 包含动态链接库 **/
	private boolean libaray;
	
	/** 在本地部署 **/
	private boolean local;
	
	/** 判断是系统软件包 **/
	private boolean systemWare;
	
	/** 更新检测时间 **/
	private long checkTime;
	
	/**
	 * 构造上传和部署云计算应用软件包
	 */
	protected DeployCloudPackage() {
		super();
		libaray = false;
		local = false;
		systemWare = false;
		setCheckTime(20000L);
	}

	/**
	 * 上传和部署上传和部署云计算应用软件包副本
	 * @param that 上传和部署云计算应用软件包
	 */
	protected DeployCloudPackage(DeployCloudPackage that) {
		super(that);
		file = that.file;
		content = that.content;
		libaray = that.libaray;
		local = that.local;
		systemWare = that.systemWare;
		checkTime = that.checkTime;
	}

	/**
	 * 设置写入文件，不允许空指针
	 * @param e File实例
	 */
	public void setFile(File e) {
		Laxkit.nullabled(e);
		file = e;
	}

	/**
	 * 返回写入文件
	 * @return File实例
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * 设置组件包字节数组
	 * @param b
	 */
	public void setContent(byte[] b) {
		content = b;
	}

	/**
	 * 返回组件包字节数组
	 * @return
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 设置有动态链接库
	 * @param b 真或者假
	 */
	public void setLibrary(boolean b) {
		libaray = b;
	}
	
	/**
	 * 判断有动态链接库
	 * @return 真或者假
	 */
	public boolean hasLibrary() {
		return libaray;
	}

	/**
	 * 设置在FRONT节点本地部署
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}
	
	/**
	 * 判断在FRONT节点本地部署
	 * @return 真或者假
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * 设置当前是系统软件包
	 * @param b 真或者假
	 */
	public void setSystemWare(boolean b) {
		systemWare = b;
	}
	
	/**
	 * 判断当前是系统软件包
	 * @return 真或者假
	 */
	public boolean isSystemWare() {
		return systemWare;
	}

	/**
	 * 设置延时更新时间
	 * @param ms 毫秒
	 */
	public void setCheckTime(long ms) {
		if (ms >= 1000L) {
			checkTime = ms;
		}
	}

	/**
	 * 返回延时更新时间
	 * @return 毫秒
	 */
	public long getCheckTime() {
		return checkTime;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeFile(file);
		writer.writeByteArray(content);
		writer.writeBoolean(libaray);
		writer.writeBoolean(local);
		writer.writeBoolean(systemWare);
		writer.writeLong(checkTime);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		file = reader.readFile();
		content = reader.readByteArray();
		libaray = reader.readBoolean();
		local = reader.readBoolean();
		systemWare = reader.readBoolean();
		checkTime = reader.readLong();
	}

}