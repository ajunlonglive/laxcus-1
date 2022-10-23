/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.awt.image.*;

import javax.swing.*;

import com.laxcus.util.color.*;

/**
 * 图像过滤器
 * 
 * @author scott.liang
 * @version 1.0 6/20/2021
 * @since laxcus 1.0
 */
public class ImageFilter {

	/** 对比度 **/
	private double contrast;

	/** 饱和度 **/
	private double saturation;

	/** 亮度 **/
	private double brightness;

	/**
	 * 构造默认的图像过滤器
	 */
	public ImageFilter() {
		super();
		brightness = 0d;
		contrast = 0d;
		saturation = 0d;
	}

	/**
	 * 构造图像过滤器，指定参数
	 * @param contrast 对比度
	 * @param saturation 饱和度
	 * @param brightness 亮度
	 */
	public ImageFilter(double contrast, double saturation, double brightness) {
		this();
		setBrightness(brightness);
		setContrast(contrast);
		setSaturation(saturation);
	}

	/**
	 * 返回亮度
	 * @return
	 */
	public double getBrightness() {
		return brightness;
	}

	/**
	 * 设置亮度
	 * @param who
	 */
	public void setBrightness(double who) {
		brightness = who;
	}

	/**
	 * 返回饱和度
	 * @return
	 */
	public double getSaturation() {
		return saturation;
	}

	/**
	 * 设置饱和度
	 * @param who
	 */
	public void setSaturation(double who) {
		saturation = who;
	}

	/**
	 * 返回对比度
	 * @return
	 */
	public double getContrast() {
		return contrast;
	}

	/**
	 * 设置对比度
	 * @param who
	 */
	public void setContrast(double who) {
		contrast = who;
	}

	/**
	 * 初始化参数，在设置完参数后调用
	 */
	public void initParameters() {
		contrast = (1.0 + contrast / 100.0);
		brightness = (1.0 + brightness / 100.0);
		saturation = (1.0 + saturation / 100.0);
	}

	/**
	 * 调整值
	 * @param value
	 * @return
	 */
	public int clamp(int value) {
		return value > 255 ? 255 : ((value < 0) ? 0 : value);
	}

	/**
	 * 过滤图像
	 * @param src
	 * @return
	 */
	public BufferedImage filter(BufferedImage src){
		// 宽度和高度
		int width = src.getWidth(null);
		int height = src.getHeight(null);

		// 依据现在的图像生成新对象
		BufferedImage dest = ImageUtil.createBufferedImage(new ImageIcon(src));

		// 参数
		int ta = 0, tr = 0, tg = 0, tb = 0;
		// 逐个像素处理
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int value = src.getRGB(x, y);
				// 透明!忽略！
				if (value == 0) {
					continue;
				}
				ta = (value >>> 24) & 0xff; // alpha

				// 生成RGB对象
				RGB rgb = new RGB(value);
				// 转成HSL颜色值
				HSL hsl = rgb.toHSL();

				float H = hsl.getH();
				// 调整饱和度
				float S = (float) (hsl.getS() * saturation);
				if (S < 0.0) {
					S = 0.0f;
				}
				if (S > 255.0) {
					S = 255.0f;
				}

				// 调整亮度
				float L = (float) (hsl.getL() * brightness);
				if (L < 0.0) {
					L = 0.0f;
				}
				if (L > 255.0) {
					L = 255.0f;
				}

				hsl = new HSL(H, S, L);
				// HSL转成RGB颜色值
				rgb = hsl.toRGB();

				// 生成限制值
				tr = clamp(rgb.red);
				tg = clamp(rgb.green);
				tb = clamp(rgb.blue);

				// 调整对比度
				double cr = ((tr / 255.0d) - 0.5d) * contrast;
				double cg = ((tg / 255.0d) - 0.5d) * contrast;
				double cb = ((tb / 255.0d) - 0.5d) * contrast;
				// 输出RGB值
				tr = (int) ((cr + 0.5f) * 255.0f);
				tg = (int) ((cg + 0.5f) * 255.0f);
				tb = (int) ((cb + 0.5f) * 255.0f);

				// 再转成ARGB值
				value = (ta << 24) | (clamp(tr) << 16 ) | (clamp(tg) << 8) | clamp(tb);
				// 保存RGB颜色
				dest.setRGB(x, y, value);
			}
		}
		return dest;
	}

	/**
	 * 过滤图像
	 * @param icon
	 * @return
	 */
	public BufferedImage filter(ImageIcon icon){
		BufferedImage src = ImageUtil.createBufferedImage(icon);
		return filter(src);
	}
	
}


//	public BufferedImage creatCompatibleDestImage(int width, int height) {
//		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//	}

//	/**
//	 * 调整
//	 */
//	public void handleParameters() {
//		contrast = (1.0 + contrast / 100.0);
//		brightness = (1.0 + brightness / 100.0);
//		saturation = (1.0 + saturation / 100.0);
//	}
