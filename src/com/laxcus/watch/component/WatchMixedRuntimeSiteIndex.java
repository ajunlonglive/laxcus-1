/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

/**
 * 站点标题栏索引
 * 
 * @author scott.liang
 * @version 1.0 4/20/2018
 * @since laxcus 1.0
 */
public enum WatchMixedRuntimeSiteIndex {
	
	/** 对应"resources.xml"中的标签 **/
	ADDRESS(0), CPU_RATE(1),
	// 系统内存
	SYS_USABLE_MEMORY(2), SYS_MAX_MEMORY(3), SYS_MEMORY_RATE(4),
	// 虚拟机内存
	VM_USABLE_MEMORY(5), VM_MAX_MEMORY(6), VM_MEMORY_RATE(7), 
	// 系统磁盘
	USABLE_DISK(8), MAX_DISK(9), DISK_RATE(10), 
	// 其它
	MEMBERS(11),THREADS(12), COMMANDS(13), INVOKERS(14), OS(15), CPU(16);
	
	/**
	 * 输出全部排序
	 * @return
	 */
	public static WatchMixedRuntimeSiteIndex[] total() {
		return new WatchMixedRuntimeSiteIndex[] { WatchMixedRuntimeSiteIndex.ADDRESS, WatchMixedRuntimeSiteIndex.CPU_RATE, 
				
				WatchMixedRuntimeSiteIndex.SYS_USABLE_MEMORY, WatchMixedRuntimeSiteIndex.SYS_MAX_MEMORY, WatchMixedRuntimeSiteIndex.SYS_MEMORY_RATE,
				WatchMixedRuntimeSiteIndex.VM_USABLE_MEMORY, WatchMixedRuntimeSiteIndex.VM_MAX_MEMORY, WatchMixedRuntimeSiteIndex.VM_MEMORY_RATE,
				WatchMixedRuntimeSiteIndex.USABLE_DISK, WatchMixedRuntimeSiteIndex.MAX_DISK, WatchMixedRuntimeSiteIndex.DISK_RATE, 
				
				WatchMixedRuntimeSiteIndex.MEMBERS,WatchMixedRuntimeSiteIndex.THREADS, WatchMixedRuntimeSiteIndex.COMMANDS, 
				WatchMixedRuntimeSiteIndex.INVOKERS, WatchMixedRuntimeSiteIndex.OS, WatchMixedRuntimeSiteIndex.CPU };
	}
	
	/**
	 * 列索引下标
	 */
	private int index = 0;
	
	/**
	 * 定义站点标题栏索引
	 * @param i 索引下标
	 */
	private WatchMixedRuntimeSiteIndex(int i) {
		index = i;
	}
	
	/**
	 * 返回索引
	 * @return
	 */
	public int getIndex(){
		return index;
	}
	
	/**
	 * 判断匹配
	 * @param who 标识符
	 * @return 返回真或者假
	 */
	public boolean isIndex(int who) {
		return index == who;
	}

	/**
	 * 返回所在下标位置的配置描述。
	 * 这些字符串在"conf/watch/resources.xml"中定义。
	 * @return 字符串描述
	 */
	public String getConfigDescribe() {
		switch (index) {
		// 节点
		case 0:
			return "Address";
		// 内存
		case 1:
			return "CPURate";
		// 系统内存
		case 2:
			return "SysUsedMemory";
		case 3:
			return "SysMaxMemory";
		case 4:
			return "SysMemoryRate";
		// 虚拟机内存
		case 5:
			return "VmUsedMemory";
		case 6:
			return "VmMaxMemory";
		case 7:
			return "VmMemoryRate";
		// 磁盘
		case 8:
			return "UsedDisk";
		case 9:
			return "MaxDisk";
		case 10:
			return "DiskRate";
		// 线程/命令
		case 11:
			return "Members";
		case 12:
			return "Threads";
		case 13:
			return "Commands";
		case 14:
			return "Invokers";
		// 操作系统/CPU类型
		case 15:
			return "OS";
		case 16:
			return "CPU";
		default:
			return "NONE";
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return getConfigDescribe();
	}

}