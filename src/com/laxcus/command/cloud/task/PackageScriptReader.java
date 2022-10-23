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

import com.laxcus.command.cloud.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.*;

/**
 * 生成包脚本读取器。<br>
 * 读取"*.script" 的XML格式的脚本文件<br>
 * 
 * @author scott.liang
 * @version 1.0 8/12/2020
 * @since laxcus 1.0
 */
public class PackageScriptReader {

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
	 * 构造生成包脚本读取器，指定内容
	 * @param b 字节内容
	 * @param off 下标位置
	 * @param len 指定度
	 */
	public PackageScriptReader(byte[] b , int off, int len) {
		super();
		setContent(b, off, len);
	}

	/**
	 * 构造生成包脚本读取器，指定内容
	 * @param b 字节内容
	 */
	public PackageScriptReader(byte[] b) {
		super();
		setContent(b);
	}

	/**
	 * 构造配置文件读取器，读取磁盘文件内容
	 * @param file 文件实例
	 * @throws IOException
	 */
	public PackageScriptReader(File file) throws IOException {
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
	 * 返回属性
	 * @return
	 */
	private String getFamily() {
		Element element = readElement("root");
		if (element != null) {
			return element.getAttribute("family");
		}
		return null;
	}
	
	/**
	 * 判断是CONDUCT脚本文件
	 * @return
	 */
	public boolean isConduct() {
		String family = getFamily();
		return Laxkit.compareTo("conduct", family) == 0;
	}

	/**
	 * 判断是ESTABLISH脚本文件
	 * @return
	 */
	public boolean isEstablish() {
		String family = getFamily();
		return Laxkit.compareTo("establish", family) == 0;
	}

	/**
	 * 判断是CONTACT脚本文件
	 * @return
	 */
	public boolean isContact() {
		String family = getFamily();
		return Laxkit.compareTo("contact", family) == 0;
	}
	
	/**
	 * 读单元
	 * @param tag
	 * @return
	 */
	private Element readElement(String tag) {
		// 解析
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return null;
		}

		// 每个标签只能有一个
		NodeList nodes = document.getElementsByTagName(tag);
		if (nodes.getLength() != 1) {
			return null;
		}

		return (Element) nodes.item(0);
	}
	
	/**
	 * 判断一个文件存在
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	private boolean exists(String filename) {
		File file = new File(filename);
		return (file.exists() && file.isFile());
	}
	
	/**
	 * 自读成员
	 * @return
	 */
	public ReadmePackageElement readReadme() {
		Element element = readElement("README");
		String logo = XMLocal.getValue(element, "logo");
		String licence = XMLocal.getValue(element, "licence");
		// 判断文件存在
		if (!exists(logo) || !exists(licence)) {
			return null;
		}
		// 生成实例
		ReadmePackageElement readme = new ReadmePackageElement();
		readme.setLogo(new FileKey(new File(logo)));
		readme.setLicence(new FileKey(new File(licence)));
		return readme;
	}

	/**
	 * 生成GUIDE单元
	 * @return
	 */
	public CloudPackageElement readGuide() {
		Element element = readElement("GUIDE");
		if(element == null) {
			return null;
		}

		CloudPackageElement guide = new CloudPackageElement("guide");
		// 引导文件
		String boot = XMLocal.getValue(element, "boot");
		if (!exists(boot)) {
			return null;
		}
		guide.setBoot(new FileKey(new File(boot)));

		// 附件
		String[] assists = XMLocal.getValues(element, "jar");
		for (String jar : assists) {
			if (!exists(jar)) {
				return null;
			}
			guide.addAssist(new FileKey(new File(jar)));
		}

		// 动态链接库
		String[] libs = XMLocal.getValues(element, "lib");
		for (String lib : libs) {
			if (!exists(lib)) {
				return null;
			}
			guide.addLibrary(new FileKey(new File(lib)));
		}
		return guide;
	}

	/**
	 * 读TASK单元
	 * @param family
	 * @return
	 */
	public 	CloudPackageElement readTask(int family) {
		String stage = PhaseTag.translate(family).toUpperCase();
		Element element = readElement(stage);
		if (element == null) {
			return null;
		}
		
		CloudPackageElement task = new CloudPackageElement(stage.toLowerCase());
		// 引导文件
		String boot = XMLocal.getValue(element, "boot");
		if (!exists(boot)) {
			return null;// 不存在返回空
		}
		task.setBoot(new FileKey(new File(boot)));

		// 附件
		String[] assists = XMLocal.getValues(element, "jar");
		for (String jar : assists) {
			if (!exists(jar)) {
				return null; // 不存在返回空
			}
			task.addAssist(new FileKey(new File(jar)));
		}

		// 动态链接库
		String[] libs = XMLocal.getValues(element, "lib");
		for (String lib : libs) {
			if (!exists(lib)) {
				return null;
			}
			task.addLibrary(new FileKey(new File(lib)));
		}
		return task;
	}

	public static void main(String[] args) {
		PackageScriptReader reader = null;
		try {
			File file = new File("D:/benchmark.script");
			reader = new PackageScriptReader(file);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		
		if (reader != null) {
			System.out.printf("conduct is:%s\n", reader.isConduct());
		}
	}

}