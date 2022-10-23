/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.util;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.log.client.*;

/**
 * 桌面工具
 * 
 * @author scott.liang
 * @version 1.0 6/6/2021
 * @since laxcus 1.0
 */
public class DesktopUtil {

	/**
	 * 主菜单边框
	 * @return
	 */
	public static Border getSubMenuBorder() {
		return new EmptyBorder(4, 4, 4, 4);
	}
	
	/**
	 * 子菜单边框
	 * @return
	 */
	public static Border getMainMenuBorder() {
		return new EmptyBorder(6, 4, 6, 6);
	}
	
	/**
	 * 桌面尺寸
	 * @return
	 */
	public static Dimension getDesktopButtonSize() {
		// return new Dimension(78, 78);
		// return new Dimension(88, 88);
		// return new Dimension(98, 98);

		return new Dimension(101, 101);
	}

	/**
	 * 复制图像到剪贴板里面
	 * @param that 组件对象
	 */
	public static void shotScreen(JComponent that) {
		int width = that.getWidth();
		int height = that.getHeight();

		//		System.out.printf("组件屏幕尺寸：%d %d\n", width, height);

		// 生成图像
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		that.printAll(g2d); // 关键，复制窗口到缓冲里
		g2d.dispose();

		// 复制到系统剪贴板
		Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transfer = new DesktopImageSelection(image);
		board.setContents(transfer, null);

		//		// 复制到磁盘
		//		try {
		//			ByteArrayOutputStream bs = new ByteArrayOutputStream();
		//			ImageIO.write(image, "PNG", bs);
		//			bs.flush();
		//			// 生成图像
		//			byte[] b = bs.toByteArray();
		//			FileOutputStream out = new FileOutputStream("d:/abc.png");
		//			out.write(b);
		//			out.close();
		//			
		//			System.out.println("完成！");
		//		} catch (IOException e) {
		//			Logger.fatal(e);
		//			e.printStackTrace();
		//		}
	}

//	/**
//	 * 指定尺寸，生成快照
//	 * @param that
//	 * @param width
//	 * @param height
//	 */
//	public static void shotScreen(JComponent that, int width, int height) {
//		// 生成图像
//		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g2d = image.createGraphics();
//		that.printAll(g2d); // 关键，复制窗口到缓冲里
//		g2d.dispose();
//
//		// 复制到系统剪贴板
//		Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
//		Transferable transfer = new ImageSelection(image);
//		board.setContents(transfer, null);
//	}
//	
//	/**
//	 * 复制图像到剪贴板里面
//	 * @param that 组件对象
//	 */
//	public static void shotScreen(JComponent that) {
//		int width = that.getWidth();
//		int height = that.getHeight();
//		// 截取屏幕图像
//		DesktopUtil.shotScreen(that, width, height);
//	}
	
	/**
	 * 指定尺寸，生成图像。
	 * 当前支持的格式包括：“
	 * BMP
	 * bmp
	 * jpg
	 * JPG
	 * wbmp
	 * jpeg
	 * png
	 * PNG
	 * JPEG
	 * WBMP
	 * GIF
	 * gif”
	 * 
	 * @param that
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon shotImageIcon(JComponent that, int compressWidth, int compressHeight) {
		// 生成图像
		int width = that.getWidth();
		int height = that.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		that.printAll(g2d); // 关键，复制窗口到缓冲里
		g2d.dispose();
		
		// 按照指定尺寸生成图像
		Image compressImage = image.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
		
		// 压缩图像
		BufferedImage dest = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = dest.createGraphics();
		dest = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
		dest.getGraphics().drawImage(compressImage, 0, 0, null);
		
		// 生成图像
		try {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
//			ImageIO.write(image, "PNG", bs);
			ImageIO.write(dest, "PNG", bs);
			bs.flush();
			// 生成图像
			byte[] b = bs.toByteArray();
			bs.close();
			
			return new ImageIcon(b);
		} catch (IOException e) {
			Logger.fatal(e);
		}
		return null;
	}
	
	/**
	 * 生成缩放图像
	 * @param that
	 * @param fixWidth
	 * @param compressHeight
	 * @return
	 */
	public static Image shotFillImage(JComponent that, int fixWidth, int fixHeight, int gap) {
		// 生成图像
		int width = that.getWidth();
		int height = that.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		that.printAll(g2d); // 关键，复制窗口到缓冲里
		g2d.dispose();
		
		// 压缩尺寸
		if (gap < 0) gap = 0;
		int compressWidth = fixWidth - (gap * 2);
		int compressHeight = fixHeight - (gap * 2);
		
		// 生成缩放图片
		return image.getScaledInstance(compressWidth, compressHeight, Image.SCALE_SMOOTH);
	}
	
	/**
	 * 生成等比例缩放图像
	 * @param component
	 * @param compressWidth
	 * @param compressHeight
	 * @return
	 */
	public static Image shotZoomImage(JComponent component, int fixWidth, int fixHeight, int gap) {
		// 生成图像
		int width = component.getWidth();
		int height = component.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		component.printAll(g2d); // 关键，复制窗口到缓冲里
		g2d.dispose();
		
		// 如果组件的尺寸小于浮窗尺寸时，返回组件的实际图像
		if ((width <= fixWidth - gap * 2) && (height <= fixHeight - gap * 2)) {
			return image;
		}
		
		// 取出最小压缩比例
		if (gap < 0) gap = 0;
		double rw = (double) (fixWidth - gap * 2) / (double) width;
		double rh = (double) (fixHeight - gap * 2) / (double) height;
		double rate = (rw < rh ? rw : rh);

		// 压缩尺寸
		int compressWidth = (int) (rate * (double) width);
		int compressHeight = (int) (rate * (double) height);

		//		System.out.printf("fix:%d,%d, source:%d,%d, rw:%.3f, rh:%.3f, rate:%.3f, compress:%d,%d\n",
		//				fixWidth, fixHeight, width,height, rw, rh, rate, compressWidth, compressHeight );

		// 小于规定值时，用默认的
		if (compressWidth < 1 || compressHeight < 1) {
			return DesktopUtil.shotFillImage(component, fixWidth, fixHeight , gap);
		}
		
		// 生成缩放图片
		return image.getScaledInstance(compressWidth, compressHeight, Image.SCALE_SMOOTH);
	}
	
}