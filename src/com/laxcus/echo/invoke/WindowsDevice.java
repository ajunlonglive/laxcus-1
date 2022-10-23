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

import com.laxcus.command.missing.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.disk.*;

/**
 * WINDOWS设备定时检查器，只限WINDOWS环境。<br>
 * 
 * 60秒（1分钟）扫描一次。
 * 
 * @author scott.liang
 * @version 1.0 8/9/2019
 * @since laxcus 1.0
 */
public class WindowsDevice extends PlatformDevice {

	/** 句柄 **/
	private static WindowsDevice selfHandle = new WindowsDevice();

	/**
	 * 构造默认和私有的WINDOWS设备定时检查器
	 */
	private WindowsDevice() {
		super();
	}

	/**
	 * 返回WINDOWS设备定时检查器实例句柄
	 * @return WINDOWS设备定时检查器
	 */
	public static WindowsDevice getInstance() {
		return WindowsDevice.selfHandle;
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
		
		// 检查虚拟机内存、计算机内存、计算机磁盘...
		checkVMMemory();
		checkMemory();
		checkDisk();
		// 检查其它参数
		checkOther();
	}

	/**
	 * 定时检查内存参数
	 */
	public void checkMemory() {
		long[] b = WindowsMemory.getMemory();
		if (b == null || b.length != 3) {
			memoryMissing = false;
			sysMaxMemory = sysUsedMemory =0;
			return;
		}

		// 三个参数，依次是：占用率、全部内存、剩余内存（可用内存）。
		long rate = b[0];
		long total = b[1];
		long free = b[2];

		boolean missing = false;
		if (memoryLeast > 0) {
			missing = (free <= memoryLeast); // 按照内存容量计算，可用内存小于规定值
		} else if (memoryLeastRate > 0.0f) {
			double left = ((double) free / (double) total) * 100.0f;
			missing = (left <= memoryLeastRate); // 判断系统的内存剩余空间小于规定值
		}
		
		// 系统全部内存/已经使用的内存
		sysMaxMemory = total;
		sysUsedMemory = (total - free);

		// 记录判断结果
		memoryMissing = missing;

		// 内存不足，报警
		if (missing) {
			MemoryMissing cmd = new MemoryMissing();
			launcher.getCommandPool().admit(cmd);
		}
		
		Logger.info(this, "checkMemory", "Memory Missing: %s, System Rate: %d, Max Memory: %s, Used Memory: %s",
				(memoryMissing ? "yes" : "no"), rate, ConfigParser.splitCapacity(sysMaxMemory, 2),
				ConfigParser.splitCapacity(sysUsedMemory, 2));
	}

	/**
	 * 输出WINDOWS平台目录标记
	 * @return PathTab列表
	 */
	public List<PathTab> getPathTabs() {
		ArrayList<PathTab> array = new ArrayList<PathTab>();
		for (LeastPath e : paths) {
			String path = e.getPath();
			File file = new File(path);

			// 保存参数
			PathTab tab = new PathTab(path);
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
		for (LeastPath a : array) {
			boolean success = updateLeastPath(a);
			if(success) count++;
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
			// WINDOWS路径大小写不敏感
			if (Laxkit.compareTo(that.getPath(), path.getPath(), false) == 0) {
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
		// 盘符集合
		TreeSet<Character> keys = new TreeSet<Character>();

		// 逐一判断
		for (LeastPath token : paths) {
			File dir = token.getDirectory();
			// 必须是目录且存在！
			boolean success = (dir.exists() && dir.isDirectory());
			if (!success) {
				continue;
			}

			// 确定盘符
			String path = token.getPath().toLowerCase();
			char sid = path.charAt(0);

			long total = dir.getTotalSpace(); // 全部空间
			long free = dir.getUsableSpace(); // 可用空间

			// 磁盘空间不足为假
			boolean missing = false;
			if (token.getCapacity() > 0) {
				missing = (free <= token.getCapacity());
			} else if (token.getRate() > 0.0f) {
				double left = ((double) free / (double) total) * 100.0f;
				missing = (left <= token.getRate());
			}

			// 如果盘符不存在，统计总量
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
		sysUsedDisk = (totalCapacity - freeCapacity); // 已经使用的空间

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