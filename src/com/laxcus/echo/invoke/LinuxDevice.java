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
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.disk.*;

/**
 * LINUX设备定时检查器，只限LINUX环境。<br><br>
 * 
 * Linux内存参数说明：<br>
 * 1. MemTotal，内存总量，是全部物理内存 - 系统保留的内存 <br>
 * 2. MemFree，空闲内存，表示系统没有使用的内存。如果计算被使用的内存，即是：MemUsed = MemTotal - MemFree <br>
 * 3. MemAvailable，应用程序可用内存。详细说明：系统中有些内存虽然已经使用但是可以回收，比如Buffers/Cached/Slab都有一部分可以回收。所以MemFree不能代表全部可用的内存，这部分可回收的内存加上MemFree才是系统真正可用的内存，即：MemAvailable = MemFree + Buffers + Cached，它由内核使用特定算法计算出来，是一个估计值。它与MemFree的区别是：MemFree是系统层面，MemAvailable属于应用程序层面。 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/13/2019
 * @since laxcus 1.0
 */
public class LinuxDevice extends PlatformDevice {

	private final static String TOTAL_REGEX = "^\\s*(?i)(?:MemTotal)\\s*(?:\\:)\\s+([0-9]+)\\s*(?i)(?:kB)\\s*$";
	private final static String FREE_REGEX = "^\\s*(?i)(?:MemFree)\\s*(?:\\:)\\s+([0-9]+)\\s*(?i)(?:kB)\\s*$";
	private final static String AVAILABLE_REGEX = "^\\s*(?i)(?:MemAvailable)\\s*(?:\\:)\\s+([0-9]+)\\s*(?i)(?:kB)\\s*$";
	
	/** 句柄 **/
	private static LinuxDevice selfHandle = new LinuxDevice();

	/**
	 * 构造默认和私有的LINUX设备定时检查器
	 */
	private LinuxDevice() {
		super();
	}
	
	/**
	 * 返回LINUX设备定时检查器实例句柄
	 * @return LINUX设备定时检查器
	 */
	public static LinuxDevice getInstance() {
		return LinuxDevice.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		// 非登录状态，忽略！
		if (!isLogined()) {
			return;
		}

		// 检查许可证超期
		checkLicenceTimeout();
		
		// 检查虚拟机内存
		checkVMMemory();
		// 检查内存
		try {
			checkMemory();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 检查磁盘
		checkDisk();
		// 检查其它参数
		checkOther();
	}

	/**
	 * 检查内存不足
	 * @return 返回真或者假
	 */
	public void checkMemory() throws IOException {
		String content = readMemory();
		if (content == null) {
			memoryMissing = false;
			sysMaxMemory = sysUsedMemory = 0;
			return;
		}
		
		BufferedReader reader = new BufferedReader(new 	StringReader(content)); 
		// 内存判断三个参数
		long total = -1;
		long free = -1;
		long available = -1;
		do {
			String line = reader.readLine();
			if(line == null) {
				break;
			}
			
			// 全部内存
			Pattern pattern = Pattern.compile(LinuxDevice.TOTAL_REGEX);
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				total = Long.parseLong(matcher.group(1)) * Laxkit.KB;
				continue;
			}

			// 自由内存
			pattern = Pattern.compile(LinuxDevice.FREE_REGEX);
			matcher = pattern.matcher(line);
			if (matcher.matches()) {
				free = Long.parseLong(matcher.group(1)) * Laxkit.KB;
				continue;
			}

			// 有效内存
			pattern = Pattern.compile(LinuxDevice.AVAILABLE_REGEX);
			matcher = pattern.matcher(line);
			if (matcher.matches()) {
				available = Long.parseLong(matcher.group(1)) * Laxkit.KB;
				continue;
			}
		} while(true);
		
		// 没有找到参数，忽略
		if (total == -1) {
			memoryMissing = false;
			sysMaxMemory = sysUsedMemory = 0;
			return;
		}
		
		// 系统全部内存
		sysMaxMemory = total;

		// 内存不足为假
		boolean missing = false;
		// "MemAvailable"是Linux新增参数，首选有效内存，次选自由内存。它们的意思都是“没有使用的内存”！
		// MemAvailable 大约等于 MemFree + Buffers + Cached
		if (available > 0) {
			sysUsedMemory = (total - available); // 已经使用的内存
			if (memoryLeast > 0) {
				missing = (available <= memoryLeast); // 按照内存容量计算，可用内存小于规定值
			} else if (memoryLeastRate > 0.0f) {
				double left = ((double) available / (double) total) * 100.0f;
				missing = (left <= memoryLeastRate); // 按照比例计算，小于最小比较
			}
		} else if (free > 0) {
			sysUsedMemory = (total - free); // 已经使用的内存
			if (memoryLeast > 0) {
				missing = (free <= memoryLeast);
			} else if (memoryLeastRate > 0.0f) {
				double left = ((double) free / (double) total) * 100.0f;
				missing = (left <= memoryLeastRate);
			}
		} 
		// 以上不成立，忽略！
		else {
			memoryMissing = false;
			sysMaxMemory = sysUsedMemory = 0;
			return;
		}
		
		// 记录判断结果
		memoryMissing = missing;

		// 内存不足，报警
		if (missing) {
			MemoryMissing cmd = new MemoryMissing();
			launcher.getCommandPool().admit(cmd);
		}
		
		Logger.info(this, "checkMemory", "Memory Missing: %s, Max Memory: %s, Used Memory: %s",
				(memoryMissing ? "yes" : "no"), ConfigParser.splitCapacity(sysMaxMemory, 2),
				ConfigParser.splitCapacity(sysUsedMemory, 2));
	}
	
	/**
	 * 从磁盘读取内存信息
	 * @return 返回读取的字符串
	 * @throws IOException
	 */
	private String readMemory() throws IOException {
		final String filename = "/proc/meminfo";
		File file = new File(filename);
		if (!file.exists()) {
			Logger.error(this, "readMemory", "not found %s", filename);
			return null;
		}

		// 读数据
		byte[] b = new byte[(int) file.length() + 256];
		FileInputStream in = new FileInputStream(file);
		int len = in.read(b, 0, b.length);
		in.close();

		// 输出字符串
		return new String(b, 0, len);
	}

	/**
	 * 输出LINUX平台目录标记
	 * @return PathTab列表
	 */
	public List<PathTab> getPathTabs() {
		ArrayList<PathTab> array = new ArrayList<PathTab>();
		for (LeastPath e : paths) {
			String path = e.getPath();
			// LINUX文件系统编号
			long sid = LinuxDisk.getFileSystemId(path);
			File file = new File(path);

			// 保存参数
			PathTab tab = new PathTab(path);
			tab.setSID(sid);
			tab.setTotal(file.getTotalSpace());
			tab.setFree(file.getUsableSpace());
			array.add(tab);
		}
		return array;
	}
	
	/**
	 * 更新匹配设备路径
	 * @param array 数组
	 * @return 全部匹配且更新，返回真，否则假
	 */
	public boolean updateLeastPaths(List<LeastPath> array) {
		int count = 0;
		// 逐一检查和更新
		for (LeastPath a : array) {
			boolean success = updateLeastPath(a);
			if (success) count++;
		}
		// 完全匹配返回真
		return count == array.size();
	}

	/**
	 * 更新路径
	 * @param path 路径
	 * @return 成功返回真，否则假
	 */
	public boolean updateLeastPath(LeastPath that){
		for (LeastPath path : paths) {
			// LINUX路径大小写敏感
			if (Laxkit.compareTo(that.getPath(), path.getPath(), true) == 0) {
				path.setCapacity(that.getCapacity());
				path.setRate(that.getRate());
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查磁盘
	 */
	public void checkDisk() {
		// 如果是空，忽略它！
		if (paths.isEmpty()) {
			diskMissing = false;
			sysMaxDisk = sysUsedDisk = 0;
			return;
		}

		// 提取磁盘空间
		long totalCapacity = 0L;
		long freeCapacity = 0L;
		// 统计磁盘不足的节目录
		ArrayList<DiskMissing> array = new ArrayList<DiskMissing>();
		
		// 文件系统ID集合
		TreeSet<Long> keys = new TreeSet<Long>();
		
		for(LeastPath token : paths) {
			File dir = token.getDirectory();
			// 必须保证是目录且存在！
			boolean success = (dir.exists() && dir.isDirectory());
			if (!success) {
				continue;
			}
			
			// 取文件系统ID，如果是-1，表示不能检测到。
			long sid = LinuxDisk.getFileSystemId(token.getPath());
			if (sid == -1) {
				continue;
			}
			
			long total = dir.getTotalSpace();	// 全部空间
			long free = dir.getUsableSpace(); // 可使用的空间
			
			// 磁盘空间不足为假
			boolean missing = false;
			if (token.getCapacity() > 0) {
				missing = (free <= token.getCapacity());
			} else if (token.getRate() > 0.0f) {
				double left = ((double) free / (double) total) * 100.0f;
				missing = (left <= token.getRate());
			}

			// 如果这个编号不存在，统计磁盘空间量
			if (!keys.contains(sid)) {
				keys.add(sid);
				totalCapacity += total;
				freeCapacity += free;
			}
			// 磁盘空间不足
			if (missing) {
				array.add(new DiskMissing(token.getPath()));
			}
		}

		// 设置参数
		sysMaxDisk = totalCapacity; // 总体空间
		sysUsedDisk = (totalCapacity - freeCapacity); // 已经使用的内存
		
		// 判断有磁盘空间不足的目录
		diskMissing = (array.size() > 0);
		if (diskMissing) {
			for (DiskMissing cmd : array) {
				launcher.getCommandPool().admit(cmd);
			}
		}

		Logger.info(this, "checkDisk", "Disk Missing: %s, Max Disk: %s Used Disk: %s",
				(diskMissing ? "yes" : "no"), ConfigParser.splitCapacity(sysMaxDisk, 2),
				ConfigParser.splitCapacity(sysUsedDisk, 2));
	}

}