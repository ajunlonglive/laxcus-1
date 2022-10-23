/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;
import com.laxcus.util.color.*;

/**
 * 状态条键头按纽
 * 
 * @author scott.liang
 * @version 1.0 5/31/2021
 * @since laxcus 1.0
 */
class DesktopFrameArrowButton extends CraftButton {

	private static final long serialVersionUID = -7696835544565522221L;

	//	/** 边框颜色 **/
	//	private static Color borderColor = new Color(197, 145, 90);

	//	private static Color darkText = new Color(224, 224, 224); // new Color(208, 208, 208);

	private Color shadowColor = Color.GRAY;

	private Color lightColor = Color.WHITE;

//	/** 按下或者否 **/
//	private boolean pressed;

	//	/** 保存窗口句柄 **/
	//	private LightFrame frame;

	/**
	 * 构造默认的状态条键头按纽
	 */
	public DesktopFrameArrowButton() {
		super();
		init();
	}
	
	/**
	 * 构造状态条键头按纽，指定图标
	 * @param icon 图标
	 */
	public DesktopFrameArrowButton(Icon icon) {
		super(icon);
		init();
	}
	
//	/**
//	 * 构造状态条键头按纽，指定文本
//	 * @param text 显示文本
//	 */
//	public FrameArrayButton(String text) {
//		super(text);
//		init();
//	}
	
//	/**
//	 * 构造默认的状态条键头按纽
//	 */
//	public FrameArrayButton(LightFrame frame) {
//		super();
//		// 初始化
//		init();
//		// 设置参数
//		setIcon(frame.getFrameIcon());
//		// 标题
//		String text = frame.getTitle();
//		if (text == null) {
//			text = frame.getToolTipText();
//		}
//		if (text == null) {
//			text = "Unknown";
//		}
//
//		setText(text);
//		setToolTipText(text);
//
//		setFrame(frame);
//		
//		
//	}

//	/**
//	 * 构造状态条键头按纽，按定文本和图标
//	 * @param text
//	 * @param icon
//	 */
//	public FrameArrayButton(String text) {
//		super(text);
//		// 初始化
//		init();
//	}
//	
//	/**
//	 * 构造状态条键头按纽，按定文本和图标
//	 * @param text 文本
//	 * @param icon 图标
//	 */
//	public FrameArrayButton(String text, Icon icon) {
//		this(text);
//		init();
//		setIcon(icon);
//	}

//	/**
//	 * 设置单元参数
//	 * @param e FrameArrayButtonItem实例
//	 */
//	public void setFrame(LightFrame e) {
//		frame = e;
//	}
//
//	/**
//	 * 返回单元参数
//	 * @return FrameArrayButtonItem实例
//	 */
//	public LightFrame getFrame() {
//		return frame;
//	}
	
	/**
	 * 设置图标
	 * @param icon
	 * @param brighter 高亮增加值
	 * @param dark 暗色
	 */
	public void setIcon(ImageIcon icon, int brighter, int dark) {
		super.setIcon(icon);
		
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
			}
		}
	}

	
//	private static Color darkText = new Color(192, 192, 192);
//	
//	/**
//	 * 设置按纽为焦点
//	 * @param b
//	 */
//	public void setFocus(boolean b) {
//		focus = b;
//		if (focus) {
//			setForeground(Color.WHITE);
//		} else {
////			setForeground(new Color(233, 233, 233));
//			setForeground(darkText); // new Color(233, 233, 233));
//		}
//	}
	
	
//	/**
//	 * 设置按纽为焦点
//	 * @param b
//	 */
//	public void setFocus(boolean b) {
//		focus = b;
//		if (focus) {
//			setForeground(Color.WHITE);
//		} else {
//			setForeground(darkText);
//		}
//	}
	
//	/**
//	 * 判断是焦点
//	 * @return
//	 */
//	public boolean isPressed() {
//		return pressed;
//	}
//	
//	/**
//	 * 设置焦点或者否
//	 * @param b
//	 */
//	public void setPressed(boolean b) {
//		pressed = b;
//		repaint();
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JComponent#getInsets()
//	 */
//	@Override
//	public Insets getInsets() {
//		//		return new Insets(2, 4, 2, 4);
//		Insets insets =  super.getInsets();
//		insets.left = 4;
//		return insets;
//	}
	
	/**
	 * 初始化参数
	 */
	private void init() {
		// // 间隔
		// setIconTextGap(5);

		// setFocus(false);

		setContentAreaFilled(false); // 平面
//		setBorderPainted(false); // 不绘制边框
		setFocusPainted(false); // 不绘制焦点边框

		// setPreferredSize(new Dimension(100, 30));
		setBorder(new EmptyBorder(4, 4, 4, 4));

		// setMargin(new Insets(2, 2, 2,2));

		// setVerticalTextPosition(SwingConstants.BOTTOM); // 文本在图标的下面
		//
		// setHorizontalTextPosition(SwingConstants.CENTER); // 居中布置
		//
		// setVerticalAlignment(SwingConstants.TOP); // 图文从下向下

		// setHorizontalAlignment(SwingConstants.LEFT);
		// setHorizontalTextPosition(SwingConstants.LEFT);

		// setVerticalTextPosition(SwingConstants.RIGHT);

		// setBorder(new EmptyBorder(4, 4, 4, 4));
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		
		// 判断有效再选择
		if (isEnabled()) {
			ButtonModel bm = getModel();
			if (bm.isRollover()) {
				if (bm.isPressed()) {
					// 图标下沉
					paintDownComponent(g);
				} else {
					// 图标上浮
					paintUpComponent(g);
				}
			} else if (bm.isPressed()) {
				// 图标下沉，颜色变暗
				paintDownComponent(g);
			} else {
				// 平面图标，保持不变
				paintFlatComponent(g);
			}
		} else {
			super.paintComponent(g);
		}
	}
	
/**
	 * 返回匹配的背景颜色
	 * @param active
	 * @return
	 */
	private Color getBackground(boolean active) {
		Color c = UIManager.getColor("Panel.background");
		if (c != null) {
			return new Color(c.getRGB());
		} else {
			return getParent().getBackground();
		}
	}

	/**
	 * 平面状态
	 */
	private void paintFlatComponent(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		
		Icon icon = getIcon();
		
		Color back = g.getColor();
		
		Color background = getBackground(false);
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		
		// 图标有效时...
		if (icon != null) {
			int iconWidth = icon.getIconWidth();
			int iconHeight = icon.getIconHeight();
			int x = (width - iconWidth) / 2;
			int y = (height - iconHeight) / 2;
			if (x < 0) x = 0;
			if (y < 0) y = 0;
			// 绘制图标
			icon.paintIcon(this, g, x, y);
		}
		
		// 恢复背景色
		g.setColor(back);
	}
	
	private void paintUpComponent(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		
		Icon icon = getRolloverIcon();
		if (icon == null) {
			icon = getIcon();
		}

		Color back = g.getColor();

		Color background = getBackground(false);
		g.setColor(background);
		g.fillRect(0, 0, width, height);

		if (icon != null) {
			int iconWidth = icon.getIconWidth();
			int iconHeight = icon.getIconHeight();
			int x = (width - iconWidth) / 2;
			int y = (height - iconHeight) / 2;
			if (x < 0) x = 0;
			y -= 1; // 上移一个像素
			if (y < 0) y = 0;
			// 绘制图标
			icon.paintIcon(this, g, x, y);
		}
		
		// 恢复背景色
		g.setColor(back);
	}
	
	private void paintDownComponent(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		
		Icon icon = getPressedIcon();
		if (icon == null) {
			icon = getIcon();
		}
		
		Color back = g.getColor();

		Color background = getBackground(false);
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		
		if (icon != null) {
			int iconWidth = icon.getIconWidth();
			int iconHeight = icon.getIconHeight();
			int x = (width - iconWidth) / 2;
			int y = (height - iconHeight) / 2;
			
			x += 1;
			y += 1; // Y坐标下移1个像素
			
			if (x >= width) x = width - iconWidth;
			if (x < 0) x = 0;
			if (y >= height) y = height - iconHeight;
			if (y < 0) y = 0;
			// 绘制图标
			icon.paintIcon(this, g, x, y);
		}
		
		// 恢复背景色
		g.setColor(back);
	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//
//		int width = getWidth();
//		int height = getHeight();
//
//		// 有效状态
//		if (isEnabled()) {
//			if (pressed) {
//				paintLowerdBorder(this, g, 0, 0, width, height);
//			} else {
//				paintRaisedBorder(this, g, 0, 0, width, height);
//			}
//		} else {
//			paintRaisedBorder(this, g, 0, 0, width, height);
//		}
//	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintBorder(Graphics g) {
//		int width = getWidth();
//		int height = getHeight();
//
//		// 有效状态
//		if (isEnabled()) {
//			ButtonModel bm = getModel();
//			if (bm.isPressed()) {
//				paintLowerdBorder(this, g, 0, 0, width, height);
//			} else {
//				paintRaisedBorder(this, g, 0, 0, width, height);
//			}
//			// if (pressed) {
//			// paintLowerdBorder(this, g, 0, 0, width, height);
//			// } else {
//			// paintRaisedBorder(this, g, 0, 0, width, height);
//			// }
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

		// 有效状态
		if (isEnabled()) {
			ButtonModel bm = getModel();
			if (bm.isRollover()) {
				if (bm.isPressed()) {
					paintLowerdBorder(this, g, 0, 0, width, height);
				} else {
					paintRaisedBorder(this, g, 0, 0, width, height);
				}
			} else if (bm.isPressed()) {
				paintLowerdBorder(this, g, 0, 0, width, height);
			}
			//			if (bm.isPressed()) {
			//				paintLowerdBorder(this, g, 0, 0, width, height);
			//			} else {
			//				paintRaisedBorder(this, g, 0, 0, width, height);
			//			}

		} else {
			//			paintRaisedBorder(this, g, 0, 0, width, height);
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
	
}


///**
// * 构造状态条键头按纽，按定文本和图标
// * @param text
// * @param icon
// */
//public FrameArrayButton(String text, ImageIcon icon) {
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
//public FrameArrayButton(Icon arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}
//
///**
// * @param arg0
// */
//public FrameArrayButton(String arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}
//
///**
// * @param arg0
// */
//public FrameArrayButton(Action arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}