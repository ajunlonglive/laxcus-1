/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.awt.*;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检测服务器系统信息调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class RayCheckSystemInfoInvoker extends RayInvoker {

	/**
	 * 构造检测服务器系统信息调用器，指定命令
	 * @param cmd 检测服务器系统信息
	 */
	public RayCheckSystemInfoInvoker(CheckSystemInfo cmd) {
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

		// 判断和清除本地内存
		if (cmd.isLocal()) {
			check();
			return useful();
		}
		// 投递到HUB站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		CheckSystemInfoProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(CheckSystemInfoProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
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
	/**
	 * 打印CPU信息
	 * @param item
	 */
	private void print(CPUInfoItem item) {
		if (item == null) {
			return;
		}

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/CPU");

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

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/MEMORY");

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

		// 剩余内存
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

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/DISK");
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

		Color color = findXMLForeground("CHECK-SYSTEM-INFO/JAVA");
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
	 * 显示一个节点
	 * @param item
	 */
	private void print(CheckSystemInfoItem item) {
		boolean success = item.isSuccessful();
		// 不成功忽略
		String site = getXMLContent("CHECK-SYSTEM-INFO/SITE");
		Color color = findXMLForeground("CHECK-SYSTEM-INFO/SITE");

		// 节点地址
		ShowItem showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, site, (success ? color : Color.RED)));
		showItem.add(new ShowStringCell(1, item.getSite().toString()));
		addShowItem(showItem);

		// 成功，显示CPU、内存、磁盘信息
		if (success) {
			print(item.getVersion());
			print(item.getCPUInfo());
			print(item.getMemInfo());
			print(item.getDiskInfo());
			print(item.getJREInfo());
		}
	}

	private void printCount(int count) {
		String key = getXMLContent("CHECK-SYSTEM-INFO/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(CheckSystemInfoProduct product) {
		// 显示运行时间
		printRuntime();

		// 取出节点
		ArrayList<Node> array = new ArrayList<Node>();
		CheckSystemInfo cmd = getCommand();
		if (cmd.isLocal() || cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}

		// 设置标题
		createShowTitle(new String[] { "CHECK-SYSTEM-INFO/T1", "CHECK-SYSTEM-INFO/T2" });

		// 非本地，显示全部节点数目
		printCount(product.size());

		// 显示单元
		for (Node node : array) {
			CheckSystemInfoItem item = product.find(node);
			// 没有找到，忽略！
			if (item == null) {
				continue;
			}
			// 打印空行
			printGap(2);
			// 打印一行
			print(item);
		}

		// 输出全部记录
		flushTable();
	}

	/**
	 * 显示本地
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
	 * 显示空
	 */
	private void checkWindows() {
		// 版本
		print(getLauncher().getVersion());
		// 内存
		MemoryWindowsReader memory = new MemoryWindowsReader();
		print(memory.read());
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
		} else if (isWindows()) {
			checkWindows();
		}

		// 输出全部记录
		flushTable();
	}

}