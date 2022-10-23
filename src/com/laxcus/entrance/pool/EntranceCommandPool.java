/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.pool;

import com.laxcus.command.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.Shutdown;
import com.laxcus.command.site.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.command.site.gate.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.entrance.*;
import com.laxcus.entrance.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;

/**
 * ENTRANCE命令管理池
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class EntranceCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static EntranceCommandPool selfHandle = new EntranceCommandPool();

	/**
	 * 构造异步命令管理池
	 */
	private EntranceCommandPool() {
		super();
	}

	/**
	 * 返回ENTRANCE命令管理池的静态句柄
	 * @return ENTRANCE命令管理池
	 */
	public static EntranceCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("EntranceCommandPool.getInstance");
		return EntranceCommandPool.selfHandle;
	}

	/** 内部默认使用命令 **/
	private Class<?>[] skips = new Class<?>[] { SwitchHub.class,
			SeekSiteRuntime.class, SeekOnlineCommand.class, SeekOnlineResource.class, Gust.class, Swarm.class };

	/** 只能是来自FRONT节点的命令 **/
	private Class<?>[] fronts = new Class<?>[] { CheckMassiveMimo.class,
			TakeAuthorizerSite.class };

	/**
	 * 判断是规定的集群内部命令
	 * @param cmd 命令实例
	 * @return 匹配返回真，否则假
	 */
	private boolean isSkipCommand(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < skips.length; i++) {
			if (cmd.getClass() == skips[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断是来自FRONT节点的命令
	 * @param cmd 命令实例
	 * @return 匹配返回真，否则假
	 */
	private boolean isFrontCommand(Command cmd) {
		for (int i = 0; i < fronts.length; i++) {
			if (cmd.getClass() == fronts[i]) {
				return true;
			}
		}
		return false;
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 必须有回显地址
		Cabin cabin = (cmd != null ? cmd.getSource() : null);
		// 不成功，返回假
		if (cabin == null) {
			return false;
		}

		Node node = cabin.getNode();

		// 来自BANK站点
		boolean success = EntranceLauncher.getInstance().isHub(node);
		// 判断是FRONT节点的命令
		if (!success) {
			success = isFrontCommand(cmd); 
			if (success) {
				// 如果不是来自FRONT节点，被忽略
				if (!cmd.getSourceSite().isFront()) {
					return false;
				}
			}
		}
		
//		// 来自FRONT站点
//		if (!success) {
//			success = (cmd.getClass() == TakeAuthorizerSite.class);
//			if (success) {
//				if (!cmd.getSourceSite().isFront()) {
//					return false;
//				}
//			}
//		}
		
		// 判断是内部命令
		if (!success) {
			success = isSkipCommand(cmd);
		}

		// 保存命令
		if (success) {
			success = add(cmd);
		}
		// 保存命令
		if (success) {
			Tigger.command(cmd);
		}
		
		Logger.note(this, "accept", success, "from %s '%s'", cabin, cmd);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// 来自FRONT站点的命令
		if (cmd.getClass() == TakeAuthorizerSite.class) {
			invoker = new EntranceTakeAuthorizerSiteInvoker((TakeAuthorizerSite)cmd);
		}
		// 管理命令
		if (cmd.getClass() == CreateSecureToken.class) {
			invoker = new SubCreateSecureTokenInvoker((CreateSecureToken) cmd);
		} else if (cmd.getClass() == DropSecureToken.class) {
			invoker = new SubDropSecureTokenInvoker((DropSecureToken) cmd);
		} else if (cmd.getClass() == FlushSecureToken.class) {
			invoker = new SubFlushSecureTokenInvoker((FlushSecureToken) cmd);
		} else if (cmd.getClass() == ShowSecureToken.class) {
			invoker = new SubShowSecureTokenInvoker((ShowSecureToken) cmd);
		} else if (cmd.getClass() == SetSecureSize.class) {
			invoker = new SubSetSecureSizeInvoker((SetSecureSize) cmd);
		} 
		else if (cmd.getClass() == ReloadLibrary.class) {
			invoker = new SubReloadLibraryInvoker((ReloadLibrary) cmd);
		} else if (cmd.getClass() == ReloadSecurityPolicy.class) {
			invoker = new SubReloadSecurityPolicyInvoker((ReloadSecurityPolicy) cmd);
		} else if (cmd.getClass() == ReloadLicence.class) {
			invoker = new SubReloadLicenceInvoker((ReloadLicence) cmd);
		} else if (cmd.getClass() == MailLicence.class) {
			invoker = new SubMailLicenceInvoker((MailLicence) cmd);
		} else if (cmd.getClass() == ScanCommandStack.class) {
			invoker = new SubScanCommandStackInvoker((ScanCommandStack) cmd);
		} else if (cmd.getClass() == ReleaseMemory.class) {
			invoker = new SubReleaseMemoryInvoker((ReleaseMemory) cmd);
		} else if (cmd.getClass() == ReleaseMemoryInterval.class) {
			invoker = new SubReleaseMemoryIntervalInvoker((ReleaseMemoryInterval) cmd);
		} else if (cmd.getClass() == ReplyPacketMode.class) {
			invoker = new SubReplyPacketModeInvoker((ReplyPacketMode) cmd);
		} else if (cmd.getClass() == ReplyPacketSize.class) {
			invoker = new SubReplyPacketSizeInvoker((ReplyPacketSize) cmd);
		} else if (cmd.getClass() == ReplySendTimeout.class) {
			invoker = new SubReplySendTimeoutInvoker((ReplySendTimeout) cmd);
		} else if (cmd.getClass() == ReplyReceiveTimeout.class) {
			invoker = new SubReplyReceiveTimeoutInvoker((ReplyReceiveTimeout) cmd);
		} else if(cmd.getClass() == ReplyFlowControl.class) {
			invoker = new SubReplyFlowControlInvoker((ReplyFlowControl)cmd);
		}
		// 强制重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new CommonRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// 设置站点日志等级
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new SubSetLogLevelInvoker((SetLogLevel) cmd);
		}
		// 切换管理站点命令，来自BANK站点
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}

		// 服务器密文超时
		else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new SubCipherTimeoutInvoker((CipherTimeout) cmd);
		}
		// 分布处理超时
		else if (cmd.getClass() == DistributedTimeout.class) {
			invoker = new SubDistributedTimeoutInvoker((DistributedTimeout) cmd);
		}
		// 最大缓存空间 / 最大调用器
		else if (cmd.getClass() == MaxEchoBuffer.class) {
			invoker = new SubMaxEchoBufferInvoker((MaxEchoBuffer) cmd);
		} else if (cmd.getClass() == MaxInvoker.class) {
			invoker = new SubMaxInvokerInvoker((MaxInvoker) cmd);
		}
		// 检测系统信息
		else if (cmd.getClass() == CheckSystemInfo.class) {
			invoker = new SubCheckSystemInfoInvoker((CheckSystemInfo) cmd);
		}
		// 强制重新加载和发布自动JAR包
		else if (cmd.getClass() == ReloadCustom.class) {
			invoker = new CommonReloadCustomInvoker((ReloadCustom) cmd);
		}
		// 当前站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new EntranceSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		}
		// 注册到BANK节点
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// 远程关闭命令
		else if (cmd.getClass() == Shutdown.class) {
			invoker = new SubShutdownInvoker((Shutdown) cmd);
		}
		// ENTRANCE申请全部GATE站点
		else if (cmd.getClass() == ShiftTakeBankSubSites.class) {
			invoker = new CommonShiftTakeBankSubSitesInvoker((ShiftTakeBankSubSites) cmd);
		}

		// BANK节点通知ENTRANCE站点，GATE注册或者注销
		else if (cmd.getClass() == PushGateSite.class) {
			invoker = new EntrancePushGateSiteInvoker((PushGateSite) cmd);
		} else if (cmd.getClass() == DropGateSite.class) {
			invoker = new EntranceDropGateSiteInvoker((DropGateSite) cmd);
		}

		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 内存/磁盘空间不足报警，通过BANK站点，转发给WATCH节点，在图形界面上通知系统管理员
		else if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new SubVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new SubMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new SubDiskMissingInvoker((DiskMissing) cmd);
		} else if (cmd.getClass() == LicenceTimeout.class) {
			invoker = new SubLicenceTimeoutInvoker((LicenceTimeout) cmd);
		}
		// CPU/内存/磁盘空间限制
		else if (cmd.getClass() == MostCPU.class) {
			invoker = new SubMostCPUInvoker((MostCPU) cmd);
		} else if(cmd.getClass() == MostVMMemory.class) {
			invoker = new SubMostVMMemoryInvoker((MostVMMemory)cmd);
		} else if (cmd.getClass() == LeastMemory.class) {
			invoker = new SubLeastMemoryInvoker((LeastMemory) cmd);
		} else if (cmd.getClass() == LeastDisk.class) {
			invoker = new SubLeastDiskInvoker((LeastDisk) cmd);
		} else if (cmd.getClass() == CheckSitePath.class) {
			invoker = new SubCheckSitePathInvoker((CheckSitePath) cmd);
		} else if (cmd.getClass() == ReflectPort.class) {
			invoker = new SubReflectPortInvoker((ReflectPort) cmd);
		}
		// 选择定位GATE站点模式
		else if (cmd.getClass() == ShadowMode.class) {
			invoker = new EntranceShadowModeInvoker((ShadowMode) cmd);
		}
		// 去全部GATE站点，检查账号已经注册
		else if (cmd.getClass() == AssertGateUser.class) {
			invoker = new EntranceAssertGateUserInvoker((AssertGateUser) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 检测MASSIVE MIMO
		else if (cmd.getClass() == CheckMassiveMimo.class) {
			invoker = new EntranceCheckMassiveMimoInvoker((CheckMassiveMimo) cmd);
		}

		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 不支持操作
		boolean success = (invoker != null);
		if (success) {
			success = EntranceInvokerPool.getInstance().launch(invoker);
		} else {
			unsupport(cmd);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#press(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 判断调用器有效
		boolean success = (invoker != null);
		if (success) {
			success = EntranceInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}