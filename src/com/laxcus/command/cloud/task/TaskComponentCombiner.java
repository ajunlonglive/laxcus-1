/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.laxcus.command.cloud.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 组件合并器
 * 
 * @author scott.liang
 * @version 1.0 4/11/2020
 * @since laxcus 1.0
 */
public class TaskComponentCombiner {

	/** 软件包集合，标签（解决唯一性） -> 包单元 **/
	private TreeMap<WareTag, CloudPackageItem> packages = new TreeMap<WareTag, CloudPackageItem>();

	/**
	 * 构造默认的组件合并器
	 */
	public TaskComponentCombiner() {
		super();
	}

	//	/**
	//	 * 返回字符串表述
	//	 * @param time 时间
	//	 * @return 字符串
	//	 */
	//	private String toDate(long time) {
	//		SimpleDateFormat style = new SimpleDateFormat(
	//				"yyyy-MM-dd HH:mm:ss SSS", Locale.ENGLISH);
	//		return style.format(new java.util.Date(time));
	//	}
	//
	//	/**
	//	 * 判断允许保存
	//	 * @return 返回真或者假
	//	 */
	//	private boolean allow(WareTag that, long time) {
	//		// 空集合，允许！
	//		if (packages.isEmpty()) {
	//			return true;
	//		}
	//
	//		Iterator<Map.Entry<WareTag, CloudPackageItem>> iterator = packages.entrySet().iterator();
	//
	//		while (iterator.hasNext()) {
	//			Map.Entry<WareTag, CloudPackageItem> entry = iterator.next();
	//			WareTag tag = entry.getKey();
	//			CloudPackageItem value = entry.getValue();
	//
	//			// 软件包一致！
	//			if (Laxkit.compareTo(tag, that) == 0) {
	//				// 新包时间在于旧包时间，返回真；否则假。
	//				boolean pass = (time > value.getTime());
	//				Logger.debug(this, "allow", pass, "%s > %s",
	//						toDate(time), toDate(value.getTime()));
	//
	//				return pass;
	//			} else if (Laxkit.compareTo(tag.getName(), that.getName()) == 0) {
	//				// 新包版本号大于旧包版本号，返回真；否则假。
	//				boolean pass = (that.getVersion().getVersion() > tag.getVersion().getVersion());
	//				Logger.debug(this, "allow", pass, "%s > %s", that.getVersion(), tag.getVersion());
	//				return pass;
	//			}
	//			
	//		}
	//
	//		// 以上不成功，允许！
	//		return true;
	//	}

	/**
	 * 判断允许保存
	 * @return 返回真或者假
	 */
	private boolean allow(WareTag that, long time) {
		// 空集合，允许！
		if (packages.isEmpty()) {
			return true;
		}

		Iterator<Map.Entry<WareTag, CloudPackageItem>> iterator = packages.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<WareTag, CloudPackageItem> entry = iterator.next();
			WareTag tag = entry.getKey();
			CloudPackageItem value = entry.getValue();

			// 软件包一致！
			if (Laxkit.compareTo(tag, that) == 0) {
				// 新包时间在于旧包时间，返回真；否则假。
				return (time > value.getTime());
			} else if (Laxkit.compareTo(tag.getNaming(), that.getNaming()) == 0) {
				// 新包版本号大于旧包版本号，返回真；否则假。
				return (that.getVersion().getVersion() > tag.getVersion().getVersion());
			}
		}

		// 以上不成功，允许！
		return true;
	}
	/**
	 * 字节内容输出到压缩包
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 长度
	 * @param selfly 是否自有组件集合
	 * @return 成功，返回写入的字节长度；如果同类包已经存在，且优先级更高时，返回0。
	 * @throws IOException
	 */
	public int write(byte[] b, int off, int len, long time) throws IOException {
		TaskComponentReader reader= new TaskComponentReader(b, off, len);
		WareTag tag = reader.readWareTag();
		if (tag == null) {
			throw new IOException("not found WareTag!");
		}

		// 生成内容的散列码
		MD5Hash hash = Laxkit.doMD5Hash(b);
		// 组件文件!
		String name = hash.toString() + TF.DTC_SUFFIX;
		
//		if (tag.isSelfly() || selfly) {
//			name = TF.SELFLY_FILE;
//		}

		// 判断允许保存！
		if (allow(tag, time)) {
			CloudPackageItem sub = new CloudPackageItem(name, time, b, off, len);
			packages.put(tag, sub);
			return len;
		}
		return 0;
	}

	/**
	 * 字节内容输出到压缩包
	 * @param b 字节数组
	 * @param time 生成时间
	 * @param selfly 是否自有组件集合
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public int write(byte[] b, long time) throws IOException {
		return write(b, 0, b.length, time);
	}

	/**
	 * 字节内容输出到压缩包
	 * @param b 字节数组
	 * @param selfly 是否自有组件集合
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public int write(byte[] b) throws IOException {
		return write(b, 0, b.length, System.currentTimeMillis());
	}

	/**
	 * 增值
	 * @param file
	 * @param ignoreSelfly
	 * @return 返回新增条目数
	 * @throws IOException
	 */
	public int writeGroup(File file, boolean ignoreSelfly) throws IOException {
		int count = 0;
		TaskComponentGroupReader reader = new TaskComponentGroupReader(file);
		List<CloudPackageItem> subs = reader.readTaskComponents();
		for (CloudPackageItem e : subs) {
			String name = e.getName();
			// 如果是“GROUP-INF/group.xml”，忽略
			if (name.equalsIgnoreCase(TF.GROUP_INF)) {
				continue;
			}
//			// 判断是自有
//			boolean selfly = name.equalsIgnoreCase(TF.SELFLY_FILE);
//			if (selfly && ignoreSelfly) {
//				continue;
//			}

			// 保存
			byte[] b = e.getContent();
			int len = write(b, e.getTime());
			if (len > 0) {
				count++;
			}
		}
		// 返回增值
		return count;
	}

	/**
	 * 生成包 "GROUP-INF/group.xml"标签文件内容
	 * @param part
	 * @return 输出UTF8格式的字节数组
	 */
	public byte[] buildGROUPINFTag(TaskPart part) {
		// 内部标签
		StringBuilder sub = new StringBuilder();
		// 用户签名
		if (part.getIssuer() != null) {
			String s = String.format("<sign>%s</sign>", part.getIssuer().toString());
			sub.append(s);
		}
		// 阶段命名
		String family = PhaseTag.translate(part.getFamily());
		String s = String.format("<phase>%s</phase>", family);
		sub.append(s);
		// 单元统计值
		s = String.format("<count>%d</count>", packages.size());
		sub.append(s);

		// 输出实例
		StringBuilder all = new StringBuilder();
		all.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		s = String.format("<root>%s</root>", sub.toString());
		all.append(s);

		// 以UTF-8格式输出！
		return new UTF8().encode(all.toString());
	}

	/**
	 * 输出带标签的字节内容
	 * @param part 工作部件
	 * @return 字节数组
	 * @throws IOException
	 */
	public byte[] flush(TaskPart part) throws IOException {
		// 统计内容空间长度，取整数
		int len = 128;
		for (CloudPackageItem sub : packages.values()) {
			len += sub.getContentLength();
		}
		len = (len - (len % 128) + 128);

		ByteArrayOutputStream buff = new ByteArrayOutputStream(len);
		ZipOutputStream zos = new ZipOutputStream(buff);
		// 内容保存到压缩缓存
		for (CloudPackageItem sub : packages.values()) {
			ZipEntry entry = new ZipEntry(sub.getName());
			entry.setTime(sub.getTime());
			byte[] b = sub.getContent();
			zos.putNextEntry(entry);
			zos.write(b);
			zos.closeEntry();
			zos.flush();
		}

		// 固定名称
		byte[] b = buildGROUPINFTag(part);
		String name = TF.GROUP_INF; // TaskBoot.TAG;
		ZipEntry entry = new ZipEntry(name);
		entry.setTime(System.currentTimeMillis());
		zos.putNextEntry(entry);
		zos.write(b, 0, b.length);
		zos.closeEntry();
		// 输出内容和关闭底层输出流
		zos.flush();
		zos.finish();
		zos.close();

		// 输出字节数组
		return buff.toByteArray();
	}

	/**
	 * 输出到磁盘文件
	 * @param part 工作部件
	 * @param file 磁盘文件名
	 * @throws IOException
	 */
	public void flush(TaskPart part, File file) throws IOException {
		byte[] b = flush(part);

		FileOutputStream out = new FileOutputStream(file);
		out.write(b, 0, b.length);
		out.close();
	}

	/**
	 * 关闭!
	 */
	public void close() throws IOException {
		packages.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		try {
			close();
		} catch (IOException e) {

		}
	}

}