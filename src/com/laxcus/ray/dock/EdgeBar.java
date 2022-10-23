/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dock;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;
import com.laxcus.platform.*;
import com.laxcus.ray.panel.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 系统边缘条
 * 
 * @author scott.liang
 * @version 1.0 12/6/2021
 * @since laxcus 1.0
 */
class EdgeBar extends RayPanel implements ActionListener, PlatformEdgeBar {
	
	private static final long serialVersionUID = 1L;
	
	private final String SHUTDOWN_TEXT = "SHUTDOWN";
	private final String RUN_TEXT = "RUN";

	/** 分割符 **/
	private JSeparator sperator;
	
	/** 边框 **/
	private DockBanner banner;
	
	/** 监听器 **/
	private RayEdgeListener listener;
	
	/**
	 * 初始化
	 */
	public EdgeBar() {
		super();
	}
	
	/**
	 * 设置监听器
	 * @param e
	 */
	public void setEdgeListener(RayEdgeListener e){
		listener = e;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}
	
	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source.getClass() != DockButton.class) {
			return;
		}
		
		DockButton button = (DockButton) source;
		
		// 名称
		String name = button.getName();
		if (SHUTDOWN_TEXT.equals(name)) {
			if (listener != null) {
				ButtonModel model = button.getModel();
				model.setPressed(false);
				model.setRollover(false);
				model.setSelected(false);
				model.setArmed(false);

				// 弹出关闭窗口
				listener.doShutdownFromEdge();
			}
		} else if (RUN_TEXT.equals(name)) {
			if (listener != null) {
				listener.doRunFromEdge();
			}
		}
	}

	// 边框10个像素，对应CompoundBorder形成的像素
	final int borderWidth = 10;
	
	/**
	 * 定义边框，NIMBUS界面无边框，METAL界面是一条线
	 */
	private void setDefaultBorder() {
		if (isMetalUI()) {
			Color c = UIManager.getColor("Panel.background");
			if (c == null) {
				c = getBackground();
			}
			// 如果是灰色，颜色加深处理
			if (Skins.isGraySkin()) {
				Color light = new ESL(120, 18,70).toColor(); // 与背景图片颜色接近一致
				LineBorder outside = new LineBorder(light, 1, true);
				EmptyBorder inside = new EmptyBorder(0, 4, 0, 4);
				setBorder(new CompoundBorder(outside, inside));
			} else {
				ESL esl = new RGB(c).toESL();
				Color light = esl.toBrighter(20).toColor();
				LineBorder outside = new LineBorder(light, 1, true);
				EmptyBorder inside = new EmptyBorder(0, 4, 0, 4);
				setBorder(new CompoundBorder(outside, inside));
			}
		}
	}
	
	public void init() {
		// 部署面板
		banner = new DockBanner();
		banner.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		banner.setLayout(new FlowLayout(FlowLayout.LEFT, RayDock.DOCK_BUTTON_GAP, 0));
		banner.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		// 垂直
		sperator = new JSeparator(SwingConstants.VERTICAL);
		sperator.setBorder(new EmptyBorder(5, 1, 5, 1));

		setDefaultBorder();
		
		// 透明
		setOpaque(false);
		sperator.setOpaque(false);
		banner.setOpaque(false);
		
		setLayout(new BorderLayout(0, 0));
		add(sperator, BorderLayout.WEST);
		add(banner, BorderLayout.CENTER);
	}
	
	/**
	 * 加载默认的按纽
	 */
	public void loadDefaultButtons() {
		showButtons();
	}

	private DockButton createButton(String link, String tooltip, String name) {
		ImageIcon icon = (ImageIcon) UIManager.getIcon(link);
		if (icon == null) {
			return null;
		}
		DockButton button = new DockButton();
		
		String text = UIManager.getString(tooltip);
		if (text != null) {
			button.setToolTipText(text);
		}
		button.setName(name);
		// button.setIcon(icon, 28, -30);
		button.setIcon(icon, PlatformButton.DOCK_BUTTON_BRIGHTER, PlatformButton.DOCK_BUTTON_DARK);	
		button.addActionListener(this);
		button.setOpaque(false); // 透明...
		button.setVisible(true);
		return button;
	}
	
	/**
	 * 显示默认的按纽
	 */
	private void showButtons() {
		// 增加关闭和运行两个按纽
		DockButton button = createButton("DockBar.RunIcon", "DockBar.RunTooltip", RUN_TEXT);
		if (button != null) {
			banner.add(button);
		}
		button = createButton("DockBar.ShutdownIcon", "DockBar.ShutdownTooltip", SHUTDOWN_TEXT);
		if (button != null) {
			banner.add(button);
		}
		
		// 刷新
		revalidate();
		repaint();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		// 特别注意！这里必须保持一个空方法，不会任何处理，否则VM会调用它的父类，这时窗口边缘会出现“毛刺”现象，影响了整体美观效果。
	}
	
	/**
	 * 绘制METAL背景色
	 * @param g
	 */
	private boolean paintMetalComponent(Graphics g, String key, int v) {
		ImageIcon icon = (ImageIcon) UIManager.getIcon(key); 
		if (icon == null) {
			return false;
		}
		
		// 宽度和高度
		int width = getWidth();
		int height = getHeight();
		
		Image image = icon.getImage();
		int imgWidth = image.getWidth(null);
		int imgHeight = image.getHeight(null);
		
		for (int y = 0; y < height; y += imgHeight) {
			for (int x = 0; x < width; x += imgWidth) {
				g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
			}
		}
		// 调整间隔符颜色
		setSeparatorBackground(v);
		return true;
	}
	
	/**
	 * 设置分隔符的高亮背景色
	 */
	private void setSeparatorBackground(int brighter) {
		Color c = UIManager.getColor("Separator.background");
		if (c != null) {
			if (brighter > 0) {
				ESL esl = new ESL(c);
				c = esl.toBrighter(brighter).toColor();
				sperator.setBackground(c);
			} else {
				sperator.setBackground(new Color(c.getRGB()));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		// 如果是灰色，定义为
		boolean b = false;
		if (Skins.isGraySkin()) {
			b = paintMetalComponent(g, "DockBar.WallWhiteIcon", 60);
		} else if (Skins.isBronzSkin()) {
			b = paintMetalComponent(g, "DockBar.WallBronzIcon", 150);
		} else if (Skins.isCyanoSkin()) {
			b = paintMetalComponent(g, "DockBar.WallCyanoIcon", 100);
		} else if (Skins.isDarkSkin()) {
			b = paintMetalComponent(g, "DockBar.WallDarkIcon", 130);
		}
		// 不成功，绘制默认的
		if (!b) {
			super.paintComponent(g);
			setSeparatorBackground(0);
		}
	}
	
	private void updateButtonUI() {
		Component[] members = super.getComponents();
		int size = (members == null ? 0 : members.length);

		for (int i = 0; i < size; i++) {
			Component component = members[i];
			if (Laxkit.isClassFrom(component, JComponent.class)) {
				((JComponent) component).updateUI();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 边框
		setDefaultBorder();
		
		// 更新UI
		if (sperator != null) {
			sperator.updateUI();
			updateButtonUI();
		}
	}

}

///**
// * METAL灰色界面时
// * @param g
// */
//private void paintMetalGrayComponent(Graphics g) {
//	// 宽度和高度
//	int width = getWidth();
//	int height = getHeight();
//
//	ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallWhiteIcon");
//	if (icon == null) {
//		return;
//	}
//
//	Image image = icon.getImage();
//	int imgWidth = image.getWidth(null);
//	int imgHeight = image.getHeight(null);
//	
//	for (int y = 0; y < height; y += imgHeight) {
//		for (int x = 0; x < width; x += imgWidth) {
//			g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
//		}
//	}
//	
//	setSeparatorBackground(60);
//}
//
//private void paintMetalBronzComponent(Graphics g) {
//	// 宽度和高度
//	int width = getWidth();
//	int height = getHeight();
//
//	ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallBronzIcon");
//	if (icon == null) {
//		return;
//	}
//
//	Image image = icon.getImage();
//	int imgWidth = image.getWidth(null);
//	int imgHeight = image.getHeight(null);
//	
//	for (int y = 0; y < height; y += imgHeight) {
//		for (int x = 0; x < width; x += imgWidth) {
//			g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
//		}
//	}
//	
//	// 调亮
//	setSeparatorBackground(150);
//}
//
//private void paintMetalCyanoComponent(Graphics g) {
//	// 宽度和高度
//	int width = getWidth();
//	int height = getHeight();
//
//	ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallCyanoIcon");
//	if (icon == null) {
//		return;
//	}
//
//	Image image = icon.getImage();
//	int imgWidth = image.getWidth(null);
//	int imgHeight = image.getHeight(null);
//	
//	for (int y = 0; y < height; y += imgHeight) {
//		for (int x = 0; x < width; x += imgWidth) {
//			g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
//		}
//	}
//	
//	setSeparatorBackground(180);
//}
//
///**
// * METAL灰色界面时
// * @param g
// */
//private void paintMetalDarkComponent(Graphics g) {
//	// 宽度和高度
//	int width = getWidth();
//	int height = getHeight();
//
//	ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallDarkIcon");
//	if (icon == null) {
//		return;
//	}
//
//	Image image = icon.getImage();
//	int imgWidth = image.getWidth(null);
//	int imgHeight = image.getHeight(null);
//	
//	for (int y = 0; y < height; y += imgHeight) {
//		for (int x = 0; x < width; x += imgWidth) {
//			g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
//		}
//	}
//	
//	setSeparatorBackground(60);
//}
