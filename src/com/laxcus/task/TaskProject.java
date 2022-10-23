/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import java.io.*;

import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 任务管理项目。<br><br>
 * 
 * 分布数据处理（CONDUCT/ESTABLISH）在实现各站点上的项目配置<br>
 * 具体说明见 DistributeTask 和 TaskPool<br>
 * Project保存<task>标签中的username、phase、version、class、resource5项参数，<br>
 * 前4项是固定的，由LAXCUS系统处理，后一项资源配置由用户的Project继承类解释。<br><br>
 * 
 * 用户需要实现自己的Project继承类。如果有其它参数，需要再实现可类化的两处接口。<br>
 * 
 * @author scott.liang 
 * @version 1.3 7/23/2015
 * @since laxcus 1.0
 */
public abstract class TaskProject implements Classable, Serializable {

	private static final long serialVersionUID = 2807065889767319447L;

	/** 分布任务组件持有人 **/
	private Siger issuer;

	/** 分布任务组件的阶段命名 */
	private Phase phase;
	
	/** 软件版本号 **/
	private WareVersion version;

	/** 组件类名称 */
	private String taskClass;

	/** 用户自定义资源数据（内容由用户自行解释） */
	private String resource;

	/**
	 * 将项目参数写入可类化存储器。
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.3
	 */
	@Override
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		// 项目持有人
		writer.writeInstance(issuer);
		// 写入阶段命名，这个参数必须存在
		writer.writeObject(phase);
		// 版本号
		writer.writeObject(version);
		// 写入类对象名称，这个参数必须存在
		writer.writeString(taskClass);
		// 写入资源参数，允许是空值
		writer.writeString(resource);
		// 写入子类参数
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析项目参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.3
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int scale = reader.getSeek();
		// 项目持有人
		issuer = reader.readInstance(Siger.class);
		// 阶段命名
		phase = new Phase(reader);
		// 版本号
		version = new WareVersion(reader);
		// 任务类
		taskClass = reader.readString();
		// 资源
		resource = reader.readString();
		// 读取子类参数
		resolveSuffix(reader);
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 构造一个默认的项目配置
	 */
	protected TaskProject() {
		super();
	}

	/**
	 * 根据传入项目实例，生成它的数据副本
	 * @param that 项目实例
	 */
	protected TaskProject(TaskProject that) {
		this();
		issuer = that.issuer;
		phase = that.phase;
		version = that.version;
		taskClass = that.taskClass;
		resource = that.resource;
	}

	/**
	 * 设置项目持有人。当参数为空值时，是系统级任务组件，适用所有请求人。
	 * @param e 项目持有人签名
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 返回项目持有人
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 判断是系统级项目。
	 * @return 返回真或者假。
	 */
	public boolean isSystemLevel() {
		return phase.isSystemLevel();
	}

	/**
	 * 判断是用户级项目。
	 * @return 返回真或者假。
	 */
	public boolean isUserLevel() {
		return phase.isUserLevel();
	}

	/**
	 * 设置任务命名
	 * @param e 阶段命名实例
	 */
	public void setPhase(Phase e) {
		phase = e;
	}

	/**
	 * 返回任务阶段命名
	 * @return 阶段命名实例
	 */
	public Phase getPhase() {
		return phase;
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
	public void setTaskClass(String e) {
		taskClass = e;
	}

	/**
	 * 返回任务类路径
	 * 
	 * @return 类路径字符串
	 */
	public String getTaskClass() {
		return taskClass;
	}

	/**
	 * 返回用户自定义配置资源
	 * 
	 * @return 字符串文本
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * 设置用户资源。<br>
	 * 用户资源是任意内容的字符串，如JAR包里的文件路径，或者一段文字。具体格式由用户定义。
	 * 
	 * @param e 字符串
	 */
	public void setResource(String e) {
		resource = e;
	}

	/**
	 * 返回阶段命名和类路径的组合，中间用“#”号分开。
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", phase, taskClass);
	}
	
	/**
	 * 这个方法由子类去实现，生成一个属于自己的数据副本
	 * @return TaskProject子类实例
	 */
	public abstract TaskProject duplicate();

	/**
	 * 子类将私有参数写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 子类从可类化数据读取器中解析属于自己的私有参数
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}