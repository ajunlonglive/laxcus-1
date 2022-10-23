/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.awt.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 检测服务器系统信息调用器。
 * 只在本地执行
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopCheckSystemInfoInvoker extends DesktopInvoker {

	/**
	 * 构造应答包传输模式调用器，指定命令
	 * @param cmd 应答包传输模式
	 */
	public DesktopCheckSystemInfoInvoker(CheckSystemInfo cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckSystemInfo getCommand() {
		return (CheckSystemInfo) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckSystemInfo cmd = getCommand();

		if (cmd.isLocal()) {
			check();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 显示一行单元
	 * @param key
	 * @param value
	 */
	private void printText(Color color, String key, String value) {
		if (value == null) {
			value = "";
		}
		ShowItem showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, key, color));
		showItem.add(new ShowStringCell(1, value));
		addShowItem(showItem);
	}

	/**
	 * 显示一行单元
	 * @param key
	 * @param value
	 */
	private void printInt(Color color, String key, int value) {
		printText(color, key, Integer.toString(value));
	}

	/**
	 * 显示容量
	 * @param color
	 * @param key
	 * @param value
	 */
	private void printCapacity(Color color, String key, long value) {
		if (value <= 0) {
			printText(color, key, "");
		} else {
			String capacity = ConfigParser.splitCapacity(value);
			printText(color, key, capacity);
		}
	}

	/**
	 * 显示占比
	 * @param color
	 * @param key
	 * @param availables
	 * @param max
	 */
	private void printCapacity(Color color, String key, long availables, long max) {
		String capacity = ConfigParser.splitRate(availables, max); 
		printText(color, key, capacity);
	}

	/**
	 * 显示版本
	 * @param version
	 */
	private void print(Version version) {
		if (version == null) {
			return;
		}

		// 颜色
		Color color = findXMLForeground("CHECK-SYSTEM-INFO/VERSION");
		// 生产商
		String name = getXMLContent("CHECK-SYSTEM-INFO/VERSION/VERSION");
		printText(color, name, version.toString());
	}

	/**
	 * 打印CPU信息
	 * @param item
	 */
	private void print(CPUInfoItem item) {
		if (item == null) {
			return;
		}

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/CPU", Color.BLACK);

		// 生产商
		String name = getXMLContent("CHECK-SYSTEM-INFO/CPU/VENDOR");
		printText(color, name, item.getVendor());

		// 产品序列
		name = getXMLContent("CHECK-SYSTEM-INFO/CPU/MODEL-NAME");
		printText(color, name, item.getModelName());

		// 物理核
		name = getXMLContent("CHECK-SYSTEM-INFO/CPU/PHYSICAL");
		int id = item.getPhysicalId();
		if (id >= 0) {
			printInt(color, name, id + 1);
		}

		// 处理器数目和逻辑核心，选择一个
		int processor = item.getProcessor();
		if (processor >= 0) {
			name = getXMLContent("CHECK-SYSTEM-INFO/CPU/PROCESSOR");
			printInt(color, name, processor + 1);
		} else {
			id = item.getCores();
			if (id >= 0) {
				name = getXMLContent("CHECK-SYSTEM-INFO/CPU/CORES");
				printInt(color, name, id);
			}
		}
		
		// 主频
		name = getXMLContent("CHECK-SYSTEM-INFO/CPU/MHZ");
		printText(color, name, item.getMHz());

		// 二级缓存
		name = getXMLContent("CHECK-SYSTEM-INFO/CPU/CACHE-SIZE");
		long size = item.getCacheSize();
		if (size > 0) {
			printCapacity(color, name, size);
		} else {
			printText(color, name, "");
		}
	}

	/**
	 * 打印内存信息
	 * @param item
	 */
	private void print(MemoryInfoItem item) {
		if (item == null) {
			return;
		}

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/MEMORY", Color.BLACK);

		// 全部内存
		String name = getXMLContent("CHECK-SYSTEM-INFO/MEMORY/TOTAL");
		printCapacity(color, name, item.getTotal());

		// 已用内存
		name = getXMLContent("CHECK-SYSTEM-INFO/MEMORY/USED");
		long used = item.getTotal() - item.getAvailable();
		printCapacity(color, name, used);

		// 使用内存空间占比
		name = getXMLContent("CHECK-SYSTEM-INFO/MEMORY/RATE");
		printCapacity(color, name, used, item.getTotal());

		// 可用内存（剩余内存）
		name = getXMLContent("CHECK-SYSTEM-INFO/MEMORY/FREE");
		printCapacity(color, name, item.getAvailable());

		// 缓存
		if (item.getCached() > 0) {
			name = getXMLContent("CHECK-SYSTEM-INFO/MEMORY/CACHED");
			printCapacity(color, name, item.getCached());
		}

		// 缓冲
		if (item.getBuffers() > 0) {
			name = getXMLContent("CHECK-SYSTEM-INFO/MEMORY/BUFFERS");
			printCapacity(color, name, item.getBuffers());
		}
	}

	/**
	 * 打印磁盘信息
	 * @param item
	 */
	private void print(DiskInfoItem item) {
		if (item == null) {
			return;
		}

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/DISK", Color.BLACK);
		// 全部容量
		String key = getXMLContent("CHECK-SYSTEM-INFO/DISK/TOTAL");
		printCapacity(color, key, item.getTotalCapacity());

		// 已经使用的磁盘容量
		key = getXMLContent("CHECK-SYSTEM-INFO/DISK/USED");
		long used = item.getTotalCapacity() - item.getFreeCapacity();
		printCapacity(color, key, used);

		// 磁盘空间占比
		key = getXMLContent("CHECK-SYSTEM-INFO/DISK/RATE");
		printCapacity(color, key, used, item.getTotalCapacity());

		// 剩余空间
		key = getXMLContent("CHECK-SYSTEM-INFO/DISK/FREE");
		printCapacity(color, key, item.getFreeCapacity());
	}

	/**
	 * 显示JRE信息
	 * @param item
	 */
	private void print(JREInfoItem item) {
		if (item == null) {
			return;
		}

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/JAVA", Color.BLACK);
		// 提供商
		String key = getXMLContent("CHECK-SYSTEM-INFO/JAVA/VENDOR");
		printText(color, key, item.getVendor());

		// 版本
		key = getXMLContent("CHECK-SYSTEM-INFO/JAVA/VERSION");
		printText(color, key, item.getVersion());

		// 虚拟机名称
		key = getXMLContent("CHECK-SYSTEM-INFO/JAVA/VMNAME");
		printText(color, key, item.getVmname());

		// 适配架构
		key = getXMLContent("CHECK-SYSTEM-INFO/JAVA/ARCH");
		printText(color, key, item.getArch());

		// 寄居系统
		key = getXMLContent("CHECK-SYSTEM-INFO/JAVA/OSNAME");
		printText(color, key, item.getOsname());
	}

	/**
	 * 显示LINUX信息
	 */
	private void checkLinux() {
		// 版本
		print(getLauncher().getVersion());

		// CPU信息
		CPULinuxReader cpu = new CPULinuxReader();
		print(cpu.read());
		// 内存信息
		MemoryLinuxReader memory = new MemoryLinuxReader();
		print(memory.read());
		// 磁盘信息
		DiskLinuxReader disk = new DiskLinuxReader();
		print(disk.read());
		// JRE信息
		JREReader jre = new JREReader();
		print(jre.read());
	}

	/**
	 * 显示WINDOWS信息
	 */
	private void checkWindows() {
		// 版本
		print(getLauncher().getVersion());
		// 内存
		MemoryWindowsReader mem = new MemoryWindowsReader();
		print(mem.read());
		DiskWindowsReader disk = new DiskWindowsReader();
		print(disk.read());
		JREReader jre = new JREReader();
		print(jre.read());
	}

	/**
	 * 调用器数目
	 */
	private void check() {
		// 设置标题
		createShowTitle(new String[] { "CHECK-SYSTEM-INFO/T1", "CHECK-SYSTEM-INFO/T2" });

		// 如果是LINUX
		if (isLinux()) {
			checkLinux();
		} else if(isWindows()){
			checkWindows();
		}

		// 输出全部记录
		flushTable();
	}

}