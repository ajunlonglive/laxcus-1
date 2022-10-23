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
import java.util.regex.*;

import org.w3c.dom.*;

import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.*;

/**
 * "GUIDE-INF/guides.xml" 
 * "TASK-INF/tasks.xml"
 * 配置文档读取器。<br>
 * 
 * 解析里面的参数，以类形式返回
 * 
 * @author scott.liang
 * @version 1.0 7/28/2020
 * @since laxcus 1.0
 */
public class PackageConfigReader {

	private static final String PRODUCT_DATE = "^\\s*(20[0-9]{2})[\\.\\-]([0-9]{1,2})[\\.\\-]([0-9]{1,2})\\s*$";

	static final String WARE_NAMING = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]{1,16}?)\\s*$";

	/** 数据内容，以字节形式保存 **/
	protected byte[] content;

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
	 * 构造配置文档读取器，指定内容
	 * @param b 字节内容
	 * @param off 下标位置
	 * @param len 指定度
	 */
	protected PackageConfigReader(byte[] b , int off, int len) {
		super();
		setContent(b, off, len);
	}

	/**
	 * 构造配置文档读取器，指定内容
	 * @param b 字节内容
	 */
	protected PackageConfigReader(byte[] b) {
		super();
		setContent(b);
	}

	/**
	 * 构造配置文件读取器，读取磁盘文件内容
	 * @param file 文件实例
	 * @throws IOException
	 */
	protected PackageConfigReader(File file) throws IOException {
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
	 * 返回产品日期
	 * @param input 输入语句
	 * @return 返回整数值，无效是0
	 */
	private int splitProductDate(String input) {
		Pattern pattern = Pattern.compile(PRODUCT_DATE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回0
		if (!matcher.matches()) {
			return 0;
		}
		
		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int day = Integer.parseInt(matcher.group(3));
		// 不能低于2020年
		if (year < 2020) {
			return 0;
		}
		// 检查日期参数，无效返回0
		if (!DateChecker.check(year, month, day)) {
			return 0;
		}
		// 格式化整数值
		return SimpleDate.format(year, month, day);
	}
	
	/**
	 * 解析版本标签 
	 * @return 返回有效标识
	 */
	public WareTag readWareTag() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			Logger.error(this, "readWareTag", "cannot load resource!");
			return null;
		}

		// 找到"ware"关键字
		NodeList nodes = document.getElementsByTagName(WareMark.WARE); // "ware");
		if (nodes.getLength() != 1) {
			Logger.error(this, "readWareTag", "cannot be find \"ware\"!");
			return null;
		}

		Element element = (Element) nodes.item(0);

		// 软件命名，在系统内部为分布计算使用
		String naming = XMLocal.getValue(element, WareMark.WARE_NAMING); // "naming"); 

		// 检查语法合规，软件命名在1-16个字符之间，支持各种语言
		if (naming.trim().isEmpty()) {
			throw new IllegalValueException("empty ware naming");
		}
		if (!naming.matches(PackageConfigReader.WARE_NAMING)) {
			throw new IllegalValueException("illegal ware naming: %s", naming);
		}

		// 版本号
		String version = XMLocal.getValue(element, WareMark.WARE_VERSION); // "version"); 

		// 软件产品名称，展示给使用者
		String productName = XMLocal.getValue(element, WareMark.WARE_PRODUCT_NAME); // "product-name"); 
		// 软件产品生产日期
		String productDate = XMLocal.getValue(element, WareMark.WARE_PRODUCT_DATE); // "product-date");
		// 生产者
		String maker = XMLocal.getValue(element, WareMark.WARE_MAKER); // "maker"); 
		// 介绍
		String comment = XMLocal.getValue(element, WareMark.WARE_COMMENT); // "comment");
		
//		// 自有!
//		String selfly = XMLocal.getValue(element, WareMark.WARE_SELFLY); // "selfly"); 

		// 如果版本号出错!
		if (!WareVersion.validate(version)) {
			Logger.error(this, "readWareTag", "illegal version %s", version);
			return null;
		}
		WareVersion v = new WareVersion(version);
		WareTag tag = new WareTag(new Naming(naming), v);
		tag.setProductName(productName);
		tag.setProductDate(splitProductDate(productDate));
		tag.setMaker(maker);
		tag.setComment(comment);
//		tag.setSelfly(ConfigParser.splitBoolean(selfly, false));

		return tag;
	}

	/**
	 * 解析版本标签 
	 * @return 返回有效标识
	 */
	public WareToken readWareToken() {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			Logger.error(this, "readWareTag", "cannot load resource!");
			return null;
		}

		// 找到"ware"关键字
		NodeList nodes = document.getElementsByTagName(WareMark.WARE); // "ware");
		if (nodes.getLength() != 1) {
			Logger.error(this, "readWareTag", "cannot be find \"ware\"!");
			return null;
		}

		Element element = (Element) nodes.item(0);

		// 软件命名，在系统内部为分布计算使用
		String naming = XMLocal.getValue(element, WareMark.WARE_NAMING); // "naming"); 

		// 检查语法合规，软件命名在1-16个字符之间，支持各种语言
		if (naming.trim().isEmpty()) {
			throw new IllegalValueException("empty ware naming");
		}
		if (!naming.matches(PackageConfigReader.WARE_NAMING)) {
			throw new IllegalValueException("illegal ware naming: %s", naming);
		}

		// 版本号
		String version = XMLocal.getValue(element, WareMark.WARE_VERSION); // "version"); 
		// 软件产品名称，展示给使用者
		String productName = XMLocal.getValue(element, WareMark.WARE_PRODUCT_NAME); // "product-name"); 
		// 软件产品生产日期
		String productDate = XMLocal.getValue(element, WareMark.WARE_PRODUCT_DATE); // "product-date");
		// 生产者
		String maker = XMLocal.getValue(element, WareMark.WARE_MAKER); // "maker"); 
		// 介绍
		String comment = XMLocal.getValue(element, WareMark.WARE_COMMENT); // "comment");
//		// 自有!
//		String selfly = XMLocal.getValue(element, WareMark.WARE_SELFLY); // "selfly"); 
		
		WareToken token = new WareToken(naming);
		token.setVersion(version);
		token.setProductName(productName);
		token.setProductDate(productDate);
		token.setMaker(maker);
		token.setComment(comment);

//		token.setSelfly(ConfigParser.splitBoolean(selfly, false));
		return token;
	}

}