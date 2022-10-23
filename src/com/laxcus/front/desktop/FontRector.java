/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;

import com.laxcus.register.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 系统初始化字体校正器
 * 
 * 必须在设置外观感知(L&F)后才能操作！
 * 
 * 在启动时校正字体
 * 
 * @author scott.liang
 * @version 1.0 7/12/2021
 * @since laxcus 1.0
 */
class FontRector {

	/**
	 * 字体校正器
	 */
	public FontRector() {
		super();
	}

	/**
	 * 建立目录
	 * @return
	 */
	private File createConfigDirectory() {
		String bin = System.getProperty("user.dir");
		bin += "/../conf";
		File file = new File(bin);
		boolean success = (file.exists() && file.isDirectory());
		if (!success) {
			success = file.mkdirs();
		}
		return (success ? file : null);
	}

	/**
	 * 解析一行字体
	 * @param input 输入参数
	 * @return 返回字体或者空指针
	 */
	private Font readFont(String input) {
		final String regex = "^\\s*(.+?)\\s*\\,\\s*(?i)([PLAIN|BOLD|ITALIC]+)\\s*\\,\\s*([\\d]+)\\s*$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		String family = matcher.group(1);
		String styleName = matcher.group(2);
		int size = Integer.parseInt(matcher.group(3));

		// 字体样式
		int style = Font.PLAIN;
		if (styleName.matches("^\\s*(?i)(PLAIN)\\s*$")) {
			style = Font.PLAIN;
		} else if (styleName.matches("^\\s*(?i)(BOLD)\\s*$")) {
			style = Font.BOLD;
		} else if (styleName.matches("^\\s*(?i)(ITALIC)\\s*$")) {
			style = Font.ITALIC;
		}

		// 生成字体
		return new Font(family, style, size);
	}

	/**
	 * 找到首个匹配的字体
	 * @param sytemFonts
	 * @return 首选字体
	 */
	private Font choiceFirst(Font[] defaultFonts, Font[] systemFonts) {
		if (defaultFonts != null && systemFonts != null) {
			for (Font hot : defaultFonts) {
				String hotName = hot.getName();
				for (Font font : systemFonts) {
					String name = font.getName();
					if (name.equals(hotName)) {
						return hot;
					}
				}
			}
		}

		// 返回系统的字体
		return systemFonts[0];
	}

	/**
	 * 读平台定义的字体
	 * @return 全部字体数组
	 */
	private Font[] readPlatformFont() {
		File dir = createConfigDirectory();
		if (dir == null) {
			return null;
		}
		// 配置目录下的字体文件
		File file = new File(dir, "fonts.conf");
		// 没有这个文件，忽略它
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return null;
		}

		// 从配置文件中读取全部配置
		ArrayList<Font> array = new ArrayList<Font>();
		try {
			FileInputStream in = new FileInputStream(file);
			InputStreamReader is = new InputStreamReader(in, "UTF-8");
			BufferedReader bf = new BufferedReader(is);
			do {
				String line = bf.readLine();
				if (line == null) {
					break;
				}
				Font font = readFont(line);
				if (font != null) {
					array.add(font);
				}
			} while (true);
			bf.close();
			is.close();
			in.close();
		} catch (IOException e) {

		}

		if(array.isEmpty()) {
			return null;
		}
		// 输出全部字体
		Font[] fonts = new Font[array.size()];
		return array.toArray(fonts);
	}

	/**
	 * 检查平台字体 <br>
	 * 规则：<br>
	 * 1. 如果没有，找到"conf/environment.conf"文件，以“FONT/SYSTEM”中的定义为准 <br>
	 * 2. 如果有"conf/fonts.conf"文件，以这个文件中的配置字体做为系统字体 <br>
	 * 3. 二者皆无时，退出不处理，返回“假”。<br><br>
	 * 
	 * @return 成功返回真，否则假
	 */
	public boolean checkPlatformFont() {
		// 从环境中取得参数
		String text = UIManager.getString("FontRector.Hello");
		if (text == null) {
			text = "Default Fonts";
		}
		
		// 系统字体
		Font df = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM");
		
		// 默认的字体
		Font[] defaultFonts = (df != null ? new Font[] { df } : readPlatformFont());

		// 找到合适的字体
		Font[] systemFonts = FontKit.findFonts(text);
		if (systemFonts == null || systemFonts.length == 0) {
			return false;
		}
		// 首选字体
		Font font = choiceFirst(defaultFonts, systemFonts);
		
		// 更新系统字体
		UITools.updateSystemFonts(font);
		if (Skins.isNimbus()) {
			UITools.updateNimbusSystemFonts(font);
		} else {
			UITools.updateMetalSystemFonts(font);
		}

		return true;
	}

}