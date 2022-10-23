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
import java.util.zip.*;

import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件读取器。
 * 是单个“.dtc”后缀的文件
 * 
 * @author scott.liang
 * @version 1.0 4/14/2020
 * @since laxcus 1.0
 */
public class TaskComponentReader {

	/** 数据内容，以字节形式保存 **/
	private byte[] content;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		// 释放内存！
		content = null;
	}

	/**
	 * 构造分布组件集读取器，指定内容
	 * @param b 字节内容
	 * @param off 下标位置
	 * @param len 指定度
	 */
	public TaskComponentReader(byte[] b , int off, int len) {
		super();
		setContent(b, off, len);
	}

	/**
	 * 构造分布组件集读取器，指定内容
	 * @param b 字节内容
	 */
	public TaskComponentReader(byte[] b) {
		super();
		setContent(b);
	}

	/**
	 * 构造分布组件集读取器，读取磁盘文件内容
	 * @param file 文件实例
	 * @throws IOException
	 */
	public TaskComponentReader(File file) throws IOException {
		super();
		readContent(file);
	}

	/**
	 * 设置字节内容
	 * @param b
	 */
	private void setContent(byte[] b) {
		// 判断是空指针
		if (Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		}
		setContent(b, 0, b.length);
	}

	/**
	 * 设置字节内容
	 * @param b
	 */
	private void setContent(byte[] b, int off, int len) {
		// 判断是空指针
		if (Laxkit.isEmpty(b) || len == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}
		// 复制内容
		content = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 输出字节内容
	 * @return 返回字节数组
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 从磁盘读取字节内容
	 * @param file
	 * @throws IOException
	 */
	private void readContent(File file) throws IOException {
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throw new FileNotFoundException(file.toString());
		}
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		setContent(b);
	}

	/**
	 * 检测和读取内容
	 * @return 返回计取的单元数目。成功是大于等于0，否则是-1。
	 */
	public int check() {
		try {
			int count = 0;
			// 读内容
			ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
			ZipInputStream jin = new ZipInputStream(bin);
			while (true) {
				ZipEntry entry = jin.getNextEntry();
				if (entry == null) {
					break;
				}

				// 只读文件，如果是目录，忽略！
				if (entry.isDirectory()) {
					continue;
				}

				// 读取字节数组
				byte[] b = new byte[1024];
				while (true) {
					int len = jin.read(b, 0, b.length);
					if (len == -1) break;
				}

				// 成功，统计加1
				count++;
			}

			jin.close();
			bin.close();
			return count;
		} catch (IOException e) {
			Logger.error(e);
		}

		return -1;
	}

	/**
	 * 读取包里面的“TASK-INF/tasks.xml”配置文本
	 * @return
	 */
	public byte[] readTaskText() throws IOException {
		byte[] taskText = null;

		// 读内容
		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
		ZipInputStream jin = new ZipInputStream(bin);
		while (true) {
			ZipEntry entry = jin.getNextEntry();
			if (entry == null) {
				break;
			}

			// 只读文件，如果是目录，忽略！
			if (entry.isDirectory()) {
				continue;
			}

			// 判断匹配
			String name = entry.getName();
			if(!name.equalsIgnoreCase(TF.TASK_INF)) {
				continue;
			}

			// 读取字节数组
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			while (true) {
				int len = jin.read(b, 0, b.length);
				if (len == -1) break;
				out.write(b, 0, len);
			}
			// 输出为字节
			taskText = out.toByteArray();

			break;
		}
		jin.close();
		bin.close();

		return taskText;
	}
	
	/**
	 * 读工作分区
	 * @return 返回TaskSection实例，失败是空指针 
	 */
	public TaskSection readTaskSection() {
		byte[] taskText = null;
		try {
			taskText = readTaskText();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(taskText)) {
			return null;
		}

		TaskConfigReader reader = new TaskConfigReader(taskText);
		return reader.readTaskSection();
	}

	/**
	 * 读工作部件
	 * @return 返回实例，失败是空指针
	 */
	public TaskPart readTaskPart() {
		byte[] taskText = null;
		try {
			taskText = readTaskText();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(taskText)) {
			return null;
		}

		TaskConfigReader reader = new TaskConfigReader(taskText);
		return reader.readTaskPart();
	}

	/**
	 * 解析版本标签 
	 * @return 返回有效标识
	 */
	public WareTag readWareTag() {
		byte[] taskText = null;
		try {
			taskText = readTaskText();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(taskText)) {
			return null;
		}

		TaskConfigReader reader = new TaskConfigReader(taskText);
		return reader.readWareTag();
	}

	/**
	 * 解析任务组件分段
	 * @return 返回有效标识
	 */
	public List<Tock> readTocks() {
		byte[] taskText = null;
		try {
			taskText = readTaskText();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 没有找到，警告！忽略！
		if (Laxkit.isEmpty(taskText)) {
			return null;
		}

		TaskConfigReader reader = new TaskConfigReader(taskText);
		return reader.readTocks();
	}

	public static void main(String[] args) {
		File file = new File("E:/laxcus/backup/demo_fork.dtc");
		try {
			TaskComponentReader reader = new TaskComponentReader(file);
			WareTag tag = reader.readWareTag();
			System.out.println(tag);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//	public Map<Phase, TaskProject> readProjects() throws IOException {
	//		// 组件实例
	//		TreeMap<Phase, TaskProject> array = new TreeMap<Phase, TaskProject>();
	//		
	//		byte[] taskText = readTaskText();
	//		// 没有找到，警告！忽略！
	//		if(Laxkit.isEmpty(content)) {
	//			Logger.error(this, "createProjects", "cannot be find %s", TaskBootItem.TAG);
	//			return null;
	//		}
	//		
	//		XMLocal xml = new XMLocal();
	//		Document document = xml.loadXMLSource(taskText);
	//		if (document == null) {
	//			continue;
	//		}
	//
	//		// 解析单项
	//		NodeList nodes = document.getElementsByTagName(TaskMark.TASK);
	//		int size = nodes.getLength();
	//		for (int i = 0; i < size; i++) {
	//			Element element = (Element) nodes.item(i);
	//
	//			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
	//			String naming = xml.getValue(element, TaskMark.TASK_NAMING); // "naming");
	//			// 任务类路径
	//			String task_class = xml.getValue(element, TaskMark.TASK_CLASS); // "class");
	//			// 资源配置(任意字符串格式.具体由用户的Project子类解释)
	//			String resource = xml.getValue(element, TaskMark.TASK_RESOURCE); // "resource");
	//			// 项目类路径(从Project.class派生)
	//			String project_class = xml.getValue(element, TaskMark.TASK_PROJECT_CLASS); // "project-class");
	////			// 当前分布任务组件版本号
	////			int version = splitVersion(xml.getValue(element, TaskMark.VERSION));
	//
	//			// 解析命名参数，建立阶段命名
	//			Phase phase = createPhase(naming);
	//			// 项目持有人
	//			phase.setIssuer(part.getIssuer());
	//
	//			
	//				Class<?> clazz = Class.forName(project_class, true, loader);
	//				TaskProject jet = (TaskProject) clazz.newInstance();
	//				// 项目持有人
	//				jet.setIssuer(part.getIssuer());
	//				//						// LOGO文件路径
	//				//						jet.setLogo(logo);
	//				// 阶段命名
	//				jet.setPhase(phase);
	//				// 版本号
	//				jet.setVersion(version);
	//				// 项目类实例
	//				jet.setTaskClass(task_class);
	//				// 资源
	//				jet.setResource(resource);
	//				// 保存命名项目
	//				array.put(phase, jet);
	//
	//				Logger.info(this, "createProjects", "%s", jet);
	//			
	//		}
	//		
	//		return array;
	//	}

	//	/**
	//	 * 解析返回包中的文件
	//	 * @param content 内容
	//	 * @return 返回文件数组
	//	 * @throws IOException
	//	 */
	//	public List<CloudPackageItem> readTaskComponents() throws IOException {
	//		ArrayList<CloudPackageItem> array = new ArrayList<CloudPackageItem>();
	//
	//		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
	//		ZipInputStream jin = new ZipInputStream(bin);
	//		while (true) {
	//			ZipEntry entry = jin.getNextEntry();
	//			if (entry == null) {
	//				break;
	//			}
	//
	//			// 只读文件，如果是目录，忽略！
	//			if (entry.isDirectory()) {
	//				continue;
	//			}
	//
	//			// 文件内容
	//			String name = entry.getName();
	//			
	//			// 读取字节数组
	//			ByteArrayOutputStream out = new ByteArrayOutputStream();
	//			byte[] b = new byte[1024];
	//			while (true) {
	//				int len = jin.read(b, 0, b.length);
	//				if (len == -1) break;
	//				out.write(b, 0, len);
	//			}
	//			b = out.toByteArray();
	//			
	//			// 保存
	//			CloudPackageItem item = new CloudPackageItem(name, b);
	//			array.add(item);
	//		}
	//
	//		jin.close();
	//		bin.close();
	//
	//		return array;
	//	}
	//
	//	/**
	//	 * 读取工作部件名称
	//	 * @return TaskPart实例
	//	 * @throws IOException
	//	 */
	//	public TaskPart readTaskPart() throws IOException {
	//		List<CloudPackageItem> items = readTaskComponents();
	//		for (CloudPackageItem e : items) {
	//			String name = e.getName();
	////			boolean success = name.equals("GROUP-INF/group.xml");
	//			boolean success = name.equals(TaskBoot.TAG);
	//			// 不匹配，忽略！
	//			if (!success) {
	//				continue;
	//			}
	//			// 内容
	//			byte[] b = e.getContent();
	//
	//			// 解析
	//			XMLocal xml = new XMLocal();
	//			org.w3c.dom.Document document = xml.loadXMLSource(b);
	//			if (document == null) {
	//				return null;
	//			}
	//
	//			// 取出根
	//			org.w3c.dom.Element root = (org.w3c.dom.Element) document.getElementsByTagName("root").item(0);
	//
	//			String sign = xml.getValue(root, "sign");
	//			String phase = xml.getValue(root, "phase");
	//			
	//			// 不是有效，返回空指针
	//			if (!PhaseTag.isPhase(phase)) {
	//				return null;
	//			}
	//			// 签名
	//			Siger siger = null;
	//			if (sign != null && Siger.validate(sign)) {
	//				siger = SHAUser.doSiger(sign);
	//			}
	//			// 返回工作部件
	//			return new TaskPart(siger, PhaseTag.translate(phase));
	//		}
	//		return null;
	//	}

}