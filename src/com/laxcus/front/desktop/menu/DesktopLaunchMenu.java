/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.menu;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 桌面启动菜单
 * 
 * @author scott.liang
 * @version 1.0 6/3/2021
 * @since laxcus 1.0
 */
public class DesktopLaunchMenu extends JPopupMenu implements LaunchMenu {

	private static final long serialVersionUID = -2508351888428110555L;

	/** "FLAG"图片，位于弹出菜单左侧，风格类似Windows XP **/
	private BufferedImage flagImage;

	/** “FLAG”文本，默认是“Laxcus Clusters Desktop” **/
	private String flagText;
	
	/** 主字体 **/
	private Font mainFont;
	private Border mainBorder;
	/** 子字体 **/
	private Font subFont;
	private Border subBorder;
	
	private int gap = 6;
	
	/** 监听接口 **/
	private ActionListener listener;

	/**
	 * 构造默认对象
	 */
	public DesktopLaunchMenu() {
		super();
		// 设置标题文本
		setFlagText("Laxcus Clusters Desktop");
		// 创建文字图片
		doFlagImage();
	}
	
//	public void setMainFont(Font f) {
//		if (f != null) {
//			this.mainFont = f;
//		}
//	}
//
//	public Font getMainFont() {
//		return this.mainFont;
//	}
//
//	public void setSubFont(Font font) {
//		if (font != null) {
//			this.subFont = font;
//		}
//	}
//
//	public Font getSubFont() {
//		return this.subFont;
//	}
	
	public void setParameter(ActionListener listener, Border mainBorder, int gap, Border subBorder) {
		this.listener = listener;
		// this.mainFont = mainFont;
		this.mainBorder = mainBorder;
		this.gap = gap;
		// this.subFont = subFont;
		this.subBorder = subBorder;
	}
	
	/**
	 * 设置子菜单参数
	 * @param menu
	 * @param font
	 * @param border
	 * @param gap
	 */
	private void setSubMenu(JMenu menu, boolean updateUI) {
		Component[] subs = menu.getMenuComponents();
		int size = (subs == null ? 0 : subs.length);
		// 判断对象
		for (int index = 0; index < size; index++) {
			Component component = subs[index];

			// 1. 先判断是JComponent，刷新它
			if (updateUI) {
				if (Laxkit.isClassFrom(component, JComponent.class)) {
					((JComponent) component).updateUI();
				}
			}

			// 给JMenuItem设置字体和边框
			if (Laxkit.isClassFrom(component, JMenuItem.class)) {
				JMenuItem item = (JMenuItem) component;
				
				item.setFont(subFont);
				item.setBorder(subBorder);
				item.setIconTextGap(gap);
				// 更新提示的字体
				FontKit.updateToolTipText(item);

				//				// 描述字，重新定义
				//				String res = item.getAccessibleContext().getAccessibleDescription();
				//				if (res != null) {
				//					FontKit.setToolTipText(item, res);
				//				}
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(component, JMenu.class)) {
				setSubMenu((JMenu) component, updateUI);
			}
		}
	}
	
	/**
	 * 定义字体
	 */
	private void doDefaultFonts() {
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM");
		if (font == null) {
			font = FontKit.findFont(new javax.swing.JLabel(), "abc");
		}
		if (font == null) {
			font = new Font("dialog", Font.PLAIN, 12);
		}
		// 确定字体
		mainFont = new Font(font.getName(), Font.PLAIN, font.getSize() + 2);
		subFont = new Font(font.getName(), Font.PLAIN, font.getSize());
	}
	
	/**
	 * 更新字体和边框
	 */
	public void updateFontAndBorder(boolean updateUI) {
		// 默认字体
		doDefaultFonts();
		// 更新当前UI
		if (updateUI) {
			updateUI();
		}
		
		// 设置字体
		int count = getComponentCount();
		for (int index = 0; index < count; index++) {
			Component component = getComponent(index);
			
			// 1. 先判断是JComponent，刷新它
			if (updateUI) {
				if (Laxkit.isClassFrom(component, JComponent.class)) {
					((JComponent) component).updateUI();
				}
			}
			
			// 1. 先判断是JMenuItem
			if (Laxkit.isClassFrom(component, JMenuItem.class)) {
				JMenuItem item = (JMenuItem) component;
				item.setFont(mainFont);
				item.setBorder(mainBorder);
				item.setIconTextGap(gap);
				
				String res = item.getAccessibleContext().getAccessibleDescription();
				if (res != null) {
					FontKit.setToolTipText(item, res);
				}
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(component, JMenu.class)) {
				setSubMenu((JMenu) component, updateUI);
			}
		}
	}
	
	/**
	 * 设置事件监听
	 * @param menu
	 */
	private void setMenuListener(JMenu menu) {
		Component[] elements = menu.getMenuComponents();
		int size = (elements == null ? 0 : elements.length);
		// 判断对象
		for (int index = 0; index < size; index++) {
			Component element = elements[index];
			// 给JMenuItem设置字体和边框
			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
				setActionListener((JMenuItem) element);
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				setMenuListener((JMenu) element);
			}
		}
	}
	
	/**
	 * 设置事件监听器，只有一个!
	 * @param item
	 */
	private void setActionListener(JMenuItem item) {
		ActionListener[] a = item.getActionListeners();
		if (a == null || a.length == 0) {
			item.addActionListener(listener);
		}
	}

	/**
	 * 更新
	 */
	public void updateActionListener() {
		int count = getComponentCount();
		for (int index = 0; index < count; index++) {
			Component element = getComponent(index);
			if (element == null) {
				continue;
			}
			// 1. 先判断是JMenuItem
			if (Laxkit.isClassFrom(element, JMenuItem.class)) {
				setActionListener((JMenuItem) element);
			}
			// JMenu是JMenuItem的子类，如果是，交给子级处理
			if (Laxkit.isClassFrom(element, JMenu.class)) {
				setMenuListener((JMenu) element);
			}
		}
	}

	/**
	 * 返回标题文本
	 * @return
	 */
	public String getFlagText(){
		return flagText;
	}
	
	/**
	 * 设置标题文本
	 * @param text
	 */
	public void setFlagText(String text) {
		if (text != null) {
			flagText = text;
		}
	}

	/**
	 * 生成位于左侧的“FLAG”图像
	 */
	public void doFlagImage() {
		flagImage = createFlagImage(0);
	}

	/**
	 * 生成位于左侧的“FLAG”图像
	 * @param menuHeight 菜单高度
	 */
	public void doFlagImage(int menuHeight) {
		flagImage = createFlagImage(menuHeight);
	}
	
	/**
	 * 生成暗的颜色
	 * @param c
	 * @param flag
	 * @return
	 */
	private Color toDrak(Color c, int flag) {
		RGB rgb = new RGB(c);
		ESL esl = rgb.toESL();
		esl.setL(esl.getL() - flag);
		return esl.toColor();
	}

	/**
	 * 生成亮的颜色
	 * @param c
	 * @param flag
	 * @return
	 */
	private Color toLight(Color c, int flag) {
		RGB rgb = new RGB(c);
		ESL esl = rgb.toESL();
		esl.setL(esl.getL() + flag);
		return esl.toColor();
	}

	/**
	 * 生成暗、亮两套颜色
	 * @return
	 */
	private Color[] createDarkLights() {
		
		//		background = new Color(c.getRGB());

		// NIMBUS界面
		// 淡兰：RGB {190,211,230}, ESL{139,107,198}
		// 深兰：RGB {52, 96, 135}, ESL{139,107,88}

		//		ESL dark = new ESL(139, 107, 128);
		//		ESL light = new ESL(139, 107, 168);
		//		return new Color[] { dark.toColor(), light.toColor() };

		if (Skins.isNimbus()) {
			ESL dark = new ESL(139, 107, 128);
			ESL light = new ESL(139, 107, 188);
			return new Color[] { dark.toColor(), light.toColor() };
		} else if (Skins.isMetal() && Skins.isGraySkin()) {
//			ESL dark = new ESL(114, 123, 88);
//			ESL light = new ESL(114, 123, 158);
			
			
			ESL dark = new ESL(120, 38, 80);
			ESL light = new ESL(120, 38, 208);

			return new Color[] { dark.toColor(), light.toColor() };
		} else {
			Color c = super.getBackground();
			if (c == null) {
				c = Color.LIGHT_GRAY;
			}
			Color dark = toDrak(c, 20);
			Color light = toLight(c, 80);
			return new Color[] { dark, light };
		}
	}
	
	/**
	 * 返回首选的字体名称
	 * @return 字体名称数组
	 */
	private String[] getPreferredFontFamilies() {
		java.util.ArrayList<String> a = new java.util.ArrayList<String>();
		// 找到系统的默认字体
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
		if (font != null) {
			String name = font.getName();
			boolean success = FontKit.hasFontName(name);
			if (success) {
				a.add(name);
			}
		}
		String[] names = { "新宋体", "DialogInput", "宋体", "Dialog", "Monospaced" };
		for (int i = 0; i < names.length; i++) {
			boolean success = FontKit.hasFontName(names[i]);
			if (success) {
				a.add(names[i]);
			}
		}
		String[] s = new String[a.size()];
		return a.toArray(s);
	}
	
	/**
	 * 生成字体
	 * @param text
	 * @return
	 */
	private Font findFont(String text) {
		// 找到菜单栏的字体
		Font font = FontKit.findFont(getClass()); // new javax.swing.JLabel(), text);
		if (font == null) {
			return null;
		}
		// 找到首选字体
		String[] names = getPreferredFontFamilies();
		for (String name : names) {
			int size = font.getSize() + 4;
			if (size > 18) size = 18;
			
			Font select = new Font(name, Font.BOLD, size);
			// 判断能够显示
			if (FontKit.canDisplay(select, text)) {
				return select;
			}
		}
		
		// 返回字体
		int size = font.getSize() + 4;
		if (size > 18) size = 18;
		return new Font(font.getName(), Font.BOLD, size);
	}
	
//	/**
//	 * 返回首选的字体名称
//	 * @return 返回名称，没有是空指针
//	 */
//	private String getPreferredFontFamily() {
//		// 找到系统的默认字体
//		Font font = UIKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
//		if (font != null) {
//			String name = font.getName();
//			boolean success = FontKit.hasFontName(name);
//			if (success) {
//				return name;
//			}
//		}
//		
//		String[] names = { "新宋体", "DialogInput", "宋体", "Dialog", "Monospaced" };
//		for (int i = 0; i < names.length; i++) {
//			boolean success = FontKit.hasFontName(names[i]);
//			if (success) {
//				return names[i];
//			}
//		}
//		return null;
//	}

//	/**
//	 * 生成字体
//	 * @param text
//	 * @return
//	 */
//	private Font findFont(String text) {
//		Font font = FontKit.findFont(new javax.swing.JLabel(), text);
//		if (font == null) {
//			return null;
//		}
//		// 首选字体，有效且或有正常显示时，返回它
//		String family = getPreferredFontFamily();
//		if (family != null) {
//			Font select = new Font(family, Font.BOLD, font.getSize() + 4);
//			// 判断能够显示
//			if (FontKit.canDisplay(select, text)) {
//				return select;
//			}
//		}
//		return new Font(font.getName(), Font.BOLD, font.getSize() + 4);
//	}

	/**
	 * 获得字体高度，用来生成
	 * @param text
	 * @return
	 */
	private int getFontHeight(String text) {
		// 默认是20个像素
		int height = 20;

		BufferedImage bufImage = new BufferedImage(40, 400, BufferedImage.TYPE_INT_ARGB);
		// 获得缓冲图片的2D绘图类变量
		Graphics2D g2d = bufImage.createGraphics();

		// 找到匹配的字体
		Font font = findFont(text);
		// 字体有效，设置到图形
		if (font != null) {
			g2d.setFont(font);
			// 获得字体高度
			FontMetrics metrics = g2d.getFontMetrics();
			height = metrics.getHeight();
		}
		// 销毁
		g2d.dispose();
		// 返回高度
		return height + 12; // 边框左右留出6个空位
	}

	/**
	 * 判断要重新绘制
	 * @return 返回真或者假
	 */
	public boolean isRepaint() {
		if (flagImage == null) {
			return true;
		}
		int h1 = flagImage.getHeight(null);
		int h2 = getHeight();
		return h1 != h2;
	}

	/**
	 * 生成“FLAG”图像
	 * @return 返回对象实例
	 */
	private BufferedImage createFlagImage(int menuHeight) {
		String text = (flagText != null ? flagText : "Laxcus Clusters Desktop");
		int fontHeight = getFontHeight(text);

		// 菜单高度
		if (menuHeight < 1) {
			menuHeight = getHeight();
		}
		if (menuHeight < 1) {
			menuHeight = 600;
		}

		// 创建缓冲图片类变量		
		BufferedImage image = new BufferedImage(fontHeight, menuHeight, BufferedImage.TYPE_INT_ARGB);
		// 获得缓冲图片的2D绘图类变量
		Graphics2D g2d = image.createGraphics();

		// 建立暗、亮两套颜色，绘制渐变背景
		Color[] colors = createDarkLights();
		GradientPaint paint = new GradientPaint(0, 0, colors[0], 100, 100, colors[1], true);
		g2d.setPaint(paint);
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

		// 逆时针旋转绘制角度90度
		AffineTransform at = new AffineTransform();
		at.rotate(-Math.PI / 2);
		g2d.setTransform(at);

		// 设置前景颜色
		Color foreground = getForeground();
		if (foreground == null) {
			foreground = Color.WHITE;
		}
		g2d.setColor(foreground);

		// 找到匹配的字体
		Font font = findFont(text);
		if (font != null) {
			g2d.setFont(font);
		}

		// 获取文字尺寸
		FontMetrics metrics = g2d.getFontMetrics();
		int width = metrics.stringWidth(text);
		
//		// 居中绘制文字
//		g2d.drawString(text, -width - 50, image.getWidth() / 2);
		
		int gap = (menuHeight - width) / 2; // 边缘
		int x = -width - gap;
		int y = image.getWidth() / 2 + 4; // 向下移动4个像素，因为字体显示偏上，下端留有空位。
		// 居中绘制文字
		g2d.drawString(text, x, y);
		
		// 销毁
		g2d.dispose();

		return image;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getInsets()
	 */
	@Override
	public Insets getInsets() {
		Insets insets = (Insets) super.getInsets().clone();
		insets.left += flagImage.getWidth(null);
		return insets;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// 图片有效，绘制它
		if (flagImage != null) {
			Insets insets = getInsets();
			g.drawImage(flagImage, insets.left - flagImage.getWidth(null), insets.top, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPopupMenu#updateUI()
	 */
	@Override
	public void updateUI() {
		
		// 更新界面
		super.updateUI();

		// 左侧边框的颜色
		doFlagImage();
		

		//		// 背景颜色
		//		Color c = super.getBackground();
		//		// 颜色不一样时
		//		if (background == null || background.getRGB() == c.getRGB()) {
		//			doImage();
		//		}
	}
}


//	/**
//	 * 生成图像
//	 * @return
//	 */
//	private Image createImage() {
//		
//		String text = "LAXCUS COS DESKTOP";
//		int fontHeight = getFontHeight(text);
//		
//		int menuHeight = getHeight();
//		if (menuHeight < 1) {
//			menuHeight = 400;
//		}
//		
//		// 创建缓冲图片类变量
////		BufferedImage bi = new BufferedImage(40, 400, BufferedImage.TYPE_INT_ARGB);
//		
//		BufferedImage bi = new BufferedImage(fontHeight, menuHeight, BufferedImage.TYPE_INT_ARGB);
//		// 获得缓冲图片的2D绘图类变量
//		Graphics2D g2d = bi.createGraphics();
//		// 绘制渐变背景
////		GradientPaint paint = new GradientPaint(0, 0, Color.black, 100, 100, Color.blue, true);
//		
//		// 建立暗、亮两套颜色，绘制渐变背景
//		Color[] colors = createDarkLights();
//		GradientPaint paint = new GradientPaint(0, 0, colors[0], 100, 100, colors[1], true);
//		g2d.setPaint(paint);
//		g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
//
//		// 逆时针旋转绘制角度90度
//		AffineTransform at = new AffineTransform();
//		at.rotate(-Math.PI / 2);
//		g2d.setTransform(at);
//		
//		// 设置前景颜色
//		Color foreground = getForeground();
//		if (foreground == null) {
//			foreground = Color.WHITE;
//		}
//		g2d.setColor(foreground);
//		
////		g2d.setColor(Color.white);
//		
////		g2d.setFont(new Font("宋体", Font.BOLD, 16));
////		g2d.setFont(new Font("宋体", Font.PLAIN, 16));
//		
//		// 找到匹配的字体
////		Font font = FontKit.findFont(this, text);
//		
//		Font font = findFont(text);
//		if (font != null) {
//			font = new Font(font.getName(), Font.BOLD, 16);
//			g2d.setFont(font);
//			
////			Rectangle2D rd = font.getStringBounds(text, g2d.getFontRenderContext());
////			rd.getHeight();
//		}
//		
//		
//		// 获取文字尺寸
//		FontMetrics metrics = g2d.getFontMetrics();
//		int width = metrics.stringWidth(text);
//		int height = metrics.getAscent();
//		
//		// 居中绘制文字
//		g2d.drawString(text, -width - 50, bi.getWidth() / 2);
//		
//		// 销毁
//		g2d.dispose();
//		
//		return bi;
//	}
