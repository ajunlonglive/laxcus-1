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
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.gui.component.*;
import com.laxcus.util.*;
import com.laxcus.util.skin.*;

/**
 * 平面分隔符
 * 
 * 分隔线在中间绘制
 *
 * @author scott.liang
 * @version 1.0 2/12/2022
 * @since laxcus 1.0
 */
public class FlatSeparatorUI extends MetalSeparatorUI {
	
	/**
	 * 构造分隔符
	 */
	public FlatSeparatorUI() {
		super();
	}

	/**
	 * 返回实例
	 * @param c
	 * @return
	 */
	public static ComponentUI createUI(JComponent c) {
		return new FlatSeparatorUI();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalSeparatorUI#getPreferredSize(javax.swing.JComponent)
	 */
	@Override
	public Dimension getPreferredSize(JComponent c) {
		if (c != null && c instanceof JSeparator) {
			JSeparator js = (JSeparator) c;
			if (js.getOrientation() == JSeparator.HORIZONTAL) {
				return new Dimension(0, 6); // 水平状态，宽度不限，高度6个像素
			} else if (js.getOrientation() == JSeparator.VERTICAL) {
				return new Dimension(6, 0); // 垂直状态，宽度6个像素，高度不限
			}
		}

		return super.getPreferredSize(c);
	}
	

//	/**
//	 * 判断来自EdgeBar
//	 * @param c 组件
//	 * @return 返回真或者假
//	 */
//	private boolean isEdgeBar(JComponent c) {
//		String name = (c.getParent() != null ? c.getParent().getClass().getName() : "");
//		return name.indexOf("EdgeBar") >= 0 || name.indexOf("StatusBar") >=0;
//	}
	
//	private void printInterfaces(Component c) {
////		Container c = t.getParent();
//		if (c == null) {
//			System.out.println();
//			return;
//		}
//		Class<?> object = c.getClass();
//		// 取接口
//		Class<?>[] array = object.getInterfaces();
//		int size = (array == null ? 0 : array.length);
//		// 逐一判断匹配
//		for (int i = 0; i < size; i++) {
//			System.out.printf("%s -> %s\n", object.getName(), array[i].getName());
//		}
//		
//		printInterfaces(c.getParent());
//	}

//	/**
//	 * 判断是工具条
//	 * @param c
//	 * @return
//	 */
//	private boolean isToolBar(JComponent c) {
//		Container parent = c.getParent();
//		// 父类是JToolBar
//		if (parent != null && Laxkit.isClassFrom(parent, JToolBar.class)) {
//			return true;
//		}
//		return false;
//	}
	
	
	/**
	 * 判断对象继承某个接口
	 * @param c 对象实例
	 * @param clazz 接口类
	 * @return 返回真或者假
	 */
	private boolean isInterfaceFrom(Component c, Class<?> clazz) {
		if (c == null) {
			return false;
		}
		
		// 取接口
		Class<?>[] vs = c.getClass().getInterfaces();
		int size = (vs != null ? vs.length : 0);
		// 逐一判断匹配
		for (int i = 0; i < size; i++) {
			if (vs[i] == clazz) {
				return true;
			}
		}
		
		return isInterfaceFrom(c.getParent(), clazz);
	}
	
	/**
	 * 判断对象或者上级父类继承自某个类
	 * @param c 对象实例
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	private boolean isClassFrom(Component c, Class<?> clazz) {
		if (c == null) {
			return false;
		}

		// 判断匹配
		if (Laxkit.isClassFrom(c.getClass(), clazz)) {
			return true;
		}

		return isClassFrom(c.getParent(), clazz);
	}

	
	/**
	 * 判断分隔符来自平台边缘栏
	 * @param c
	 * @return
	 */
	private boolean isPlatformEdgeBar(JComponent c) {
		return isInterfaceFrom(c, PlatformEdgeBar.class);
	}
	
	/**
	 * 判断是工具条
	 * @param c
	 * @return
	 */
	private boolean isToolBar(JComponent c) {
		return isClassFrom(c, JToolBar.class);
		// Container parent = c.getParent();
		// // 父类是JToolBar
		// return (parent != null && Laxkit.isClassFrom(parent,
		// JToolBar.class));
	}

	/**
	 * 判断是分隔符来自平台任务栏
	 * @param c
	 * @return
	 */
	private boolean isPlatformStatusBar(JComponent c) {
		return isInterfaceFrom(c, PlatformStatusBar.class);
	}
	
	private Color getForeground(JComponent c) {
		Color color = UIManager.getColor("Separator.foreground");
		if(color == null) {
			color = c.getForeground();
		}
		return new Color(color.getRGB());
	}
	
	private Color getBackground(JComponent c) {
		Color color = UIManager.getColor("Separator.background");
		if(color == null) {
			color = c.getBackground();
		}
		return new Color(color.getRGB());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		// if("ROLLBAR".equals(c.getName())) {
		// paintEdgeBar(g, c);
		// printInterfaces(c);
		// }

		if (isPlatformEdgeBar(c)) {
			paintEdgeBar(g, c);
		} else if (isToolBar(c) || isPlatformStatusBar(c)) {
			paintToolBar(g, c);
		} else {
			paintOther(g, c);
		}
	}

	/**
	 * 绘制其它父组件的上的分隔符
	 * 随线条颜色产生凹陷或者凸起的效果
	 */
	private void paintOther(Graphics g, JComponent c) {
		// 来自其他组件的分隔符
		Dimension d = c.getSize();

		Color foreground = getForeground(c);
		Color background = getBackground(c);
		JSeparator js = (JSeparator) c;

		if (js.getOrientation() == JSeparator.VERTICAL) {
			int x = d.width / 2 - 1;
			if (x < 0) x = 0;

			g.setColor(foreground); 
			g.drawLine(x, 0, x, d.height);

			g.setColor(background); 
			g.drawLine(x + 1, 0, x + 1, d.height);
		}
		// HORIZONTAL
		else {
			int y = d.height / 2 - 1;
			if (y < 0) y = 0;

			g.setColor(foreground); 
			g.drawLine(0, y, d.width, y);

			g.setColor(background); 
			g.drawLine(0, y + 1, d.width, y + 1);
		}
	}

	/**
	 * 来自EdgeBar，以它的本色为基准绘制分隔线
	 * @param g
	 * @param c
	 */
	private void paintEdgeBar(Graphics g, JComponent c) {
		// 来自其他组件的分隔符
		Dimension d = c.getSize();

		// 分隔符本色
		Color foreground = c.getForeground();
		Color background = c.getBackground();
		JSeparator js = (JSeparator) c;

		if (js.getOrientation() == JSeparator.VERTICAL) {
			int x = d.width / 2 - 1;
			if (x < 0) x = 0;

			g.setColor(foreground); 
			g.drawLine(x, 0, x, d.height);

			g.setColor(background); 
			g.drawLine(x + 1, 0, x + 1, d.height);
		}
		// HORIZONTAL
		else {
			int y = d.height / 2 - 1;
			if (y < 0) y = 0;

			g.setColor(foreground); 
			g.drawLine(0, y, d.width, y);

			g.setColor(background); 
			g.drawLine(0, y + 1, d.width, y + 1);
		}
	}
	
	/**
	 * 绘制工具条的分隔符
	 * 始终以凹陷下去的效果出现
	 * @param g
	 * @param c
	 */
	private void paintToolBar(Graphics g, JComponent c) {
		Dimension d = c.getSize();
		// 灰色
		boolean gray = Skins.isGraySkin();

		// 使用默认的系统环境的分隔符颜色定义
		Color foreground = getForeground(c);
		Color background = getBackground(c);
		
		JSeparator js = (JSeparator) c;

		if (js.getOrientation() == JSeparator.VERTICAL) {
			int x = d.width / 2 - 1;
			if (x < 0) x = 0;

			if (gray) {
				g.setColor(foreground);
				g.drawLine(x, 0, x, d.height);
				g.setColor(background);
				g.drawLine(x + 1, 0, x + 1, d.height);
			} else {
				g.setColor(background);
				g.drawLine(x, 0, x, d.height);
				g.setColor(foreground);
				g.drawLine(x + 1, 0, x + 1, d.height);
			}
		}
		// HORIZONTAL
		else {
			int y = d.height / 2 - 1;
			if (y < 0) y = 0;

			if (gray) {
				g.setColor(foreground);
				g.drawLine(0, y, d.width, y);
				g.setColor(background);
				g.drawLine(0, y + 1, d.width, y + 1);
			} else {
				g.setColor(background);
				g.drawLine(0, y, d.width, y);
				g.setColor(foreground);
				g.drawLine(0, y + 1, d.width, y + 1);
			}
		}
	}
	
}


///**
// * 定义最佳尺寸
// * @param c
// */
//private void setSeparatorSize(JSeparator c) {
//	Dimension d = getPreferredSize(c);
//	
//	if (Laxkit.isClassFrom(c, JToolBar.Separator.class)) {
//		((JToolBar.Separator) c).setSeparatorSize(d);
//	} else {
//		c.setPreferredSize(d);
////		c.setSize(d);
//		c.setMinimumSize(d);
//		c.setMaximumSize(d);
//	}
//}

//public Dimension getPreferredSize(JComponent c) {
//	if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL)
//		return new Dimension(6, 0);
//	else
//		return new Dimension(0, 6);
//}



//
//private boolean setToolBarSeparator(JSeparator c) {
//	// 最小宽度
//	boolean success = (c != null && Laxkit.isClassFrom(c, JToolBar.Separator.class));
//	if (!success) {
//		return false;
//	}
//	
//	if (c.getOrientation() == JSeparator.VERTICAL) {
//		JToolBar.Separator js = (JToolBar.Separator) c;
//		Dimension d = js.getSeparatorSize();
//		if (d.width < 6) {
//			d.width = 6;
//			js.setSeparatorSize(d);
//		}
//	}
//	return true;
//}



//private void setSeparatorSize(JSeparator c) {
////	// 最小宽度
////	boolean success = (c != null && Laxkit.isClassFrom(c, JToolBar.Separator.class));
////	if (!success) {
////		return false;
////	}
//	
//	if(c.getOrientation() == JSeparator.HORIZONTAL) {
//		
//	}
//	
//	else if (c.getOrientation() == JSeparator.VERTICAL) {
//		
//		Dimension d = c.getSeparatorSize();
//		if (d.width < 6) {
//			d.width = 6;
//			js.setSeparatorSize(d);
//		}
//	}
//}


//protected void installDefaults(JSeparator c) {
//	super.installDefaults(c);
////	setSeparatorSize(c);
//}


///*
// * (non-Javadoc)
// * @see javax.swing.plaf.metal.MetalSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
// */
//@Override
//public void paint(Graphics g, JComponent c) {
////	// 如果是来自工具栏，绘制它
////	if (isToolBar(c)) {
////		FlatToolBarSeparatorUI.draw(g, c);
////		return;
////	}
//	
////	System.out.printf("FlatSeparatorUI.paint, paint %s\n", c.getClass().getName());
//	
//	// 来自其他组件的分隔符
//	Dimension d = c.getSize();
//	
//	System.out.printf("FlatSeparatorUI.paint, from %s, w:%d,h:%d\n", c.getClass().getName(), d.width, d.height);
//
//	Color foreground = getForeground(c);
//	Color background = getBackground(c);
//
//	if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
//		int x = d.width / 2 - 1;
//		if (x < 0) x = 0;
//
//		g.setColor(foreground); // c.getForeground());
//		g.drawLine(x, 0, x, d.height);
//
//		g.setColor(background); // c.getBackground());
//		g.drawLine(x + 1, 0, x + 1, d.height);
//	}
//	// HORIZONTAL
//	else {
//		int y = d.height / 2 - 1;
//		if (y < 0) y = 0;
//
//		g.setColor(foreground); // c.getForeground());
//		g.drawLine(0, y, d.width, y);
//
//		g.setColor(background); // c.getBackground());
//		g.drawLine(0, y + 1, d.width, y + 1);
//	}
//}

//System.out.printf("FlatSeparatorUI.draw, from %s, w:%d,h:%d\n", c.getClass().getName(), d.width, d.height);
