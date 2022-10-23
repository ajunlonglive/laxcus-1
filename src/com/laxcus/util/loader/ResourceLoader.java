/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.loader;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;

import javax.imageio.*;
import javax.swing.*;

import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;

/**
 * 资源加载器<br>
 * 
 * 资源加载器是从JAR文件包中解压并且提取资源，这些资源包括:文档、图片、视频、音频等。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2013
 * @since laxcus 1.0
 */
public class ResourceLoader {

	/** JAR资源目录，例如:"conf/front/" */
	private String root;

	/**
	 * 构造默认的资源加载器
	 */
	public ResourceLoader() {
		super();
		root = "";
	}

	/**
	 * 构造资源加载器，同时指定它的读取目录
	 * @param path JAR资源目录
	 */
	public ResourceLoader(String path) {
		this();
		setRoot(path);
	}

	/**
	 * 设置JAR文件的资源目录
	 * @param name 资源目录
	 */
	public void setRoot(String name) {
		if (name.charAt(name.length() - 1) != '/') {
			name += "/";
		}
		root = name;
	}

	/**
	 * 返回JAR文件资源目录
	 * @return 资源目录
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * resource url address, from jar file
	 * eg: <jar:file:/E:/laxcus/lib/spider.jar!/com/laxcus/spider/app.gif>
	 * @param name
	 * @return
	 */
	public URL findURL(String name) {
		String path = root + name;
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		return loader.getResource(path);
	}

	/**
	 * 按照绝对路径加载字节流并且输出
	 * @param path URI路径
	 * @return 字节数组
	 */
	public byte[] findAbsoluteStream(String path) {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream in = loader.getResourceAsStream(path);
		if (in == null) {
//			throw new IllegalValueException("cannot be find " + path);
			Logger.warning(this, "findAbsoluteStream", "cannot be find '%s'", path);
			return null;
		}

		byte[] b = new byte[1024];
		ClassWriter buff = new ClassWriter(1024);
		try {
			while (true) {
				int len = in.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				buff.write(b, 0, len);
			}
			in.close();
		} catch (IOException e) {
			return null;
		}

		if (buff.size() == 0) {
			return new byte[0];
		}
		return buff.effuse();
	}

	/**
	 * 指定资源文件名称，从JAR文件中查找资源数据
	 * @param name 文件名称（路径已经设定）
	 * @return 字节数组
	 */
	public byte[] findStream(String name) {
		return findAbsoluteStream(root + name);
	}

//	/**
//	 * 根据绝对目录找到图像
//	 * @param path 绝对目录
//	 * @return 返回ImageIcon实例
//	 */
//	public ImageIcon findAbsoluteIamgeX(String path) {
//		byte[] b = findAbsoluteStream(path);
//		if (b == null) {
//			return null;
//		}
//		return new ImageIcon(b);
//	}
	
	/**
	 * 根据绝对目录找到图像
	 * @param path 绝对目录
	 * @return 返回ImageIcon实例
	 */
	public ImageIcon findAbsoluteIamge(String path) {
		byte[] b = findAbsoluteStream(path);
		if (b == null) {
			return null;
		}
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(b));
			if (img != null) {
				return new ImageIcon(img);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 从JAR文件中取出解压缩后的图像
	 * @param name 文件名称（如果没有指定根目标，文件名称包含绝对路径）
	 * @return ImageIcon实例
	 */
	public ImageIcon findImage(String name) {
		return findAbsoluteIamge(root + name);
	}

	/**
	 * 压缩图像
	 * @param name
	 * @param width
	 * @param height
	 * @return
	 */
	private ImageIcon compress(String name, int width, int height) {
		byte[] b = findAbsoluteStream(root + name);
		// 没有取得字节数组
		if (b == null) {
			return null;
		}
		
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			BufferedImage sourceBI = ImageIO.read(in);

			// 平滑缩小图象
			Image compressImage = sourceBI.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
			// 生成一个新图像
			BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D gra = newBI.createGraphics();
			newBI = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);

			newBI.getGraphics().drawImage(compressImage, 0, 0, null);

			// 写入缓存
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(newBI, "PNG", out);
			out.flush();
			// 生成图像
			b = out.toByteArray();

			// 关闭
			out.close();
			in.close();

			// 读数据流
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(b));
			if (img != null) {
				return new ImageIcon(img);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}
	
	/**
	 * 从JAR文件中取出解压缩后的图像，同时指定图像的宽度和高度
	 * @param name 图像名称（忽略前面的路径）
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @return 返回ImageIcon实例
	 */
	public ImageIcon findImage(String name, int width, int height) {
		ImageIcon icon = findImage(name);
		if (icon == null) {
			return null;
		}
		// 判断尺寸一致
		boolean success = (icon.getIconWidth() == width && icon.getIconHeight() == height);
		if (success) {
			return icon;
		}
		// 不一致时，使用压缩，将图像转成指定的尺寸
		return compress(name, width, height);
	}
	
//	/**
//	 * 从JAR文件中取出解压缩后的图像，同时指定图像的宽度和高度
//	 * @param name 图像名称（忽略前面的路径）
//	 * @param width 图像宽度
//	 * @param height 图像高度
//	 * @return 返回ImageIcon实例
//	 */
//	public ImageIcon findImage(String name, int width, int height) {
//		ImageIcon icon = findImage(name);
//		if (icon != null) {
//			Image image = icon.getImage().getScaledInstance(width, height,Image.SCALE_SMOOTH);
//			return new ImageIcon(image);
//		}
//		return null;
//	}

	/**
	 * 根据绝对路径，从JAR文件取出解压缩的图像，同时指定输出图像的宽度和高度
	 * @param path 绝对路径
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @return 返回ImageIcon实例
	 */
	public ImageIcon findAbsoluteImage(String path, int width, int height) {
		ImageIcon icon = findAbsoluteIamge(path);
		if (icon != null) {
			Image image = icon.getImage().getScaledInstance(width, height,Image.SCALE_SMOOTH);
			return new ImageIcon(image);
		}
		return null;
	}

}