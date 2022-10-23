/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;

import com.laxcus.front.desktop.util.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.frame.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 桌面图标按纽
 * 
 * @author scott.liang
 * @version 1.0 9/18/2021
 * @since laxcus 1.0
 */
class FrameButton extends CraftButton {

	private static final long serialVersionUID = -7696835544565522221L;

	private static final Color NIMBUS_ACTIVE = new Color(225,225,225); // 187, 192, 199);  // 143,23,182

	private static final Color NIMBUS_INACTIVE = new Color(208,208,208); // 186, 190, 198); // 147,23, 181

	//	private static final Color NIMBUS_ACTIVE = new ESL(147, 30, 206).toBrighter(12).toColor(); // 187, 192, 199);  // 143,23,182
	//	
	//	private static final Color NIMBUS_INACTIVE =	new ESL(147, 30, 196).toColor();

	/** 左侧开始间隔 **/
	static final int X_GAP = 6;

	/** 按下时，下沉2个像素 **/
	static final int PRESS_Y_ASCENT = 1;

	/** NIMBUS, 阴影是灰色 **/
	private Color shadowColor = Color.GRAY;

	/** NIMBUS, 高亮是白色 **/
	private Color lightColor = Color.WHITE;

	/** 图标 **/
	private Icon logo;

	/** 保存窗口句柄 **/
	private LightFrame frame;

	/** 按下按纽 **/
	private boolean pressed;

	/** 弹出菜单 **/
	private JPopupMenu rockMenu;
	
//	private RockMenu rockMenu;

	/** 悬浮窗口 **/
	private SnapshotWindow snapshotWindow;

	/** 浮窗进入状态 **/
	private volatile boolean loadSnapshot;

	/** 悬浮窗口模式 **/
	private int snapshotMode;

	/** 按纽组 **/
	private ButtonGroup group = new ButtonGroup();

//	/**
//	 * 构造默认的桌面图标按纽
//	 */
//	private FrameButton() {
//		super();
//		initButton();
//		initSnapshotWindow();
//		initPopupMenu();
//	}

	//	/**
	//	 * 构造默认的桌面图标按纽
	//	 */
	//	public FrameButton(LightFrame frame) {
	//		this();
	//
	//		// 图像参数
	//		Icon icon = frame.getFrameIcon();
	//		if (icon != null) {
	//			if (Laxkit.isClassFrom(icon, ImageIcon.class)) {
	//				ImageIcon im = (ImageIcon) icon;
	//				setIcon(im, 10, -10);
	//			} else {
	//				setIcon(icon);
	//			}
	//		}
	//		// 标题
	//		String text = frame.getTitle();
	//		if (text == null) {
	//			text = frame.getToolTipText();
	//		}
	//		if (text == null) {
	//			text = "...";
	//		}
	//
	//		setText(text);
	//		setToolTipText(text);
	//		
	//		// 设置边框，Insets基于这个值。系统默认是：java.awt.Insets[top=2,left=4,bottom=2,right=4]
	//		setBorder(new EmptyBorder(2, X_GAP, 2, 4));
	//
	//		setFrame(frame);
	//	}

	//	private ImageIcon getIcon(ImageIcon icon) {
	//		// 生成一个新图像
	//		BufferedImage buff = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
	//		Graphics2D gra = buff.createGraphics();
	//		buff = gra.getDeviceConfiguration().createCompatibleImage(32, 32, Transparency.TRANSLUCENT);
	//		buff.getGraphics().drawImage(icon.getImage(), 0, 0, 32, 32, null);
	//		// 输出
	//		try {
	//			ByteArrayOutputStream out = new ByteArrayOutputStream();
	//			ImageIO.write(buff, "png", out);
	//			out.flush();
	//			// 读出字节
	//			byte[] b = out.toByteArray();
	//			// 关闭！
	//			out.close();
	//			// 新的图片
	//			return new ImageIcon(b);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

	private ImageIcon zoomX(ImageIcon icon, int width, int height) {
		// 生成一个新图像
		BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = buff.createGraphics();
		buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		buff.getGraphics().drawImage(icon.getImage(), 0, 0, width, height, null);
		// 输出
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(buff, "png", out);
			out.flush();
			// 读出字节
			byte[] b = out.toByteArray();
			// 关闭！
			out.close();
			// 新的图片
			return new ImageIcon(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 构造默认的桌面图标按纽
	 */
	public FrameButton(LightFrame w) {
		super();
		frame = w;
		
		initButton();
		initSnapshotWindow();
		initPopupMenu();

		// 图像参数
		Icon icon = frame.getFrameBigIcon();
		if (icon != null) {
			if (Laxkit.isClassFrom(icon, ImageIcon.class)) {
				ImageIcon im = (ImageIcon) icon;
				setIcon(im, PlatformButton.FRAME_BUTTON_BRIGHTER, PlatformButton.FRAME_BUTTON_DARK);
			} else {
				setIcon(icon);
			}
		} else {
			icon = frame.getFrameIcon();
			if (icon != null) {
				if (Laxkit.isClassFrom(icon, ImageIcon.class)) {
					ImageIcon im = (ImageIcon) icon;
					im = zoomX(im, 32, 32); // 放大
					if (im != null) {
						setIcon(im, PlatformButton.FRAME_BUTTON_BRIGHTER, PlatformButton.FRAME_BUTTON_DARK);
					} else {
						setIcon((ImageIcon) icon, PlatformButton.FRAME_BUTTON_BRIGHTER, PlatformButton.FRAME_BUTTON_DARK);
					}
				} else {
					setIcon(icon);
				}
			}
		}

		//		// 标题
		//		String text = frame.getTitle();
		//		if (text == null) {
		//			text = frame.getToolTipText();
		//		}
		//		if (text == null) {
		//			text = "...";
		//		}
		//
		////		setText(text);
		//		setToolTipText(text);

		// 设置边框，Insets基于这个值。系统默认是：java.awt.Insets[top=2,left=4,bottom=2,right=4]
		//		setBorder(new EmptyBorder(2, X_GAP, 2, 4));
		setBorder(new EmptyBorder(0,0,0,0));

		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

	/**
	 * 初始化参数
	 */
	private void initButton() {
		pressed = false;
		// 间隔
		//		setIconTextGap(5);

		setIconTextGap(0);

		setContentAreaFilled(false); // 平面
		//		setBorderPainted(false); // 不绘制边框
		setFocusPainted(false); // 不绘制焦点边框
		setRolloverEnabled(true); // 反转...

		//		setBorder(new EmptyBorder(2, 4, 2, 4));
		//		setHorizontalAlignment(SwingConstants.LEFT);

		setBorder(new EmptyBorder(0,0,0,0));
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

//	/**
//	 * 设置单元参数
//	 * @param e FrameButtonItem实例
//	 */
//	private void setFrame(LightFrame e) {
//		frame = e;
//	}

	/**
	 * 返回单元参数
	 * @return FrameButtonItem实例
	 */
	public LightFrame getFrame() {
		return frame;
	}

	/**
	 * 设置图标
	 * @param icon
	 * @param brighter 高亮增加值
	 * @param dark 暗色
	 */
	public void setIcon(ImageIcon icon, int brighter, int dark) {
		super.setIcon(icon);
		logo = icon;

		// ESL的亮度增加
		if (brighter > 0) {
			// 亮
			ImageIcon image = brighter(icon, brighter);
			if (image != null) {
				super.setRolloverIcon(image);
			}
		}
		// 调暗
		if (dark < 0) {
			setRolloverEnabled(true);
			// 暗
			ImageIcon image = dark(icon, dark);
			if (image != null) {
				super.setPressedIcon(image);
				super.setSelectedIcon(image);
				super.setRolloverSelectedIcon(image);
			}
		}
	}

	/**
	 * 判断是焦点
	 * @return
	 */
	public boolean isPressed() {
		return pressed;
	}

	/**
	 * 设置为按下
	 * @param b
	 */
	public void setPressed(boolean b) {
		// 按下或者否
		pressed = b;
		repaint();
	}

	//	/**
	//	 * 返回匹配的背景颜色
	//	 * @param active
	//	 * @return
	//	 */
	//	private Color getBackground(boolean active) {
	//		if (isNimbusUI()) {
	//			return (active ? FrameButton.NIMBUS_ACTIVE : FrameButton.NIMBUS_INACTIVE);
	//		} else if (isMetalUI()) {
	//			Color c = (active ? MetalLookAndFeel.getWindowTitleBackground()
	//					: MetalLookAndFeel.getWindowTitleInactiveBackground());
	//			if (c != null) {
	//				if (active) {
	//					ESL esl = new ESL(c); // new RGB(c).toESL();
	//					esl.darker(10); // 调暗一些，降低10
	//					return esl.toColor();
	//				} else {
	//					return new Color(c.getRGB());
	//				}
	//			}
	//		}
	//
	//		// 背景
	//		Color color = getBackground();
	//		if (color == null) {
	//			color = new Color(208, 208, 208);
	//		}
	//		// 非激活，返回这个颜色
	//		if (!active) {
	//			return color;
	//		}
	//		// 根据UI调亮
	//		int light = (isNimbusUI() ? 25 : 30);
	//		ESL esl = new RGB(color).toESL();
	//		esl.brighter(light); // 调亮
	//		return esl.toColor();
	//	}

	/**
	 * 返回匹配的背景颜色
	 * @param active
	 * @return
	 */
	private Color getBackground(boolean active) {
		if (isNimbusUI() || Skins.isGraySkin()) {
			// 激活时
			if (active) {
				return FrameButton.NIMBUS_ACTIVE;
			}
			// 非激活
			Color c = UIManager.getColor("Panel.background");
			if (c != null) {
				return new Color(c.getRGB());
			}
			return FrameButton.NIMBUS_INACTIVE;
		} else if (isMetalUI()) {
			Color c = (active ? MetalLookAndFeel.getWindowTitleBackground()
					: MetalLookAndFeel.getWindowTitleInactiveBackground());
			if (c != null) {
				if (active) {
					ESL esl = new ESL(c); // new RGB(c).toESL();
					esl.darker(10); // 调暗一些，降低10
					return esl.toColor();
				} else {
					return new Color(c.getRGB()); // 原色...
				}
			}
		}

		// 背景
		Color color = getBackground();
		if (color == null) {
			color = new Color(208, 208, 208);
		}
		// 非激活，返回这个颜色
		if (!active) {
			return color;
		}
		// 根据UI调亮
		int light = (isNimbusUI() ? 25 : 30);
		ESL esl = new RGB(color).toESL();
		esl.brighter(light); // 调亮
		return esl.toColor();
	}

	//	/**
	//	 * 平面状态
	//	 */
	//	private void paintPlainComponent(Graphics g) {
	//		final int width = getWidth();
	//		final int height = getHeight();
	//
	//		Color back = g.getColor();
	//		
	////		Color background = getBackground(false);
	//		Color background = UIManager.getColor("Panel.background");
	//		if (background == null) {
	//			background = getBackground(false);
	//		}
	//		g.setColor(background);
	//		g.fillRect(0, 0, width, height);
	//
	//		// 字符串的X值
	//		int stringX = 0;
	//
	//		Insets insets = getInsets();
	//		int x = (insets != null ? insets.left : X_GAP);
	//		
	//		if (logo != null) {
	//			int y = (height - logo.getIconHeight()) / 2;
	//			
	//			// 进入了...
	//			ButtonModel model = getModel();
	//			if (model.isRollover()) {
	//				Icon icon = getRolloverIcon();
	//				if (icon != null) {
	//					icon.paintIcon(this, g, x, y);
	//				}
	//			} else {
	//				logo.paintIcon(this, g, x, y);
	//			}
	//
	//			// 定位字符串的X值
	//			stringX = x + logo.getIconWidth() + getIconTextGap();
	//		} else {
	//			stringX = x + getIconTextGap();
	//		}
	//
	//		// 前景颜色
	//		Color color = getForeground();
	//		if (color == null) {
	//			color = Color.DARK_GRAY;
	//		}
	//		g.setColor(color);
	//		g.setFont(getFont());
	//
	//		FontMetrics fm = g.getFontMetrics();
	//		int fontHeight = fm.getHeight();
	//		int stringY = (height - fontHeight) / 2;
	//		stringY = stringY + fm.getAscent();// + 1;  // 下沉1像素
	//
	//		// 显示文本
	//		String text = getText();
	//		if (text == null) {
	//			text = "";
	//		}
	//
	//		// 字符串
	//		int stringWidth = fm.stringWidth(text);
	//		int leftWidth = width - stringX;
	//		// 字符串宽度超过剩余长度
	//		if (stringWidth > leftWidth) {
	//			StringBuilder bf = new StringBuilder();
	//			int gap = (insets != null ? insets.right + 10 : 4);
	//			for (int i = 0; i < text.length(); i++) {
	//				bf.append(text.charAt(i));
	//				String s = bf.toString() + "...";
	//				int charWidth = fm.stringWidth(s);
	//				if (charWidth + gap >= leftWidth) {
	//					text = s;
	//					break;
	//				}
	//			}
	//		}
	//
	//		g.drawString(text, stringX, stringY);
	//
	//		// 恢复背景色
	//		g.setColor(back);
	//	}

	/**
	 * 平面状态
	 */
	private void paintPlainComponent(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();

		Color back = g.getColor();

		//		Color background = getBackground(false);
		Color background = UIManager.getColor("Panel.background");
		if (background == null) {
			background = getBackground(false);
		}
		g.setColor(background);
		g.fillRect(0, 0, width, height);

		//		// 字符串的X值
		//		int stringX = 0;

		//		Insets insets = getInsets();
		//		int x = (insets != null ? insets.left : X_GAP);


		if (logo != null) {
			int x = (width - logo.getIconWidth()) / 2;
			if (x < 0) x = 0;
			int y = (height - logo.getIconHeight()) / 2;
			if (y < 0) y = 0;

			// 进入了...
			ButtonModel model = getModel();
			if (model.isRollover()) {
				Icon icon = getRolloverIcon();
				if (icon != null) {
					icon.paintIcon(this, g, x, y);
				}
			} else {
				logo.paintIcon(this, g, x, y);
			}

			//			// 定位字符串的X值
			//			stringX = x + logo.getIconWidth() + getIconTextGap();
		} 
		//		else {
		//			stringX = x + getIconTextGap();
		//		}

		//		// 前景颜色
		//		Color color = getForeground();
		//		if (color == null) {
		//			color = Color.DARK_GRAY;
		//		}
		//		g.setColor(color);
		//		g.setFont(getFont());
		//
		//		FontMetrics fm = g.getFontMetrics();
		//		int fontHeight = fm.getHeight();
		//		int stringY = (height - fontHeight) / 2;
		//		stringY = stringY + fm.getAscent();// + 1;  // 下沉1像素
		//
		//		// 显示文本
		//		String text = getText();
		//		if (text == null) {
		//			text = "";
		//		}
		//
		//		// 字符串
		//		int stringWidth = fm.stringWidth(text);
		//		int leftWidth = width - stringX;
		//		// 字符串宽度超过剩余长度
		//		if (stringWidth > leftWidth) {
		//			StringBuilder bf = new StringBuilder();
		//			int gap = (insets != null ? insets.right + 10 : 4);
		//			for (int i = 0; i < text.length(); i++) {
		//				bf.append(text.charAt(i));
		//				String s = bf.toString() + "...";
		//				int charWidth = fm.stringWidth(s);
		//				if (charWidth + gap >= leftWidth) {
		//					text = s;
		//					break;
		//				}
		//			}
		//		}
		//
		//		g.drawString(text, stringX, stringY);

		// 恢复背景色
		g.setColor(back);
	}

	//	/**
	//	 * 阴刻下沉
	//	 */
	//	private void paintPressedComponent(Graphics g) {
	//		final int width = getWidth();
	//		final int height = getHeight();
	//
	//		Color back = g.getColor();
	//		
	//		// 选择不同的背景...
	//		Color background = getBackground(true);
	//		g.setColor(background);
	//		g.fillRect(0, 0, width, height);
	//
	//		// 字符串的X值
	//		int stringX = 0;
	//
	//		Insets insets = getInsets();
	//		int x = (insets != null ? insets.left : X_GAP);
	//		
	//		if (logo != null) {
	//			int y = (height - logo.getIconHeight()) / 2;
	//			if (y < 0) y = 0;
	//			y += PRESS_Y_ASCENT; // 下沉2个像素
	//
	//			ButtonModel model = getModel();
	//			if (model.isPressed() || pressed) {
	//				Icon icon = getSelectedIcon();
	//				if (icon != null) {
	//					icon.paintIcon(this, g, x, y);
	//				} else {
	//					logo.paintIcon(this, g, x, y);
	//				}
	//			} else {
	//				logo.paintIcon(this, g, x, y);
	//			}
	//
	//			// 定位字符串的X值
	//			stringX = x + logo.getIconWidth() + getIconTextGap();
	//		} else {
	//			stringX = x + getIconTextGap();
	//		}
	//
	//		// 前景颜色
	//		Color color = getForeground();
	//		if (color == null) {
	//			color = Color.DARK_GRAY;
	//		}
	//		g.setColor(color);
	//		g.setFont(getFont());
	//
	//		FontMetrics fm = g.getFontMetrics();
	//		int fontHeight = fm.getHeight();
	//		int stringY = (height - fontHeight) / 2;
	//		if(stringY < 0) stringY = 0;
	//		stringY = stringY + fm.getAscent();// + 1;  // 下沉1像素
	//		stringY += PRESS_Y_ASCENT; // 下沉2个像素
	//
	//		// 显示文本
	//		String text = getText();
	//		if (text == null) {
	//			text = "";
	//		}
	//
	//		// 字符串
	//		int stringWidth = fm.stringWidth(text);
	//		int leftWidth = width - stringX;
	//		// 字符串宽度超过剩余长度
	//		if (stringWidth > leftWidth) {
	//			StringBuilder bf = new StringBuilder();
	//			int gap = (insets != null ? insets.right + 10 : 4);
	//			for (int i = 0; i < text.length(); i++) {
	//				bf.append(text.charAt(i));
	//				String s = bf.toString() + "...";
	//				int charWidth = fm.stringWidth(s);
	//				if (charWidth + gap >= leftWidth) {
	//					text = s;
	//					break;
	//				}
	//			}
	//		}
	//
	//		g.drawString(text, stringX, stringY);
	//
	//		// 恢复背景色
	//		g.setColor(back);
	//	}

	/**
	 * 阴刻下沉
	 */
	private void paintPressedComponent(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();

		Color back = g.getColor();

		// 选择不同的背景...
		Color background = getBackground(true);
		g.setColor(background);
		g.fillRect(0, 0, width, height);

		//		// 字符串的X值
		//		int stringX = 0;
		//
		//		Insets insets = getInsets();
		//		int x = (insets != null ? insets.left : X_GAP);



		if (logo != null) {
			int x = (width - logo.getIconWidth()) / 2;
			if (x < 0) x = 0;
			int y = (height - logo.getIconHeight()) / 2;
			if (y < 0) y = 0;
			y += PRESS_Y_ASCENT; // 下沉2个像素

			ButtonModel model = getModel();
			if (model.isPressed() || model.isSelected() || pressed) {
				Icon icon = getPressedIcon(); // getSelectedIcon();
				if (icon != null) {
					icon.paintIcon(this, g, x, y);
				} else {
					logo.paintIcon(this, g, x, y);
				}
			} else {
				logo.paintIcon(this, g, x, y);
			}

			//			// 定位字符串的X值
			//			stringX = x + logo.getIconWidth() + getIconTextGap();
		} 

		//		else {
		//			stringX = x + getIconTextGap();
		//		}

		//		// 前景颜色
		//		Color color = getForeground();
		//		if (color == null) {
		//			color = Color.DARK_GRAY;
		//		}
		//		g.setColor(color);
		//		g.setFont(getFont());
		//
		//		FontMetrics fm = g.getFontMetrics();
		//		int fontHeight = fm.getHeight();
		//		int stringY = (height - fontHeight) / 2;
		//		if(stringY < 0) stringY = 0;
		//		stringY = stringY + fm.getAscent();// + 1;  // 下沉1像素
		//		stringY += PRESS_Y_ASCENT; // 下沉2个像素
		//
		//		// 显示文本
		//		String text = getText();
		//		if (text == null) {
		//			text = "";
		//		}
		//
		//		// 字符串
		//		int stringWidth = fm.stringWidth(text);
		//		int leftWidth = width - stringX;
		//		// 字符串宽度超过剩余长度
		//		if (stringWidth > leftWidth) {
		//			StringBuilder bf = new StringBuilder();
		//			int gap = (insets != null ? insets.right + 10 : 4);
		//			for (int i = 0; i < text.length(); i++) {
		//				bf.append(text.charAt(i));
		//				String s = bf.toString() + "...";
		//				int charWidth = fm.stringWidth(s);
		//				if (charWidth + gap >= leftWidth) {
		//					text = s;
		//					break;
		//				}
		//			}
		//		}
		//
		//		g.drawString(text, stringX, stringY);

		// 恢复背景色
		g.setColor(back);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		// 阴刻 / 阳刻
		if (pressed) {
			paintPressedComponent(g);
		} else {
			// super.paintComponent(g);
			paintPlainComponent(g);
		}
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
	//	 */
	//	@Override
	//	protected void paintBorder(Graphics g) {
	//		int width = getWidth();
	//		int height = getHeight();
	//
	//		if (pressed) {
	//			paintLowerdBorder(this, g, 0, 0, width, height);
	//		} else {
	//			paintRaisedBorder(this, g, 0, 0, width, height);
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		int width = getWidth();
		int height = getHeight();

		if (pressed) {
			paintLowerdBorder(this, g, 0, 0, width, height);
		} else {
			ButtonModel bm = getModel();
			if (bm.isRollover()) {
				if (bm.isPressed()) {
					paintLowerdBorder(this, g, 0, 0, width, height);
				} else {
					paintRaisedBorder(this, g, 0, 0, width, height);
				}
			} else if (bm.isPressed()) {
				paintLowerdBorder(this, g, 0, 0, width, height);
			} else {
				// 平面，不处理
			}
		}
	}

	/**
	 * 绘制阳刻浮雕界面
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintRaisedBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color dark = null;
		Color light = null;

		// 如果是“Nimbus”界面，生成蚀刻效果；如果不是，平面显示
		if (isNimbusUI()) {
			dark = shadowColor;
			light = lightColor;
		} else {
			Color color = c.getBackground();
			ESL esl = new RGB(color).toESL();
			dark = esl.toDraker(50).toColor();
			light = esl.toBrighter(50).toColor();
		}

		int x2 = x + width - 1;
		int y2 = y + height - 1;

		// 原色
		Color oldColor = g.getColor();

		// 亮色
		g.setColor(light);
		g.drawLine(x, y, x2, y); // 上线
		g.drawLine(x, y, x, y2); // 左线

		// 暗色
		g.setColor(dark);
		g.drawLine(x, y2, x2, y2); // 下线
		g.drawLine(x2, y, x2, y2); // 右线

		// 设置颜色
		g.setColor(oldColor);
	}

	/**
	 * 绘制阴刻浮雕界面
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintLowerdBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color shadow = null;
		Color light = null;

		// 如果是“Nimbus”界面，生成蚀刻效果；如果不是，平面显示
		if (isNimbusUI()) {
			shadow = shadowColor;
			light = lightColor;
		} else {
			// 取组件的背景色
			Color color = c.getBackground();
			ESL esl = new RGB(color).toESL();
			shadow = esl.toDraker(50).toColor();
			light = esl.toBrighter(50).toColor();

			//			light = MetalLookAndFeel.getWindowTitleBackground();
			//			shadow = MetalLookAndFeel.getWindowTitleInactiveBackground();
		}

		int x2 = x + width - 1;
		int y2 = y + height - 1;

		// 原色
		Color oldColor = g.getColor();

		// 暗色
		g.setColor(shadow);
		g.drawLine(x, y, x2, y); // 上线
		g.drawLine(x, y, x, y2); // 左线

		// 高亮色
		g.setColor(light);
		g.drawLine(x, y2, x2, y2); // 下线
		g.drawLine(x2, y, x2, y2); // 右线

		// 设置颜色
		g.setColor(oldColor);
	}

	/** 以下是菜单 **/

//	/**
//	 * 返回助记符
//	 * @param mnemonic
//	 * @return
//	 */
//	private char getMnemonic(String mnemonic) {
//		if (mnemonic == null) {
//			return 0;
//		}
//		String ws = UIManager.getString(mnemonic);
//		if (ws == null || ws.length() == 0) {
//			return 0;
//		}
//
//		ws = ws.trim();
//		if (ws.length() > 0) {
//			return ws.charAt(0);
//		}
//		return 0;
//	}

//	/**
//	 * 判断是字符
//	 * @param w
//	 * @return
//	 */
//	private boolean isWord(char w) {
//		return (w >= 'A' && w <= 'Z') || (w >= 'a' && w <= 'z');
//	}

//	/**
//	 * 生成菜单项
//	 * @param key
//	 * @param mnemonic
//	 * @param method
//	 * @return
//	 */
//	private JMenuItem createMenuItem(JMenuItem item, String iconKey, String key, String mnemonic, String method) {
//		FontKit.setButtonText(item, UIManager.getString(key));
//		// 助记符
//		char w = getMnemonic(mnemonic);
//		if (isWord(w)) {
//			item.setMnemonic(w);
//		}
//		// 方法名
//		if (method != null) {
//			String linkText = UIManager.getString(method);
//			if (linkText != null) {
//				item.setName(linkText);
//				// 菜单事件
//				item.addActionListener(new MenuItemClick());
//			}
//		}
//		// 图标
//		if (iconKey != null) {
//			Icon icon = UIManager.getIcon(iconKey);
//			if (icon != null) {
//				item.setIcon(icon);
//				item.setIconTextGap(4);
//			}
//		}
//
//		// 默认有效
//		item.setEnabled(true);
//		// 边框
////		item.setBorder(new EmptyBorder(6, 5, 6, 5));
//		
//		item.setBorder(new EmptyBorder(3,4,3,4));
//
//		return item;
//	}

//	/**
//	 * 生成菜单项
//	 * @param key
//	 * @param mnemonic
//	 * @param method
//	 * @return
//	 */
//	private JMenuItem createMenuItem(String iconKey, String key, String mnemonic, String method) {
//		return createMenuItem(new JMenuItem(), iconKey, key, mnemonic, method);
//	}
	

	//	/**
	//	 * 生成菜单项
	//	 * @param key
	//	 * @param mnemonic
	//	 * @param method
	//	 * @return
	//	 */
	//	private JMenuItem createMenuItem(String iconKey, String key, String mnemonic, String method) {
	//		JMenuItem item = new JMenuItem();
	//		FontKit.setButtonText(item, UIManager.getString(key));
	//		// 助记符
	//		char w = getMnemonic(mnemonic);
	//		if (isWord(w)) {
	//			item.setMnemonic(w);
	//		}
	//		// 方法名
	//		item.setName(UIManager.getString(method)); 
	//		// 菜单事件
	//		item.addActionListener(new MenuItemClick());
	//		
	//		// 图标
	//		if (iconKey != null) {
	//			Icon icon = UIManager.getIcon(iconKey);
	//			if (icon != null) {
	//				item.setIcon(icon);
	//				item.setIconTextGap(4);
	//			}
	//		}
	//		
	//		// 默认有效
	//		item.setEnabled(true);
	//		// 边框
	//		item.setBorder(new EmptyBorder(6, 5, 6, 5));
	//
	//		return item;
	//	}

	class MenuItemClick implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			click(e);
		}
	}

	class ButtonMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}
	}

//	/**
//	 * 根据方法名称，查找菜单项
//	 * @param launchMenu 弹出菜单
//	 * @param method 方法名称
//	 * @return 返回匹配方法名称的菜单项目，没有是空指针
//	 */
//	private JMenuItem findMenuItemByMethod(String method) {
//		if(rockMenu == null) {
//			return null;
//		}
//		int count = rockMenu.getComponentCount();
//		for (int index = 0; index < count; index++) {
//			Component element = rockMenu.getComponent(index);
//			if (element == null) {
//				continue;
//			}
//			if(Laxkit.isClassFrom(element, JMenuItem.class) ) {
//				JMenuItem item = (JMenuItem)element;
//				String text = item.getName();
//				if (method.equals(text)) {
//					return item;
//				}
//			}
//		}
//		return null;
//	}

	/**
	 * 显示弹出菜单
	 * @param e
	 */
	private void showPopupMenu(MouseEvent e) {
		// 不满足SWING条件的POPUP触发，不处理
		if (!e.isPopupTrigger()) {
			return;
		}

		// 还原
		JMenuItem item = MenuBuilder.findMenuItemByMethod(rockMenu, "doMenuRestore");
		if (item != null) {
			item.setEnabled(hasMenuRestore());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doMenuMini");
		if (item != null) {
			item.setEnabled(hasMenuMini());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doMenuMax");
		if (item != null) {
			item.setEnabled(hasMenuMax());
		}
		// 标题栏菜单
		redoTitlePaneMenu(frame.isHiddenTitle());

		int newX = e.getX();
		int newY = e.getY();
		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}

	/**
	 * 菜单事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object object = event.getSource();
		// 必须是继承自“JMenuItem”
		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
			JMenuItem source = (JMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}

	/**
	 * 调用实例
	 * @param methodName
	 */
	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		}
	}

	/**
	 * 生成悬浮窗口菜单
	 * @return 返回菜单实例
	 */
	private JMenu createSnapshotMenu() {
//		JMenu menu = (JMenu) createMenuItem(new JMenu(), null, "StatusBar.FrameButton.PopupMenu.SnapshotWindow.MenuText","StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideMWord",null);

//		JRadioButtonMenuItem zoom = (JRadioButtonMenuItem) createMenuItem(new JRadioButtonMenuItem(),
//				null,"StatusBar.FrameButton.PopupMenu.SnapshotWindow.ZoomText",
//				"StatusBar.FrameButton.PopupMenu.SnapshotWindow.ZoomMWord", "StatusBar.FrameButton.PopupMenu.SnapshotWindow.ZoomMethod");
//		JRadioButtonMenuItem fill = (JRadioButtonMenuItem) createMenuItem(new JRadioButtonMenuItem(),
//				null,"StatusBar.FrameButton.PopupMenu.SnapshotWindow.FillText",
//				"StatusBar.FrameButton.PopupMenu.SnapshotWindow.FillMWord", "StatusBar.FrameButton.PopupMenu.SnapshotWindow.FillMethod");
//		JRadioButtonMenuItem hide = (JRadioButtonMenuItem) createMenuItem(new JRadioButtonMenuItem(),
//				null,"StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideText",
//				"StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideMWord", "StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideMethod");

		JMenu menu = MenuBuilder.createMenu(null, "StatusBar.FrameButton.PopupMenu.SnapshotWindow.MenuText",
				"StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideMWord", null, null, null, null);
		
		JRadioButtonMenuItem zoom = (JRadioButtonMenuItem) MenuBuilder.createMenuItem(new JRadioButtonMenuItem(), null,
						"StatusBar.FrameButton.PopupMenu.SnapshotWindow.ZoomText", "StatusBar.FrameButton.PopupMenu.SnapshotWindow.ZoomMWord",
						null, "StatusBar.FrameButton.PopupMenu.SnapshotWindow.ZoomMethod", new MenuItemClick(), null);
		JRadioButtonMenuItem fill = (JRadioButtonMenuItem) MenuBuilder.createMenuItem(new JRadioButtonMenuItem(), null,
				"StatusBar.FrameButton.PopupMenu.SnapshotWindow.FillText", "StatusBar.FrameButton.PopupMenu.SnapshotWindow.FillMWord", 
				null, "StatusBar.FrameButton.PopupMenu.SnapshotWindow.FillMethod", new MenuItemClick(), null);
		JRadioButtonMenuItem hide = (JRadioButtonMenuItem) MenuBuilder.createMenuItem(new JRadioButtonMenuItem(), null,
				"StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideText", "StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideMWord", 
				null, "StatusBar.FrameButton.PopupMenu.SnapshotWindow.HideMethod", new MenuItemClick(), null);

		group.add(zoom);
		group.add(fill);
		group.add(hide);

		menu.add(zoom);
		menu.add(fill);
		menu.add(hide);
		// 选择
		if (SnapshotWindowModel.isZoom(snapshotMode)) {
			zoom.setSelected(true);
		} else if (SnapshotWindowModel.isFill(snapshotMode)) {
			fill.setSelected(true);
		} else if (SnapshotWindowModel.isHide(snapshotMode)) {
			hide.setSelected(true);
		}

		return menu;
	}

//	/**
//	 * 生成菜单项
//	 * @param iconKey
//	 * @param textKey
//	 * @param mnemonicKey
//	 * @param methodKey
//	 * @return
//	 */
//	private JMenuItem createMenuItem(String iconKey, String textKey, String mnemonicKey, String methodKey) {
//		return MenuBuilder.createMenuItem(iconKey, textKey, mnemonicKey, null, methodKey,
//				new MenuItemClick());
//	}

	/**
	 * 生成菜单项
	 * @param iconKey
	 * @param textKey
	 * @param mnemonicKey
	 * @param methodKey
	 * @return
	 */
	private JMenuItem createMenuItem(Icon icon, String iconKey, String textKey,
			String mnemonicKey, String methodKey) {
		return MenuBuilder.createMenuItem(icon, iconKey, textKey, mnemonicKey, null, methodKey, new MenuItemClick());
	}
	
	/**
	 * 初始化弹出菜单
	 */
	void initPopupMenu() {
		// 菜单
		rockMenu = new JPopupMenu();
		rockMenu.add(createSnapshotMenu());
		rockMenu.add(createMenuItem(null, null, "StatusBar.FrameButton.PopupMenu.HideText", 
				"StatusBar.FrameButton.PopupMenu.HideMWord", "StatusBar.FrameButton.PopupMenu.ShowHideMethod"));
		rockMenu.addSeparator();
		
		JMenuItem recover = createMenuItem(frame.getRestoreIcon(), "StatusBar.FrameButton.PopupMenu.RestoreIcon","StatusBar.FrameButton.PopupMenu.RestoreText", "StatusBar.FrameButton.PopupMenu.RestoreMWord", "StatusBar.FrameButton.PopupMenu.RestoreMethod");
		JMenuItem mini = createMenuItem(frame.getMinimizeIcon(), "StatusBar.FrameButton.PopupMenu.MiniIcon", "StatusBar.FrameButton.PopupMenu.MiniText", "StatusBar.FrameButton.PopupMenu.MiniMWord", "StatusBar.FrameButton.PopupMenu.MiniMethod");
		JMenuItem max = createMenuItem(frame.getMaximizeIcon(), "StatusBar.FrameButton.PopupMenu.MaxIcon","StatusBar.FrameButton.PopupMenu.MaxText", "StatusBar.FrameButton.PopupMenu.MaxMWord", "StatusBar.FrameButton.PopupMenu.MaxMethod");
		rockMenu.add(recover);
		rockMenu.add(mini);
		rockMenu.add(max);
		
		rockMenu.addSeparator();
		JMenuItem close = createMenuItem(frame.getCloseIcon(), "StatusBar.FrameButton.PopupMenu.CloseIcon","StatusBar.FrameButton.PopupMenu.CloseText", "StatusBar.FrameButton.PopupMenu.CloseMWord", "StatusBar.FrameButton.PopupMenu.CloseMethod");
		rockMenu.add(close);

		// 调用
		rockMenu.setInvoker(this);
		addMouseListener(new ButtonMouseAdapter());
	}

	void doMenuZoomSWindow() {
		// System.out.println("等比例收缩");
		snapshotMode = SnapshotWindowModel.ZOOM;
	}

	void doMenuFillSWindow() {
		// System.out.println("填充悬浮窗口");
		snapshotMode = SnapshotWindowModel.FILL;
	}

	void doMenuHideSWindow() {
		// System.out.println("隐藏");
		snapshotMode = SnapshotWindowModel.HIDE;
	}

	private boolean hasMenuRestore() {
		if (frame.isMaximizable() && frame.isMaximum() && frame.isIcon()) {
			//			try {
			//				frame.setIcon(false);
			//			} catch (PropertyVetoException e) {
			//
			//			}
			return true;
		} else if (frame.isMaximizable() && frame.isMaximum()) {
			//			try {
			//				frame.setMaximum(false);
			//			} catch (PropertyVetoException e) {
			//			}
			return true;
		} else if (frame.isIconifiable() && frame.isIcon()) {
			//			// try {
			//			frame.setIcon(false);
			//			// } catch (PropertyVetoException e) { }
			return true;
		}
		return false;
	}

	void doMenuRestore() {
		// 还原窗口
		if (frame.isMaximizable() && frame.isMaximum() && frame.isIcon()) {
			// try {
			frame.setIcon(false);
			// } catch (PropertyVetoException e) {
			//
			// }
		} else if (frame.isMaximizable() && frame.isMaximum()) {
			try {
				frame.setMaximum(false);
			} catch (PropertyVetoException e) {
			}
		} else if (frame.isIconifiable() && frame.isIcon()) {
			// try {
			frame.setIcon(false);
			// } catch (PropertyVetoException e) { }
		}
	}

	private boolean hasMenuMini() {
		// 最小化
		if (frame.isIconifiable()) {
			if (!frame.isIcon()) {
				// // try {
				// frame.setIcon(true);
				// // } catch (PropertyVetoException e1) {
				// // }
				return true;
			} else {
				// // try {
				// frame.setIcon(false);
				// // } catch (PropertyVetoException e1) {
				// // }
				return false;
			}
		}
		return false;
	}

	void doMenuMini() {
		// 最小化
		if (frame.isIconifiable()) {
			if (!frame.isIcon()) {
				// try {
				frame.setIcon(true);
				// } catch (PropertyVetoException e1) {
				// }
			} else {

				// try {
				frame.setIcon(false);
				// } catch (PropertyVetoException e1) {
				// }
			}
		}
	}

	//	private boolean hasMenuMax() {
	//		if (frame.isMaximizable()) {
	//			if (frame.isMaximum() && frame.isIcon()) {
	//				// frame.setIcon(false);
	//				return true;
	//			} else if (!frame.isMaximum()) {
	//				// try {
	//				// frame.setMaximum(true);
	//				// } catch (PropertyVetoException e) { }
	//				return true;
	//			} else {
	//				// try {
	//				// frame.setMaximum(false);
	//				// } catch (PropertyVetoException e) { }
	//				return false;
	//			}
	//		}
	//		return false;
	//	}

	private boolean hasMenuMax() {
		if (frame.isMaximizable()) {
			// 最小化后，最大化不可用，需要经过还原才能再用
			if (frame.isIcon()) {
				return false;
			}
			// 已经最大化，返回假
			if (frame.isMaximum()) {
				return false;
			} else {
				return true;
			}

			//			if (frame.isMaximum() && frame.isIcon()) {
			//				// frame.setIcon(false);
			//				return true;
			//			} else if (!frame.isMaximum()) {
			//				// try {
			//				// frame.setMaximum(true);
			//				// } catch (PropertyVetoException e) { }
			//				return true;
			//			} else {
			//				// try {
			//				// frame.setMaximum(false);
			//				// } catch (PropertyVetoException e) { }
			//				return false;
			//			}
		}
		return false;
	}


	void doMenuMax() {
		if (frame.isMaximizable()) {
			if (frame.isMaximum() && frame.isIcon()) {
				frame.setIcon(false);
			} else if (!frame.isMaximum()) {
				try {
					frame.setMaximum(true);
				} catch (PropertyVetoException e) { }
			} else {
				try { 
					frame.setMaximum(false); 
				} catch (PropertyVetoException e) { }
			}
		}
	}

	void doMenuClose() {
		// 关闭
		// if (frame.isClosable()) {
		// frame.doDefaultCloseAction();
		// }

		// 关闭
		frame.doDefaultCloseAction();
	}

	/**
	 * 重置标题栏菜单
	 * @param show
	 */
	private void redoTitlePaneMenu(boolean show) {
		JMenuItem item = MenuBuilder.findMenuItemByMethod(rockMenu, "doTitlePane");
		if (item == null) {
			return;
		}
		if (show) {
			String text = UIManager.getString("StatusBar.FrameButton.PopupMenu.ShowText");
			char w = MenuBuilder.getMnemonic("StatusBar.FrameButton.PopupMenu.ShowMWord");
			item.setText(text);
			if (MenuBuilder.isWord(w)) {
				item.setMnemonic(w);
			}
		} else {
			String text = UIManager.getString("StatusBar.FrameButton.PopupMenu.HideText");
			char w = MenuBuilder.getMnemonic("StatusBar.FrameButton.PopupMenu.HideMWord");
			item.setText(text);
			if (MenuBuilder.isWord(w)) {
				item.setMnemonic(w);
			}
		}
	}
	
	void doTitlePane() {
		boolean hidden = frame.isHiddenTitle();
		if (hidden) {
			frame.showTitlePane();
//			// 修改为隐藏状态
//			redoTitlePaneMenu(false);
		} else {
			frame.hideTitlePane();
//			// 修改为显示状态
//			redoTitlePaneMenu(true);
		}
	}

	/**
	 * 初始化悬浮窗口
	 */
	private void initSnapshotWindow() {
		// 读取环境中的定义
		snapshotMode = SnapshotWindowModel.readEnvironmentModel();
		// 初始化
		snapshotWindow = new SnapshotWindow();

		snapshotWindow.setInvoker(this);
		// 调用
		loadSnapshot = false;
		addMouseListener(new SnapshotWindowMouseAdapter());
	}

	class ShowSnapshotWindow extends SwingEvent {

		ShowSnapshotWindow() {
			super(true);
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.event.SwingEvent#process()
		 */
		@Override
		public void process() {
			// 如果处于进入状态时，显示它
			if (loadSnapshot) {
				showSnapshotWindow();
			}
		}
	}

	class SnapshotWindowMouseAdapter extends MouseAdapter {

		public void mouseEntered(MouseEvent e) {
			// 如果隐藏就不显示
			if (SnapshotWindowModel.isHide(snapshotMode)) {
				return;
			}
			// 处于非进入状态时，才可进入
			if (!loadSnapshot) {
				loadSnapshot = true;
				// 放入队列
				addThread(new ShowSnapshotWindow());
			}
		}

		public void mouseExited(MouseEvent e) {
			if (loadSnapshot) {
				loadSnapshot = false;
				unshowSnapshotWindow();
			}
		}
	}

	/**
	 * 显示浮窗
	 */
	private void showSnapshotWindow() {
		// 按纽位置
		Point p = getLocationOnScreen();
		// 弹出菜单尺寸
		Dimension d = snapshotWindow.getPreferredSize();
		int width = d.width;
		int height = d.height;
		// Y轴显示位置，向上
		p.y -= (height + 6); // 4个像素，是DesktopStatusBar对自己Border间隔定义，可见DesktopStatusBar.init方法中的setBorder

		// 两种模式，等比例缩放/填充，把窗口转成图像后输出，间隔像素在边框和图像之间形成透明		
		Image image = null;
		if (SnapshotWindowModel.isZoom(snapshotMode)) {
			image = DesktopUtil.shotZoomImage(frame, width, height, 2);
			snapshotWindow.setPaintBorder(true);
		} else if (SnapshotWindowModel.isFill(snapshotMode)) {
			// image = DesktopUtil.shotFillImage(frame, width, height, 3);
			image = DesktopUtil.shotFillImage(frame, width, height, 0);
			snapshotWindow.setPaintBorder(false);
		}
		// 两个参数有效时，才执行
		if (loadSnapshot && image != null) {
			snapshotWindow.setCurrentImage(image);
			// 设置位置，显示它
			snapshotWindow.setLocation(p.x, p.y);
			snapshotWindow.setVisible(true);
		}
	}

	/**
	 * 隐藏浮窗
	 */
	public void unshowSnapshotWindow() {
		snapshotWindow.setVisible(false);
	}

	@Override
	public void updateUI() {
		super.updateUI();

		// 更新悬浮窗口
		if (snapshotWindow != null) {
			snapshotWindow.updateUI();
		}

		// 更新弹出菜单
		if (rockMenu != null) {
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();

			//			rockMenu.updateFontAndBorder(true);
			//			rockMenu.updateUI();
		}
	}

}


//	 /**
//     * Fires an 
//     * <code>INTERNAL_FRAME_CLOSING</code> event
//     * and then performs the action specified by
//     * the internal frame's default close operation.
//     * This method is typically invoked by the 
//     * look-and-feel-implemented action handler
//     * for the internal frame's close button.
//     *
//     * @since 1.3
//     * @see #setDefaultCloseOperation
//     * @see javax.swing.event.InternalFrameEvent#INTERNAL_FRAME_CLOSING
//     */
//    public void doDefaultCloseAction() {
//        fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
//        switch(defaultCloseOperation) {
//          case DO_NOTHING_ON_CLOSE:
//	    break;
//          case HIDE_ON_CLOSE:
//            setVisible(false);
//	    if (isSelected())
//                try {
//                    setSelected(false);
//                } catch (PropertyVetoException pve) {}
//	      
//	    /* should this activate the next frame? that's really
//	       desktopmanager's policy... */
//            break;
//          case DISPOSE_ON_CLOSE:
//              try {
//		fireVetoableChange(IS_CLOSED_PROPERTY, Boolean.FALSE,
//				   Boolean.TRUE);
//		isClosed = true;
//                setVisible(false);
//		firePropertyChange(IS_CLOSED_PROPERTY, Boolean.FALSE,
//				   Boolean.TRUE);
//		dispose();
//	      } catch (PropertyVetoException pve) {}
//              break;
//          default: 
//              break;
//        }
//    }

//    /**
//     * This class should be treated as a &quot;protected&quot; inner class.
//     * Instantiate it only within subclasses of <Foo>.
//     */  
//    public class CloseAction extends AbstractAction {
//        public CloseAction() {
//	    super(CLOSE_CMD);
//        }
//
//        public void actionPerformed(ActionEvent e) {
//	    if(frame.isClosable()) {
//		frame.doDefaultCloseAction();
//	    }
//	}      
//    } // end CloseAction

//	  public class MaximizeAction extends AbstractAction {
//	        public MaximizeAction() {
//		    super(MAXIMIZE_CMD);
//	        }
//
//	        public void actionPerformed(ActionEvent evt) {
//		    if (frame.isMaximizable()) {
//	                if (frame.isMaximum() && frame.isIcon()) {
//	                    try {
//	                        frame.setIcon(false);
//	                    } catch (PropertyVetoException e) { }
//	                } else if (!frame.isMaximum()) {
//			    try {
//	                        frame.setMaximum(true);
//	                    } catch (PropertyVetoException e) { }
//			} else {
//			    try { 
//			        frame.setMaximum(false); 
//			    } catch (PropertyVetoException e) { }
//			}
//		    }
//		}
//	    }

//	 /**
//     * This class should be treated as a &quot;protected&quot; inner class.
//     * Instantiate it only within subclasses of <Foo>.
//     */
//    public class IconifyAction extends AbstractAction {
//        public IconifyAction() {
//	    super(ICONIFY_CMD);
//        }
//
//        public void actionPerformed(ActionEvent e) {
//	    if(frame.isIconifiable()) {
//	      if(!frame.isIcon()) {
//		try { frame.setIcon(true); } catch (PropertyVetoException e1) { }
//	      } else{
//		try { frame.setIcon(false); } catch (PropertyVetoException e1) { }
//	      }
//	    }
//	}
//    } // end IconifyAction
//
//    /**
//     * This class should be treated as a &quot;protected&quot; inner class.
//     * Instantiate it only within subclasses of <Foo>.
//     */
//    public class RestoreAction extends AbstractAction {
//        public RestoreAction() {
//	    super(RESTORE_CMD);
//        }
//
//        public void actionPerformed(ActionEvent evt) {
//	    if (frame.isMaximizable() && frame.isMaximum() && frame.isIcon()) {
//	        try {
//                    frame.setIcon(false);
//                } catch (PropertyVetoException e) { }
//	    } else if (frame.isMaximizable() && frame.isMaximum()) {
//                try {
//                    frame.setMaximum(false);
//                } catch (PropertyVetoException e) { }
//            } else if (frame.isIconifiable() && frame.isIcon()) {
//	        try {
//                    frame.setIcon(false);
//                } catch (PropertyVetoException e) { }
//	    }
//	}      
//    }

//	public void showDriftMenu(MouseEvent e) {
////		int newX = e.getXOnScreen(); // e.getX();
////		int newY = e.getYOnScreen(); // e.getY();
//////		newY -= (getHeight() + 20);
//		
//		int newX = e.getX();
//		int newY = e.getY();
//		
//		Point p = getLocationOnScreen();
//		System.out.printf("%s -> %d,%d | %d,%d\n", frame.getTitle(), newX, newY, p.x, p.y);
//		
//		// 显示窗口
//		floatMenu.show(this, newX, newY);
//	}

//	public void showDriftMenu(MouseEvent e) {
//		// int newX = e.getXOnScreen(); // e.getX();
//		// int newY = e.getYOnScreen(); // e.getY();
//		// // newY -= (getHeight() + 20);
//
////		int newX = e.getX();
////		int newY = e.getY();
//
//		// 按纽位置
//		Point p = getLocationOnScreen();
//		// 弹出菜单尺寸
//		Dimension d = floatMenu.getPreferredSize();
//		// 显示位置
//		p.y -= d.height;
//		p.y -= 6; // 6是FrameBanner和FrameButton之间的间隔
//
//		// 设置位置，显示它
//		floatMenu.setLocation(p.x, p.y);
//		floatMenu.setVisible(true);
//
//		// System.out.printf("%s -> %d,%d | %d,%d\n", frame.getTitle(), newX,
//		// newY, p.x, p.y);
//		//
//		// // 显示窗口
//		// floatMenu.show(this, newX, newY);
//	}



///**
// * 构造桌面图标按纽，按定文本和图标
// * @param text
// * @param icon
// */
//public FrameButton(String text, ImageIcon icon) {
//	super(text);
//	// 初始化
//	init();
//	setIcon(icon);
//}

///**
// * 图标高度显示
// * @param image
// * @param flag ESL的亮度
// * @return 
// */
//private ImageIcon brighter(ImageIcon image, double flag) {
//	int width = image.getIconWidth();
//	int height = image.getIconHeight();
//	
//	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics g = sourceBI.createGraphics();
//	g.drawImage(image.getImage(), 0, 0, width, height, null); 
//
//	BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	for (int x = 0; x < width; x++) {
//		for (int y = 0; y < height; y++) {
//			RGB rgb = new RGB(sourceBI.getRGB(x, y));
////			ESL esl = rgb.toESL();
////			// 加亮
////			esl.brighter(flag);
////			// 加亮后的返回值
////			int value = esl.toRGB().getRGB();
//			
//			int value = rgb.getRGB();
//
//			// 加亮调整后的颜色
//			newBI.setRGB(x, y, value);
//		}
//	}
//	
//	// 转成输出流
//	try {
////		// 透明转换
////		Graphics2D gra = newBI.createGraphics();
////		newBI = gra.getDeviceConfiguration().createCompatibleImage(width, width, Transparency.TRANSLUCENT);
//
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//		ImageIO.write(newBI, "png", imOut);
//		byte[] b = bs.toByteArray();
//		// 输出为图像对象
//		return new ImageIcon(b);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	return null;
//}

///**
// * 图标高度显示
// * @param image
// * @param flag ESL的亮度
// * @return 
// */
//private ImageIcon brighter3(ImageIcon image, double flag) {
////	int width = image.getIconWidth();
////	int height = image.getIconHeight();
//	
////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics g = sourceBI.createGraphics();
////	g.drawImage(image.getImage(), 0, 0, width, height, null);
//	
//	BufferedImage sourceBI = null;
//	try {
//		sourceBI = ImageIO.read(new File("g:/desktop/icon/work2.png"));
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	int width = sourceBI.getWidth();
//	int height = sourceBI.getHeight();
//	
//	// 平滑缩小图象
//	Image compressImage = sourceBI.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
//	// 测试
//	Graphics2D gra = sourceBI.createGraphics();
//	sourceBI = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//	sourceBI.getGraphics().drawImage(compressImage, 0, 0, null);
//	
//	for (int x = 0; x < width; x++) {
//		for (int y = 0; y < height; y++) {
//			RGB rgb = new RGB(sourceBI.getRGB(x, y));
//			//			ESL esl = rgb.toESL();
//			//			// 加亮
//			//			esl.brighter(flag);
//			//			// 加亮后的返回值
//			//			int value = esl.toRGB().getRGB();
//
//			int value = rgb.getRGB();
//
//			// 加亮调整后的颜色
//			sourceBI.setRGB(x, y, value);
//		}
//	}
//	
//	try {
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		ImageIO.write(sourceBI, "png", bs);
//		bs.flush();
//		byte[] b = bs.toByteArray();
//		// 输出为图像对象
//		return new ImageIcon(b);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	return null;
//	
////	// 转成输出流
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(sourceBI, "png", imOut);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
//	
////	BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	for (int x = 0; x < width; x++) {
////		for (int y = 0; y < height; y++) {
////			RGB rgb = new RGB(sourceBI.getRGB(x, y));
//////			ESL esl = rgb.toESL();
//////			// 加亮
//////			esl.brighter(flag);
//////			// 加亮后的返回值
//////			int value = esl.toRGB().getRGB();
////			
////			int value = rgb.getRGB();
////
////			// 加亮调整后的颜色
////			newBI.setRGB(x, y, value);
////		}
////	}
////	
////	// 转成输出流
////	try {
//////		// 透明转换
//////		Graphics2D gra = newBI.createGraphics();
//////		newBI = gra.getDeviceConfiguration().createCompatibleImage(width, width, Transparency.TRANSLUCENT);
////
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(newBI, "png", imOut);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
//}

///**
// * 图标高度显示
// * @param image
// * @param flag ESL的亮度
// * @return 
// */
//private ImageIcon brighter(ImageIcon image, double flag) {
////	int width = image.getIconWidth();
////	int height = image.getIconHeight();
////	
////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics g = sourceBI.createGraphics();
////	g.drawImage(image.getImage(), 0, 0, width, height, null); 
//
//	BufferedImage sourceBI = null;
//	try {
//		sourceBI = ImageIO.read(new File("g:/desktop/icon/work2.png"));
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	int width = sourceBI.getWidth();
//	int height = sourceBI.getHeight();
//	
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(image.getImage(), "png", bs);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
//	
//	// 透明色
//	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics2D gra = buff.createGraphics();
//	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//
//	// 如果是0，忽略。如果是其它颜色，加亮
//	for (int x = 0; x < width; x++) {
//		for (int y = 0; y < height; y++) {
//			int value = sourceBI.getRGB(x, y);
//			if (value == 0) {
//				// 透明
//				buff.setRGB(x, y, 0);
//			} else {
//				RGB rgb = new RGB(value);
//				ESL esl = rgb.toESL();
//				// 加亮
//				esl.brighter(flag);
//				// 加亮后的返回值
//				value = esl.toRGB().getRGB();
//				buff.setRGB(x, y, value);
//			}
//		}
//	}
//	
//	// 转成输出流
//	try {
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//		ImageIO.write(buff, "png", bs);
//		byte[] b = bs.toByteArray();
//		// 输出为图像对象
//		return new ImageIcon(b);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	return null;
//}

//private byte[] compress(String xmlPath, int width, int height) throws IOException  {
//	ResourceLoader loader = new ResourceLoader();
//	byte[] b = loader.findAbsoluteStream(xmlPath);
//	ByteArrayInputStream in = new ByteArrayInputStream(b);
//	BufferedImage img = ImageIO.read(in);
//	
//	// 平滑缩小图象
//	Image compressImage = img.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
//	// 生成一个新图像
//	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics2D gra = buff.createGraphics();
//	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//
//	buff.getGraphics().drawImage(compressImage, 0, 0, null);
//
//	// 写入磁盘
//	ByteArrayOutputStream out = new ByteArrayOutputStream();
//	ImageIO.write(buff, "PNG", out);
//	out.flush();
//	// 生成图像
//	b = out.toByteArray();
//	
//	// 关闭
//	out.close();
//	in.close();
//	
//	return b;
//}

///**
// * 图标高度显示
// * @param image
// * @param flag ESL的亮度
// * @return 
// */
//private ImageIcon brighter(String xmlPath, double flag) {
////	ResourceLoader loader = new ResourceLoader();
////	byte[] b = loader.findAbsoluteStream(xmlPath);
////
////	BufferedImage sourceBI = null;
////	try {
////		sourceBI = ImageIO.read(new ByteArrayInputStream(b));
////	} catch (Exception e) {
////		e.printStackTrace();
////	}
////	int width = sourceBI.getWidth();
////	int height = sourceBI.getHeight();
//
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(image.getImage(), "png", bs);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
//	
////	ResourceLoader loader = new ResourceLoader();
////	byte[] b = loader.findAbsoluteStream(xmlPath);
////	ImageIcon image = new ImageIcon(b);
////	int width = image.getIconWidth();
////	int height = image.getIconHeight();
////	Image im = image.getImage().getScaledInstance(width, height,Image.SCALE_SMOOTH);
////	
////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics2D gd = sourceBI.createGraphics();
////	sourceBI = gd.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//////	Graphics g = sourceBI.createGraphics();
////	gd.drawImage(image.getImage(), 0, 0, width, height, null); 
//
//	
//	
//	
//	BufferedImage sourceBI = null;
//	try {
//		byte[] b = compress(xmlPath, 32, 32); // loader.findAbsoluteStream(xmlPath);
//		sourceBI = ImageIO.read(new ByteArrayInputStream(b));
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	int width = sourceBI.getWidth();
//	int height = sourceBI.getHeight();
//
//	// 透明色
//	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics2D gra = buff.createGraphics();
//	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//
//	// 如果是0，忽略。如果是其它颜色，加亮
//	for (int x = 0; x < width; x++) {
//		for (int y = 0; y < height; y++) {
//			int value = sourceBI.getRGB(x, y);
//			if (value == 0) {
//				// 透明
//				buff.setRGB(x, y, 0);
//			} else {
//				RGB rgb = new RGB(value);
//				ESL esl = rgb.toESL();
//				// 加亮
//				esl.brighter(flag);
//				// 加亮后的返回值
//				value = esl.toRGB().getRGB();
//				buff.setRGB(x, y, value);
//			}
//		}
//	}
//	
//	// 转成输出流
//	try {
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//		ImageIO.write(buff, "png", bs);
//		 byte[] b = bs.toByteArray();
//		// 输出为图像对象
//		return new ImageIcon(b);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	return null;
//}

//private BufferedImage createBufferedImage(ImageIcon image) {
//	int width = image.getIconWidth();
//	int height = image.getIconHeight();
//	
//	// 生成一个新图像
//	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics2D gra = buff.createGraphics();
//	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//	buff.getGraphics().drawImage(image.getImage(), 0, 0, null);
//
//	// 写入磁盘
//	try {
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		ImageIO.write(buff, "PNG", out);
//		out.flush();
//		// 生成图像
//		byte[] b = out.toByteArray();
//		return ImageIO.read(new ByteArrayInputStream(b));
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	return null;
//}
///**
// * 图标高度显示
// * @param image
// * @param flag ESL的亮度
// * @return 
// */
//private ImageIcon light(ImageIcon image, double flag) {
////	int width = image.getIconWidth();
////	int height = image.getIconHeight();
//	
////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics g = sourceBI.createGraphics();
////	g.drawImage(image.getImage(), 0, 0, width, height, null);
//	
////	BufferedImage sourceBI = null;
////	try {
////		sourceBI = ImageIO.read(new File("g:/desktop/icon/work2.png"));
////	} catch (Exception e) {
////		e.printStackTrace();
////	}
////	int width = sourceBI.getWidth();
////	int height = sourceBI.getHeight();
//
//	// 生成图像
//	BufferedImage sourceBI = createBufferedImage(image);
//	int width = sourceBI.getWidth();
//	int height = sourceBI.getHeight();
//
//	// 透明色
//	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics2D gra = buff.createGraphics();
//	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//
//	// 如果是0，忽略。如果是其它颜色，加亮
//	for (int x = 0; x < width; x++) {
//		for (int y = 0; y < height; y++) {
//			int value = sourceBI.getRGB(x, y);
//			if (value == 0) {
//				// 透明
//				buff.setRGB(x, y, 0);
//			} else {
//				RGB rgb = new RGB(value);
//				ESL esl = rgb.toESL();
//				// 加亮
//				esl.brighter(flag);
//				// 加亮后的返回值
//				value = esl.toRGB().getRGB();
//				buff.setRGB(x, y, value);
//			}
//		}
//	}
//	
//	// 转成输出流
//	try {
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		ImageIO.write(buff, "png", bs);
//		byte[] b = bs.toByteArray();
//		// 输出为图像对象
//		return new ImageIcon(b);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	return null;
//}

///**
// * 设置图标
// * @param icon
// */
//public void setStateIcon(String xmlPath) {
////	super.setIcon(icon);
//
//	// ESL的亮度增加50
//	ImageIcon image = brighter(xmlPath, 50);
//	if (image != null) {
//		super.setPressedIcon(image);
//		super.setSelectedIcon(image);
//		super.setRolloverIcon(image);
//		super.setRolloverSelectedIcon(image);
//	}
//}


//private void drawDashedLine(Graphics g, int x1, int x2, int y1, int y2) {
//	Graphics2D g2 = (Graphics2D)g.create();
//	Stroke dashed= new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
//	g2.setStroke(dashed);
//	g2.drawLine(x1, y2, x2, y2);
//	g2.dispose();
//}


///**
// * @param arg0
// */
//public FrameButton(Icon arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}
//
///**
// * @param arg0
// */
//public FrameButton(String arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}
//
///**
// * @param arg0
// */
//public FrameButton(Action arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}


//	/**
//	 * 根据菜单栏和菜单荐方法名，查找一个菜单项
//	 * @param menu 菜单栏
//	 * @param method 方法名
//	 * @return 返回菜单项，如果没有找到是空值
//	 */
//	private JMenuItem findMenuItemByMethod(JMenu menu, String method) {
//		if(rockMenu == null) {
//			return null;
//		}
//	
//		Component[] elements = rockMenu.getMenuComponents();
//		int size = (elements == null ? 0 : elements.length);
//		// 判断对象
//		for (int index = 0; index < size; index++) {
//			Component element = elements[index];
//			// 判断是JMenu，递归，取它的子级
//			if (Laxkit.isClassFrom(element, JMenu.class)) {
//				JMenuItem item = findMenuItemByMethod((JMenu) element, method);
//				if (item != null) {
//					return item;
//				}
//			} 
//			// 判断是JMenuItem，取出名字，判断参数一致
//			else if(Laxkit.isClassFrom(element, JMenuItem.class) ) {
//				JMenuItem item = (JMenuItem)element;
//				String text = item.getName();
//				if (method.equals(text)) {
//					return item;
//				}
//			}
//		}
//
//		return null;
//	}
