/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;

/**
 * 图像工具
 * 
 * @author scott.liang
 * @version 1.0 6/4/2021
 * @since laxcus 1.0
 */
public class ImageUtil {

//	/**
//	 * 压缩图像
//	 * @param b
//	 * @param width
//	 * @param height
//	 * @return
//	 */
//	public static ImageIcon scale(byte[] b, int width, int height, boolean translucent) {
//		try {
//			ByteArrayInputStream in = new ByteArrayInputStream(b);
//			BufferedImage sourceBI = ImageIO.read(in);
//
//			// 平滑改变图象
//			Image compressImage = sourceBI.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
//			// 生成一个新图像
//			BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//			// 透明处理
//			if (translucent) {
//				Graphics2D gra = newBI.createGraphics();
//				newBI = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//			}
//			
//			newBI.getGraphics().drawImage(compressImage, 0, 0, null);
//			
//			return new ImageIcon(newBI);
//
////			// 写入磁盘
////			ByteArrayOutputStream out = new ByteArrayOutputStream();
////			ImageIO.write(newBI, "PNG", out);
////			out.flush();
////			// 生成图像
////			b = out.toByteArray();
////
////			// 关闭
////			out.close();
////			in.close();
////
////			return new ImageIcon(b);
//		} catch (IOException e) {
//
//		}
//		return null;
//	}
	
	
	/**
	 * 压缩图像
	 * @param b
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scale(Image image, int width, int height, boolean translucent) {
		
//			createBufferedImage(image);
		
//		try {
//			ByteArrayInputStream in = new ByteArrayInputStream(b);
			BufferedImage sourceBI = createBufferedImage(image); // ImageIO.read(in);
			if(sourceBI == null){
				return null;
			}
			// 尺寸一致，返回
			boolean match = (sourceBI.getWidth() == width && sourceBI.getHeight() == height);
			if (match) {
				return new ImageIcon(sourceBI);
			}

			// 平滑改变图象
			Image compressImage = sourceBI.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
			// 生成一个新图像
			BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			// 透明处理
			if (translucent) {
				Graphics2D gra = newBI.createGraphics();
				newBI = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
			}
			
			newBI.getGraphics().drawImage(compressImage, 0, 0, null);
			
			return new ImageIcon(newBI);
//		} catch (IOException e) {
//
//		}
//		return null;
	}
	
	/**
	 * 压缩图像
	 * @param b
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scale(byte[] b, int width, int height, boolean translucent) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			BufferedImage sourceBI = ImageIO.read(in);
			// 尺寸一致，返回
			boolean match = (sourceBI.getWidth() == width && sourceBI.getHeight() == height);
			if (match) {
				return new ImageIcon(sourceBI);
			}

			// 平滑改变图象
			Image compressImage = sourceBI.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
			// 生成一个新图像
			BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			// 透明处理
			if (translucent) {
				Graphics2D gra = newBI.createGraphics();
				newBI = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
			}
			
			newBI.getGraphics().drawImage(compressImage, 0, 0, null);
			
			return new ImageIcon(newBI);
		} catch (IOException e) {

		}
		return null;
	}
	
	/**
	 * 压缩图像，默认是透明处理
	 * @param b
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scale(byte[] b, int width, int height) {
		return ImageUtil.scale(b, width, height, true);
	}
	
	/**
	 * 把缓存图像转成字节数组输出
	 * @param buff
	 * @return 返回字节数组
	 */
	protected static byte[] transform(BufferedImage buff) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(buff, "PNG", out);
		out.flush();
		// 生成图像
		return out.toByteArray();
	}

	/**
	 * 生成一个缓存对象
	 * @param image 图像
	 * @return 返回缓存对象，或者空指针
	 */
	public static BufferedImage createBufferedImage(Image image) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		// 生成一个新图像
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = dest.createGraphics();
		dest = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		dest.getGraphics().drawImage(image, 0, 0, null);

		// 转换
		try {
			// 转换
			byte[] b = transform(dest);
			return ImageIO.read(new ByteArrayInputStream(b));
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 生成一个缓存对象
	 * @param src 图像
	 * @return 返回缓存对象，或者空指针
	 */
	public static BufferedImage createBufferedImage(ImageIcon src) {
		return createBufferedImage(src.getImage());
	}

	/**
	 * 从对比度、饱和度、亮度三个角度，把一个新对象转换成另一个对象
	 * 
	 * @param image 源图像
	 * @param contrast 对比度
	 * @param saturation 饱和度
	 * @param brightness 亮度，正数是提高亮度，负数是减少亮度
	 * @return 返回新的对象
	 */
	public static ImageIcon adjust(ImageIcon image, double contrast, double saturation, double brightness) {		
		ImageFilter filter = new ImageFilter();
		// 对比度
		filter.setContrast(contrast);
		// 饱和度
		filter.setSaturation(saturation);
		// 调整亮度
		filter.setBrightness(brightness);
		// 初始化参数
		filter.initParameters();
		// 执行过滤
		BufferedImage dest = filter.filter(image);
		// 返回图像对象
		return new ImageIcon(dest);
	}

	/**
	 * 根据输入图像，生成一个新的高亮图像
	 * 
	 * @param image 图像
	 * @param brightness 高亮提升幅度
	 * @return 返回新的图像按纽，失败返回空指针
	 */
	public static ImageIcon brighter(ImageIcon image, double brightness) {
		if (brightness < 0) {
			throw new IllegalValueException("illegal brighter value %f", brightness);
		}
		return adjust(image, 0.0f, 0.0f, brightness);
	}

	/**
	 * 根据输入图像，生成一个新的暗色图像
	 * 
	 * @param image 图像
	 * @param dark 高亮降低幅度，必须是小于等于0的值
	 * @return 返回新的图像按纽，失败返回空指针
	 */
	public static ImageIcon dark(ImageIcon image, double dark) {
		if (dark > 0) {
			throw new IllegalValueException("illegal dark value %f", dark);
		}

		return adjust(image, 0.0f, 0.0f, dark);
	}
	
//	/**
//	 * 过滤图像
//	 * @param src
//	 * @return
//	 */
//	public static BufferedImage disable(BufferedImage src, Color gray){
//		// 宽度和高度
//		int width = src.getWidth(null);
//		int height = src.getHeight(null);
//
//		// 依据现在的图像生成新对象
//		BufferedImage dest = ImageUtil.createBufferedImage(new ImageIcon(src));
//
//		int g = gray.getRGB();
//
//		// 逐个像素处理
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				int value = src.getRGB(x, y);
//				// 透明!忽略！
//				if (value == 0) {
//					continue;
//				}
//				// 如果是白色，忽略
//				else {
//					ESL esl = new ESL(new Color(value));
//					double s = esl.getS(); // 饱和度
//					double l = esl.getL(); // 亮度
//					if ((0 <= s && s <= 20) || (220 <= l && l <= 240)) {
//						continue;
//					}
//				}
//				
//				// 保存RGB颜色
//				dest.setRGB(x, y, g);
//			}
//		}
//		return dest;
//	}

	/**
	 * 过滤图像
	 * @param src
	 * @return
	 */
	public static BufferedImage disable(BufferedImage src, Color gray){
		// 宽度和高度
		int width = src.getWidth(null);
		int height = src.getHeight(null);

		// 依据现在的图像生成新对象
		BufferedImage dest = ImageUtil.createBufferedImage(new ImageIcon(src));

		//	int g = gray.getRGB();

		ESL source = new ESL(gray);

		// 逐个像素处理
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int value = src.getRGB(x, y);
				// 透明!忽略！
				if (value == 0) {
					continue;
				}
				
				//				// 如果是白色，忽略
				//				else {
				//					ESL esl = new ESL(new Color(value));
				//					double s = esl.getS(); // 饱和度
				//					double l = esl.getL(); // 亮度
				//					if ((0 <= s && s <= 20) || (220 <= l && l <= 240)) {
				//						continue;
				//					}
				//				}

				ESL esl = new ESL(new Color(value));
				source.setS(esl.getS());
				source.setL(esl.getL()); // 采用原图的亮度
				int g = source.toColor().getRGB();

				// 保存RGB颜色
				dest.setRGB(x, y, g);
			}
		}
		return dest;
	}

	
	/**
	 * 生成无效的图像
	 * @param icon 图标
	 * @param gray 灰颜色
	 * @return 返回失效颜色
	 */
	public static ImageIcon disable(Image icon, Color gray) {
		BufferedImage src = ImageUtil.createBufferedImage(icon);
		BufferedImage dest = ImageUtil.disable(src, gray);
		// 返回灰色图像
		return new ImageIcon(dest);
	}

	/**
	 * 生成无效的图像
	 * @param icon 图标
	 * @param gray 灰颜色
	 * @return 返回失效颜色
	 */
	public static ImageIcon disable(ImageIcon icon, Color gray) {
		BufferedImage src = ImageUtil.createBufferedImage(icon);
		BufferedImage dest = ImageUtil.disable(src, gray);
		// 返回灰色图像
		return new ImageIcon(dest);
	}

}

///**
// * 生成一个缓存对象
// * @param src 图像
// * @return 返回缓存对象，或者空指针
// */
//public static BufferedImage createBufferedImage(ImageIcon src) {
//	int width = src.getIconWidth();
//	int height = src.getIconHeight();
//	
//	// 生成一个新图像
//	BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	Graphics2D gra = dest.createGraphics();
//	dest = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
//	dest.getGraphics().drawImage(src.getImage(), 0, 0, null);
//
//	// 转换
//	try {
//		// 转换
//		byte[] b = transform(dest);
//		return ImageIO.read(new ByteArrayInputStream(b));
//	} catch (IOException e) {
//		Logger.error(e);
//	}
//	return null;
//}



///**
// * 将图像变亮或者变暗，返回真的图像
// * 
// * @param image 源图像
// * @param brighter 变亮或者否
// * @param flag 调整值，必须是正数
// * @return 返回真或者假
// */
//public static ImageIcon adjust(ImageIcon image, boolean brighter, double flag) {
//	if (flag < 0) {
//		throw new IllegalValueException("illegal value %f", flag);
//	}
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
//				// 加亮或者暗
//				if (brighter) {
//					esl.brighter(flag);
//				} else {
//					esl.darker(flag);
//				}
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

///**
// * 根据输入图像，生成一个新的高亮图像
// * 
// * @param image 图像
// * @param flag 高亮提升幅度
// * @return 返回新的图像按纽，失败返回空指针
// */
//public static ImageIcon brighter(ImageIcon image, double flag) {
//	return adjust(image, true, flag);
//}
//
///**
// * 根据输入图像，生成一个新的高亮图像
// * 
// * @param image 图像
// * @param flag 高亮提升幅度
// * @return 返回新的图像按纽，失败返回空指针
// */
//public static ImageIcon dark(ImageIcon image, double flag) {
//	return adjust(image, false, flag);
//}


///**
// * 生成新的图像
// * 
// * @param image 图像
// * @param brighter 亮度
// * @return 返回新的对象
// */
//public static ImageIcon adjust(ImageIcon image, double brighter) {
//	return adjust(image, 0.0f, 0.0f, brighter);
//}
