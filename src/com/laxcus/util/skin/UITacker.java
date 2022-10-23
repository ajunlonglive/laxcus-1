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

import com.laxcus.log.client.*;
import com.laxcus.util.charset.*;

/**
 * 图形窗口参数记录器。<br>
 * 记录图形窗口界面的参数，包括：<br>
 * 1. 位置 <br>
 * 2. 字体 <br>
 * 3. 字符串 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/9/2018
 * @since laxcus 1.0
 */
public class UITacker {

	/** 键名称 -> 实例对象 **/
	private TreeMap<String, Object> params = new TreeMap<String, Object>();

	/**
	 * 构造默认的图形窗口参数记录器
	 */
	public UITacker() {
		super();
	}
	
	/**
	 * 查找布尔值
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key) {
		Object value = get(key);
		if(value != null && value.getClass() == Boolean.class) {
			return (Boolean)value;
		}
		return null;
	}
	
	/**
	 * 查找整数值
	 * @param key
	 * @return
	 */
	public Integer getInteger(String key) {
		Object value = get(key);
		if(value != null && value.getClass() == Integer.class) {
			return (Integer)value;
		}
		return null;
	}
	
	/**
	 * 查找位置实例
	 * @param key 键名称
	 * @return 位置实例或者空指针
	 */
	public Rectangle getRectangle(String key) {
		Object value = get(key);
		if (value != null && value.getClass() == Rectangle.class) {
			return (Rectangle) value;
		}
		return null;
	}

	/**
	 * 查找尺寸实例
	 * @param key 键名称
	 * @return 尺寸实例或者空指针
	 */
	public Dimension getDimension(String key) {
		Object value = get(key);
		if (value != null && value.getClass() == Dimension.class) {
			return (Dimension) value;
		}
		return null;
	}
	
	/**
	 * 查找字体实例
	 * @param key 键名称
	 * @return 字体实例或者空指针
	 */
	public Font getFont(String key) {
		Object value = get(key);
		if (value != null && value.getClass() == Font.class) {
			return (Font) value;
		}
		return null;
	}

	/**
	 * 查找字符串实例
	 * @param key 键名称
	 * @return 字符串实例或者空指针
	 */
	public String getString(String key) {
		Object value = get(key);
		if (value != null && value.getClass() == String.class) {
			return (String) value;
		}
		return null;
	}

	/**
	 * 通过键读取值实例
	 * @param key 键名
	 * @return 返回值对象或者空指针
	 */
	public Object get(String key) {
		return params.get(key);
	}

	/**
	 * 写入键/值对象，不允许空指针
	 * @param key 键名
	 * @param value 值
	 */
	public void put(String key, Object value) {
		if (key != null && value != null) {
			params.put(key, value);
		}
	}

	/**
	 * 解析布尔值
	 * @param input 输入字符串
	 * @return 返回布尔值
	 */
	private Boolean splitBoolean(String input) {
		return new Boolean(input.matches("^\\s*(?i)(YES)\\s*$"));
	}

	/**
	 * 解析整数值
	 * @param input 输入字符串
	 * @return 返回整数，或者空指针
	 */
	private Integer splitInteger(String input) {
		if (!input.matches("^\\s*(?i)(\\d+)\\s*$")) {
			return null;
		}
		return new Integer(Integer.parseInt(input.trim(), 10));
	}

	/**
	 * 解析字体
	 * @param input 输入参数
	 * @return 返回字体实例
	 */
	private Font splitFont(String input) {
		final String regex = "^\\s*(.+?)\\,([0-9]+)\\,([0-9]+)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，忽略
		if (!matcher.matches()) {
			return null;
		}
		// 解析和返回字体实例
		String family = matcher.group(1);
		int style = Integer.parseInt(matcher.group(2));
		int size = Integer.parseInt(matcher.group(3));
		return new Font(family, style, size);
	}

	/**
	 * 取出范围参数
	 * @param input 输入参数
	 * @return 范围实例
	 */
	private Rectangle splitRectangle(String input) {
		final String regex = "^\\s*([0-9]+)\\,([0-9]+)\\,([0-9]+)\\,([0-9]+)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，忽略！
		if (!matcher.matches()) {
			return null;
		}

		// 解析和返回范围
		int x = Integer.parseInt(matcher.group(1));
		int y = Integer.parseInt(matcher.group(2));
		int height = Integer.parseInt(matcher.group(3));
		int width = Integer.parseInt(matcher.group(4));
		return new Rectangle(x, y, height, width);
	}

	/**
	 * 取出范围参数
	 * @param input 输入参数
	 * @return 范围实例
	 */
	private Dimension splitDimension(String input) {
		final String regex = "^\\s*([0-9]+)\\,([0-9]+)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，忽略！
		if (!matcher.matches()) {
			return null;
		}

		// 解析和返回范围
		int width = Integer.parseInt(matcher.group(1));
		int height = Integer.parseInt(matcher.group(2));
		return new Dimension(width, height);
	}
	
	/**
	 * 解析一行参数
	 * @param input 输入参数
	 */
	private void splitParam(String input) {
		final String regex = "^\\s*(?i)(.+?)\\[(.+?)\\]=(.+?)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，忽略！
		if (!matcher.matches()) {
			return;
		}

		String key = matcher.group(1);
		String className = matcher.group(2);
		String value = matcher.group(3);

		try {
			Class<?> clazz = Class.forName(className);
			if (clazz == Font.class) {
				Font font = splitFont(value);
				put(key, font);
			} else if (clazz == Rectangle.class) {
				Rectangle rect = splitRectangle(value);
				put(key, rect);
			} else if (clazz == Dimension.class) {
				Dimension d = splitDimension(value);
				put(key, d);
			} else if(clazz == String.class) {
				put(key, value);
			} else if (clazz == Boolean.class) {
				Boolean b = splitBoolean(value);
				if (b != null) put(key, b);
			} else if (clazz == Integer.class) {
				Integer b = splitInteger(value);
				if (b != null) put(key, b);
			}
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		}
	}

	/**
	 * 解析参数
	 * @param text 文本
	 * @throws IOException
	 */
	private void splitFull(String text) throws IOException {
		StringReader sr = new StringReader(text);
		BufferedReader reader = new BufferedReader(sr);

		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			} else {
				splitParam(line);
			}
		}
		reader.close();
		sr.close();
	}

	/**
	 * 从磁盘文件中读取配置信息
	 * @param file 磁盘文件实例
	 * @return 返回读取的单元数目，失败返回-1。
	 */
	public int read(File file) {
		// 如果不存在，或者0长度时，忽略
		if (!file.exists() || file.length() == 0) {
			return 0;
		}

		// 解析参数
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			// UTF8解码
			String text = new UTF8().decode(b);
			splitFull(text);
			// 返回单元数目
			return params.size();
		} catch (IOException e) {
			Logger.error(e);
		}

		return -1;
	}
	
	/**
	 * 写一个布尔值
	 * @param value
	 * @return 返回字符串
	 */
	private String writeBoolean(Boolean value) {
		return (value.booleanValue() ? "Yes" : "No");
	}

	/**
	 * 写一个整数
	 * @param value
	 * @return
	 */
	private String writeInteger(Integer value) {
		return value.toString();
	}
	
	/**
	 * 写入字体
	 * @param font 字符实例
	 * @return 返回字符串参数
	 */
	private String writeFont(Font font) {
		return String.format("%s,%d,%d", font.getName(), font.getStyle(), font.getSize());
	}

	/**
	 * 写入范围值
	 * @param rect 范围参数
	 * @return 返回字符串参数
	 */
	private String writeRectangle(Rectangle rect) {
		return String.format("%d,%d,%d,%d", rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * 写入尺寸值
	 * @param rect 范围参数
	 * @return 返回字符串参数
	 */
	private String writeDimension(Dimension d) {
		return String.format("%d,%d", d.width, d.height);
	}
	
	/**
	 * 合并参数
	 * @param key
	 * @param clazz
	 * @param value
	 * @return
	 */
	private String combin(String key, Class<?> clazz, String value) {
		return String.format("%s[%s]=%s\r\n", key, clazz.getName(),value);
	}

	/**
	 * 转换成字符串输出
	 * @return 字符串
	 */
	private String transfer() {
		StringBuilder buff = new StringBuilder();
		Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
		// 迭代处理
		while (iterator.hasNext()) {
			// 下一行
			Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			// 判断类型
			if (value.getClass() == Font.class) {
				String suffix = writeFont((Font) value);
				buff.append(combin(key, value.getClass(), suffix));
			} else if (value.getClass() == Rectangle.class) {
				String suffix = writeRectangle((Rectangle) value);
				buff.append(combin(key, value.getClass(), suffix));
			} else if(value.getClass() == Dimension.class) {
				String suffix = writeDimension((Dimension)value);
				buff.append(combin(key, value.getClass(), suffix));
			} else if (value.getClass() == String.class) {
				buff.append(combin(key, value.getClass(), (String) value));
			} else if (value.getClass() == Boolean.class) {
				String suffix = writeBoolean((Boolean) value);
				buff.append(combin(key, value.getClass(), suffix));
			} else if (value.getClass() == Integer.class) {
				String suffix = writeInteger((Integer) value);
				buff.append(combin(key, value.getClass(), suffix));
			}
		}
		// 输出字符串
		return buff.toString();
	}

	/**
	 * 返回写入的配置信息
	 * @param file 磁盘文件
	 * @return 返回写入的单元数目
	 */
	public int write(File file) {
		String text = transfer();
		if(text.isEmpty()) {
			return 0;
		}
		// 转换成UTF8编码
		byte[] b = new UTF8().encode(text);
		// 写入参数
		try {
			FileOutputStream writer = new FileOutputStream(file);
			writer.write(b);
			writer.close();
			// 返回写入单元数目
			return params.size();
		} catch (IOException e) {
			Logger.error(e);
		}
		return -1;
	}
	

	//	/**
	//	 * 读参数
	//	 * @param file
	//	 * @throws IOException
	//	 */
	//	private void readParams(File file) throws IOException {
	//		FileReader fr = new FileReader(file);
	//		BufferedReader reader = new BufferedReader(fr);
	//		while (true) {
	//			String line = reader.readLine();
	//			if (line == null) {
	//				break;
	//			} else {
	//				splitParam(line);
	//			}
	//		}
	//		reader.close();
	//		fr.close();
	//	}

	//	public String output() {
	//		Rectangle rect = new Rectangle(1, 23, 344, 777);
	//		put("rect", rect);
	//		Font font = new Font(Font.DIALOG, Font.PLAIN, 14);
	//		put("font", font);
	//		return gush();
	//	}

	//	public static void main(String[] args) {
	//		UITacker ti = new UITacker();
	//		String s = ti.output();
	//		System.out.println(s);
	//		
	//		String[] ss = s.split("\r\n");
	//		for(String item : ss) {
	//			System.out.println(item);
	//			ti.splitParam(item);
	//		}
	//	}

}