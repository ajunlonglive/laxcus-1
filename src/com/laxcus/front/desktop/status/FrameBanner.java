/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;

import com.laxcus.front.desktop.panel.*;

/**
 * 保存FRAME按纽的面板
 * 
 * @author scott.liang
 * @version 1.0 9/22/2021
 * @since laxcus 1.0
 */
class FrameBanner extends DesktopPanel {

	private static final long serialVersionUID = 3750831143383133292L;

	/** BANNER的高度 **/
	private int defaultHeight;

	/**
	 * 构造默认的FRAME按纽的面板
	 */
	public FrameBanner() {
		super();
		defaultHeight = 0;
	}

	/**
	 * 设置窗口按纽的尺寸
	 * @param button
	 */
	 void setButtonSize(FrameButton button) {
		int height = getSize().height;
//		Dimension d = new Dimension(DesktopFrameBar.FRAMEBUTTON_WIDTH, height);
		Dimension d = new Dimension(height, height);
		button.setMinimumSize(d);
		button.setMaximumSize(d);
		button.setPreferredSize(d);

//		System.out.printf("banner height %d\n", height);
	}

	/**
	 * 加一个按纽
	 * @param button
	 */
	public void add(FrameButton button) {
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
			if (component instanceof FrameButton) {
				FrameButton button = (FrameButton) component;
				setButtonSize(button);
				button.revalidate();
				button.repaint();
			}
		}
	}
	
//	/**
//	 * 取出全部运行窗口句柄
//	 * @return LightFrame数组，没有是空集合
//	 */
//	public LightFrame[] getLightFrames() {
//		ArrayList<LightFrame> array = new ArrayList<LightFrame>();
//		Component[] components = getComponents();
//		int size = (components == null ? 0 : components.length);
//		for (int i = 0; i < size; i++) {
//			Component component = components[i];
//			// 取出窗口句柄
//			if (component != null && (component instanceof FrameButton)) {
//				FrameButton button = (FrameButton) component;
//				array.add(button.getFrame());
//			}
//		}
//		// 输出
//		LightFrame[] a = new LightFrame[array.size()];
//		return array.toArray(a);
//	}

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
	//			//			for (FrameButton but : array) {
	//			//				setFrameButtonSize(but);
	//			//				if (but.isVisible()) {
	//			//					but.validate();
	//			//					but.repaint();
	//			//				}
	//			//			}
	//		}
	//	}

}
