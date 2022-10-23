/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.awt.*;

import javax.swing.*;

/**
 * 图形工具，提供基础的配置说明
 * 
 * @author scott.liang
 * @version 1.0 9/3/2021
 * @since laxcus 1.0
 */
public class GUIKit {
	
	/**
	 * 判断是高分辨屏幕
	 * 宽/高超过：1200 * 800就是高分辨率真
	 * @return 返回真或者假
	 */
	public static boolean isHighScreen() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		return d.width > 1200 && d.height > 800;
	}

	/**
	 * 从容器中查找指定的第一个组件！
	 * 
	 * @param <T>
	 * @param container 容器实例
	 * @param clazz 对象类
	 * @param index 指定下标，从0开始
	 * @return 关联对象类，或者空指针
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findComponent(Container container, Class<?> clazz, int index) {
		if (container == null) {
			return null;
		}
		// 逐个检测
		Component[] objects = container.getComponents();
		int size = (objects != null && objects.length > 0 ? objects.length : 0);
		int count = 0;
		for (int i = 0; i < size; i++) {
			Component object = objects[i];
			if (object.getClass() == clazz) {
				if (count == index) {
					return (T) object;
				} else {
					count++;
				}
			} else if (Laxkit.isClassFrom(object, Container.class)) {
				Object o = GUIKit.findComponent((Container) object, clazz, index);
				if (o != null) {
					return (T) o;
				}
			}
		}
		return null;
	}
	
	/**
	 * 查找第一个下标组件
	 * @param <T>
	 * @param container 容器
	 * @param clazz 对象类
	 * @return 返回关联对象类，没有是空指针
	 */
	public static <T> T findComponent(Container container, Class<?> clazz) {
		return GUIKit.findComponent(container, clazz, 0);
	}

	/**
	 * 返回界面名称ID
	 * @return 字符串或者空指针
	 */
	public static String getUIID() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf == null) {
			return null;
		}
		return laf.getID();
	}

	/**
	 * 判断是METAL界面
	 * @return 真或者假
	 */
	public static boolean isMetalUI() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf == null) {
			return false;
		}
		String name = laf.getID();
		return (name != null && name.equalsIgnoreCase("Metal"));
	}

	/**
	 * 判断是NIMBUS界面
	 * @return 真或者假
	 */
	public static boolean isNimbusUI() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf == null) {
			return false;
		}
		String name = laf.getID();
		return (name != null && name.equalsIgnoreCase("Nimbus"));
	}

	
}
