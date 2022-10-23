/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 站点运行状态。<br><br>
 * 
 * 所在站点，向WATCH站点返回自己的运行状态。
 * 
 * @author scott.liang
 * @version 1.3 1/20/2020
 * @since laxcus 1.0
 */
public final class SiteRuntime extends Command {

	private static final long serialVersionUID = 4323361837026161106L;
	
	/** 节点操作者签名 **/
	private String signature;

	/** 节点地址 **/
	private Node node;

	/** 操作系统 **/
	private String os;

	/** 中央处理器 **/
	private String cpu;

	/** CPU使用比率 **/
	private double cpuRate;
	
	/** 节点默认的CPU使用占比 **/
	private double defaultCPURate;
	
	/** 图形处理器 **/
	private String gpu;

	/** GPU使用比率 **/
	private double gpuRate;
	
	/** 节点默认的GPU使用占比 **/
	private double defaultGPURate;

	/** 操作系统最大可用内存 **/
	private long sysMaxMemory;

	/** 操作系统当已经使用的内存 **/
	private long sysUsedMemory;
	
	/** 系统内存不足 **/
	private boolean sysMemoryMissing;

	/** 虚拟机最大可用内存 **/
	private long vmMaxMemory;

	/** 虚拟机已经占用的内存 **/
	private long vmUsedMemory;
	
	/** 虚拟机内存不足！ **/
	private boolean vmMemoryMissing;

	/** 操作系统的最大磁盘空间 **/
	private long sysMaxDisk;

	/** 操作系统已经占用的磁盘空间 **/
	private long sysUsedDisk;
	
	/** 系统磁盘空间不足 **/
	private boolean sysDiskMissing;

	/** 进程中的全部线程数目 **/
	private int threads;

	/** 命令数目 **/
	private int commands;

	/** 调用器数目 **/
	private int invokers;
	
	/** 注册用户数 **/
	private int registerMembers;
	
	/** 在线人数 **/
	private int onlineMembers;
	
	/** 记录时间 **/
	private long recordTime;

	/** 命令信道TCP缓存 **/
	private SocketBuffer commandStreamBuffer;

	/** 命令信道UDP缓存 **/
	private SocketBuffer commandPacketBuffer;

	/** 数据接收信道SOCKET缓存 **/
	private SocketBuffer replySuckerBuffer;

	/** 数据分派信道SOCKET缓存 **/
	private SocketBuffer replyDispatcherBuffer;
	
	/** 数据接收信道SOCKET监听端口 **/
	private int replySuckerPort;

	/** 数据分派信道SOCKET监听端口 **/
	private int replyDispatcherPort;
	
	/** 数据接收信道SOCKET映射监听端口 **/
	private int replyReflectSuckerPort;

	/** 数据分派信道SOCKET映射监听端口 **/
	private int replyReflectDispatcherPort;
	
	/** MASSIVE MIMO的MI单元数目 **/
	private int mi;
	
	/** MASSIVE MIMO的MO单元数目 **/
	private int mo;
	
	/**
	 * 构造默认的站点运行状态
	 */
	public SiteRuntime() {
		super();
		sysMaxMemory = sysUsedMemory = 0;
		vmMaxMemory = vmUsedMemory = 0;
		sysMaxDisk = sysUsedDisk = 0;
		// 默认无定义
		registerMembers = -1;
		onlineMembers = -1;
		recordTime = System.currentTimeMillis();
		
		defaultCPURate = 0.0f;
		
		// 虚拟机空间不足
		vmMemoryMissing = false;
		// 系统内存/磁盘空间不足
		sysMemoryMissing = false;
		sysDiskMissing = false;
		
		replySuckerPort = 0;
		replyDispatcherPort = 0;
		// MASSIVE MIMO单元
		mi = mo =0;
		// 网关映射端口
		replyReflectSuckerPort = 0;
		replyReflectDispatcherPort = 0;
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that SiteRuntime实例
	 */
	private SiteRuntime(SiteRuntime that) {
		super(that);
		set(that);
	}

	/**
	 * 从可类化数据读取器中解析站点运行状态
	 * @param reader 可类化数据读取器
	 */
	public SiteRuntime(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置副本参数
	 * 
	 * @param that
	 */
	public void set(SiteRuntime that) {
		signature = that.signature;
		node = that.node;
		os = that.os;
		// CPU
		cpu = that.cpu;
		cpuRate = that.cpuRate;
		defaultCPURate = that.defaultCPURate;
		// GPU
		gpu = that.gpu;
		gpuRate = that.gpuRate;
		defaultGPURate = that.defaultGPURate;
		// 系统内存
		sysMaxMemory = that.sysMaxMemory;
		sysUsedMemory = that.sysUsedMemory;
		sysMemoryMissing = that.sysMemoryMissing;
		// 虚拟机内存
		vmMaxMemory = that.vmMaxMemory;
		vmUsedMemory = that.vmUsedMemory;
		vmMemoryMissing = that.vmMemoryMissing;
		// 系统磁盘
		sysMaxDisk = that.sysMaxDisk;
		sysUsedDisk = that.sysUsedDisk;
		sysDiskMissing = that.sysDiskMissing;
		// 其它参数
		threads = that.threads;
		commands = that.commands;
		invokers = that.invokers;
		//
		registerMembers = that.registerMembers;
		onlineMembers = that.onlineMembers;
		// 记录时间
		recordTime = that.recordTime;
		
		// SOCKET缓存
		commandStreamBuffer = that.commandStreamBuffer;
		commandPacketBuffer = that.commandPacketBuffer;
		replySuckerBuffer = that.replySuckerBuffer;
		replyDispatcherBuffer = that.replyDispatcherBuffer;
		// 端口
		replySuckerPort = that.replySuckerPort;
		replyDispatcherPort = that.replyDispatcherPort;
		// Massive MIMO单元
		mi = that.mi;
		mo = that.mo;
		// 映射端口
		replyReflectSuckerPort = that.replyReflectSuckerPort;
		replyReflectDispatcherPort = that.replyReflectDispatcherPort;
	}

	/**
	 * 设置节点使用者签名
	 * @param e 字符串或者空指针
	 */
	public void setSignature(String e) {
		signature = e;
	}

	/**
	 * 返回节点使用者签名
	 * @return 字符串或者空指针
	 */
	public String getSignature() {
		return signature;
	}
	
	/**
	 * 设置节点
	 * @param e 节点实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);
		node = e;
	}

	/**
	 * 返回节点
	 * @return 节点实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 设置操作系统
	 * @param e
	 */
	public void setOS(String e) {
		os = e;
	}

	/**
	 * 返回操作系统
	 * @return
	 */
	public String getOS() {
		return os;
	}

	/**
	 * 设置CPU类型
	 * @param e
	 */
	public void setCPU(String e) {
		cpu = e;
	}

	/**
	 * 返回CPU类型
	 * @return
	 */
	public String getCPU() {
		return cpu;
	}

	/**
	 * 设置CPU使用比率
	 * @param e CPU使用比率
	 */
	public void setCPURate(double e){
		cpuRate = e;
	}
	
	/**
	 * 返回CPU使用比率
	 * @return CPU使用比率
	 */
	public double getCPURate(){
		return cpuRate;
	}

	/**
	 * 设置节点规定的CPU占比
	 * @param e
	 */
	public void setDefaultCPURate(double e) {
		defaultCPURate = e;
	}

	/**
	 * 返回节点规定的CPU占比
	 * @return 双浮点数
	 */
	public double getDefaultCPURate() {
		return defaultCPURate;
	}

	/**
	 * 判断CPU使用占比超过规定值
	 * @return 返回真或者假
	 */
	public boolean isCPUFull() {
		return defaultCPURate >0 && cpuRate >= defaultCPURate;
	}

	/**
	 * 设置GPU类型
	 * @param e
	 */
	public void setGPU(String e) {
		gpu = e;
	}

	/**
	 * 返回GPU类型
	 * @return
	 */
	public String getGPU() {
		return gpu;
	}
	
	/**
	 * 有GPU
	 * @return
	 */
	public boolean hasGPU() {
		return gpu != null;
	}

	/**
	 * 设置GPU使用比率
	 * @param e GPU使用比率
	 */
	public void setGPURate(double e){
		gpuRate = e;
	}
	
	/**
	 * 返回GPU使用比率
	 * @return GPU使用比率
	 */
	public double getGPURate(){
		return gpuRate;
	}

	/**
	 * 设置节点规定的GPU占比
	 * @param e
	 */
	public void setDefaultGPURate(double e) {
		defaultGPURate = e;
	}

	/**
	 * 返回节点规定的GPU占比
	 * @return 双浮点数
	 */
	public double getDefaultGPURate() {
		return defaultGPURate;
	}

	/**
	 * 判断GPU使用占比超过规定值
	 * @return 返回真或者假
	 */
	public boolean isGPUFull() {
		return defaultGPURate >0 && cpuRate >= defaultGPURate;
	}

	/**
	 * 设置操作系统最大可用内存
	 * @param i 操作系统最大可用内存
	 */
	public void setSysMaxMemory(long i){
		sysMaxMemory = i;
	}
	
	/**
	 * 返回操作系统最大可用内存
	 * @return 操作系统最大可用内存
	 */
	public long getSysMaxMemory(){
		return sysMaxMemory;
	}
	
	/**
	 * 设置操作系统已经占用的内存
	 * @param i 操作系统已经占用的内存
	 */
	public void setSysUsedMemory(long i) {
		sysUsedMemory = i;
	}
	
	/**
	 * 返回操作系统已经占用的内存
	 * @return 操作系统已经占用的内存
	 */
	public long getSysUsedMemory() {
		return sysUsedMemory;
	}
	
	/**
	 * 判断系统内存空间不足
	 * @param b 真或者假
	 */
	public void setSysMemoryMissing(boolean b){
		sysMemoryMissing = b;
	}
	
	/**
	 * 判断系统内存不足
	 * @return 返回真或者假
	 */
	public boolean isSysMemoryMissing() {
		return sysMemoryMissing;
	}
	
	
	/**
	 * 设置虚拟机最大可用内存
	 * @param i 虚拟机最大可用内存
	 */
	public void setVmMaxMemory(long i){
		vmMaxMemory = i;
	}
	
	/**
	 * 返回虚拟机最大可用内存
	 * @return 虚拟机最大可用内存
	 */
	public long getVmMaxMemory(){
		return vmMaxMemory;
	}
	
	/**
	 * 设置虚拟机已经占用的内存
	 * @param i 虚拟机已经占用的内存
	 */
	public void setVmUsedMemory(long i) {
		vmUsedMemory = i;
	}
	
	/**
	 * 返回虚拟机已经占用的内存
	 * @return 虚拟机已经占用的内存
	 */
	public long getVmUsedMemory() {
		return vmUsedMemory;
	}
	
	/**
	 * 设置虚拟机内存不足
	 * @param b 真或者假
	 */
	public void setVmMemoryMissing(boolean b) {
		vmMemoryMissing = b;
	}

	/**
	 * 返回虚拟机内存不足
	 * @return 真或者假
	 */
	public boolean isVmMemoryMissing() {
		return vmMemoryMissing;
	}

	/**
	 * 设置操作系统最大可用磁盘空间
	 * @param i 操作系统最大可用磁盘空间
	 */
	public void setSysMaxDisk(long i){
		sysMaxDisk = i;
	}
	
	/**
	 * 返回操作系统最大可用磁盘空间
	 * @return 操作系统最大可用磁盘空间
	 */
	public long getSysMaxDisk(){
		return sysMaxDisk;
	}
	
	/**
	 * 设置操作系统已经占用的磁盘空间
	 * @param i 操作系统已经占用的磁盘空间
	 */
	public void setSysUsedDisk(long i) {
		sysUsedDisk = i;
	}

	/**
	 * 返回操作系统已经占用的磁盘空间
	 * @return 操作系统已经占用的磁盘空间
	 */
	public long getSysUsedDisk() {
		return sysUsedDisk;
	}

	/**
	 * 设置系统磁盘空间不足
	 * @param b 真或者假
	 */
	public void setSysDiskMissing(boolean b) {
		sysDiskMissing = b;
	}

	/**
	 * 判断系统磁盘空间不足
	 * @return 真或者假
	 */
	public boolean isSysDiskMissing() {
		return sysDiskMissing;
	}

	/**
	 * 设置线程数目
	 * @param n 线程数目
	 */
	public void setThreads(int n) {
		threads = n;
	}

	/**
	 * 返回线程数目
	 * @return 线程数目
	 */
	public int getThreads() {
		return threads;
	}

	/**
	 * 设置命令数目
	 * @param i 命令数目
	 */
	public void setCommands(int i) {
		commands = i;
	}

	/**
	 * 返回命令数目
	 * @return 命令数目
	 */
	public int getCommands() {
		return commands;
	}
	
	/**
	 * 设置调用器数目
	 * @param i 调用器数目
	 */
	public void setInvokers(int i) {
		invokers = i;
	}

	/**
	 * 返回调用器数目
	 * @return 调用器数目
	 */
	public int getInvokers() {
		return invokers;
	}
	
	/**
	 * 设置注册人数。
	 * -1：无定义
	 * 0：没有
	 * >0：实际人数
	 * @param who 整数
	 */
	public void setRegisterMembers(int who) {
		registerMembers = who;
	}

	/**
	 * 返回注册人数
	 * @return 整数
	 */
	public int getRegisterMembers() {
		return registerMembers;
	}

	/**
	 * 设置在线人数
	 * @param who 整数
	 */
	public void setOnlineMembers(int who) {
		onlineMembers = who;
	}

	/**
	 * 返回在线人数
	 * @return 整数
	 */
	public int getOnlineMembers() {
		return onlineMembers;
	}
	
	/**
	 * 最后记录时间 
	 * @return 记录时间
	 */
	public long getRecordTime() {
		return recordTime;
	}


	/**
	 * 设置命令信道TCP缓存
	 * @param len TCP缓存
	 */
	public void setCommandStreamBuffer(SocketBuffer that) {
		commandStreamBuffer = that;
	}

	/**
	 * 返回命令信道TCP缓存
	 * @return TCP缓存
	 */
	public SocketBuffer getCommandStreamBuffer() {
		return commandStreamBuffer;
	}
	
	/**
	 * 设置命令信道UDP缓存
	 * @param len UDP缓存
	 */
	public void setCommandPacketBuffer(SocketBuffer that) {
		commandPacketBuffer = that;
	}

	/**
	 * 返回命令信道UDP缓存
	 * @return UDP缓存
	 */
	public SocketBuffer getCommandPacketBuffer() {
		return commandPacketBuffer;
	}
	
	/**
	 * 设置数据接收信道SOCKET缓存
	 * @param len SOCKET缓存尺寸
	 */
	public void setReplySuckerBuffer(SocketBuffer that) {
		replySuckerBuffer = that;
	}

	/**
	 * 返回数据接收信道SOCKET缓存
	 * @return SOCKET缓存尺寸
	 */
	public SocketBuffer getReplySuckerBuffer() {
		return replySuckerBuffer;
	}
	
	/**
	 * 设置数据分派信道SOCKET缓存
	 * @param len SOCKET缓存尺寸
	 */
	public void setReplyDispatcherBuffer(SocketBuffer that) {
		replyDispatcherBuffer = that;
	}

	/**
	 * 返回数据分派信道SOCKET缓存
	 * @return SOCKET缓存尺寸
	 */
	public SocketBuffer getReplyDispatcherBuffer() {
		return replyDispatcherBuffer;
	}
	
	/**
	 * 设置数据接收信道SOCKET监听端口
	 * @param port SOCKET监听端口
	 */
	public void setReplySuckerPort(int port) {
		replySuckerPort = port;
	}

	/**
	 * 返回数据接收信道SOCKET监听端口
	 * @return SOCKET监听端口
	 */
	public int getReplySuckerPort() {
		return replySuckerPort;
	}
	
	/**
	 * 设置数据分派信道SOCKET监听端口
	 * @param port SOCKET监听端口
	 */
	public void setReplyDispatcherPort(int port) {
		replyDispatcherPort = port;
	}

	/**
	 * 返回数据分派信道SOCKET监听端口
	 * @return SOCKET监听端口
	 */
	public int getReplyDispatcherPort() {
		return replyDispatcherPort;
	}

	/**
	 * 设置MASSIVE MIMO的MI单元
	 * @param who MI单元
	 */
	public void setMI(int who) {
		mi = who;
	}

	/**
	 * 返回MASSIVE MIMO的MI单元
	 * @return MI单元
	 */
	public int getMI() {
		return mi;
	}

	/**
	 * 设置MASSIVE MIMO的MO单元
	 * @param who MO单元
	 */
	public void setMO(int who) {
		mo = who;
	}

	/**
	 * 返回MASSIVE MIMO的MO单元
	 * @return MO单元
	 */
	public int getMO() {
		return mo;
	}

	/**
	 * 设置数据接收信道SOCKET映射监听端口
	 * @param port SOCKET映射监听端口
	 */
	public void setReplyReflectSuckerPort(int port) {
		replyReflectSuckerPort = port;
	}

	/**
	 * 返回数据接收信道SOCKET映射监听端口
	 * @return SOCKET映射监听端口
	 */
	public int getReplyReflectSuckerPort() {
		return replyReflectSuckerPort;
	}
	
	/**
	 * 设置数据分派信道SOCKET映射监听端口
	 * @param port SOCKET映射监听端口
	 */
	public void setReplyReflectDispatcherPort(int port) {
		replyReflectDispatcherPort = port;
	}

	/**
	 * 返回数据分派信道SOCKET映射监听端口
	 * @return SOCKET映射监听端口
	 */
	public int getReplyReflectDispatcherPort() {
		return replyReflectDispatcherPort;
	}


	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SiteRuntime duplicate() {
		return new SiteRuntime(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %s %s %.2f (%s %s) (%s %s) (%s %s) %d %d %d", 
				node, os, cpu, cpuRate,
				ConfigParser.splitCapacity(sysMaxMemory,2), ConfigParser.splitCapacity(sysUsedMemory,2),
				ConfigParser.splitCapacity(vmMaxMemory,2), ConfigParser.splitCapacity(vmUsedMemory,2),
				ConfigParser.splitCapacity(sysMaxDisk,2), ConfigParser.splitCapacity(sysUsedMemory,2), 
				threads, commands, invokers);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#hashCode()
	 */
	@Override
	public int hashCode(){
		return node.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#compareTo(com.laxcus.command.Command)
	 */
	@Override
	public int compareTo(Command that) {
		if (that == null) {
			return -1;
		}

		// 比较站点地址，或者否
		if (that.getClass() == SiteRuntime.class) {
			SiteRuntime status = (SiteRuntime) that;
			return Laxkit.compareTo(node, status.node);
		} else {
			return super.compareTo(that);
		}
	}


	/**
	 * 生成版本号是0的参数
	 * @param writer
	 */
	private void __build0(ClassWriter writer) {
		writer.writeString(signature);
		writer.writeObject(node);
		writer.writeString(os);
		// CPU
		writer.writeString(cpu);
		writer.writeDouble(cpuRate);
		writer.writeDouble(defaultCPURate);
		// GPU
		writer.writeString(gpu);
		writer.writeDouble(gpuRate);
		writer.writeDouble(defaultGPURate);
		// 系统内存
		writer.writeLong(sysMaxMemory);
		writer.writeLong(sysUsedMemory);
		writer.writeBoolean(sysMemoryMissing);
		// 虚拟机内存
		writer.writeLong(vmMaxMemory);
		writer.writeLong(vmUsedMemory);
		writer.writeBoolean(vmMemoryMissing);
		// 系统磁盘
		writer.writeLong(sysMaxDisk);
		writer.writeLong(sysUsedDisk);
		writer.writeBoolean(sysDiskMissing);
		// 其它参数
		writer.writeInt(threads);
		writer.writeInt(commands);
		writer.writeInt(invokers);
		// 人数
		writer.writeInt(registerMembers);
		writer.writeInt(onlineMembers);
		// 记录时间
		writer.writeLong(recordTime);
		
		// SOCKET缓存
		writer.writeObject(commandStreamBuffer);
		writer.writeObject(commandPacketBuffer);
		writer.writeObject(replySuckerBuffer);
		writer.writeObject(replyDispatcherBuffer);
		
		// 端口
		writer.writeInt(replySuckerPort);
		writer.writeInt(replyDispatcherPort);
		// MASSIVE MIMO单元
		writer.writeInt(mi);
		writer.writeInt(mo);
		// 映射端口
		writer.writeInt(replyReflectSuckerPort);
		writer.writeInt(replyReflectDispatcherPort);
	}

	/**
	 * 解析版本号是0的参数
	 * @param reader
	 */
	private void __resolve0(ClassReader reader) {
		signature = reader.readString();
		node = new Node(reader);
		os = reader.readString();
		// CPU
		cpu = reader.readString();
		cpuRate = reader.readDouble();
		defaultCPURate = reader.readDouble();
		// GPU
		gpu = reader.readString();
		gpuRate = reader.readDouble();
		defaultGPURate = reader.readDouble();
		// 内存
		sysMaxMemory = reader.readLong();
		sysUsedMemory = reader.readLong();
		sysMemoryMissing = reader.readBoolean();
		vmMaxMemory = reader.readLong();
		vmUsedMemory = reader.readLong();
		vmMemoryMissing = reader.readBoolean();
		// 磁盘
		sysMaxDisk = reader.readLong();
		sysUsedDisk = reader.readLong();
		sysDiskMissing = reader.readBoolean();
		// 其它参数
		threads = reader.readInt();
		commands = reader.readInt();
		invokers = reader.readInt();
		// 人数
		registerMembers = reader.readInt();
		onlineMembers = reader.readInt();
		// 记录时间
		recordTime = reader.readLong();
		
		// SOCKET缓存
		commandStreamBuffer = new SocketBuffer(reader);
		commandPacketBuffer = new SocketBuffer(reader);
		replySuckerBuffer = new SocketBuffer(reader);
		replyDispatcherBuffer = new SocketBuffer(reader);
		
		// 端口
		replySuckerPort = reader.readInt();
		replyDispatcherPort = reader.readInt();
		mi = reader.readInt();
		mo = reader.readInt();
		// 映射端口
		replyReflectSuckerPort = reader.readInt();
		replyReflectDispatcherPort = reader.readInt();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		switch (getVersion()) {
		case 0:
			__build0(writer);
			break;
		default:
			__build0(writer);
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		switch (getVersion()) {
		case 0:
			__resolve0(reader);
			break;
		default:
			__resolve0(reader);
			break;
		}
	}
}