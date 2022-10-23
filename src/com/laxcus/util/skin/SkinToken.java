/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.io.*;
import java.awt.Color;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.plaf.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.color.*;

/**
 * 皮肤颜色配置标记
 * 
 * @author scott.liang
 * @version 1.0 2/17/2020
 * @since laxcus 1.0
 */
public class SkinToken implements Comparable<SkinToken> {
	
	/** 带ESL前缀的表达式 **/
	private static final String ESL_REGEX = "^\\s*(.+?)\\s+(?i)(?:ESL\\s*\\:\\s*\\{)\\s*([0-9]+[\\.0-9]*)\\s*\\,\\s*([0-9]+[\\.0-9]*)\\s*\\,\\s*([0-9]+[\\.0-9]*)\\s*(?:\\})\\s*$";
	
	/** 三个10进制数字，以逗号分开, eg: TextField.background {255,255,255} **/
	private static final String REGEX1 = "^\\s*(.+?)\\s+(?:\\{)\\s*((\\d+)\\s*\\,\\s*(\\d+)\\s*\\,\\s*(\\d+))\\s*(?:\\})\\s*$";
	
	/** 一个16进制数字，以"0x"或者"#"做为前缀, eg: TextField.background {#FFFFFF} **/
	private static final String REGEX2 = "^\\s*(.+?)\\s+(?:\\{)\\s*(?i)(?:0X|#)\\s*([0-9a-fA-F]{1,8})\\s*(?:\\})\\s*$";

	/** 重定向的颜色，eg: Viewport:background { " TextField.background " } **/
	private static final String REGEX3 = "^\\s*(.+?)\\s+(?:\\{\\s*\\\")\\s*(.+)\\s*(?:\\\"\\s*\\})\\s*$";
	
	/** 判断是忽略标记 **/
	private static final String IGNORE = "^\\s*(#)(.+?)\\s*$";
	
	/** 标题，针对不同的语言 **/
	private String title;
	
	/** 名字，具有唯一性，写入配置文件 watch.conf / terminal.conf  **/
	private String name;
	
	/** 图标在JAR中的路径 **/
	private String icon;
	
	/** 效果图在JAR中的路径 **/
	private String impress; // example;
	
	/** 方法名称，在WatchWindow / TerminalWindow 中有要有对应的定义，具有唯一性 **/
	private String method;
	
	/** 配置文件链 **/
	private String link;
	
	/** 外观，关键字，与系统的定义保持一致 **/
	private String lookAndFeel;
	
	/** 主题引导类 **/
	private String themeClass;
	
	/** 被选中 **/
	private boolean checked;

	/**
	 * 构造默认的皮肤颜色配置标记
	 */
	public SkinToken() {
		super();
		checked = false;
	}
	
	/**
	 * 构造皮肤颜色配置标记，指定参数
	 * @param title 标题，匹配环境语言
	 * @param name 名称，唯一值
	 * @param method 调用方法名称
	 * 
	 * @param lookAndFeel 感知外观
	 * @param themeClazz 主题引导类
	 * 
	 * @param link 文件jar包中的链路
	 */
	public SkinToken(String title, String name, String method,  String lookAndFeel, String themeClazz, String link) {
		this();
		setTitle(title);
		setName(name);
		setMethod(method);
		
		setLookAndFeel(lookAndFeel);
		setThemeClass(themeClazz);
		
		setLink(link);
	}

	/**
	 * 设置标题
	 * @param e 标题
	 */
	public void setTitle(String e) {
		Laxkit.nullabled(e);
		title = e.trim();
	}

	/**
	 * 返回标题
	 * @return 标题
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 设置名字
	 * @param e 名字
	 */
	public void setName(String e) {
		Laxkit.nullabled(e);
		name = e.trim();
	}

	/**
	 * 返回名字
	 * @return 名字
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 设置图标在JAR的绝对路径。注意，是绝对路径。
	 * @param link 字符串
	 */
	public void setIcon(String link) {
		icon = link;
	}

	/**
	 * 返回图标在JAR的绝对路径
	 * @return 字符串
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * 设置impress在JAR的绝对路径。注意，是绝对路径。
	 * @param link 字符串
	 */
	public void setImpress(String link) {
		impress = link;
	}

	/**
	 * 返回impress在JAR的绝对路径
	 * @return 字符串
	 */
	public String getImpress() {
		return impress;
	}
	
	/**
	 * 设置方法名称
	 * @param e 方法名称
	 */
	public void setMethod(String e) {
		Laxkit.nullabled(e);
		method = e.trim();
	}

	/**
	 * 返回方法名称
	 * @return 方法名称
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * 设置配置文件链
	 * @param e 配置文件链
	 */
	public void setLink(String e) {
		Laxkit.nullabled(e);
		link = e.trim();
	}

	/**
	 * 返回配置文件链
	 * @return 配置文件链
	 */
	public String getLink() {
		return link;
	}

	/**
	 * 设置感知外观。关键定，与系统保持一致！
	 * @param e 字符串
	 */
	public void setLookAndFeel(String e) {
		Laxkit.nullabled(e);
		lookAndFeel = e.trim();
	}

	/**
	 * 返回感知外观
	 * @return 字符串
	 */
	public String getLookAndFeel() {
		return lookAndFeel;
	}
	
	/**
	 * 判断是NIMBUS界面
	 * @return
	 */
	public boolean isNimbus() {
		return lookAndFeel != null && lookAndFeel.equalsIgnoreCase("Nimbus");
	}
	
	/**
	 * 判断是METAL界面
	 * @return
	 */
	public boolean isMetal() {
		return lookAndFeel != null && lookAndFeel.equalsIgnoreCase("Metal");
	}

	/**
	 * 设置界面主题引导类，允许空指针
	 * @param e 界面主题引导类
	 */
	public void setThemeClass(String e) {
		if (e != null && e.trim().length() > 0) {
			themeClass = e.trim();
		}
	}

	/**
	 * 返回界面主题引导类
	 * @return 界面主题引导类
	 */
	public String getThemeClass() {
		return themeClass;
	}

	/**
	 * 设置被选中
	 * @param b
	 */
	public void setChecked(boolean b) {
		checked = b;
	}

	/**
	 * 判断被选中
	 * @return 返回真或者假
	 */
	public boolean isChecked() {
		return checked;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((SkinToken) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s#%s#%s", title, name, link, lookAndFeel);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SkinToken that) {
		if (that == null) {
			return -1;
		}
		return name.compareToIgnoreCase(that.name);
	}

	/**
	 * 从JAR文件中读取一个配置文件
	 * @return 返回字节数组
	 */
	private byte[] findAbsoluteStream(String path) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream in = loader.getResourceAsStream(path);
		if (in == null) {
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
	 * 解析一行参数
	 * @param input 输出参数
	 * @return 成功返回真，否则假
	 */
	private boolean resolve(String input) {
		// 空行忽略
		if (input == null || input.trim().isEmpty()) {
			return false;
		}
		// 判断是带"#"符号的忽略值
		Pattern pattern = Pattern.compile(SkinToken.IGNORE);
		Matcher matcher = pattern.matcher(input);
		// 匹配是忽略
		if (matcher.matches()) {
			return false;
		}
		
		// 1. ESL正则表达式
		pattern = Pattern.compile(SkinToken.ESL_REGEX);
		matcher = pattern.matcher(input);
		// 匹配，解析它!
		if (matcher.matches()) {
			// 颜色标记值
			String key = matcher.group(1).trim();
			// HSL颜色值
			double H = Double.parseDouble(matcher.group(2));
			double S = Double.parseDouble(matcher.group(3));
			double L = Double.parseDouble(matcher.group(4));
			ESL esl = new ESL(H, S, L);
			RGB rgb = ESLConverter.convert(esl);
			// 开发者默认值
			UITools.putProperity(key, rgb.toColorUIResource());
			return true;
		}

		// 2. 标准的正则表达式
		pattern = Pattern.compile(SkinToken.REGEX1);
		matcher = pattern.matcher(input);
		// 匹配，解析它!
		if (matcher.matches()) {
			// 颜色标记值
			String key = matcher.group(1).trim();
			// 颜色值
			int red = Integer.parseInt(matcher.group(3));
			int green = Integer.parseInt(matcher.group(4));
			int blue = Integer.parseInt(matcher.group(5));
			ColorUIResource color = new ColorUIResource(red, green, blue);
			// 关键一行!!!：颜色值导入到系统的默认外观集合内存！
			// 开发者默认值
			UITools.putProperity(key, color);
			return true;
		}

		// 3. 十六进制表达式
		pattern = Pattern.compile(SkinToken.REGEX2);
		matcher = pattern.matcher(input);
		// 匹配，解析它
		if (matcher.matches()) {
			// 颜色标记值
			String key = matcher.group(1).trim();
			// 颜色值
			int rgb = Integer.parseInt(matcher.group(2), 16);
			ColorUIResource color = new ColorUIResource(rgb);
			// 关键一行!!!：颜色值导入到系统的默认外观集合内存！
			UITools.putProperity(key, color);
			return true;
		}
		
		// 4. 指向其他颜色
		pattern = Pattern.compile(SkinToken.REGEX3);
		matcher = pattern.matcher(input);
		// 匹配，解析它
		if (matcher.matches()) {
			// 颜色标记值
			String key = matcher.group(1).trim();
			// 代理值
			String agentValue = matcher.group(2);
			// 找到代理颜色
			Color color = UIManager.getDefaults().getColor(agentValue);
			// 如果没有，查找颜色面板中的自定义值
			if (color == null) {
				Color sub = ColorTemplate.findColor(agentValue);
				if (sub != null) {
					color = new ColorUIResource(sub);
				}
			}

			if (color != null) {
				// 关键一行!!!：颜色值导入到系统的默认外观集合内存！
				UITools.putProperity(key, color);
				return true;
			}
		}
		
		// 不成功！
		return false;
	}

	/**
	 * 根据默认的JAR文件路径，解析JAR包里的皮肤颜色配置参数，把颜色值导入内存
	 * @return 返回成功导入的行数
	 */
	public int loadColors() {
		return loadColors(link);
	}

	/**
	 * 解析JAR包里的皮肤颜色配置参数，把颜色值导入内存
	 * @param xmlPath JAR文件XML路径
	 * @return 返回成功导入的行数
	 */
	public int loadColors(String xmlPath) {
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
	
	/**
	 * 更新界面主题
	 * @param updateMetalUI 更新成自定义的METAL组件UI
	 * 
	 * @return 成功返回真，否则假
	 */
	public boolean updateTheme(boolean updateMetalUI) {
		SkinSheet sheet = null;
		// 如果是METAL界面，并且外部要求更新组件UI时
		if (isMetal() && updateMetalUI) {
			sheet = new FlatSkinSheet();
		}

		// 1. 先加载一下界面颜色参数（这样做的原因是给第二步做准备。防止头次加载之前没有这些参数。）
		loadColors();
		// 2. 更新界面主题，更新过程中，系统会把相关系统颜色、图标、字体等参数进行调整！
		boolean success = UITools.updateLookAndFeel(lookAndFeel, themeClass, sheet);
		// 3. 再次加载界面颜色参数，导入所需要的皮肤颜色值
		if (success) {
			int count = loadColors();
			success = (count > 0);
		}
		// 返回结果
		return success;
	}
	
	/**
	 * 根据指定的路径，导入皮肤颜色参数
	 * @param xmlPath JAR包中的配置路径
	 * @return 返回读取的行数
	 */
	public static int loadSkins(String xmlPath) {
		return new SkinToken().loadColors(xmlPath);
	}
}