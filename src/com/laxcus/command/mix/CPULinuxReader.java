/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;

/**
 * LINUX平台CPU信息读取器
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class CPULinuxReader {

	/** CPU核心数**/
	private final static String PROCESSOR = "^\\s*(?i)(?:processor)\\s*(?:\\:)\\s*([0-9]+)\\s*$";

	/** 生产商 **/
	private final static String VENDOR_ID = "^\\s*(?i)(?:vendor_id)\\s*(?:\\:)\\s+([\\w\\W]+)\\s*$";

	/** 等同于生产商 **/
	private final static String SYSTEM_TYPE = "^\\s*(?i)(?:system\\s+type)\\s*(?:\\:)\\s*([\\w\\W]+)\\s*$";

	/** 产品名称 **/
	private final static String MODEL_NAME = "^\\s*(?i)(?:model\\s+name)\\s*(?:\\:)\\s*([\\w\\W]+)\\s*$";

	/** CPU频率 **/
	private final static String MHZ = "^\\s*(?i)(?:cpu\\s+MHz)\\s*(?:\\:)\\s*([\\w\\W]+)\\s*$";

	/** CPU二级缓存 **/
	private final static String CACHE_SIZE = "^\\s*(?i)(?:cache\\s+size)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";

	/** CPU物理编号 **/
	private final static String PHYSICAL_ID = "^\\s*(?i)(?:physical\\s+id)\\s*(?:\\:)\\s+([0-9]+)\\s*$";

	/** CPU核心数目 **/
	private final static String CPU_CORES = "^\\s*(?i)(?:cpu\\s+cores)\\s*(?:\\:)\\s+([0-9]+)\\s*$";

	/**
	 * 构造LINUX平台CPU信息读取器
	 */
	public CPULinuxReader() {
		super();
	}

//	/**
//	 * 从磁盘读取内存信息
//	 * @return 返回读取的字符串
//	 * @throws IOException
//	 */
//	private String readCPU() throws IOException {
//		final String filename = "/proc/cpuinfo";
//
//		// String filename = "j:/loongson_80_cpuinfo";
//		// final String filename = "/media/cpuinfo";
//		// String filename = "j:/cpu80";
//
//		File file = new File(filename);
//		if (!file.exists()) {
//			return null;
//		}
//
//		// 读数据
//		byte[] b = new byte[(int) file.length() + 256];
//		FileInputStream in = new FileInputStream(file);
//		int len = in.read(b, 0, b.length);
//		in.close();
//
//		// 输出字符串
//		return new String(b, 0, len);
//	}

	/**
	 * 从磁盘读取内存信息
	 * @return 返回读取的字符串
	 * @throws IOException
	 */
	private String readCPU() throws IOException {
		final String filename = "/proc/cpuinfo";

		// String filename = "j:/loongson_80_cpuinfo";
		// final String filename = "/media/cpuinfo";
		// String filename = "j:/cpu80";

		File file = new File(filename);
		if (!file.exists()) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] b = new byte[64];
		// 读数据
		FileInputStream in = new FileInputStream(file);
		do {
			int len = in.read(b, 0, b.length);
			if (len < 0) {
				break;
			}
			out.write(b, 0, len);
		} while (true);
		in.close();

		// 输出字符串
		b = out.toByteArray();
		return new String(b, 0, b.length);
	}

	
	/**
	 * 设置CPU处理核心数
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitProcessor(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.PROCESSOR);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			int processor = Integer.parseInt(matcher.group(1));
			// 核心数目
			if (processor >= item.getProcessor()) {
				item.setProcessor(processor);
			}
		}
		return success;
	}

	/**
	 * 设置生产商
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitVendorId(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.VENDOR_ID);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String name = matcher.group(1);
			item.setVendor(name);
		}
		return success;
	}

	/**
	 * 设置生产商
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitSystemType(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.SYSTEM_TYPE);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String name = matcher.group(1);
			item.setVendor(name);
		}
		return success;
	}

	/**
	 * 设置产品名
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitModelName(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.MODEL_NAME);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String name = matcher.group(1);
			name = name.replaceAll("\\s+", " ");
			item.setModelName(name);
		}
		return success;
	}

	/**
	 * 设置主频
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitMHz(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.MHZ);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			String name = matcher.group(1);
			item.setMHz(name);
		}
		return success;
	}

	/**
	 * 设置缓存尺寸
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitCacheSize(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.CACHE_SIZE);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setCacheSize(len);
			}
		}
		return success;
	}

	/**
	 * 解析物理核心数目
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitPhysicalId(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.PHYSICAL_ID);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			int id = Integer.parseInt(matcher.group(1));
			// 编号必须大于等于指定值
			if (id >= item.getPhysicalId()) {
				item.setPhysicalId(id);
			}
		}
		return success;
	}

	/**
	 * 芯心数
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitCores(String input, CPUInfoItem item) {
		Pattern pattern = Pattern.compile(CPULinuxReader.CPU_CORES);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			int cores = Integer.parseInt(matcher.group(1));
			// 核心数目
			item.setCores(cores);
		}
		return success;
	}

	/**
	 * 返回CPU参数
	 * @return
	 */
	public CPUInfoItem read() {
		String content =null;
		try {
			content = readCPU();
		} catch (IOException e) {

		}
		if (content == null) {
			return null;
		}

		String[] infs = content.split("\\r\\n|\\n");

		CPUInfoItem item = new CPUInfoItem();
		for(String line : infs) {
			// 解析参数
			boolean success = splitProcessor(line, item);
			if (!success) {
				success = splitVendorId(line, item);
			}
			if (!success) {
				success = splitSystemType(line, item); // 在龙芯上
			}
			if (!success) {
				success = splitModelName(line, item);
			}
			if (!success) {
				success = splitMHz(line, item);
			}
			if (!success) {
				success = splitCacheSize(line, item);
			}
			if (!success) {
				success = splitPhysicalId(line, item);
			}
			if (!success) {
				success = splitCores(line, item);
			}
		}

		return item;
	}

	//	/**
	//	 * 返回CPU参数
	//	 * @return
	//	 */
	//	public CPUInfoItem read() {
	//		String content =null;
	//		try {
	//			content = readCPU();
	//		} catch (IOException e) {
	//
	//		}
	//		if (content == null) {
	//			return null;
	//		}
	//		
	//		String[] items = content.split("\\r\\n|\\n");
	//		for(String item : items) {
	//			System.out.println(item);
	//		}
	//
	//		CPUInfoItem item = new CPUInfoItem();
	//
	//		BufferedReader reader = new BufferedReader(new 	StringReader(content)); 
	//		do {
	//			String line = null;
	//			try {
	//				line = reader.readLine();
	//			} catch (IOException e) {
	//
	//			}
	//			if (line == null) {
	//				break;
	//			}
	//
	//			// 解析参数
	//			boolean success = splitProcessor(line, item);
	//			if (!success) {
	//				success = splitVendorId(line, item);
	//			}
	//			if (!success) {
	//				success = splitSystemType(line, item); // 在龙芯上
	//			}
	//			if (!success) {
	//				success = splitModelName(line, item);
	//			}
	//			if (!success) {
	//				success = splitMHz(line, item);
	//			}
	//			if (!success) {
	//				success = splitCacheSize(line, item);
	//			}
	//			if (!success) {
	//				success = splitPhysicalId(line, item);
	//			}
	//			if (!success) {
	//				success = splitCores(line, item);
	//			}
	//		} while(true);
	//
	//		try {
	//			reader.close();
	//		} catch (IOException e) {
	//
	//		}
	//
	//		return item;
	//	}

	public static void main(String[] args) {
		CPULinuxReader e = new CPULinuxReader();
		CPUInfoItem item = e.read();
		System.out.printf("processoer:%d\n", item.getProcessor());
		System.out.printf("cache size: %d\n", item.getCacheSize());
		System.out.printf("cores %d\n", item.getCores());
		System.out.printf("MHz %s\n", item.getMHz());
		System.out.printf("model name %s\n", item.getModelName());
		System.out.printf("physical id %d\n", item.getPhysicalId());
		System.out.printf("vendor %s\n", item.getVendor());
	}

}