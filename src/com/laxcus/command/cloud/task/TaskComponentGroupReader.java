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

import com.laxcus.access.diagram.*;
import com.laxcus.command.cloud.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.XMLocal;

/**
 * 分布任务组件集读取器
 * 
 * @author scott.liang
 * @version 1.0 4/11/2020
 * @since laxcus 1.0
 */
public class TaskComponentGroupReader {

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
	 * @param content 字节内容
	 */
	public TaskComponentGroupReader(byte[] content) {
		super();
		setContent(content);
	}

	/**
	 * 构造分布组件集读取器，读取磁盘文件内容
	 * @param file 文件实例
	 * @throws IOException
	 */
	public TaskComponentGroupReader(File file) throws IOException {
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
		content = b;
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
	 * 解析返回包中的文件
	 * @param content 内容
	 * @return 返回文件数组
	 * @throws IOException
	 */
	public List<CloudPackageItem> readTaskComponents() throws IOException {
		ArrayList<CloudPackageItem> array = new ArrayList<CloudPackageItem>();

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

			// 文件内容
			String name = entry.getName();
			
			// 读取字节数组
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			while (true) {
				int len = jin.read(b, 0, b.length);
				if (len == -1) break;
				out.write(b, 0, len);
			}
			b = out.toByteArray();
			
			// 保存
			long time = entry.getTime();
			CloudPackageItem item = new CloudPackageItem(name, time, b);
			array.add(item);
		}

		jin.close();
		bin.close();

		return array;
	}
	
//	/**
//	 * 读取工作部件名称
//	 * @return TaskPart实例
//	 * @throws IOException
//	 */
//	public TaskPart readTaskPart() throws IOException {
//		List<CloudPackageItem> items = readTaskComponents();
//		for (CloudPackageItem e : items) {
//			String name = e.getName();
////			boolean success = name.equals(TaskBoot.TAG);
//			boolean success = name.equals(TF.GROUP_INF);
//			// 不匹配，忽略！
//			if (!success) {
//				continue;
//			}
//			// 内容
//			byte[] b = e.getContent();
//
//			// 解析
//			org.w3c.dom.Document document = XMLocal.loadXMLSource(b);
//			if (document == null) {
//				return null;
//			}
//
//			// 取出根
//			org.w3c.dom.Element root = (org.w3c.dom.Element) document.getElementsByTagName("root").item(0);
//
//			String sign = XMLocal.getValue(root, TaskMark.SIGN); // "sign");
//			String phase = XMLocal.getValue(root, TaskMark.PHASE); // "phase");
//			
//			// 不是有效，返回空指针
//			if (!PhaseTag.isPhase(phase)) {
//				return null;
//			}
//
//			// 生成签名，允许空指针
//			Siger issuer = null;
//			if (sign != null && sign.trim().length() > 0) {
//				issuer = SHAUser.doSiger(sign);
//			}
//			// 返回工作部件
//			return new TaskPart(issuer, PhaseTag.translate(phase));
//		}
//		return null;
//	}

	/**
	 * 读取工作部件名称
	 * @return TaskPart实例
	 * @throws IOException
	 */
	public TaskPart readTaskPart() throws IOException {
		List<CloudPackageItem> items = readTaskComponents();
		for (CloudPackageItem e : items) {
			String name = e.getName();
//			boolean success = name.equals(TaskBoot.TAG);
			boolean success = name.equals(TF.GROUP_INF);
			// 不匹配，忽略！
			if (!success) {
				continue;
			}
			// 内容
			byte[] b = e.getContent();

			// 解析
			org.w3c.dom.Document document = XMLocal.loadXMLSource(b);
			if (document == null) {
				return null;
			}

			// 取出根
//			org.w3c.dom.Element root = (org.w3c.dom.Element) document.getElementsByTagName("root").item(0);

			org.w3c.dom.Element root = document.getDocumentElement();
			String sign = XMLocal.getValue(root, TaskMark.SIGN); // "sign");
			String phase = XMLocal.getValue(root, TaskMark.PHASE); // "phase");

//			// 取出签名，没有定义是系统组件
//			String sign = null;
//			org.w3c.dom.NodeList nodes = document.getElementsByTagName(TaskMark.SIGN);
//			if (nodes.getLength() == 1) {
//				org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
//				sign = element.getTextContent();
//			} 
//			
//			// 阶段类型
//			String phase = "";
//			nodes = document.getElementsByTagName(TaskMark.PHASE);
//			if (nodes.getLength() == 1) {
//				org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
//				phase = element.getTextContent();
//			} else {
//				Logger.error(this, "readTaskPart", "cannot read phase tag!");
//				return null;
//			}
			
			// 不是有效，返回空指针
			if (!PhaseTag.isPhase(phase)) {
				Logger.error(this, "readTaskPart", "illegal phase:%s", phase);
				return null;
			}

			// 生成签名，允许空指针，空指针表示系统组件！
			Siger issuer = null;
			if (sign != null && sign.trim().length() > 0) {
				issuer = SHAUser.doSiger(sign);
			}
			// 返回工作部件
			return new TaskPart(issuer, PhaseTag.translate(phase));
		}
		return null;
	}

	
	/**
	 * 统计包中有效文件（DTC单元）数目
	 * @return -1是检测失败，0是没有有效单元，大于0是有效单元数目
	 * @throws IOException
	 */
	public int getAvailableItems() throws IOException {
		int ret = check();
		if (ret < 1) {
			return -1;
		}

		int count = 0;
		List<CloudPackageItem> items = readTaskComponents();
		for (CloudPackageItem item : items) {
			// 如果不是“GROUP-INF/group.xml”，则统计！
			String name = item.getName();
			if (!name.matches(TF.GROUP_INF)) {
				count++;
			}
		}

		// 返回对应值
		return count;
	}
}