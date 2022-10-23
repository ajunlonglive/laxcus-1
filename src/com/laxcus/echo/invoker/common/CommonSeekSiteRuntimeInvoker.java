/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.watch.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.launch.*;
import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.net.*;

/**
 * 诊断节点运行时状态调用器 <br><br>
 * 
 * 此方法由WATCH节点调用，返回结果参数显示在GUI界面上。
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public abstract class CommonSeekSiteRuntimeInvoker extends CommonInvoker {

	/**
	 * 构造诊断节点运行时状态，指定命令
	 * @param cmd 诊断节点运行时状态
	 */
	protected CommonSeekSiteRuntimeInvoker(SeekSiteRuntime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekSiteRuntime getCommand() {
		return (SeekSiteRuntime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Runtime rt = Runtime.getRuntime();
		SiteLauncher launcher = getLauncher();

		// 返回当前站点状态
		SiteRuntime cmd = new SiteRuntime();
		cmd.setSignature(getSignature());
		cmd.setNode(getLocal());
		cmd.setTigger(false); // 不要记录Tigger.command
		
		// 判断LINUX/WINDOWS操作系统，选择参数
		if (isLinux()) {
			// LINUX版本
			String name = System.getProperty("os.name");
			String version = LinuxEffector.getInstance().getVersion();
			if (version != null) {
				name = String.format("%s/%s", name, version);
			}
			cmd.setOS(name);
			
			// CPU版本
			String cpu = LinuxEffector.getInstance().getCPUName();
			if (cpu != null) {
				cmd.setCPU(cpu);
			}
			// 系统内存
			cmd.setSysMaxMemory(LinuxDevice.getInstance().getSysMaxMemory());
			cmd.setSysUsedMemory(LinuxDevice.getInstance().getSysUsedMemory());
			LinuxDevice.getInstance().getMemoryLeast();
			cmd.setSysMemoryMissing(LinuxDevice.getInstance().isMemoryMissing());
			// 系统磁盘空间
			cmd.setSysMaxDisk(LinuxDevice.getInstance().getSysMaxDisk());
			cmd.setSysUsedDisk(LinuxDevice.getInstance().getSysUsedDisk());
			cmd.setSysDiskMissing(LinuxDevice.getInstance().isDiskMissing());

		} else if (isWindows()) {
			cmd.setOS(System.getProperty("os.name") + "/" + System.getProperty("os.version"));
			cmd.setCPU(System.getProperty("os.arch"));
			// 系统内存
			cmd.setSysMaxMemory(WindowsDevice.getInstance().getSysMaxMemory());
			cmd.setSysUsedMemory(WindowsDevice.getInstance().getSysUsedMemory());
			cmd.setSysMemoryMissing(WindowsDevice.getInstance().isMemoryMissing());
			// 系统磁盘空间
			cmd.setSysMaxDisk(WindowsDevice.getInstance().getSysMaxDisk());
			cmd.setSysUsedDisk(WindowsDevice.getInstance().getSysUsedDisk());
			cmd.setSysDiskMissing(WindowsDevice.getInstance().isDiskMissing());
		} 
		
		// 空置GPU
		cmd.setGPU(null);
		cmd.setGPURate(0.0f);
		cmd.setDefaultGPURate(0.0f);

		// JVM可分配最大内存
		long maxMemory = rt.maxMemory();
		// totalMemory：JVM已分配内存，freeMemory：是totalMemory中未使用部分
		// 实际使用的内存 = JVM已分配内存 - JVM已分配内存中未使用部分 
		long usedMemory = (rt.totalMemory() - rt.freeMemory());
		// 判断虚拟机内存不足
		boolean vmMemoryMissing = EchoTransfer.isVMMemoryMissing(usedMemory, maxMemory);
		// 设置参数
		cmd.setVmMaxMemory(maxMemory);
		cmd.setVmUsedMemory(usedMemory);
		cmd.setVmMemoryMissing(vmMemoryMissing);

		// 命令/调用器池
		CommandPool commandPool = launcher.getCommandPool();
		InvokerPool invokerPool = launcher.getInvokerPool();

		// CPU使用比率
		cmd.setCPURate(invokerPool.getCPURate());
		// 节点默认的CPU占用比率
		cmd.setDefaultCPURate(EchoTransfer.getMaxCpuRate());

		// 命令和调用器数目
		cmd.setCommands(commandPool.size()); // 等待处理的命令数目
		cmd.setInvokers(invokerPool.size() - 1); // 不包括它自己
		// 当前运行线程数目
		cmd.setThreads(Thread.activeCount());

		// 设置优先级
		cmd.setPriority(getPriority());
		
		// 注册成员/在线成员
		cmd.setRegisterMembers(getRegisterMembers());
		cmd.setOnlineMembers(getOnlineMembers());
		
		// 控制信道SOCKET缓存
		cmd.setCommandStreamBuffer(EchoTransfer.getCommandStreamBuffer());
		cmd.setCommandPacketBuffer(EchoTransfer.getCommandPacketBuffer());
		// 数据信道SOCKET缓存
		cmd.setReplySuckerBuffer(EchoTransfer.getReplySuckerBuffer());
		cmd.setReplyDispatcherBuffer(EchoTransfer.getReplyDispatcherBuffer());
		
		// 数据端口
		cmd.setReplySuckerPort(getReplySuckerPort());
		cmd.setReplyDispatcherPort(getReplyDispatcherPort());
		
		// MASSIVE MIMO单元
		cmd.setMI(launcher.getMIMembers());
		cmd.setMO(launcher.getMOMembers());

		// 映射端口
		cmd.setReplyReflectSuckerPort(launcher.getReflectSuckerPort());
		cmd.setReplyReflectDispatcherPort(launcher.getReflectDispatcherPort());
		
		// 投递命令到WATCH站点
		Node watch = getCommandSite();
		boolean success = directTo(watch, cmd);

		return useful(success);
	}
	
	/**
	 * 返回端口
	 * @return
	 */
	private int getReplySuckerPort() {
		SocketHost host = getLauncher().getReplyHelper().getDefinePrivateHost();
		if (host == null) {
			host = getLauncher().getReplyHelper().getDefinePublicHost();
		}
		return (host == null ? 0 : host.getPort());
	}

	/**
	 * 返回端口
	 * @return
	 */
	private int getReplyDispatcherPort() {
		SocketHost host = getLauncher().getReplyWorker().getDefinePrivateHost();
		if (host == null) {
			host = getLauncher().getReplyWorker().getDefinePublicHost();
		}
		return (host == null ? 0 : host.getPort());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 返回注册成员人数。<br><br>
	 * -1：无定义 <br>
	 * 0：无人数 <br>
	 * >0：是实际人数<br><br>
	 * 
	 * @return 整数
	 */
	protected abstract int getRegisterMembers();

	/**
	 * 返回在线成员人数 <br><br>
	 * -1：无定义 <br>
	 * 0：无人数 <br>
	 * >0：是实际人数<br><br>
	 * 
	 * @return 整数
	 */
	protected abstract int getOnlineMembers();
}