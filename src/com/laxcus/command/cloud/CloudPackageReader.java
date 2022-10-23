/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.util.*;

import java.io.*;
import java.util.zip.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 云应用包读取器。
 * 生成一个ZIP格式的数据包
 * 
 * @author scott.liang
 * @version 1.0 3/31/2020
 * @since laxcus 1.0
 */
public class CloudPackageReader {

	/** 数据内容，以字节形式保存 **/
	private byte[] content;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		// 释放内存！
		content = null;
	}

	/**
	 * 构造云应用包读取器，指定内容
	 * @param content 字节内容
	 */
	public CloudPackageReader(byte[] content) {
		super();
		setContent(content);
	}

	/**
	 * 构造云应用包读取器，读取磁盘文件内容
	 * @param file 文件实例
	 * @throws IOException
	 */
	public CloudPackageReader(File file) throws IOException {
		super();
		readContent(file);
	}

	/**
	 * 设置字节内容
	 * @param b
	 */
	private void setContent(byte[] b) {
		// 判断是空指针
		if (Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		}
		content = b;
	}

	/**
	 * 从磁盘读取字节内容
	 * @param file
	 * @throws IOException
	 */
	private void readContent(File file) throws IOException {
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throw new FileNotFoundException(file.toString());
		}
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();
		setContent(b);
	}

	/**
	 * 判断是CONDUCT应用
	 * @return 返回真或者假
	 */
	public boolean isConduct() {
		int[] families = PhaseTag.conduct();
		for (int family : families) {
			boolean success = hasElement(family);
			if (!success) return false;
		}
		return true;
	}

	/**
	 * 判断是ESTABLISH应用
	 * @return 返回真或者假
	 */
	public boolean isEstablish() {
		int[] families = PhaseTag.establish();
		for (int family : families) {
			boolean success = hasElement(family);
			if (!success) return false;
		}
		return true;
	}

	/**
	 * 判断是CONTACT应用
	 * @return 返回真或者假
	 */
	public boolean isContact() {
		int[] families = PhaseTag.contact();
		for (int family : families) {
			boolean success = hasElement(family);
			if (!success) return false;
		}
		return true;
	}

	/**
	 * 判断有某个阶段的成员
	 * @param family 阶段类型
	 * @return 返回真或者假
	 */
	private boolean hasElement(int family) {
		final String prefix = "^\\s*(?i)(%s\\/[\\w\\W]+?)\\s*$";
		String mark = PhaseTag.translate(family);
		String regex = String.format(prefix, mark);

		int count = 0;
		try {
			List<String> files = scan();
			if (files != null) {
				for (String filename : files) {
					boolean success = filename.matches(regex);
					if (success) count++;
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return count > 0;
	}

	/**
	 * 解析返回包中的文件
	 * @param content 内容
	 * @return 返回文件数组
	 * @throws IOException
	 */
	public List<String> scan() throws IOException {
		ArrayList<String> a = new ArrayList<String>();
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
			a.add(name);
		}

		jin.close();
		bin.close();

		return a;
	}

	/**
	 * 判断有动态链接库文件
	 * @return 返回真或者假
	 */
	public boolean hasLibrary() {
		final String regex = "^\\s*([\\w\\W]+)(?i)(.so|.dll)\\s*$";
		int count = 0;
		try {
			List<String> files = scan();
			for (String filename : files) {
				boolean b = filename.matches(regex);
				if(b) count++;
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return count > 0;
	}

	/**
	 * 判断有JAR文件
	 * @return
	 */
	public boolean hasAssist() {
		final String regex = "^\\s*([\\w\\W]+)(?i)(.jar)\\s*$";
		int count = 0;
		try {
			List<String> files = scan();
			for (String filename : files) {
				boolean b = filename.matches(regex);
				if (b) count++;
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return count > 0;
	}

	/**
	 * 检测和读取内容
	 * @return 返回计取的单元数目。成功是大于等于0，否则是-1。
	 */
	public int check() {
		try {
			int count = 0;
			// 读内容
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

				// 读取字节数组
				byte[] b = new byte[1024];
				while (true) {
					int len = jin.read(b, 0, b.length);
					if (len == -1) break;
				}

				// 成功，统计加1
				count++;
			}

			jin.close();
			bin.close();
			return count;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		return -1;
	}

	/**
	 * 取出自读文件中的单元
	 * @return CloudPackageItem 集合
	 * @throws IOException
	 */
	public List<CloudPackageItem> readReadmeItems() throws IOException {
		final String readme = "^\\s*(?i)(README)(\\/[\\w\\W]+)\\s*$";
		return readItems(readme);
	}

	/**
	 * 基于文件路径正则表达的判断，读取数据内容
	 * @param pathRegex 正则表达式
	 */
	public List<CloudPackageItem> readItems(String pathRegex) throws IOException {
		ArrayList<CloudPackageItem> array = new ArrayList<CloudPackageItem>();

		// 读内容
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
			if (!name.matches(pathRegex)) {
				continue;
			}

			// 读取字节数组
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			while (true) {
				int len = jin.read(b, 0, b.length);
				if (len == -1) break;
				out.write(b, 0, len);
			}
			b = out.toByteArray();

			// 保存
			long time = entry.getTime();
			CloudPackageItem item = new CloudPackageItem(name, time, b);
			array.add(item);
		}

		jin.close();
		bin.close();

		return array;
	}

	/**
	 * 读取引导文件
	 * @return 包字节流成员
	 * @throws IOException
	 */
	public CloudPackageItem readGTC() throws IOException {
		final String regex = "^\\s*(?i)(GUIDE\\/[\\w\\W]+)(?i)(.GTC)\\s*$";
		// 读取内容
		List<CloudPackageItem> array = readItems(regex);
		return (array.size() > 0 ? array.get(0) : null);
	}

	/**
	 * 读取引导JAR附件
	 * @return 返回相关的单元
	 * @throws IOException
	 */
	public List<CloudPackageItem> readGTCAssists() throws IOException {
		String regex = "^\\s*(?i)(GUIDE\\/[\\w\\W]+)(?i)(.JAR)\\s*$";
		return readItems(regex);
	}

	/**
	 * 读取引导全部动态库
	 * @return
	 * @throws IOException
	 */
	public List<CloudPackageItem> readGTCLibraries() throws IOException {
		// 生成正则表达式
		final String regex = "^\\s*(?i)(GUIDE\\/[\\w\\W]+)(?i)(.DLL|.SO)\\s*$";
		return readItems(regex);
	}

	/**
	 * 读取文件中的分布任务组件引导包
	 * @param family 阶段类型
	 * @return 返回包字节数据内容
	 */
	public CloudPackageItem readDTC(int family) throws IOException {
		// 这行参数与BuildConductPackageParser保持一致
		String mark = PhaseTag.translate(family);
		// 生成正则表达式
		final String prefix = "^\\s*(?i)(%s\\/[\\w\\W]+)(?i)(.DTC)\\s*$";
		String regex = String.format(prefix, mark);

		// 读取内容
		List<CloudPackageItem> array = readItems(regex);
		return (array.size() > 0 ? array.get(0) : null);
	}

	/**
	 * 读取JAR附件
	 * @param family 阶段命名
	 * @return 返回相关的单元
	 * @throws IOException
	 */
	public List<CloudPackageItem> readAssists(int family) throws IOException {
		// 这行参数与BuildConductPackageParser保持一致
		String mark = PhaseTag.translate(family);

		// 生成正则表达式
		final String prefix = "^\\s*(?i)(%s\\/[\\w\\W]+)(?i)(.JAR)\\s*$";
		String regex = String.format(prefix, mark);
		return readItems(regex);
	}

	/**
	 * 读取动态链接库
	 * @param family 阶段命名
	 * @return 返回相关的单元
	 * @throws IOException
	 */
	public List<CloudPackageItem> readLibraries(int family) throws IOException {
		// 这行参数与BuildConductPackageParser保持一致
		String mark = PhaseTag.translate(family);

		// 生成正则表达式
		final String prefix = "^\\s*(?i)(%s\\/[\\w\\W]+)(?i)(.DLL|.SO)\\s*$";
		String regex = String.format(prefix, mark);
		return readItems(regex);
	}

	/**
	 * 读取全部附件
	 * @return
	 * @throws IOException
	 */
	public List<CloudPackageItem> readAssists() throws IOException {
		// 匹配的正则表达式
		String regex = "^\\s*(?i)([\\w\\W]+)(?i)(.JAR)\\s*$";
		return readItems(regex);
	}

	/**
	 * 读取全部动态库
	 * @return
	 * @throws IOException
	 */
	public List<CloudPackageItem> readLibraries() throws IOException {
		// 匹配的正则表达式
		String regex = "^\\s*(?i)([\\w\\W]+)(?i)(.DLL|.SO)\\s*$";
		return readItems(regex);
	}

	//	public static void main(String[] args) {
	//		File file = new File("e:/aixbit/conduct.cpk");
	//		file = new File("e:/aixbit/unix.cpk");
	//		file = new File("e:/aixbit/print.spk");
	//
	//		final String licence = "^\\s*(?i)(LICENCE)([\\w\\W]+)\\s*$";
	//		final String readme = "^\\s*(?i)(README)(\\/[\\w\\W]+)\\s*$";// "^\\s*(?i)(LICENCE)([\\w\\W]+)\\s*$";
	//
	//		try {
	//			CloudPackageReader reader = new CloudPackageReader(file);
	//			List<CloudPackageItem> items = reader.readReadmeItems();
	//			System.out.printf("count %d\n", items.size());
	//			
	//			System.out.printf("conduct is %s\n", reader.isConduct());
	//			System.out.printf("establish is %s\n", reader.isEstablish());
	//			System.out.printf("contact is %s\n", reader.isContact());
	//			
	////			for (CloudPackageItem e : items) {
	////				String name = e.getSimpleName();
	////				System.out.println(name);
	////				// 名字匹配
	////				if (name.matches(licence)) {
	////					String content = new UTF8().decode(e.getContent());
	////					System.out.println(content);
	//////					break;
	////				}
	////			}
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}

}