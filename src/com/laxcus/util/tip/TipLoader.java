/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.loader.*;
import com.laxcus.xml.*;

/**
 * 提示文本加载器 <br>
 * 
 * 从磁盘/JAR文件中加载提示内容，它们被用户根据编号调用，输出格式化的多语言文本内容。
 * 
 * @author scott.liang
 * @version 1.0 11/25/2013
 * @since laxcus 1.0
 */
public class TipLoader {

	/** 提示编号 -> 文本数据 **/
	private Map<java.lang.Integer, String> tokens = new TreeMap<java.lang.Integer, String>();

	/**
	 * 构造默认的提示文本加载器
	 */
	public TipLoader() {
		super();
	}

	/**
	 * 统计提示数目
	 * @return 提示数目
	 */
	public int size() {
		return tokens.size();
	}

	/**
	 * 判断一个提示编号对应的多语言文本数据存在
	 * @param no 提示编号
	 * @return 返回真或者假
	 */
	public boolean contains(int no) {
		return tokens.containsKey(no);
	}

	/**
	 * 依据提示编号，找到对应的多语言文本，输出格式化的内容
	 * 
	 * @param no 提示编号
	 * @param params 参数
	 * @return 返回字符串
	 */
	public String format(int no, Object... params) {
		String value = tokens.get(no);
		if (value == null) {
			throw new IllegalValueException("cannot be find \"%s\"", no);
		}
		return String.format(value, params);
	}

	/**
	 * 根据提示编号，输出多种语言的文本内容
	 * @param no 提示编号
	 * @return 返回字符串
	 */
	public String format(int no) {
		String value = tokens.get(no);
		if (value == null) {
			throw new IllegalValueException("cannot be find \"%s\"", no);
		}
		return value;
	}

	/**
	 * 判断是ELEMENT属性
	 * @param node NODE属性
	 * @return 返回真或者假
	 */
	private boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * 过滤两侧空格
	 * @param text 字符串
	 * @return 返回过滤后的字符串
	 */
	private String trim(String text) {
		Pattern pattern = Pattern.compile("^\\s*(.*?)\\s*$");
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return "";
	}

	/**
	 * 加载和解析XML文档
	 * @param b 字节数组
	 */
	public void loadXML(byte[] b) throws IOException {
		// 逐段解析，XML路径做为KEY，保存在内容
		Document document = XMLocal.loadXMLSource(b);
		if (document == null) {
			return;
		}

		final String regex = "^\\s*([\\-]{0,1}\\d+)\\s*$";
		
		Element element = document.getDocumentElement();
		NodeList list = element.getChildNodes();
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Node node = list.item(i);
			// 判断是ELEMENT
			if (!isElement(node)) {
				continue;
			}

			// 取出参数
			Element sub = (Element) node;
			// 找到“no”属性值，必须是10进制数字（正数、零、负数）
			String key = sub.getAttribute("no");
			// 正则表达式
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(key);
			// 不匹配，返回默认值
			if (!matcher.matches()) {
				throw new IOException("illegal attribute " + key);
			}
			int no = Integer.parseInt(matcher.group(1));
			
			// 过滤两侧空格，这是无意义的字符
			String value = trim(sub.getTextContent());

			// 判断存在重复的参数
			if (tokens.containsKey(no)) {
				throw new IOException("duplicate " + key);
			}

			// 保存配置参数
			tokens.put(no, value);

//						System.out.printf("[%s,%d] - [%s]\n", key, no, value);
		}
	}

	/**
	 * 从JAR文件中加载
	 * @param jarpath JAR文件路径
	 * @throws IOException
	 */
	public void loadXMLFromJar(String jarpath) throws IOException {
		ResourceLoader loader = new ResourceLoader();
		byte[] b = loader.findAbsoluteStream(jarpath);
		loadXML(b);
	}

	/**
	 * 加载和解析文件中的提示参数
	 * @param filename 文件名
	 * @throws IOException
	 */
	public void load(String filename) throws IOException {
		load(new File(filename));
	}

	/**
	 * 解析磁盘文件中的参数
	 * @param file 文件实例
	 * @throws IOException
	 */
	public void load(File file) throws IOException {
		if(!(file.exists() && file.isFile())) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		FileInputStream in = new FileInputStream(file);
		load(in);
		in.close();
	}

	/**
	 * 解析输入读的参数
	 * @param in 输入读
	 * @throws IOException
	 */
	public void load(InputStream in) throws IOException {
		byte[] b = new byte[1024];
		ClassWriter buff = new ClassWriter(1024);

		// 读文本内容
		while (true) {
			int len = in.read(b, 0, b.length);
			if (len == -1) {
				break;
			}
			buff.write(b, 0, len);
		}
		in.close();

		if (buff.size() == 0) {
			throw new NullPointerException();
		}

		loadXML(buff.effuse());
	}

	//	private void load() {
	//		String filename = "D:/lexst/conf/front/resources/messages_zh_CN.xml";
	//		
	//		try {
	//			load(filename);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	//
	//	public static void main(String[] args) {
	//		TipLoader e = new TipLoader();
	//		e.load();
	//		System.out.printf("size is:%d\n", e.size());
	//		
	//		String s = e.format(10000, "I KAI");
	//		System.out.println(s);
	//		
	//		s = e.format(1);
	//		System.out.println(s);
	//	}
}