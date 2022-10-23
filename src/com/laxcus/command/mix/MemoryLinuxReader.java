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
 * LINUX平台内存信息读取器
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class MemoryLinuxReader {

	//	final String filename = "/proc/meminfo";

	private final static String TOTAL_REGEX = "^\\s*(?i)(?:MemTotal)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";
	private final static String FREE_REGEX = "^\\s*(?i)(?:MemFree)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";
	private final static String AVAILABLE_REGEX = "^\\s*(?i)(?:MemAvailable)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";

	private final static String BUFFERS = "^\\s*(?i)(?:Buffers)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";
	private final static String CACHED = "^\\s*(?i)(?:Cached)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";

	private final static String SWAP_TOTAL = "^\\s*(?i)(?:SwapTotal)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";
	private final static String SWAP_FREE = "^\\s*(?i)(?:SwapFree)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";
	private final static String SWAP_CACHED = "^\\s*(?i)(?:SwapCached)\\s*(?:\\:)\\s+(?i)([0-9]+\\s*kB)\\s*$";


	/**
	 * 内存读取器
	 */
	public MemoryLinuxReader() {
		super();
	}

	/**
	 * 从磁盘读取内存信息
	 * @return 返回读取的字符串
	 * @throws IOException
	 */
	private String readMemory() throws IOException {
		final String filename = "/proc/meminfo";

		// final String filename = "/media/meminfo";

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

		//		// 读数据
		//		byte[] b = new byte[(int) file.length() + 256];
		//		FileInputStream in = new FileInputStream(file);
		//		int len = in.read(b, 0, b.length);
		//		in.close();
		//
		//		// 输出字符串
		//		return new String(b, 0, len);
	}

	/**
	 * 解析
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitTotal(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.TOTAL_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setTotal(len);
			}
		}
		return success;
	}

	/**
	 * 剩余内存
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitFree(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.FREE_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			// 在没有定义“MemAvailable”情况下设置它!
			if (len > 0 && item.getAvailable() < 1) {
				item.setAvailable(len);
			}
		}
		return success;
	}

	/**
	 * 有效内存
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitAvailable(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.AVAILABLE_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setAvailable(len);
			}
		}
		return success;
	}

	/**
	 * 缓存
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitBuffers(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.BUFFERS);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setBuffers(len);
			}
		}
		return success;
	}

	/**
	 * 缓冲
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitCached(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.CACHED);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setCached(len);
			}
		}
		return success;
	}

	/**
	 * 交换缓冲
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitSwapCached(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.SWAP_CACHED);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setSwapCached(len);
			}
		}
		return success;
	}

	/**
	 * 全部交换缓冲
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitSwapTotal(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.SWAP_TOTAL);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setSwapTotal(len);
			}
		}
		return success;
	}

	/**
	 * 自由交换缓冲
	 * @param input
	 * @param item
	 * @return
	 */
	private boolean splitSwapFree(String input, MemoryInfoItem item) {
		Pattern pattern = Pattern.compile(MemoryLinuxReader.SWAP_FREE);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			long len = ConfigParser.splitLongCapacity(matcher.group(1), 0);
			if (len > 0) {
				item.setSwapFree(len);
			}
		}
		return success;
	}

	/**
	 * 读取信息
	 * @return
	 */
	public MemoryInfoItem read() {
		String content =null;
		try {
			content = readMemory();
		} catch (IOException e) {

		}
		if (content == null) {
			return null;
		}

		String[] infs = content.split("\\r\\n|\\n");

		MemoryInfoItem item = new MemoryInfoItem();
		// 解析参数
		for (String line : infs) {
			boolean success = splitTotal(line, item);
			if (!success) {
				success = splitFree(line, item);
			}
			if (!success) {
				success = splitAvailable(line, item);
			}
			if (!success) {
				success = splitBuffers(line, item);
			}
			if (!success) {
				success = splitCached(line, item);
			}
			if (!success) {
				success = splitSwapTotal(line, item);
			}
			if (!success) {
				success = splitSwapFree(line, item);
			}
			if (!success) {
				success = splitSwapCached(line, item);
			}
		}

		return item;
	}

	//	/**
	//	 * 读取信息
	//	 * @return
	//	 */
	//	public MemoryInfoItem read() {
	//		String content =null;
	//		try {
	//			content = readMemory();
	//		} catch (IOException e) {
	//			
	//		}
	//		if (content == null) {
	//			return null;
	//		}
	//		
	//		MemoryInfoItem item = new MemoryInfoItem();
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
	//			boolean success = splitTotal(line, item);
	//			if (!success) {
	//				success = splitFree(line, item);
	//			}
	//			if (!success) {
	//				success = splitAvailable(line, item);
	//			}
	//			if (!success) {
	//				success = splitBuffers(line, item);
	//			}
	//			if (!success) {
	//				success = splitCached(line, item);
	//			}
	//			if (!success) {
	//				success = splitSwapTotal(line, item);
	//			}
	//			if (!success) {
	//				success = splitSwapFree(line, item);
	//			}
	//			if (!success) {
	//				success = splitSwapCached(line, item);
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

}
