/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;

import com.laxcus.util.charset.*;
import com.laxcus.util.color.*;
import com.laxcus.util.loader.*;

/**
 * UI参数解析器
 * 
 * @author scott.liang
 * @version 1.0 7/1/2021
 * @since laxcus 1.0
 */
public class UISplitter {

	/** 忽略字符 **/
	private final static String ignore = "#";

	/** 正则表达式 **/
	private final static String REGEX ="^\\s*([\\w\\W]+?)\\s+([\\w\\W]+)\\s*$";
	
	/** 图标正则表达式 **/
	private final static String ICON_REGEX = "^\\s*(?:\\[)\\s*(?i)(?:ICON)\\s*(?:\\])\\s*([\\w\\W]+)\\s*$";
	
	private final static String ICON_WH_REGEX = "^\\s*(?:\\[)\\s*(?i)(?:ICON)\\s*(\\d+)\\s*\\*\\s*(\\d+)\\s*(?:\\])\\s*([\\w\\W]+)\\s*$";

	/** 颜色正则表达式 **/
	private final static String COLOR_REGEX = "^\\s*(?i)(?:\\[\\s*COLOR\\s*\\])\\s*([\\w\\W]+)\\s*$";
	
	/** 带ESL前缀的表达式 **/
	private static final String ESL_REGEX = "^\\s*(?i)(?:ESL\\s*\\:\\s*\\{)\\s*([0-9]+[\\.0-9]*)\\s*\\,\\s*([0-9]+[\\.0-9]*)\\s*\\,\\s*([0-9]+[\\.0-9]*)\\s*(?:\\})\\s*$";
	
	/** 三个10进制数字，以逗号分开, eg: TextField.background {255,255,255} **/
	private static final String REGEX1 = "^\\s*(?:\\{)\\s*(\\d+)\\s*\\,\\s*(\\d+)\\s*\\,\\s*(\\d+)\\s*(?:\\})\\s*$";
	
	/** 一个16进制数字，以"0x"或者"#"做为前缀, eg: TextField.background {#FFFFFF} **/
	private static final String REGEX2 = "^\\s*(?:\\{)\\s*(?i)(?:0X|#)\\s*([0-9a-fA-F]{1,8})\\s*(?:\\})\\s*$";

	/** 重定向的颜色，eg: Viewport:background { " TextField.background " } **/
	private static final String REGEX3 = "^\\s*(?:\\{\\s*\\\")\\s*(.+)\\s*(?:\\\"\\s*\\})\\s*$";
	
	/** 保存的文本、图标，大小写敏感 **/
	private TreeMap<String, Object> objects = new TreeMap<String, Object>();

	/**
	 * 构造文本解析器
	 */
	public UISplitter() {
		super();
	}

	/**
	 * 返回全部KEY值
	 * @return
	 */
	public java.util.List<String> getKeys() {
		return new ArrayList<String>(objects.keySet());
	}

	/**
	 * 查找匹配的VALUE
	 * @param key
	 * @return
	 */
	public Object find(String key) {
		return objects.get(key);
	}
	
	/**
	 * 判断有BOM且一致
	 * 
	 * @param file 磁盘文件
	 * @param bom BOM符号
	 * @return 返回真或者假
	 */
	private boolean matchs(byte[] bytes, byte[] bom)  {
		for (int i = 0; i < bom.length; i++) {
			if (bytes[i] != bom[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是错误码
	 * 
	 * @param c
	 *            字符
	 * @return 返回真或者假
	 */
	private final boolean isFaultCode(char c) {
		return ((int) c) == 0xfffd;
	}

	/**
	 * 判断是乱码。<br>
	 * <br>
	 * 
	 * 两个判断，任何一个成立就是乱码。<br>
	 * 1. 是规定的错误码 <br>
	 * 2. 不是UNICODE规定的编码<br>
	 * <br>
	 * 
	 * @param c
	 *            字符
	 * @return 返回真或者假
	 */
	private final boolean isMessy(char c) {
		// 是错误码
		if (isFaultCode(c)) {
			return true;
		}
		// 不在UNICODE定义
		return !Character.isDefined(c);
	}

	/**
	 * 判断一行中有乱码
	 * @param input
	 * @return
	 */
	private final boolean isMessy(String input) {
		int len = input.length();
		for (int i = 0; i < len; i++) {
			char c = input.charAt(i);
			if (isMessy(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 加载图标
	 * @param key
	 * @param xmlPath
	 * @param w
	 * @param h
	 */
	private void loadIcon(String key, String xmlPath, int w, int h) {
		ResourceLoader loader = new ResourceLoader();
		if (w > 0 && h > 0) {
			ImageIcon icon = loader.findImage(xmlPath, w, h);
			if (icon != null) {
				objects.put(key, icon);
			}
		} else {
			ImageIcon icon = loader.findImage(xmlPath);
			if (icon != null) {
				objects.put(key, icon);
			}
		}
	}
	
	/**
	 * 判断和加载图标实例
	 * @param key KEY
	 * @param input 配置参数
	 * @return 成功返回真，否则假
	 */
	private boolean loadIcon(String key, String input) {
		// 图标，带宽和高
		Pattern pattern = Pattern.compile(ICON_WH_REGEX);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			int w = Integer.parseInt(matcher.group(1));
			int h = Integer.parseInt(matcher.group(2));
			String path = matcher.group(3);
			loadIcon(key, path, w, h);
			return true;
		}

		// 图标，不带宽和高
		pattern = Pattern.compile(ICON_REGEX);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String path = matcher.group(1);
			loadIcon(key, path, 0, 0);
			return true;
		}
		// 以上不成功，返回真
		return false;
	}
	
	/**
	 * 解析颜色
	 * @param value
	 * @return
	 */
	private Color splitColor(String input) {
		// 1. ESL正则表达式
		Pattern pattern = Pattern.compile(ESL_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 匹配，解析它!
		if (matcher.matches()) {
			// HSL颜色值
			double H = Double.parseDouble(matcher.group(1));
			double S = Double.parseDouble(matcher.group(2));
			double L = Double.parseDouble(matcher.group(3));
			ESL esl = new ESL(H, S, L);
			return esl.toColor();
		}

		// 2. 标准的正则表达式
		pattern = Pattern.compile(REGEX1);
		matcher = pattern.matcher(input);
		// 匹配，解析它!
		if (matcher.matches()) {
			// 颜色值
			int red = Integer.parseInt(matcher.group(2));
			int green = Integer.parseInt(matcher.group(2));
			int blue = Integer.parseInt(matcher.group(3));
			return new Color(red, green, blue);
		}

		// 3. 十六进制表达式
		pattern = Pattern.compile(REGEX2);
		matcher = pattern.matcher(input);
		// 匹配，解析它
		if (matcher.matches()) {
			// 颜色值
			int rgb = Integer.parseInt(matcher.group(1), 16);
			return new Color(rgb);
		}
		
		// 4. 指向其他颜色
		pattern = Pattern.compile(REGEX3);
		matcher = pattern.matcher(input);
		// 匹配，解析它
		if (matcher.matches()) {
			// 代理值
			String agentValue = matcher.group(1);
			// 找到代理颜色
			Color color = UIManager.getDefaults().getColor(agentValue);
			// 如果没有，查找颜色面板中的自定义值
			if (color == null) {
				color = ColorTemplate.findColor(agentValue);
			}
			
			// 返回实例
			if (color != null) {
				return new Color(color.getRGB());
			} else {
				return null;
			}
		}
		
		return null;
	}

	/**
	 * 解析和加载颜色
	 * @param key
	 * @param input
	 * @return
	 */
	private boolean loadColor(String key, String input) {
		// 颜色
		Pattern pattern = Pattern.compile(COLOR_REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

		// 解析颜色
		String value = matcher.group(1);
		Color color = splitColor(value);
		boolean success = (color != null);
		if (success) {
			objects.put(key.trim(), color);
		}
		// 解析参数
		return success;
	}
	
	/**
	 * 加载和解析保存在配置中的文本内容
	 * @param xmlPath 文件在JAR包中的路径
	 * @throws IOException
	 */
	public void load(String xmlPath) throws IOException {
		ResourceLoader loader = new ResourceLoader();
		byte[] b = loader.findAbsoluteStream(xmlPath);
		// 如果没有，返回空指针
		if (b == null) {
			return;
		}

		final String charset = "UTF-8";
		int skip = 0;
		// 取出BOM字节
		byte[] bom = BOM.find(charset);
		if (bom != null) {
			boolean success = (matchs(b, bom));
			if (success) {
				skip = bom.length;
			}
		}
		
		// 打开字节流
		ByteArrayInputStream in = new ByteArrayInputStream(b ,skip, b.length - skip);
		InputStreamReader is = new InputStreamReader(in, charset);
		// 放进缓冲
		BufferedReader reader = new BufferedReader(is);

		// 正则表达式
		Pattern pattern = Pattern.compile(REGEX);

		// 读取全部信息
		do {
			// 读一行文本
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			// 有乱码，忽略它
			if (isMessy(line)) {
				continue;
			}

			// 读一行
			line = line.trim();
			// 空行忽略
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith(ignore)) {
				continue;
			}
			
			Matcher matcher = pattern.matcher(line);
			// 不匹配忽略它
			if (!matcher.matches()) {
				continue;
			}
			// 保存参数
			String key = matcher.group(1);
			String value = matcher.group(2);
			// 加载图标
			boolean success = loadIcon(key, value);
			// 颜色
			if (!success) {
				success = loadColor(key, value);
			}
			// 不成立，保存字符串
			if (!success) {
				objects.put(key.trim(), value.trim());
			}
		} while (true);

		reader.close();
		is.close();
		in.close();
	}
}
