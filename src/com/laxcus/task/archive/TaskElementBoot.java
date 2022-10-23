/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 分布任务组件档案资源。<br><br>
 * 
 * 分布任务组件档案资源是一个以“.dct”为后缀的JAR文件，如“LaxcusSystem.dtc”。
 * 它由管理员上传到ARCHIVE站点存储。按照要求，通过网络分发DATA/WORK/BUILD/CALL站点上使用，执行数据计算和数据构建工作。
 * 在各个工作站点上，它被加载到分布任务组件管理器的内存中，供使用者检查和调用。
 * 文件中必须有一个“TASK-INF”目录，目录中有“tasks.xml”文件，它声明包中的全部文件配置。<br><br>
 * 
 * 分布组件包含一个账号下某个阶段的全部分布组件。例如DEMO用户的“TO”阶段分布组件，必须全部打包在一起，不允许一个阶段的分布组件分别打包的情况出现。若发生这种情况，将产生部署上的混乱。<br>
 * 
 * tasks.xml文档格式：<br><br>
 * 
 * 题头（声明组件拥有人和阶段类型，每个文件只有一个）：<br>
 * &ltusername&gt 013271A04A1FDE715827FD702F252FF97C500CEC &lt/username&gt <br>
 * &ltphase&gt ISSUE &lt/phase&gt <br><br>
 * 
 * 组件单元（文件中重复多组）：<br>
 * &lttask&gt  <br>
 * 	&ltnaming&gt any word (only one) &lt/naming&gt <br>
 *  &ltclass&gt org.xxx.xxx.EngineTask &lt/class&gt <br>
 *  &ltspaces&gt [EMPTY] | [schema1.table1 , schema2.table2, schema3.table3] | [NONE] &lt/spaces&gt <br>
 *  &ltresource&gt schema or table or filename or other &lt/resource&gt  <br>
 * 	&ltproject-class&gt org.xxx.xxx.EngineProject &lt/project-class&gt  <br>
 * &lt/task&gt <br><br><br>
 * 
 * 说明：<br>
 * <1> username，分布组件的持有人签名，是64个16进制字符的SHA256编码。如果是系统级组件，此项忽略。<br>
 * <2> phase，阶段关键字。文本描述见PhaseTag中的定义。<br>
 * 
 * <3> naming，分布任务命名，在运行系统中唯一，否则会引起调用混乱。格式由ASCII码的"字母、数字、下划线"组成，其它非法。<br>
 * <4> class，任务实现类，必须从DistributeTask类派生。<br>
 * <5> spaces，用户配置的数据表名集合，数据表名之间用逗号分隔，如：Catalog.Ship, STA.abs 。如果没有标明，表示适用于本账号下的所有表；若是“NONE”关键字表示不适用于任何表。 <br> 
 * <6> resource， 用户自定义的资源(文本格式)，具体由用户去解析。一般有用户定义的参数、硬盘文件等。<br>
 * <7> project-class，项目管理类，必须从Project类派生。<br>
 * 
 * @author scott.liang
 * @version 1.2 10/19/2015
 * @since laxcus 1.0
 */
public final class TaskElementBoot implements Classable, Serializable, Comparable<TaskElementBoot> {

	private static final long serialVersionUID = -4528703780115983930L;

	/** 文件路径 **/
	private String path;

	/** DTC文件里的“TASK-INF/tasks.xml”文本内容 **/
	private byte[] taskText;

	/** 字节内容 **/
	private byte[] content;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		path = null;
		taskText = null;
		content = null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 文件参数
		writer.writeString(path);
		// TASKS.XML配置文档
		writer.writeByteArray(taskText);
		// TASKS.XML配置文档
		writer.writeByteArray(content);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 解析文件
		path = reader.readString();
		// TASKS.XML文档
		taskText = reader.readByteArray();
		content = reader.readByteArray();
		// 返回解析的长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的分布任务组件档案资源，生成它的浅层数据副本
	 * @param that TaskElementBoot实例
	 */
	private TaskElementBoot(TaskElementBoot that) {
		this();
		path = that.path;
		taskText = that.taskText;
		content = that.content;
	}

	/**
	 * 构造默认的分布任务组件档案资源
	 */
	private TaskElementBoot() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析分布任务组件档案资源
	 * @param reader 可类化数据读取器
	 */
	public TaskElementBoot(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造默认的分布任务组件档案资源
	 * @param path 文件路径
	 * @param content 字节内容
	 */
	public TaskElementBoot(String path, byte[] content) {
		super();
		setPath(path);
		setContent(content);
	}
	
	/**
	 * 设置文件路径
	 * @param e 文件路径
	 */
	public void setPath(String e) {
		Laxkit.nullabled(e);
		path = e;
	}
	
	/**
	 * 返回文件路径
	 * @return 文件路径
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 只在内容
	 * @param b
	 */
	public void setContent(byte[] b) {
		content = b;
	}
	
	/**
	 * 返回内容
	 * @return 字符数组
	 */
	public byte[] getContent() {
		return content;
	}
	
	/**
	 * 设置“tasks.xml”文件的文本内容
	 * @param b tasks.xml文件的字节数组
	 */
	public void setTaskText(byte[] b) {
		taskText = new byte[b.length];
		System.arraycopy(b, 0, taskText, 0, b.length);
	}

	/**
	 * 返回“tasks.xml”文件的文本内容
	 * @return  tasks.xml文件的字节数组
	 */
	public byte[] getTaskText() {
		return taskText;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return 返回TaskElementBoot实例
	 */
	public TaskElementBoot duplicate() {
		return new TaskElementBoot(this);
	}

	/**
	 * 比较对象一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskElementBoot.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskElementBoot) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(content);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskElementBoot that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(content, that.content);
	}

	/**
	 * 克隆当前实例的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回档案文件的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		MD5Hash hash = Laxkit.doMD5Hash(content);
		return hash.toString();
	}

	/**
	 * 从JAR流中读类文件
	 * @param reader JAR读取流
	 * @return 返回解码后的数据流
	 * @throws IOException
	 */
	private byte[] readContent(JarInputStream reader) throws IOException {
		// 读数据流
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		while(true) {
			int len = reader.read(b, 0, b.length);
			if(len == -1) break;
			writer.write(b, 0, len);
		}
		return writer.toByteArray();
	}
	
	/**
	 * 从JAR文件中提取TASK-INF/tasks.xml后，根据定义解析类文件
	 * @return 成功返回真，否则假
	 */
	public boolean suckup() {
		// 清除记录
		taskText = null;

		boolean success = false;
		try {
			// 找到和解析tasks.xml
			ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
			JarInputStream jin = new JarInputStream(bin);
			while (true) {
				JarEntry entry = jin.getNextJarEntry();
				if (entry == null) {
					break;
				}
				// 忽略目录
				if (entry.isDirectory()) {
					continue;
				}

				String name = entry.getName();
				// 只读取"TASK-INF/tasks.xml"，其它.class类文件和附助文件忽略
				if (!TF.TASK_INF.equals(name)) {
					continue;
				}
				
				// 文件，这个标签文件解释DTC文件的全部配置信息，其它文件忽略

				// 读JAR条目，保存和解析参数
				byte[] b = readContent(jin);
				setTaskText(b);
				success = true;
				// 找到后退出
				break;
			}
			
			jin.close();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 返回结果
		return success;
	}

}