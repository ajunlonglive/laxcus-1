/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;

import javax.swing.*;

/**
 * 节点本地属性，保存在内在中
 * 
 * @author scott.liang
 * @version 1.0 2/28/2020
 * @since laxcus 1.0
 */
public class LocalProperties {

	/**
	 * 输出属性值
	 * @param key 键
	 * @param value 值
	 * @return 被替换的值
	 */
	public static Object putProperity(Object key, Object value) {
		return UIManager.getDefaults().put(key, value);
	}

	/**
	 * 根据键查找值
	 * @param key 键
	 * @return 返回值
	 */
	public static Object findProperity(Object key) {
		return UIManager.getDefaults().get(key);
	}
	
	/**
	 * 查找字体
	 * @param key
	 * @return
	 */
	public static Font findFont(Object key) {
		Object value = findProperity(key);
		if (value != null && value.getClass() == Font.class) {
			return (Font) value;
		}
		return null;
	}

	/**
	 * 查找布尔值
	 * @param key
	 * @return
	 */
	public static Boolean findBoolean(Object key) {
		Object value = findProperity(key);
		if (value != null && value.getClass() == Boolean.class) {
			return (Boolean) value;
		}
		return null;
	}

	/**
	 * 查找整数值
	 * @param key
	 * @return
	 */
	public static Integer findInteger(Object key) {
		Object value = findProperity(key);
		if (value != null && value.getClass() == Integer.class) {
			return (Integer) value;
		}
		return null;
	}

	/**
	 * 查找字符串
	 * @param key
	 * @return
	 */
	public static String findString(Object key) {
		Object value = findProperity(key);
		if (value != null && value.getClass() == String.class) {
			return (String) value;
		}
		return null;
	}
	
	/**
	 * 返回范围
	 * @param key
	 * @return
	 */
	public static Rectangle findRectangle(Object key) {
		Object value = findProperity(key);
		if (value != null && value.getClass() == Rectangle.class) {
			return (Rectangle) value;
		}
		return null;
	}
}
