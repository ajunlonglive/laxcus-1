/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dock;

import java.awt.*;

import com.laxcus.ray.panel.*;

/**
 * 保存FRAME按纽的面板
 * 
 * @author scott.liang
 * @version 1.0 9/22/2021
 * @since laxcus 1.0
 */
class DockBanner extends RayPanel {

	private static final long serialVersionUID = 3750831143383133292L;
	
	/** 宽度固定是36 **/
	static final int BUTTON_WIDTH = 36; // 48;

	/** BANNER的高度 **/
	private int defaultHeight;

	/**
	 * 构造默认的FRAME按纽的面板
	 */
	public DockBanner() {
		super();
		defaultHeight = 0;
	}

	/**
	 * 设置窗口按纽的尺寸
	 * @param button
	 */
	 void setButtonSize(DockButton button) {
		int height = getSize().height;
		// 高度与BANNER一致
		Dimension d = new Dimension(DockBanner.BUTTON_WIDTH, height); // DesktopFrameBar.FRAMEBUTTON_WIDTH, height);
		button.setMinimumSize(d);
		button.setMaximumSize(d);
		button.setPreferredSize(d);

//		System.out.printf("banner height %d\n", height);
	}

	/**
	 * 加一个按纽
	 * @param button
	 */
	public void add(DockButton button) {
		setButtonSize(button);
		super.add(button);
	}

	/**
	 * 更新树
	 */
	public void updateTree() {
		super.validateTree();
	}
	
	/**
	 * 更新尺寸
	 */
	private void updateButtonsSize() {
		int height = getSize().height;

		//		System.out.printf("updateButtonSize , Old %d -> New %d\n",
		//				defaultHeight, height);

		// 高度一样，忽略
		if (defaultHeight == height) {
			return;
		}

		// 重新定义按纽的尺寸
		defaultHeight = height;

		Component[] components = getComponents();
		int size = (components == null ? 0 : components.length);
		for (int i = 0; i < size; i++) {
			Component component = components[i];
			if (component == null) {
				continue;
			}

			// 重新设置尺寸
			if (component instanceof DockButton) {
				DockButton button = (DockButton) component;
				setButtonSize(button);
				button.revalidate();
				button.repaint();
			}
		}
	}

	//	class UpdateButtonSize extends SwingEvent {
	//		UpdateButtonSize(){
	//			super();
	//		}
	//
	//		@Override
	//		public void process() {
	//			updateButtonsSize();
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		updateButtonsSize();

		//		// 重新定义尺寸
		//		UpdateButtonSize thread = new UpdateButtonSize();
		//		// thread.setIntervalTime(800);
		//		// thread.setTouchTime(System.currentTimeMillis() + 800);
		//		addThread(thread);
	}
	
//	private void paintNimbusComponent(Graphics g) {
//		// 宽度和高度
//		int width = getWidth();
//		int height = getHeight();
//
//		ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallIcon");
//
//		Image image = icon.getImage();
//		int imgWidth = image.getWidth(null);
//		int imgHeight = image.getHeight(null);
//
//		// 拉伸铺满
//		Color old = g.getColor();
//
//		g.drawImage(image, 0, 0, width - 1, height - 1, 0, 0, imgWidth - 1,
//				imgHeight - 1, null);
//		
//		g.setColor(old);
//	}
	
//	protected void paintComponent(Graphics g) {
//		if(this.isNimbusUI()) {
//			paintNimbusComponent(g);
//		} else {
//			super.paintComponent(g);
//		}
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintBorder(Graphics g) {
//		Dimension d = getSize();
//		int width = d.width; // getWidth();
//		int height = d.height; // getHeight();
//
//		int x = 0;
//		int y = 0;
//		int x2 = x + width - 1;
//		int y2 = y + height - 1;
//
//		// 原色
//		Color oldColor = g.getColor();
//
//		// 暗色
//		g.setColor(Color.DARK_GRAY);
//		g.drawLine(x, y, x2, y); // 上线
//		g.drawLine(x, y, x, y2); // 左线
//		// 高亮色
//		g.drawLine(x, y2, x2, y2); // 下线
//		g.drawLine(x2, y, x2, y2); // 右线
//
//		// 设置颜色
//		g.setColor(oldColor);
//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.JPanel#updateUI()
	//	 */
	//	@Override
	//	public void updateUI() {
	//		super.updateUI();
	//
	//		// 高度不一致时，更新
	//		int height = getHeight();
	//		if (height != bannerHeight) {
	//			// 记录高度
	//			bannerHeight = height;
	//			validate();
	//			repaint();
	//			//			// 调整按纽
	//			//			for (DockButton but : array) {
	//			//				setDockButtonSize(but);
	//			//				if (but.isVisible()) {
	//			//					but.validate();
	//			//					but.repaint();
	//			//				}
	//			//			}
	//		}
	//	}

}