/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.laxcus.access.diagram.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.*;

/**
 * "TASK-INF/tasks.xml"配置文档读取器。<br>
 * 
 * 解析里面的参数，以类形式返回
 * 
 * @author scott.liang
 * @version 1.0 5/12/2020
 * @since laxcus 1.0
 */
public class TaskConfigReader extends PackageConfigReader {

	/**
	 * 构造"TASK-INF/tasks.xml"配置文档读取器，指定内容
	 * @param b 字节内容
	 * @param off 下标位置
	 * @param len 指定度
	 */
	public TaskConfigReader(byte[] b , int off, int len) {
		super(b, off, len);
	}

	/**
	 * 构造"TASK-INF/tasks.xml"配置文档读取器，指定内容
	 * @param b 字节内容
	 */
	public TaskConfigReader(byte[] b) {
		super(b);
	}

	/**
	 * 构造"TASK-INF/tasks.xml"配置文档读取器，指定磁盘文件
	 * @param file 磁盘文件
	 */
	public TaskConfigReader(File file) throws IOException {
		super(file);
	}
	
	/**
	 * 读工作分区
	 * @return 返回TaskSection实例，失败是空指针 
	 */
	public TaskSection readTaskSection() {
		// 读工作部件
		TaskPart part = readTaskPart();
		// 读软件包标记
		WareTag tag = readWareTag();
		// 任意不成立返回空指针
		if (part == null || tag == null) {
			Logger.error(this, "readTaskSection", "cannot be resolve!");
			return null;
		}
		// 返回实例
		return new TaskSection(part.getIssuer(), part.getFamily(), tag.getNaming());
	}

	/**
	 * 读工作部件
	 * @return 返回实例，失败是空指针
	 * @throws IOException
	 */
	public TaskPart readTaskPart() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return null;
		}

		// 任务阶段
		String phase = XMLocal.getXMLValue(document.getElementsByTagName(TaskMark.PHASE)); //"phase"));
		int phaseFamily = PhaseTag.translate(phase);
		// 存在错误，忽略它
		if (!PhaseTag.isPhase(phaseFamily)) {
			Logger.error(this, "readTaskPart", "illegal phase:%s", phase);
			return null;
		}

		// 生成签名。注意！如果SHA256是空字符串，返回空值！
		String sha256 = XMLocal.getXMLValue(document.getElementsByTagName(TaskMark.SIGN));
		Siger sign = SHAUser.doSiger(sha256);

		// 设置部件
		return new TaskPart(sign, phaseFamily);
	}

	/**
	 * 解析任务组件分段！
	 * @return 返回Tock数组，失败是空指针
	 */
	public List<Tock> readTocks() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return null;
		}

		// 取出软件标签
		WareTag tag = readWareTag();
		if (tag == null) {
			return null;
		}

		// 数组
		ArrayList<Tock> array = new ArrayList<Tock>();

		// 解析命名（分为系统命名和用户命名）
		NodeList nodes = document.getElementsByTagName(TaskMark.TASK); // "task");
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element element = (Element) nodes.item(i);

			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
			String naming = XMLocal.getValue(element, TaskMark.TASK_NAMING); // "naming");
			// 生成！
			try {
				Tock tock = new Tock(tag.getNaming(), naming);
				array.add(tock);
			} catch (Throwable e) {
				Logger.fatal(e);
				return null;
			}
		}
		return array;
	}

	public List<TaskToken> readTaskTokens() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return null;
		}
		
		ArrayList<TaskToken> array = new ArrayList<TaskToken>();
		
		// 解析单项
		NodeList nodes = document.getElementsByTagName(TaskMark.TASK);
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element element = (Element) nodes.item(i);

			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
			String naming = XMLocal.getValue(element, TaskMark.TASK_NAMING); // "naming");
			// 任务类路径
			String task_class = XMLocal.getValue(element, TaskMark.TASK_CLASS); //  "boot-class");
			// 资源配置(任意字符串格式.具体由用户的Project子类解释)
			String resource = XMLocal.getValue(element, TaskMark.TASK_RESOURCE); // "resource");
			// 项目类路径(从Project.class派生)
			String project_class = XMLocal.getValue(element, TaskMark.TASK_PROJECT_CLASS); // "project-class");
			
			TaskToken token = new TaskToken(naming);
			token.setBootClass(task_class);
			token.setResource(resource);
			token.setProjectClass(project_class);
			array.add(token);
		}
		
		return array;
	}
}