/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.licence;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

/**
 * 冗余，无用！
 * 
 * @author scott.liang
 * @version 1.0 2020-7-19
 * @since laxcus 1.0
 */
public class LicenceHash {

	/**
	 * 把一个文件压缩成另一个文件
	 * @param source
	 * @param target
	 * @param compressWidth
	 * @param compressHeight
	 * @throws IOException
	 */
	public void compress(File source, File target, int compressWidth, int compressHeight)  throws IOException {
		FileInputStream in = new FileInputStream(source);
		BufferedImage img = ImageIO.read(in);

		// 平滑缩小图象
		Image compressImage = img.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
		// 生成一个新图像
		BufferedImage buff = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = buff.createGraphics();
		buff = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);

		buff.getGraphics().drawImage(compressImage, 0, 0, null);

		// 写入磁盘
		FileOutputStream out = new FileOutputStream(target);
		ImageIO.write(buff, "PNG", out);
		out.flush();
		out.close();

		in.close();
	}
	
	public void compres() {
		File source = new File("D:/laxcus/conf/front/terminal/image/window/status/client.png");
		File target = new File("D:/laxcus/conf/front/terminal/image/window/status/client_compress.png");
		try {
		this.compress(source, target, 16, 16);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finished!");
	}
	
}
