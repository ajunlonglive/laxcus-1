/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dock;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.component.*;

/**
 * 桌面图标按纽
 * 
 * @author scott.liang
 * @version 1.0 9/18/2021
 * @since laxcus 1.0
 */
class DockButton extends CraftButton {

	private static final long serialVersionUID = -7696835544565522221L;
	
//	private static final Color NIMBUS_ACTIVE = new Color(225,225,225); // 187, 192, 199);  // 143,23,182
//
//	private static final Color NIMBUS_INACTIVE = new Color(208,208,208); // 186, 190, 198); // 147,23, 181
	
//	private static final Color NIMBUS_ACTIVE = new ESL(147, 30, 206).toBrighter(12).toColor(); // 187, 192, 199);  // 143,23,182
//	
//	private static final Color NIMBUS_INACTIVE =	new ESL(147, 30, 196).toColor();
	
//	/** 左侧开始间隔 **/
//	static final int X_GAP = 6;
//	
//	/** 按下时，下沉2个像素 **/
//	static final int PRESS_Y_ASCENT = 1;
	

//	/** NIMBUS, 阴影是灰色 **/
//	private Color shadowColor = Color.GRAY;
//
//	/** NIMBUS, 高亮是白色 **/
//	private Color lightColor = Color.WHITE;

//	/** 图标 **/
//	private Icon logo;

//	/** 保存窗口句柄 **/
//	private LightFrame frame;
//	
//	/** 按下按纽 **/
//	private boolean pressed;
	
	/** 撤销是假 **/
	private volatile boolean cancel = false;

	/** 属性单元 **/
	private RayDockButtonItem item;
	
	/**
	 * 构造默认的桌面图标按纽
	 */
	public DockButton() {
		super();
		init();
	}
	
	/**
	 * 判断这个按纽可以删除。
	 * 系统应用不可删除，用户应用可以
	 * @return 返回真或者假
	 */
	public boolean canDelete() {
		if (item == null) {
			return true;
		}
		return item.isUser();
	}

//	/**
//	 * 构造默认的桌面图标按纽
//	 */
//	public DockButton(LightFrame frame) {
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
////		setBorder(new EmptyBorder(2, X_GAP, 2, 4));
//		
//		
//
////		setFrame(frame);
//	}

	/**
	 * 初始化参数
	 */
	private void init() {
//		pressed = false;
		
		this.cancel = false;
		// 间隔
		setIconTextGap(5);

		setContentAreaFilled(false); // 平面
		setBorderPainted(false); // 不绘制边框
		setFocusPainted(false); // 不绘制焦点边框
		setRolloverEnabled(true); // 反转...

//		setBorder(new EmptyBorder(2, 4, 2, 4));
		
		setBorder(new EmptyBorder(0, 0, 0, 0));

		setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * 设置单元参数
	 * @param e DesktopButtonItem实例
	 */
	public void setItem(RayDockButtonItem e) {
		item = e;
	}

	/**
	 * 返回单元参数
	 * @return DesktopButtonItem实例
	 */
	public RayDockButtonItem getItem() {
		return item;
	}

//	/**
//	 * 设置单元参数
//	 * @param e DockButtonItem实例
//	 */
//	public void setFrame(LightFrame e) {
//		frame = e;
//	}
//
//	/**
//	 * 返回单元参数
//	 * @return DockButtonItem实例
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
//		logo = icon;

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

//	/**
//	 * 判断是焦点
//	 * @return
//	 */
//	public boolean isPressed() {
//		return pressed;
//	}
//
//	/**
//	 * 设置为按下
//	 * @param b
//	 */
//	public void setPressed(boolean b) {
//		// 按下或者否
//		pressed = b;
//		repaint();
//	}
	
	/**
	 * 返回匹配的背景颜色
	 * @param active
	 * @return
	 */
	private Color getBackground(boolean active) {
		Color c =	UIManager.getColor("Panel.background");
		if(c != null) {
			return new Color(c.getRGB());
		} else {
			return super.getParent().getBackground();
		}
		
//		if (isNimbusUI()) {
//			return (active ? DockButton.NIMBUS_ACTIVE : DockButton.NIMBUS_INACTIVE);
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
//		g.fillRect(0, 0, width, height);
		
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
	
//	/**
//	 * 平面状态
//	 */
//	private void paintUpComponent(Graphics g) {
//		final int width = getWidth();
//		final int height = getHeight();
//		
//		Color back = g.getColor();
//		
//		// 找高亮图标
//		Icon icon = getRolloverIcon();
//		if(icon == null){
//			icon = getIcon();
//		}
//		
//		Color background = getBackground(false);
//		g.setColor(background);
////		g.fillRect(0, 0, width, height);
//		
//		// 图标有效时...
//		if (icon != null) {
//			int iconWidth = icon.getIconWidth();
//			int iconHeight = icon.getIconHeight();
//			int x = (width - iconWidth) / 2;
//			int y = (height - iconHeight) / 2;
//			y -= 10; // Y坐标上浮
//			if (x < 0) x = 0;
//			if (y < 0) y = 0;
//			// 绘制图标
//			icon.paintIcon(this, g, x, y);
//		}
//		
//		// 恢复背景色
//		g.setColor(back);
//	}
	
	/**
	 * 平面状态
	 */
	private void paintUpComponent(Graphics g) {
		final int width = getWidth();
		//		final int height = getHeight();
		//		
		//		Color back = g.getColor();

		// 找高亮图标
		Icon icon = getRolloverIcon();
		if (icon == null) {
			icon = getIcon();
		}

		//		Color background = getBackground(false);
		//		g.setColor(background);
		//		g.fillRect(0, 0, width, height);

		// 图标有效时...
		if (icon != null) {
			int iconWidth = icon.getIconWidth();
			int x = (width - iconWidth) / 2;
			int y = 0;
			if (x < 0) x = 0;
			// 绘制图标
			icon.paintIcon(this, g, x, y);
		}

		//		// 恢复背景色
		//		g.setColor(back);
	}
	
//	/**
//	 * 平面状态
//	 */
//	private void paintDownComponent(Graphics g) {
//		final int width = getWidth();
//		final int height = getHeight();
//		
//		Color back = g.getColor();
//		
//		// 图标
//		Icon icon = getPressedIcon();
//		if (icon == null) {
//			icon = getIcon();
//		}
//		
//		Color background = getBackground(false);
//		g.setColor(background);
////		g.fillRect(0, 0, width, height);
//		
//		if (icon != null) {
//			int iconWidth = icon.getIconWidth();
//			int iconHeight = icon.getIconHeight();
//			int x = (width - iconWidth) / 2;
//			int y = (height - iconHeight) / 2;
//			y += 10; // Y坐标下沉
//			if (x < 0) x = 0;
//			// 如果超过
//			if (y + iconHeight >= height) y = height - iconHeight;
//			if (y < 0) y = 0;
//			// 绘制图标
//			icon.paintIcon(this, g, x, y);
//		}
//		
//		// 恢复背景色
//		g.setColor(back);
//	}
	
	/**
	 * 平面状态
	 */
	private void paintDownComponent(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		
//		Color back = g.getColor();
		
		// 图标
		Icon icon = getPressedIcon();
		if (icon == null) {
			icon = getIcon();
		}
		
//		Color background = getBackground(false);
//		g.setColor(background);
//		g.fillRect(0, 0, width, height);
		
		if (icon != null) {
			int iconWidth = icon.getIconWidth();
			int iconHeight = icon.getIconHeight();
			int x = (width - iconWidth) / 2;
			
			// Y坐标降到最低
			int y = height - iconHeight;
//			y += 10; // Y坐标下沉
			if (x < 0) x = 0;
			// 如果超过
//			if (y + iconHeight >= height) y = height - iconHeight;
//			if (y < 0) y = 0;
			// 绘制图标
			icon.paintIcon(this, g, x, y);
		}
		
//		// 恢复背景色
//		g.setColor(back);
	}
	
//	/**
//	 * 平面状态
//	 */
//	private void paintPlainComponentX(Graphics g) {
//		final int width = getWidth();
//		final int height = getHeight();
//
//		Color back = g.getColor();
//		
//		Color background = getBackground(false);
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
	 * 取消执行
	 */
	public void cancel() {
		cancel = true;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		ButtonModel bm = getModel();
		
		// 来自外部的撤销请求，重绘成平面状态
		if (cancel) {
			cancel = false;
			bm.setRollover(false);
			bm.setPressed(false);
			paintFlatComponent(g);
			return;
		}

		// 其它情况
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
		
//		// 阴刻 / 阳刻
//		if (pressed) {
//			paintPressedComponent(g);
//		} else {
//			// super.paintComponent(g);
//			paintPlainComponent(g);
//		}
	}

	/*
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

//	/**
//	 * 绘制阳刻浮雕界面
//	 * @param c
//	 * @param g
//	 * @param x
//	 * @param y
//	 * @param width
//	 * @param height
//	 */
//	private void paintRaisedBorder(Component c, Graphics g, int x, int y, int width, int height) {
//		Color dark = null;
//		Color light = null;
//
//		// 如果是“Nimbus”界面，生成蚀刻效果；如果不是，平面显示
//		if (isNimbusUI()) {
//			dark = shadowColor;
//			light = lightColor;
//		} else {
//			Color color = c.getBackground();
//			ESL esl = new RGB(color).toESL();
//			dark = esl.toDraker(50).toColor();
//			light = esl.toBrighter(50).toColor();
//		}
//
//		int x2 = x + width - 1;
//		int y2 = y + height - 1;
//
//		// 原色
//		Color oldColor = g.getColor();
//
//		// 亮色
//		g.setColor(light);
//		g.drawLine(x, y, x2, y); // 上线
//		g.drawLine(x, y, x, y2); // 左线
//
//		// 暗色
//		g.setColor(dark);
//		g.drawLine(x, y2, x2, y2); // 下线
//		g.drawLine(x2, y, x2, y2); // 右线
//
//		// 设置颜色
//		g.setColor(oldColor);
//	}

//	/**
//	 * 绘制阴刻浮雕界面
//	 * @param c
//	 * @param g
//	 * @param x
//	 * @param y
//	 * @param width
//	 * @param height
//	 */
//	private void paintLowerdBorder(Component c, Graphics g, int x, int y, int width, int height) {
//		Color shadow = null;
//		Color light = null;
//
//		// 如果是“Nimbus”界面，生成蚀刻效果；如果不是，平面显示
//		if (isNimbusUI()) {
//			shadow = shadowColor;
//			light = lightColor;
//		} else {
//			// 取组件的背景色
//			Color color = c.getBackground();
//			ESL esl = new RGB(color).toESL();
//			shadow = esl.toDraker(50).toColor();
//			light = esl.toBrighter(50).toColor();
//			
////			light = MetalLookAndFeel.getWindowTitleBackground();
////			shadow = MetalLookAndFeel.getWindowTitleInactiveBackground();
//		}
//
//		int x2 = x + width - 1;
//		int y2 = y + height - 1;
//
//		// 原色
//		Color oldColor = g.getColor();
//
//		// 暗色
//		g.setColor(shadow);
//		g.drawLine(x, y, x2, y); // 上线
//		g.drawLine(x, y, x, y2); // 左线
//
//		// 高亮色
//		g.setColor(light);
//		g.drawLine(x, y2, x2, y2); // 下线
//		g.drawLine(x2, y, x2, y2); // 右线
//
//		// 设置颜色
//		g.setColor(oldColor);
//	}

}


///**
// * 构造桌面图标按纽，按定文本和图标
// * @param text
// * @param icon
// */
//public DockButton(String text, ImageIcon icon) {
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
//public DockButton(Icon arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}
//
///**
// * @param arg0
// */
//public DockButton(String arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}
//
///**
// * @param arg0
// */
//public DockButton(Action arg0) {
//	super(arg0);
//	// TODO Auto-generated constructor stub
//}