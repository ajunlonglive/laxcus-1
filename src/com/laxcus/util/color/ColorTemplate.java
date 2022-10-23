/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.color;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 颜色模板 <br>
 * 
 * 以静态文件的方式，导入到UIManager里面去
 * 
 * @author scott.liang
 * @version 1.0 3/82020
 * @since laxcus 1.0
 */
public class ColorTemplate {

	/** 判断是忽略标记，以"#"符号开始！ **/
	private static final String IGNORE = "^\\s*(#)(.+?)\\s*$";

	/** 三个10进制数字，以逗号分开, eg: TextField.background {255,255,255} **/
	private static final String REGEX1 = "^\\s*([\\w\\W]+?)\\s+(?:\\{)\\s*((\\d+)\\s*\\,\\s*(\\d+)\\s*\\,\\s*(\\d+))\\s*(?:\\})\\s*$";

	/** 一个16进制数字，以"0x"或者"#"做为前缀, eg: TextField.background {#FFFFFF} **/
	private static final String REGEX2 = "^\\s*([\\w\\W]+?)\\s+(?:\\{)\\s*(?i)(?:0X|#)\\s*([0-9a-fA-F]{1,8})\\s*(?:\\})\\s*$";

	/** 存储队列 **/
	private static TreeMap<String, Color> elements = new TreeMap<String, Color>();

	/**
	 * 返回颜色统计值
	 * @return 数值
	 */
	public static int count() {
		return elements.size();
	}

	/**
	 * 保存参数
	 * @param name 名称
	 * @param color 颜色值
	 * @return 返回旧值
	 */
	private static Color put(String name, Color color) {
		// 改成小写保存颜色
		return elements.put(name.trim().toLowerCase(), color);
	}

	/**
	 * 返回查找的颜色值
	 * @param name 名称
	 * @return 颜色值，或者空指针
	 */
	public static Color get(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}

		// 返回适配的颜色值
		return elements.get(name.trim().toLowerCase());
	}

	/**
	 * 查找适配的颜色值
	 * @param name 关键字
	 * @return 返回对应的颜色值
	 */
	public static Color findColor(String name) {
		if (name == null) {
			return null;
		}
		return ColorTemplate.get(name);
	}

	/**
	 * 查找颜色
	 * @param name
	 * @param defaultColor
	 * @return
	 */
	public static Color findColor(String name, Color defaultColor) {
		if (name == null || name.isEmpty()) {
			return defaultColor;
		}
		Color color = ColorTemplate.get(name);
		// 没有找到，返回默认值
		if (color == null) {
			return defaultColor;
		}
		return color;
	}

	/**
	 * 解析一行参数
	 * @param input 输出参数
	 * @return 成功返回真，否则假
	 */
	private static boolean resolve(String input) {
		if (input == null || input.isEmpty()) {
			return false;
		}

		// 判断是带"#"符号的忽略值
		Pattern pattern = Pattern.compile(ColorTemplate.IGNORE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (matcher.matches()) {
			return false;
		}

		//1. 标准的正则表达式
		pattern = Pattern.compile(ColorTemplate.REGEX1);
		matcher = pattern.matcher(input);
		// 匹配，解析它!
		if (matcher.matches()) {
			// 颜色标记值，改成小写字符表示
			String key = matcher.group(1).toLowerCase();
			// 颜色值
			int red = Integer.parseInt(matcher.group(3));
			int green = Integer.parseInt(matcher.group(4));
			int blue = Integer.parseInt(matcher.group(5));
			Color color = new Color(red, green, blue);
			// 导入队列中
			ColorTemplate.put(key, color);
			return true;
		}

		//2. 十六进制表达式
		pattern = Pattern.compile(ColorTemplate.REGEX2);
		matcher = pattern.matcher(input);
		// 匹配，解析它
		if (matcher.matches()) {
			// 颜色标记值，转与小写字符表示
			String key = matcher.group(1).toLowerCase();
			// 颜色值
			int rgb = Integer.parseInt(matcher.group(2), 16);
			Color color = new Color(rgb);
			// 导入队列中
			ColorTemplate.put(key, color);
			return true;
		}

		// 不成功！
		return false;
	}

	/**
	 * 从JAR文件中读取一个配置文件
	 * @return 返回字节数组
	 */
	private static byte[] findAbsoluteStream(String path) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream in = loader.getResourceAsStream(path);
		if(in == null) {
			return null;
		}

		byte[] b = new byte[1024];
		ClassWriter buff = new ClassWriter(1024);
		try {
			while (true) {
				int len = in.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				buff.write(b, 0, len);
			}
			in.close();
		} catch (IOException e) {
			return null;
		}

		if (buff.size() == 0) {
			return new byte[0];
		}
		return buff.effuse();
	}

	/**
	 * 加载颜色模板
	 * @param xmlPath JAR包中的文件路径
	 * @return 返回导入的颜色数目
	 */
	public static int load(String xmlPath) {
		byte[] b = findAbsoluteStream(xmlPath);
		if (Laxkit.isEmpty(b)) {
			return 0;
		}

		// 转成UTF8编码，解析颜色值，导入到内存里
		int count = 0;
		try {
			String str = new String(b, "UTF-8");
			StringReader is = new StringReader(str);
			BufferedReader reader = new BufferedReader(is);
			do {
				// 读一行记录
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				// 解析它
				boolean success = resolve(line);
				if (success) {
					count++;
				}
			} while (true);
			// 关闭！
			reader.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

}