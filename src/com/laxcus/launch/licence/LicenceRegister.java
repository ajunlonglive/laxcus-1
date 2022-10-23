/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.licence;

import java.io.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.xml.*;

/**
 * 许可证解码读取器
 * 
 * @author scott.liang
 * @version 1.0 7/4/2020
 * @since laxcus 1.0
 */
public class LicenceRegister {

	/**
	 * 切换内容
	 * @param content
	 * @return
	 */
	private static byte[] split(byte[] content) {
		// 找到标记符，实际字符串
		byte[] tar = new byte[RSALicence.SPLITTER.length];
		for (int off = 0; off < content.length; off++) {
			int len = (content.length - off >= tar.length ? tar.length : content.length - off);
			System.arraycopy(content, off, tar, 0, len);
			// 比较一致，输出实际字符串
			if (Laxkit.compareTo(tar, RSALicence.SPLITTER) == 0) {
				byte[] b = new byte[off];
				System.arraycopy(content, 0, b, 0, off);
				return b;
			}
		}
		return null;
	}

	/**
	 * 对内容进行解码
	 * @param full 字节数组
	 * @return 返回字节数组
	 */
	private static byte[] decode(byte[] full) {
		// 混淆解码
		RSALicence.confuse(full);

		// 取出数据内容
		byte[] stream = LicenceRegister.split(full);
		// 错误，返回空指针
		if (stream == null) {
			return null;
		}

		// 解密数据

		// 1. 取出前面是SHA512签名
		byte[] sha512 = new byte[64];
		if (sha512.length >= stream.length) {
			return null;
		}
		System.arraycopy(stream, 0, sha512, 0, sha512.length);
		// 2. 取出后面的数据内容
		int len = stream.length - sha512.length;
		byte[] content = new byte[len];
		System.arraycopy(stream, sha512.length, content, 0, len);

		// 3. 根据SHA512签名进行解密
		RSALicence.xor(content, sha512);

		// 4. 再次异或
		RSALicence.xor(sha512, RSALicence.SPLITTER);
		RSALicence.xor(content, sha512);

		// 4. 根据数据长度进行解密
		RSALicence.admix(content);
		return content;
	}

	/**
	 * 从磁盘中解码
	 * @param file
	 * @return
	 */
	public static byte[] read(File file) {
		// 判断文件存在
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return null;
		}
		// 文件必须有内容！
		if (file.length() == 0) {
			return null;
		}

		// 从磁盘中读取，解码！
		try {
			FileInputStream in = new FileInputStream(file);
			byte[] b = new byte[(int) file.length()];
			in.read(b);
			in.close();

			// 编码
			return LicenceRegister.decode(b);
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 解密许可证文件，转换成XML档案实例
	 * @param file 磁盘文件
	 * @return XML档案实例
	 */
	public static org.w3c.dom.Document readXMLDocument(File file) {
		// 解决许可证
		byte[] b = LicenceRegister.read(file);
		if (b != null) {
			return XMLocal.loadXMLSource(b);
		}
		return null;
	}

	/**
	 * 解密许可证文件，读取根成员
	 * @param file 磁盘文件
	 * @param rootTag 根标记
	 * @return XML Element实例，或者空指针
	 */
	public static org.w3c.dom.Element readXMLElement(File file, String rootTag) {
		// 解析文本
		org.w3c.dom.Document document = LicenceRegister.readXMLDocument(file);
		if (document == null) {
			return null;
		}

		// rootTag是根签名
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(rootTag);
		if (nodes.getLength() == 1) {
			return (org.w3c.dom.Element) nodes.item(0);
		}
		
		return null;
	}

//	public static void main(String[] args) {
//		File file = new File("f:/encrypt/licence");
//		String tag = "account-configure";
//		org.w3c.dom.Element element = LicenceReader.readXMLElement(file, tag);
//		if(element != null) {
////			System.out.println(element.getTextContent());// .getTextContent());
//			System.out.printf("max-user is:%s\n", 	XMLocal.getValue(element, "max-users"));
//			System.out.printf("tick is:%s\n", 	XMLocal.getValue(element, "max-members"));
//		} else {
//			System.out.println("FUCK!");
//		}
//		
//	}
}