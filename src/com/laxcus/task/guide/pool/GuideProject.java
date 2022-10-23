/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.pool;

import java.io.*;

import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 任务管理项目。<br><br>
 * 
 * 分布数据处理（CONDUCT/ESTABLISH）在实现各站点上的项目配置<br>
 * 具体说明见 DistributeBoot 和 BootPool<br>
 * Project保存<task>标签中的username、phase、version、class、resource5项参数，<br>
 * 前4项是固定的，由LAXCUS系统处理，后一项资源配置由用户的Project继承类解释。<br><br>
 * 
 * 用户需要实现自己的Project继承类。如果有其它参数，需要再实现可类化的两处接口。<br>
 * 
 * @author scott.liang 
 * @version 1.3 7/23/2015
 * @since laxcus 1.0
 */
public class GuideProject implements  Serializable, Cloneable,java.lang.Comparable<GuideProject> {

	private static final long serialVersionUID = 2807065889767319447L;

	/** 分布任务组件的阶段命名 */
	private Sock sock;

	/** 软件版本号 **/
	private WareVersion version;

	/** 应用软件名称，显示给用户 **/
	private String caption;

	/** 提示 **/
	private String tooltip;

	/** 引导类路径 */
	private String guideClass;

	/** 图标类路径 */
	private String iconPath;

	//	/**
	//	 * 将项目参数写入可类化存储器。
	//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	//	 * @since 1.3
	//	 */
	//	@Override
	//	public int build(ClassWriter writer) {
	//		final int scale = writer.size();
	//		// 项目持有人
	//		writer.writeInstance(issuer);
	//		// 写入阶段命名，这个参数必须存在
	//		writer.writeObject(phase);
	//		// 版本号
	//		writer.writeObject(version);
	//		// 写入类对象名称，这个参数必须存在
	//		writer.writeString(taskClass);
	//		// 写入资源参数，允许是空值
	//		writer.writeString(resource);
	//		// 写入子类参数
	//		buildSuffix(writer);
	//		// 返回写入的字节长度
	//		return writer.size() - scale;
	//	}
	//
	//	/**
	//	 * 从可类化读取器中解析项目参数
	//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	//	 * @since 1.3
	//	 */
	//	@Override
	//	public int resolve(ClassReader reader) {
	//		final int scale = reader.getSeek();
	//		// 项目持有人
	//		issuer = reader.readInstance(Naming.class);
	//		// 阶段命名
	//		phase = new Sock(reader);
	//		// 版本号
	//		version = new WareVersion(reader);
	//		// 任务类
	//		taskClass = reader.readString();
	//		// 资源
	//		resource = reader.readString();
	//		// 读取子类参数
	//		resolveSuffix(reader);
	//		// 返回读取的字节长度
	//		return reader.getSeek() - scale;
	//	}

	/**
	 * 构造一个默认的项目配置
	 */
	public GuideProject() {
		super();
	}

	/**
	 * 构造一个默认的项目配置
	 */
	public GuideProject(Sock sock) {
		this();
		setSock(sock);
	}

	/**
	 * 根据传入项目实例，生成它的数据副本
	 * @param that 项目实例
	 */
	private GuideProject(GuideProject that) {
		this();
		sock = that.sock;
		version = that.version;
		caption = that.caption;
		tooltip = that.tooltip;
		guideClass = that.guideClass;
		iconPath = that.iconPath;
	}

	/**
	 * 设置应用组件名称。名称显示给用户
	 * @param e 应用组件名称
	 */
	public void setCaption(String e) {
		caption = e;
	}

	/**
	 * 返回应用组件名称
	 * @return Naming实例
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * 设置工具提示
	 * @param e
	 */
	public void setTooltip(String e) {
		tooltip = e;
	}

	/**
	 * 返回应用组件提示
	 * @return 字符串
	 */
	public String getTooltip() {
		return tooltip;
	}

	//	/**
	//	 * 判断是系统级项目。
	//	 * @return 返回真或者假。
	//	 */
	//	public boolean isSystemLevel() {
	//		return sock.isSystemLevel();
	//	}
	//
	//	/**
	//	 * 判断是用户级项目。
	//	 * @return 返回真或者假。
	//	 */
	//	public boolean isUserLevel() {
	//		return sock.isUserLevel();
	//	}

	/**
	 * 设置任务命名
	 * @param e 阶段命名实例
	 */
	public void setSock(Sock e) {
		Laxkit.nullabled(e);
		sock = e;
	}

	/**
	 * 返回任务阶段命名
	 * @return 阶段命名实例
	 */
	public Sock getSock() {
		return sock;
	}

	/**
	 * 设置版本号。<br>
	 * 是固定方法，参数在“tasks.xml”中配置。系统根据版本号，选择对应的分布调用。
	 * 
	 * @param i 版本号
	 */
	public final void setVersion(WareVersion i) {
		Laxkit.nullabled(i);
		version = i;
	}

	/**
	 * 返回版本号。<br>
	 * 是固定方法，不允许子类继承。
	 * 
	 * @return 版本号
	 */
	public final WareVersion getVersion() {
		return version;
	}

	/**
	 * 设置类路径
	 * @param e 类路径字符串
	 */
	public void setGuideClass(String e) {
		guideClass = e;
	}

	/**
	 * 返回任务类路径
	 * 
	 * @return 类路径字符串
	 */
	public String getGuideClass() {
		return guideClass;
	}

	/**
	 * 返回用户自定义配置资源
	 * 
	 * @return 字符串文本
	 */
	public String getIconPath() {
		return iconPath;
	}

	/**
	 * 设置用户资源。<br>
	 * 用户资源是任意内容的字符串，如JAR包里的文件路径，或者一段文字。具体格式由用户定义。
	 * 
	 * @param e 字符串
	 */
	public void setIconPath(String e) {
		iconPath = e;
	}

	/**
	 * 返回阶段命名和类路径的组合，中间用“#”号分开。
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", sock, guideClass);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		return sock.hashCode();
	}

	/**
	 * 这个方法由子类去实现，生成一个属于自己的数据副本
	 * @return BootProject子类实例
	 */
	public GuideProject duplicate() {
		return new GuideProject(this);
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
	public int compareTo(GuideProject that) {
		if (that == null) {
			return 1;
		}
		// 比较根命名和版本号
		int ret = Laxkit.compareTo(sock, that.sock);
		if (ret == 0) {
			ret = Laxkit.compareTo(version, that.version);
		}
		return ret;
	}

	//	/**
	//	 * 子类将私有参数写入可类化数据存储器
	//	 * @param writer 可类化数据存储器
	//	 */
	//	protected abstract void buildSuffix(ClassWriter writer);
	//
	//	/**
	//	 * 子类从可类化数据读取器中解析属于自己的私有参数
	//	 * @param reader 可类化数据读取器
	//	 */
	//	protected abstract void resolveSuffix(ClassReader reader);	

}