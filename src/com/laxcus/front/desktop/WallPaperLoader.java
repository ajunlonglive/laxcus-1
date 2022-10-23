/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.awt.*;
import java.io.*;

import javax.imageio.*;

import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;

/**
 * 桌面壁纸加载器 <br><br>
 * 
 * 1. 判断是文件 <br>
 * 2. 文件转存到临时目录 <br>
 * 3. 读取文件内容，生成数字签名做成文件名 <br>
 * 4. 读出图片文件，显示在桌面 <br>
 * 5. 新的文件名记录到内存里 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 3/2/2022
 * @since laxcus 1.0
 */
final class WallPaperLoader {

	/** 桌面 **/
	private PlatformDesktop desktop;

	/**
	 * 构造桌面壁纸加载器
	 * @param p 平台桌面
	 */
	public WallPaperLoader(PlatformDesktop p) {
		super();
		desktop = p;
	}
	
	/**
	 * 加载图像做为壁纸
	 * @param o
	 * @param layout
	 * @return
	 */
	public boolean load(Object o, int layout) {
		boolean success = (o.getClass() == File.class);
		if (!success) {
			return false;
		}

		File file = (File) o;
		success = (file.exists() && file.isFile());
		if (!success) {
			return false;
		}

		File dest = null;
		try {
			dest = moveTo(file);
		} catch (IOException e) {
			Logger.error(e);
		}
		if (dest == null) {
			return false;
		}

		// 设置桌面壁纸
		success = setWallPaper(dest, layout);
		if (success) {
			setReadFile(dest, layout);
		}

		return success;
	}
	
	/**
	 * 设置壁纸
	 * @param file
	 * @param layout
	 * @return
	 */
	private boolean setWallPaper(File file, int layout)  {
		Image img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			Logger.error(e);
		}
		if(img == null) {
			return false;
		}
		
		// 设置桌面壁纸
		DesktopWall wall = new DesktopWall(null, img, layout);
		desktop.setDesktopWall(wall);
		return true;
	}
	
	/**
	 * 移到系统的临时存储目录
	 * @param source 源文件
	 * @return 返回转存后的文件
	 * @throws IOException
	 */
	private File moveTo(File source) throws IOException {
		// 1. 找到临时目录
		File root = PlatformKit.getSystemTemporaryRoot();
		if (root == null) {
			return null;
		}

		// 取出文件后缀
		String filename = Laxkit.canonical(source);
		int last = filename.lastIndexOf('.');
		if (last == -1) {
			return null;
		}
		String suffix = filename.substring(last + 1);
		byte[] b = new byte[(int) source.length()];

		// 读取
		FileInputStream in = new FileInputStream(source);
		in.read(b);
		in.close();

		// 生成签名，做为文件名
		MD5Hash hash = Laxkit.doMD5Hash(b);
		String name = String.format("%s.%s", hash.toString(), suffix);

		// 写入指定的文件
		File file = new File(root, name);
		FileOutputStream out = new FileOutputStream(file);
		out.write(b);
		out.flush();
		out.close();

		// 新的文件名
		return file;
	}
	
//	/**
//	 * 保存读的脚本文件
//	 * 这个文件的同名方法在“BackgroundPane”类，让BackgroundPane可以读取
//	 * @param file
//	 */
//	private void setReadFile(File file) {
//		// 最后选中的文件
//		String filename = Laxkit.canonical(file);
//		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog/Background/ChoiceFile", filename);
//
//		// 图像文件
//		long code = com.laxcus.util.each.EachTrustor.sign(filename);
//		String paths = String.format("PropertiesDialog/Background/ImageFile/%X",code);
//		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, paths, filename);
//	}
	
	/**
	 * 保存读的脚本文件
	 * 这个文件的同名方法在“BackgroundPane”类，让BackgroundPane可以读取
	 * @param file
	 * @param layout
	 */
	private void setReadFile(File file, int layout) {
		// 最后选中的文件
		String filename = Laxkit.canonical(file);
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog/Background/ChoiceFile", filename);

		// 图像文件
		long code = com.laxcus.util.each.EachTrustor.sign(filename);
		String paths = String.format("PropertiesDialog/Background/ImageFile/%X",code);
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, paths, filename);
		
		// 选中的背景图片
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM,  "DesktopWindow/Background/File", filename);
		RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopWindow/Background/Layout", layout);
	}

}