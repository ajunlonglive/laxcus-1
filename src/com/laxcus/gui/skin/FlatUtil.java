/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;

import javax.swing.*;

/**
 * 属性参数 <br><br>
 * 
 * 关键字/值对：<br>
 * NotBorder -> Boolean <br>
 * FlatDivider -> Boolean <br>
 * DrawStyle -> Rectangle, Rect, TopLine, BottomLine, LeftLine, RightLine <br>
 * 
 * @author scott.liang
 * @version 1.0 6/17/2022
 * @since laxcus 1.0
 */
public class FlatUtil {
	

	/**
	 * 生成默认的菜单栏边框
	 * @return 返回DefaultMenuItemBorder
	 */
	public static DefaultMenuItemBorder createMenuItemBorder() {
		return new DefaultMenuItemBorder(new Insets(3, 0, 3, 0));
	}

	/**
	 * 生成指定区域的菜单栏边框
	 * @param s 边框
	 * @return 返回DefaultMenuItemBorder实例
	 */
	public static DefaultMenuItemBorder createMenuItemBorder(Insets s) {
		return new DefaultMenuItemBorder(s);
	}

	/**
	 * 生成默认的菜单边框
	 * @return 返回DefaultMenuItemBorder
	 */
	public static DefaultMenuItemBorder createMenuBorder() {
		return new DefaultMenuItemBorder(new Insets(4, 3, 4, 3));
	}

	/**
	 * 生成指定区域的菜单边框
	 * @param s 边框
	 * @return 返回DefaultMenuItemBorder实例
	 */
	public static DefaultMenuItemBorder createMenuBorder(Insets s) {
		return new DefaultMenuItemBorder(s);
	}

	/**
	 * 从左到右布局
	 * @param c
	 * @return 返回真或者假
	 */
	public static boolean isLeftToRight(Component c) {
		return c.getComponentOrientation().isLeftToRight();
	}

	/**
	 * 平面的分隔符
	 * @param c 组件实例
	 * @return 隐藏返回真，否则假
	 */
	public static boolean isFlatDivider(Component c) {
		// 组件取消
		if (c == null) {
			return false;
		}
		// 实例
		if (!(c instanceof JComponent)) {
			return false;
		}

		// 实例
		JComponent jsp = (JComponent) c;
		Object o = jsp.getClientProperty("FlatDivider");
		if (o != null && o.getClass() == Boolean.class) {
			Boolean b = (Boolean) o;
			return b.booleanValue();
		}
		return false;
	}

	/**
	 * 判断隐藏组件边框 
	 * @param c 组件实例
	 * @return 隐藏返回真，否则假
	 */
	public static boolean isNotBorder(Component c) {
		// 组件取消
		if (c == null) {
			return false;
		}
		// 实例
		if (!(c instanceof JComponent)) {
			return false;
		}

		// 实例
		JComponent jsp = (JComponent) c;
		Object o = jsp.getClientProperty("NotBorder");
		if (o != null && o.getClass() == Boolean.class) {
			Boolean b = (Boolean) o;
			return b.booleanValue();
		}
		return false;
	}

	//	/**
	//	 * 判断有底栏边线，用于菜单条（JMenuBar）
	//	 * @param c
	//	 * @param defaultValue 默认值
	//	 * @return 没有定义返回真，
	//	 */
	//	public static boolean isDrawBottomLine(Component c, boolean defaultValue) {
	//		// 组件取消
	//		if (c == null) {
	//			return defaultValue;
	//		}
	//		// 实例
	//		if (!(c instanceof JComponent)) {
	//			return defaultValue;
	//		}
	//
	//		// 实例
	//		JComponent jsp = (JComponent) c;
	//		Object o = jsp.getClientProperty("DrawBottomLine");
	//		if (o != null && o.getClass() == Boolean.class) {
	//			Boolean b = (Boolean) o;
	//			return b.booleanValue();
	//		}
	//		return defaultValue;
	//	}

	/**
	 * 判断绘制样式
	 * @param c 组件实例
	 * @param value 比较值
	 * @param defaultValue 默认结果值
	 * @return 返回真或者假
	 */
	public static boolean isDrawStyle(Component c, String value, boolean defaultValue) {
		// 组件取消
		if (c == null) {
			return defaultValue;
		}
		// 实例
		if (!(c instanceof JComponent)) {
			return defaultValue;
		}

		// 实例
		JComponent jsp = (JComponent) c;
		Object o = jsp.getClientProperty("DrawStyle");
		// 有这个对象
		if (o != null) {
			if (o.getClass() == String.class) {
				String s = (String) o;
				return s.equalsIgnoreCase(value);
			} else if (o.getClass() == String[].class) {
				String[] s = (String[]) o;
				for (int i = 0; i < s.length; i++) {
					if (s[i].equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
		}
		return defaultValue;
	}

	/**
	 * 绘制矩型
	 * @param c 组件实例
	 * @param defaultValue
	 * @return 返回真或者假
	 */
	public static boolean isDrawRectangle(Component c, boolean defaultValue) {
		return FlatUtil.isDrawStyle(c, "Rect", defaultValue) || FlatUtil.isDrawStyle(c, "Rectangle", defaultValue);
	}

	/**
	 * 绘制顶部边线
	 * @param c 组件实例
	 * @param defaultValue
	 * @return 返回真或者假
	 */
	public static boolean isDrawTopLine(Component c, boolean defaultValue) {
		return FlatUtil.isDrawStyle(c, "TopLine", defaultValue);
	}

	/**
	 * 绘制底部边线
	 * @param c 组件实例
	 * @param defaultValue
	 * @return 返回真或者假
	 */
	public static boolean isDrawBottomLine(Component c, boolean defaultValue) {
		return FlatUtil.isDrawStyle(c, "BottomLine", defaultValue);
	}

	/**
	 * 绘制左侧边线
	 * @param c 组件实例
	 * @param defaultValue
	 * @return 返回真或者假
	 */
	public static boolean isDrawLeftLine(Component c, boolean defaultValue) {
		return FlatUtil.isDrawStyle(c, "LeftLine", defaultValue);
	}

	/**
	 * 绘制右侧边线
	 * @param c 组件实例
	 * @param defaultValue
	 * @return 返回真或者假
	 */
	public static boolean isDrawRightLine(Component c, boolean defaultValue) {
		return FlatUtil.isDrawStyle(c, "RightLine", defaultValue);
	}

}