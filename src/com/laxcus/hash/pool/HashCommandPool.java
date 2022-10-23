/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.Shutdown;
import com.laxcus.command.site.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.hash.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;

/**
 * HASH命令管理池
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class HashCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static HashCommandPool selfHandle = new HashCommandPool();

	/**
	 * 构造异步命令管理池
	 */
	private HashCommandPool() {
		super();
	}

	/**
	 * 返回HASH命令管理池的静态句柄
	 * @return HASH命令管理池
	 */
	public static HashCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("HashCommandPool.getInstance");
		return HashCommandPool.selfHandle;
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

		// 保存命令
		boolean success = add(cmd);
		
		// 保存命令
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - %s", cabin, cmd);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// 管理命令
		// 密钥令牌管理命令
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
			invoker = new HashSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
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
		// 向BANK站点申请主机编号
		else if (cmd.getClass() == ShiftTakeSiteSerial.class) {
			invoker = new CommonShiftTakeSiteSerialInvoker((ShiftTakeSiteSerial) cmd);
		}
		// 向BANK站点查询某类站点数目
		else if(cmd.getClass() == ShiftTakeBankSubSiteCount.class) {
			invoker = new CommonShiftTakeBankSubSiteCountInvoker((ShiftTakeBankSubSiteCount)cmd);
		}
		// HASH申请全部ACCOUNT站点
		else if (cmd.getClass() == ShiftTakeBankSubSites.class) {
			invoker = new CommonShiftTakeBankSubSitesInvoker((ShiftTakeBankSubSites) cmd);
		}
		// GATE/BANK查询一个签名的ACCOUNT站点地址
		else if (cmd.getClass() == TakeAccountSite.class) {
			invoker = new HashTakeAccountSiteInvoker((TakeAccountSite) cmd);
		}
		// GATE/BANK查询数据库名称
		else if (cmd.getClass() == AssertSchema.class) {
			invoker = new HashAssertSchemaInvoker((AssertSchema) cmd);
		}
		// 根据坐标获得ACCOUNT站点账号签名
		else if (cmd.getClass() == ShiftTakeAccountSiger.class) {
			invoker = new HashShiftTakeAccountSigerInvoker((ShiftTakeAccountSiger) cmd);
		}
		// BANK站点广播账号和地址
		else if (cmd.getClass() == RefreshAccount.class) {
			invoker = new HashRefreshAccountInvoker((RefreshAccount) cmd);
		}
		// 删除账号
		else if (cmd.getClass() == AwardDropRefer.class) {
			invoker = new HashAwardDropReferInvoker((AwardDropRefer) cmd);
		}

		// BANK节点通知HASH站点，ACCOUNT注册或者注销
		else if (cmd.getClass() == PushAccountSite.class) {
			invoker = new HashPushAccountSiteInvoker((PushAccountSite) cmd);
		} else if (cmd.getClass() == DropAccountSite.class) {
			invoker = new HashDropAccountSiteInvoker((DropAccountSite) cmd);
		}

		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 检索用户分布区域
		else if (cmd.getClass() == SeekUserArea.class) {
			invoker = new HashSeekUserAreaInvoker((SeekUserArea) cmd);
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
		// CPU/虚拟机内存/系统内存/磁盘空间限制
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
		// 查找注册节点 / 推送注册用户
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new HashAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == ShiftPushRegisterMember.class) {
			invoker = new HashShiftPushRegisterMemberInvoker((ShiftPushRegisterMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new SubOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new SubCloseTiggerInvoker((CloseTigger) cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 不支持操作
		boolean success = (invoker != null);
		if (success) {
			success = HashInvokerPool.getInstance().launch(invoker);
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
			success = HashInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}