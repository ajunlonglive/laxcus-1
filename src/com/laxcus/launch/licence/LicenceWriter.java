/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.licence;

import java.io.*;
import java.util.*;

/**
 * 许可证编码生成器
 * 
 * @author scott.liang
 * @version 1.0 7/4/2020
 * @since laxcus 1.0
 */
public class LicenceWriter {

	/**
	 * 对内容进行编码
	 * @param content 内容
	 * @return 返回编码后的内容
	 */
	private static byte[] encode(byte[] content) throws IOException {
		// 混淆数据
		RSALicence.admix(content);

		// 生成SHA512签名,64个字节；再次混淆数据
		byte[] sha512 = RSALicence.doSHA512(content, 0, content.length);
		RSALicence.xor(content, sha512);

		// 再次异或混淆
		RSALicence.xor(sha512, RSALicence.SPLITTER);
		RSALicence.xor(content, sha512);

		// 填充位
		final int base = 1024;
		long begin = 32 * base;
		long end = 64 * base;
		// 确定一个填充范围
		int left = 0;
		Random rnd = new Random(content.length + System.currentTimeMillis());
		while (true) {
			int value = rnd.nextInt();
			if (begin <= value && value <= end) {
				left = value;
				break;
			}
		}

		// 总长度
		int length = sha512.length + content.length + RSALicence.SPLITTER.length + left;

		ByteArrayOutputStream writer = new ByteArrayOutputStream(length);
		// 依次是SHA512签名、加密后的数据、分隔符
		writer.write(sha512);
		writer.write(content);
		writer.write(RSALicence.SPLITTER);
		// 填充位
		byte[] b = new byte[1024];
		for(int off =0; off < left; ) {
			// 产生随机字节
			rnd.nextBytes(b);
			int len = ( left - off >= b.length ? b.length : left - off);
			writer.write(b, 0, len);
			// 追加字符串
			off += len;
		}

		// 输出
		byte[] full = writer.toByteArray();

		// 混淆编码
		RSALicence.confuse(full);
		return full;
	}

	/**
	 * 编码文件，结果输出到磁盘
	 * @param src 源文件
	 * @param dest 目标文件
	 * @return 成功返回真，否则假
	 */
	public static boolean write(File src, File dest) {
		// 判断文件存在
		boolean success = (src.exists() && src.isFile());
		if (!success) {
			return false;
		}
		// 文件必须有内容！
		if (src.length() == 0) {
			return false;
		}

		// 编码，写入磁盘
		success = false;
		try {
			FileInputStream in = new FileInputStream(src);
			byte[] b = new byte[(int) src.length()];
			in.read(b);
			in.close();

			// 编码
			b = LicenceWriter.encode(b);
			// 写入磁盘
			FileOutputStream out = new FileOutputStream(dest);
			out.write(b);
			out.close();
			success = true;
		} catch (IOException e) {
			//			Logger.error(e);
			e.printStackTrace();
		}
		return success;
	}

}