/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

import java.io.*;
import java.util.zip.*;

/**
 *
 * @author scott.liang
 * @version 1.0 7/2/2021
 * @since laxcus 1.0
 */
 class BasketReader {

	/**
	 * 从磁盘读取字节内容
	 * @param file
	 * @throws IOException
	 */
	private byte[] readContent(File file) throws IOException {
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throw new FileNotFoundException(file.toString());
		}
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		return b;
	}
	
	/**
	 * 导出
	 * @param tmp
	 * @param software
	 * @param pid
	 * @param file
	 * @throws IOException
	 */
	public void export(File tmp, String software, long pid, File file)
			throws IOException {
		// 生成目录
		File root = new File(tmp, String.format("%s%d", software, pid));
		boolean success = (root.exists() && root.isDirectory());
		if (!success) {
			success = root.mkdirs();
		}

		// 读文件
		byte[] content = this.readContent(file);
		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
		ZipInputStream jin = new ZipInputStream(bin);
		while (true) {
			ZipEntry entry = jin.getNextEntry();
			if (entry == null) {
				break;
			}

			// 只读文件，如果是目录，忽略！
			if (entry.isDirectory()) {
				continue;
			}

			// 文件内容
			String name = entry.getName();
//			System.out.println(name);
			
			File temp = root;
			int last = name.lastIndexOf("/");
			if (last > -1) {
				String prefix = name.substring(0, last);
				temp = new File(root, prefix);
				temp.mkdirs();
				name = name.substring(last + 1);
			}

			// 读一个文件
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			do {
				int len = jin.read(b, 0, b.length);
				if (len == -1) {
					break;
				}
				out.write(b, 0, len);
			} while (true);
			b = out.toByteArray();
			
			// 写入磁盘
			File w = new File(temp, name);
			FileOutputStream os = new FileOutputStream(w);
			os.write(b);
			os.close();
		}
		
		jin.close();
		bin.close();
	}
	
	
}
