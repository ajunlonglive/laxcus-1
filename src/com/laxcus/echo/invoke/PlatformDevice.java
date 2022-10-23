/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.command.missing.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.disk.*;

/**
 * 平台资源检查器，子类包括WINDOWS/LINUX环境。<br><br>
 * 
 * 资源检查器定时收集操作系统的内存和磁盘剩余空间情况，达到规定的最低下限时报警，提交到WATCH/FRONT节点，提醒管理员/终端用户注意，并做出相应的补救措施。<br>
 * 默认是不限制内存/磁盘剩余空间，限制参数由管理员/终端用户设置。<br>
 * 通常内存的限制是保持20%的剩余空间，磁盘是10%的剩余空间。具体由管理员根据计算机和业务情况下确定。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 6/13/2019
 * @since laxcus 1.0
 */
public abstract class PlatformDevice extends TimerTask {

	/** 站点句柄 **/
	protected SiteLauncher launcher;

	/** 最小内存数 **/
	protected volatile long memoryLeast = -1;

	/** 最小剩余内存占比  **/
	protected volatile double memoryLeastRate = 0.0f;

	/** 内存不足标记，在运行过程中持续更新 **/
	protected volatile boolean memoryMissing;

	/** 操作系统最大可用内存 **/
	protected volatile long sysMaxMemory;

	/** 操作系统已经占用的内存 **/
	protected volatile long sysUsedMemory;

	/** 磁盘路径集合  **/
	protected TreeSet<LeastPath> paths = new TreeSet<LeastPath>();

	/** 磁盘空间不足标记 **/
	protected volatile boolean diskMissing;

	/** 磁盘系统的最大磁盘空间 **/
	protected volatile long sysMaxDisk;

	/** 操作系统已经占用的磁盘空间 **/
	protected volatile long sysUsedDisk;

	/**
	 * 构造默认平台资源检查器
	 */
	protected PlatformDevice() {
		super();
		// 默认是无限制
		setMemoryUnlimit();
		// 内存不足是假，默认当前有足够内存！
		memoryMissing = false;
		// 系统内存
		sysMaxMemory = sysUsedMemory = 0;

		// 磁盘无限制
		setDiskUnlimit();
		diskMissing = false;
		sysMaxDisk = sysUsedDisk = 0;
	}

	/**
	 * 设置站点句柄
	 * @param e SiteLauncher实例
	 */
	public void setSiteLauncher(SiteLauncher e) {
		launcher = e;
	}

	/**
	 * 返回站点句柄
	 * @return SiteLauncher实例
	 */
	public SiteLauncher getSiteLauncher() {
		return launcher;
	}
	
	/**
	 * 判断登录状态
	 * @return 返回真或者假
	 */
	protected boolean isLogined() {
		// 如果是TOP节点，存在就是登录状态
		byte family = launcher.getFamily();
		if (SiteTag.isTop(family)) {
			return true;
		}
		return launcher.isLogined();
	}

	/**
	 * 返回操作系统最大可用内存
	 * @return 操作系统最大可用内存
	 */
	public long getSysMaxMemory(){
		return sysMaxMemory;
	}

	/**
	 * 返回操作系统已经占用的内存
	 * @return 操作系统已经占用的内存
	 */
	public long getSysUsedMemory() {
		return sysUsedMemory;
	}

	/**
	 * 解析最小内存限值。<br>
	 * 分为两种：<br>
	 * 1. xxx% ，最小百分比 <br>
	 * 2. xxxM|G，最小容量<br><br>
	 * 
	 * @param input 输入参数
	 * @return 解析成功返回真，否则假
	 */
	public boolean splitMemory(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		// 没有限制
		if (input.matches("^\\s*(?i)(UNLIMIT)\\s*$")) {
			setMemoryUnlimit();
			Logger.info(this, "splitMemory", "memory is infinitely");
			return true;
		}

		// 判断是以“G/M/K”为后缀的内存容量
		long value = ConfigParser.splitLongCapacity(input, -1);
		if (value > 0) {
			setMemoryLeast(value);
			Logger.info(this, "splitMemory", "least memory %s",
					ConfigParser.splitCapacity(memoryLeast));
			return true;
		}

		// 判断是以“%”为后缀的浮点数
		double rate = ConfigParser.splitRate(input, 0.0f);
		if (rate > 0.0f) {
			setMemoryLeastRate(rate);
			Logger.info(this, "splitMemory", "least memory rate %.2f",
					memoryLeastRate);
			return true;
		}

		// 解析失败！
		return false;
	}

	/**
	 * 解析“路径加磁盘最小容量”
	 * @param cmd 命令
	 * @param input 语句
	 */
	private LeastPath splitPathCapacity( String input) {
		final String PATH_CAPACITY = "^\\s*([\\w\\W]+)\\s+(?i)(\\d+\\s*[E|EB|P|PB|T|TB|G|GB|M|MB|K|KB|B|BYTE|BYTES])\\s*$";
		// 解析判断
		Pattern pattern = Pattern.compile(PATH_CAPACITY);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，忽略！
		if (!matcher.matches()) {
			return null;
		}

		// 路径和磁盘最小容量
		String path = matcher.group(1);
		String suffix = matcher.group(2);

		// 磁盘空间
		long value = ConfigParser.splitLongCapacity(suffix, -1);
		if (value == -1) {
			return null;
		}
		path = ConfigParser.splitPath(path);
		// 设备路径
		return new LeastPath(path, value);
	}

	/**
	 * 解析“路径加磁盘最小占比”
	 * @param cmd 命令
	 * @param input 语句
	 */
	private LeastPath splitPathRate(String input) {
		/** 包含：路径 磁盘最小占比 **/
		final String PATH_RATE = "^\\s*([\\w\\W]+)\\s+([0-9]+[\\.0-9]*\\s*\\%)\\s*$";

		// 解析判断
		Pattern pattern = Pattern.compile(PATH_RATE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			return null;
		}

		// 路径和磁盘最小占比
		String path = matcher.group(1);
		String suffix = matcher.group(2);
		double rate = ConfigParser.splitRate(suffix, 0.0f);
		if (rate <= 0.0f) {
			return null;
		}
		// 设备路径
		path = ConfigParser.splitPath(path);
		return new LeastPath(path, rate);
	}

	/**
	 * 解析磁盘参数 <br>
	 * 三种格式：<br><br>
	 * 
	 * 1. xxx%，最小磁盘百分比 <br>
	 * 2. xxxM，最小磁盘容量 <br>
	 * 3. 磁盘路径 xxx% , 磁盘路径 xxxM <br><br>
	 * 
	 * @param input 输入语句
	 * @return 成功返回真，否则假
	 */
	public boolean splitDisk(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		// 没有限制
		if (input.matches("^\\s*(?i)(UNLIMIT)\\s*$")) {
			setDiskUnlimit();
			Logger.info(this, "splitDisk", "disk is infinitely");
			return true;
		}

		// 判断是以“G/M/K”为后缀的内存容量
		long value = ConfigParser.splitLongCapacity(input, -1);
		if (value > 0) {
			setDiskLeast(value);
			Logger.info(this, "splitDisk", "least disk %s", ConfigParser.splitCapacity(value));
			return true;
		}

		// 判断是以“%”为后缀的浮点数
		double rate = ConfigParser.splitRate(input, 0.0f);
		if (rate > 0.0f) {
			setDiskLeastRate(rate);
			Logger.info(this, "splitDisk", "least disk rate %.2f", rate);
			return true;
		}

		// 解析参数
		String[] items = input.split("\\s*\\,\\s*");
		int count = 0;
		for (String item : items) {
			// 忽略
			if (item.trim().isEmpty()) {
				continue;
			}
			// 解析参数
			LeastPath path = splitPathCapacity(item);
			if (path == null) {
				path = splitPathRate(item);
			}
			// 判断解析成功
			boolean success = (path != null);
			if (success) {
				success = updateLeastPath(path);
				if (success) count++;
			}
			Logger.info(this, "splitDisk", item + (success ? " Successful" : " Failed"));
		}
		// 成功
		return (items.length > 0 && count == items.length);
	}

	/**
	 * 设置内存为无限制
	 */
	public void setMemoryUnlimit() {
		memoryLeast = -1;
		memoryLeastRate = 0.0f;
	}

	/**
	 * 设置最小内存，小于1是不限制内存。
	 * @param len 空间尺寸
	 */
	public void setMemoryLeast(long len) {
		memoryLeast = len;
	}

	/**
	 * 返回最小内存
	 * @return 最小内存数
	 */
	public long getMemoryLeast() {
		return memoryLeast;
	}

	/**
	 * 最低内存占比
	 * @param value 双浮点数
	 */
	public void setMemoryLeastRate(double value) {
		memoryLeastRate = value;
	}

	/**
	 * 最小比例
	 * @return 最小比例
	 */
	public double getMemoryLeastRate() {
		return memoryLeastRate;
	}

	/**
	 * 判断是内存不足
	 * @return 返回真或者假
	 */
	public boolean isMemoryMissing() {
		return memoryMissing;
	}

	/******** 以下是磁盘参数 ********/

	/**
	 * 设置为无限制
	 */
	public void setDiskUnlimit() {
		for(LeastPath e : paths) {
			e.setUnlimit();
		}
	}

	/**
	 * 设置最小磁盘空间，小于1是不限制磁盘空间。
	 * @param len 空间尺寸
	 */
	public void setDiskLeast(long len) {
		for(LeastPath e : paths) {
			e.setCapacity(len);
		}
	}

	/**
	 * 最低磁盘空间占比
	 * @param b 双浮点数
	 */
	public void setDiskLeastRate(double b) {
		for(LeastPath e : paths) {
			e.setRate(b);
		}
	}

	/**
	 * 判断是磁盘空间不足
	 * @return 返回真或者假
	 */
	public boolean isDiskMissing() {
		return diskMissing;
	}

	/**
	 * 返回操作系统最大可用磁盘空间
	 * @return 操作系统最大可用磁盘空间
	 */
	public long getSysMaxDisk() {
		return sysMaxDisk;
	}

	/**
	 * 返回操作系统已经占用的磁盘空间
	 * @return 操作系统已经占用的磁盘空间
	 */
	public long getSysUsedDisk() {
		return sysUsedDisk;
	}

	/**
	 * 保存LINUX定时检测目录，在节点启动时保存。<br>
	 * 这些目录全部由系统规定，不允许修改/增加。<br><br>
	 * 
	 * @param path 磁盘目录
	 * @return 保存成功返回真，否则假
	 */
	public boolean addDirectory(String path) {
		// 如果空值
		if (path == null || path.isEmpty()) {
			return false;
		}
		// 目录转义
		path = ConfigParser.splitPath(path);
		try {
			// 保存路径
			path = new File(path).getCanonicalPath();
			paths.add(new LeastPath(path));
		} catch (IOException e) {
			Logger.error(e);
		}
		return false;
	}

	/**
	 * 保存LINUX定时检测目录，在节点启动时保存。<br>
	 * 这些目录全部由系统规定，不允许修改/增加。<br><br>
	 * 
	 * @param dir 磁盘目录
	 * @return 成功返回真，否则假
	 */
	public boolean addDirectory(File dir) {
		Laxkit.nullabled(dir);
		// 保存目录，无论有效无效
		return addDirectory(dir.getAbsolutePath());
	}

	/**
	 * 打印CPU/命令资源
	 */
	protected void checkOther() {
		if (launcher == null) {
			return;
		}

		CommandPool commandPool = launcher.getCommandPool();
		InvokerPool invokerPool = launcher.getInvokerPool();

		// CPU使用占比
		double cpuRate = invokerPool.getCPURate();

		// 命令数目
		int commands = commandPool.size();
		int invokers = invokerPool.size();
		int threads = Thread.activeCount();

		Logger.info(this, "checkOther", "CPU rate: %.2f%s, Commands: %d, Invokers: %d, Threads: %d", 
				cpuRate, "%", commands, invokers, threads);
	}

	/**
	 * 检查JVM内存
	 */
	protected void checkVMMemory() {
		// 参数语义：maxMemory是JVM可分配最大内存数，totalMemory是JVM已经分配的内存，freeMemory是totalMemory中未使用部分
		Runtime rt = Runtime.getRuntime();
		long usedMemory = (rt.totalMemory() - rt.freeMemory()); // 实际使用的内存
		boolean missing = EchoTransfer.isVMMemoryMissing(usedMemory, rt.maxMemory()); 
		// 内存不足，报警
		if (missing) {
			VMMemoryMissing cmd = new VMMemoryMissing();
			launcher.getCommandPool().admit(cmd);
		}
	}

	/**
	 * 检查节点的许可证使用超期
	 */
	protected void checkLicenceTimeout() {
		if (launcher == null) {
			return;
		}
		// 判断许可证没有时间限制
		if (launcher.isLicenceInfinite()) {
			return;
		}

		boolean success = launcher.isLicenceTimeout();
		if (success) {
			LicenceTimeout cmd = new LicenceTimeout(launcher.getListener());
			launcher.getCommandPool().admit(cmd);
			return;
		}

		// 判断在30天
		for (int day = 1; day <= 30; day++) {
			success = launcher.isLicenceTimeout(day);
			if (success) {
				LicenceTimeout cmd = new LicenceTimeout(launcher.getListener(), day);
				launcher.getCommandPool().admit(cmd);
				break;
			}
		}
	}

	/**
	 * 更新路径
	 * @param path 路径
	 * @return 成功返回真，否则假
	 */
	public abstract boolean updateLeastPath(LeastPath path);

}