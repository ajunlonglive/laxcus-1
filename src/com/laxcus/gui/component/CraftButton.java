/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.component;

import javax.swing.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 动感按纽 <br>
 * 提供生成高亮算法的按纽
 * 
 * @author scott.liang
 * @version 1.0 6/3/2021
 * @since laxcus 1.0
 */
public class CraftButton extends JButton {

	private static final long serialVersionUID = -8472648794240854405L;

	/**
	 * 构造默认的动感按纽
	 */
	public CraftButton() {
		super();
	}
	
	/**
	 * 生成动感按纽
	 * @param text 提示文本
	 */
	public CraftButton(String text) {
		super(text);
	}
	
	/**
	 * 生成动感按纽
	 * @param icon 图标
	 */
	public CraftButton(Icon icon) {
		super(icon);
	}
	
	/**
	 * 生成动感按纽
	 * @param text 提示文本
	 * @param icon 图标
	 */
	public CraftButton(String text, Icon icon) {
		super(text, icon);
	}
	
	/**
	 * 线程加入分派器
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	/**
	 * 判断是NIMBUS界面
	 * @return 返回真或者假
	 */
	public boolean isNimbusUI() {
		return GUIKit.isNimbusUI();
	}

	/**
	 * 判断是METAL界面
	 * @return 返回真或者假
	 */
	public boolean isMetalUI() {
		return GUIKit.isMetalUI();
	}

	/**
	 * 根据输入图像，生成一个新的高亮图像
	 * 
	 * @param image 图像
	 * @param value 高亮提升幅度
	 * @return 返回新的图像按纽，失败返回空指针
	 */
	protected ImageIcon brighter(ImageIcon image, double value) {
		return ImageUtil.brighter(image, value);
	}
	
	/**
	 * 根据输入图像，生成一个新的暗色图像
	 * 
	 * @param image 图像
	 * @param value 高亮提升幅度
	 * @return 返回新的图像按纽，失败返回空指针
	 */
	protected ImageIcon dark(ImageIcon image, double value) {
		return ImageUtil.dark(image, value);
	}
	
	/**
	 * 设置默认字体
	 */
	private void setDefaultFont() {
		// 更新默认字体
		FontKit.updateDefaultFonts(this);
		// 更新提示
		FontKit.updateToolTipText(this);
	}
	
//	class UpdateFontThread extends SwingEvent {
//		UpdateFontThread() {
//			super();
//		}
//		public void process() {
//			setDefaultFont();
//		}
//	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JButton#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// 设置默认字体
		setDefaultFont();

		//		SwingDispatcher.invokeThread(new UpdateFontThread());
	}
	
}


///**
// * 把缓存图像转成字节数组输出
// * @param buff
// * @return 返回字节数组
// */
//protected byte[] transform(BufferedImage buff) throws IOException{
//	ByteArrayOutputStream out = new ByteArrayOutputStream();
//	ImageIO.write(buff, "PNG", out);
//	out.flush();
//	// 生成图像
//	return out.toByteArray();
//	//			return ImageIO.read(new ByteArrayInputStream(b));
//}
//
///**
// * 生成一个缓存对象
// * @param image 图像
// * @return 返回缓存对象，或者空指针
// */
//protected BufferedImage createBufferedImage(ImageIcon image) {
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
//		// ByteArrayOutputStream out = new ByteArrayOutputStream();
//		// ImageIO.write(buff, "PNG", out);
//		// out.flush();
//		// // 生成图像
//		// byte[] b = out.toByteArray();
//
//		// 转换
//		byte[] b = transform(buff);
//		return ImageIO.read(new ByteArrayInputStream(b));
//	} catch (IOException e) {
//		Logger.error(e);
//	}
//	return null;
//}
//
///**
// * 根据输入图像，生成一个新的高亮图像
// * 
// * @param image 图像
// * @param flag 高亮提升幅度
// * @return 返回新的图像按纽，失败返回空指针
// */
//protected ImageIcon brighter(ImageIcon image, double flag) {
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
//		byte[] b = transform(buff);
//		// 输出为图像对象
//		return new ImageIcon(b);
//	} catch (IOException e) {
//		Logger.error(e);
//	}
//	return null;
//}

