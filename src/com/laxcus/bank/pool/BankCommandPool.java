/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;

import com.laxcus.command.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.mix.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.reload.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.command.site.front.*;
import com.laxcus.command.site.gate.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.task.*;
import com.laxcus.command.traffic.*;
import com.laxcus.command.secure.*;
import com.laxcus.command.shutdown.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.cyber.*;
import com.laxcus.command.licence.*;
import com.laxcus.command.login.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.bank.*;
import com.laxcus.bank.invoker.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * BANK站点的异步命令管理池。<br>
 * 
 * 受理和处理来自TOP站点和下属站点的异步命令。
 * 
 * @author scott.liang
 * @version 1.3 07/08/2013
 * @since laxcus 1.0
 */
public class BankCommandPool extends CommandPool {

	/** 管理池句柄 **/
	private static BankCommandPool selfHandle = new BankCommandPool();

	/**
	 * 初始化异步命令管理池
	 */
	private BankCommandPool() {
		super();
	}

	/**
	 * 返回异步命令管理池的静态句柄
	 * @return BankCommandPool实例
	 */
	public static BankCommandPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("BankCommandPool.getInstance");
		return BankCommandPool.selfHandle;
	}

	//	/**
	//	 * 必须串行处理的命令，它们的参数在全网具有唯一性
	//	 */
	//	private Class<?>[] serials = new Class<?>[] { CreateUser.class,
	//			CreateSchema.class };

	/**
	 * 必须串行处理的命令，它们的参数在全网具有唯一性
	 */
	private Class<?>[] serials = new Class<?>[] { 
			CreateSchema.class };

	/**
	 * 判断是需要串行处理的命令。<br>
	 * 这些命令携事的参数具有全网唯一性，并行处理存在同名冲突的可能，必须采用串行处理。<br>
	 * 命令包括建立用户账号、建立数据库。<br><br>
	 * 
	 * @param cmd 命令
	 * @return 返回真或者假
	 */
	private boolean isSerialCommand(Command cmd) {
		for (int i = 0; i < serials.length; i++) {
			if (cmd.getClass() == serials[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 系统内部跨集群、忽略检查的命令
	 */
	private Class<?>[] SKIP_COMMANDS = new Class<?>[] { SwitchHub.class,
			SeekSiteRuntime.class, SeekOnlineCommand.class,
			SeekOnlineResource.class, Gust.class, Swarm.class , AskSite.class};

	/**
	 * 判断是规定的内部命令，跨过检查
	 * @param cmd 命令实例
	 * @return 返回真或者假
	 */
	private boolean isSkip(Command cmd) {
		// 判断是内部命令
		for (int i = 0; i < SKIP_COMMANDS.length; i++) {
			if (cmd.getClass() == SKIP_COMMANDS[i]) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 没有定义回显地址不受理
		Cabin cabin = cmd.getSource();
		if (cabin == null) {
			return false;
		}

		Node node = cabin.getNode();
		// 是TOP站点发来的命令
		boolean success = BankLauncher.getInstance().isHub(node);
		// BANK站点的下属站点发来的命令，包括ACCOUNT/HASH/GATE/ENTRANCE/WATCH，LOG节点不会主动发命令。
		if (!success) {
			success = AccountOnBankPool.getInstance().contains(node);
		}
		if (!success) {
			success = HashOnBankPool.getInstance().contains(node);
		}
		if (!success) {
			success = GateOnBankPool.getInstance().contains(node);
		}
		if (!success) {
			success = EntranceOnBankPool.getInstance().contains(node);
		}
		if (!success) {
			success = WatchOnBankPool.getInstance().contains(node);
		}
		if (!success) {
			success = LogOnBankPool.getInstance().contains(node);
		}

		// 判断是BANK管理站点发来的命令
		if (!success) {
			Node hub = BankLauncher.getInstance().getManager();
			success = (Laxkit.compareTo(node, hub) == 0);
			// 判断来自监视站点（通常是发送“SWITCH PARTENT”命令）
			if (!success) {
				success = BankLauncher.getInstance().hasMonitor(node);
			}
		}

		// 判断是跨集群的内部命令
		if (!success) {
			success = isSkip(cmd);
		}

		// 保存命令。如果是必须串行的命令，转给串行命令池逐一调用处理。
		if (success) {
			if (isSerialCommand(cmd)) {
				success = SerialCommandPool.getInstance().add(cmd);
			} else {
				success = add(cmd);
			}
		}

		// 成功！
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - %s", cabin, cmd);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		// 询问分布站点，只能由WATCH站点发出
		if (cmd.getClass() == AskSite.class) {
			invoker = new BankAskSiteInvoker((AskSite) cmd);
		}
		// 处理密钥令牌
		else if (cmd.getClass() == CreateSecureToken.class) {
			invoker = new BankCreateSecureTokenInvoker((CreateSecureToken) cmd);
		} else if (cmd.getClass() == DropSecureToken.class) {
			invoker = new BankDropSecureTokenInvoker((DropSecureToken) cmd);
		} else if (cmd.getClass() == FlushSecureToken.class) {
			invoker = new BankFlushSecureTokenInvoker((FlushSecureToken) cmd);
		} else if (cmd.getClass() == ShowSecureToken.class) {
			invoker = new BankShowSecureTokenInvoker((ShowSecureToken) cmd);
		} else if (cmd.getClass() == SetSecureSize.class) {
			invoker = new BankSetSecureSizeInvoker((SetSecureSize) cmd);
		}
		// 重置链接库/沙箱策略/扫描命令
		else if (cmd.getClass() == ReloadLibrary.class) {
			invoker = new BankReloadLibraryInvoker((ReloadLibrary) cmd);
		} else if (cmd.getClass() == ReloadSecurityPolicy.class) {
			invoker = new BankReloadSecurityPolicyInvoker((ReloadSecurityPolicy) cmd);
		} else if (cmd.getClass() == ReloadLicence.class) {
			invoker = new BankReloadLicenceInvoker((ReloadLicence) cmd);
		} else if (cmd.getClass() == MailLicence.class) {
			invoker = new BankMailLicenceInvoker((MailLicence) cmd);
		} else if (cmd.getClass() == ScanCommandStack.class) {
			invoker = new BankScanCommandStackInvoker((ScanCommandStack) cmd);
		} else if (cmd.getClass() == ReleaseMemory.class) {
			invoker = new BankReleaseMemoryInvoker((ReleaseMemory) cmd);
		} else if (cmd.getClass() == ReleaseMemoryInterval.class) {
			invoker = new BankReleaseMemoryIntervalInvoker((ReleaseMemoryInterval) cmd);
		}
		// 设置应答数据模式
		else if (cmd.getClass() == ReplyPacketMode.class) {
			invoker = new BankReplyPacketModeInvoker((ReplyPacketMode) cmd);
		}  else if (cmd.getClass() == ReplyPacketSize.class) {
			invoker = new BankReplyPacketSizeInvoker((ReplyPacketSize) cmd);
		} else if (cmd.getClass() == ReplySendTimeout.class) {
			invoker = new BankReplySendTimeoutInvoker((ReplySendTimeout) cmd);
		} else if (cmd.getClass() == ReplyReceiveTimeout.class) {
			invoker = new BankReplyReceiveTimeoutInvoker((ReplyReceiveTimeout) cmd);
		} else if(cmd.getClass() == ReplyFlowControl.class) {
			invoker = new BankReplyFlowControlInvoker((ReplyFlowControl)cmd);
		}
		// 强制重新注册
		else if (cmd.getClass() == RefreshLogin.class) {
			invoker = new BankRefreshLoginInvoker((RefreshLogin) cmd);
		}
		// 设置站点日志等级
		else if (cmd.getClass() == SetLogLevel.class) {
			invoker = new BankSetLogLevelInvoker((SetLogLevel) cmd);
		}
		// 新的BANK管理站点向下属的站点发出切换站点命令
		else if(cmd.getClass() == ShiftSwitchHub.class) {
			invoker = new BankShiftSwitchHubInvoker((ShiftSwitchHub)cmd);
		}
		// 来自TOP站点，切换管理站点命令
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new CommonSwitchHubInvoker((SwitchHub) cmd);
		}

		// 切换BANK同级站点
		else if (cmd.getClass() == ShiftSwitchPartner.class) {
			invoker = new BankShiftSwitchPartnerInvoker((ShiftSwitchPartner) cmd);
		} else if (cmd.getClass() == SwitchPartner.class) {
			invoker = new BankSwitchPartnerInvoker((SwitchPartner) cmd);
		}

		// 客户机/服务器密文超时
		else if (cmd.getClass() == CipherTimeout.class) {
			invoker = new BankCipherTimeoutInvoker((CipherTimeout) cmd);
		}
		// 分布处理超时
		else if (cmd.getClass() == DistributedTimeout.class) {
			invoker = new BankDistributedTimeoutInvoker((DistributedTimeout) cmd);
		}
		// 最大缓存空间 / 最大调用器
		else if (cmd.getClass() == MaxEchoBuffer.class) {
			invoker = new BankMaxEchoBufferInvoker((MaxEchoBuffer) cmd);
		} else if (cmd.getClass() == MaxInvoker.class) {
			invoker = new BankMaxInvokerInvoker((MaxInvoker) cmd);
		}
		// 检测系统信息
		else if (cmd.getClass() == CheckSystemInfo.class) {
			invoker = new BankCheckSystemInfoInvoker((CheckSystemInfo) cmd);
		}
		// 强制重新加载和发布自动JAR包
		else if (cmd.getClass() == ReloadCustom.class) {
			invoker = new BankReloadCustomInvoker((ReloadCustom) cmd);
		}
		// 请求来自WATCH站点，检查站点状态
		else if (cmd.getClass() == SeekSiteRuntime.class) {
			invoker = new BankSeekSiteRuntimeInvoker((SeekSiteRuntime) cmd);
		}
		// 检索在线命令
		else if (cmd.getClass() == SeekOnlineCommand.class) {
			invoker = new CommonSeekOnlineCommandInvoker((SeekOnlineCommand) cmd);
		}
		// 接受子节点注册
		else if (cmd.getClass() == LoginSite.class) {
			invoker = new BankLoginSiteInvoker((LoginSite) cmd);
		}
		// 注册到上级管理节点（BANK是TOP节点）
		else if (cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite) cmd);
		}
		// BANK管理站点发送，BANK监视站点接收的子级站点注册、注销、故障销毁命令（是CAST SITE命令子类）
		else if (cmd.getClass() == PushSite.class) {
			invoker = new BankPushSiteInvoker((PushSite) cmd);
		} else if (cmd.getClass() == DropSite.class) {
			invoker = new BankDropSiteInvoker((DropSite) cmd);
		} else if (cmd.getClass() == DestroySite.class) {
			invoker = new BankDestroySiteInvoker((DestroySite) cmd);
		}
		// BANK管理站点通知BANK监视站点、WATCH站点，推送一个子级站点（发出CAST SITE命令）
		else if (cmd.getClass() == ShiftCastSite.class) {
			invoker = new CommonShiftCastSiteInvoker((ShiftCastSite) cmd);
		}
		// 账号、数据库、表的查询判断
		else if (cmd.getClass() == AssertUser.class) {
			invoker = new BankAssertUserInvoker((AssertUser) cmd);
		} else if (cmd.getClass() == AssertSchema.class) {
			invoker = new BankAssertSchemaInvoker((AssertSchema) cmd);
		} else if (cmd.getClass() == AssertTable.class) {
			invoker = new BankAssertTableInvoker((AssertTable) cmd);
		}
		// 串行方式建立用户账号，并行方式删除账号及全部表
		else if (cmd.getClass() == CreateUser.class) {
			invoker = new BankCreateUserInvoker((CreateUser) cmd);
		} else if (cmd.getClass() == DropUser.class) {
			invoker = new BankDropUserInvoker((DropUser) cmd);
		} else if (cmd.getClass() == AlterUser.class) {
			invoker = new BankAlterUserInvoker((AlterUser) cmd);
		} 
		// 禁用/解禁用户账号
		else if (cmd.getClass() == CloseUser.class) {
			invoker = new BankCloseUserInvoker((CloseUser) cmd);
		} else if (cmd.getClass() == OpenUser.class) {
			invoker = new BankOpenUserInvoker((OpenUser) cmd);
		}
		// 串行方式建立用户数据库，并行方式删除数据库及下属全部表
		else if (cmd.getClass() == ShiftCreateSchema.class) {
			invoker = new BankSerialShiftCreateSchemaInvoker((ShiftCreateSchema) cmd);
		} else if (cmd.getClass() == DropSchema.class) {
			invoker = new BankDropSchemaInvoker((DropSchema) cmd);
		}
		// 并行方式，建立、删除数据表
		else if (cmd.getClass() == CreateTable.class) {
			invoker = new BankCreateTableInvoker((CreateTable) cmd);
		} else if (cmd.getClass() == DropTable.class) {
			invoker = new BankDropTableInvoker((DropTable) cmd);
		}
		// 授权，解除授权操作
		else if (cmd.getClass() == Grant.class) {
			invoker = new BankGrantInvoker((Grant) cmd);
		} else if (cmd.getClass() == Revoke.class) {
			invoker = new BankRevokeInvoker((Revoke) cmd);
		}

		// 接收GATE请求，BANK在此转发，删除TOP/HOME集群全部记录
		else if(cmd.getClass() == AwardDropRefer.class) {
			invoker = new BankAwardDropReferInvoker((AwardDropRefer)cmd);
		}
		// 接收GATE请求，BANK在此转发，删除GATE站点，TOP/HOME集群的密码
		else if (cmd.getClass() == AwardAlterUser.class) {
			invoker = new BankAwardAlterUserInvoker((AwardAlterUser) cmd);
		}

		// 接受HASH/GATE站点的命令，反馈结果
		else if (cmd.getClass() == TakeSiteSerial.class) {
			invoker = new BankTakeSiteSerialInvoker((TakeSiteSerial) cmd);
		} 
		// 来自ACCOUNT/HASH/GATE/ENTRANCE站点，BANK接收
		else if (cmd.getClass() == TakeBankSubSiteCount.class) {
			invoker = new BankTakeBankSubSiteCountInvoker((TakeBankSubSiteCount) cmd);
		}
		// HASH/GATE/ENTRANCE发送，BANK管理站点接收，HASH/GATE/ENTRANCE请求获取关联的站点地址
		else if (cmd.getClass() == TakeBankSubSites.class) {
			invoker = new BankTakeBankSubSitesInvoker((TakeBankSubSites) cmd);
		}
		// BANK在自己的集群里广播一个账号
		else if (cmd.getClass() == RefreshAccount.class) {
			invoker = new BankRefreshAccountInvoker((RefreshAccount) cmd);
		}
		// BANK向TOP/HOME集群广播一个更新资源引用
		else if (cmd.getClass() == RefreshRefer.class) {
			invoker = new BankRefreshReferInvoker((RefreshRefer) cmd);
		}
		// 获得账号实例或者账号的资源引用
		else if (cmd.getClass() == TakeAccount.class) {
			invoker = new BankTakeAccountInvoker((TakeAccount) cmd);
		} else if(cmd.getClass() == TakeRefer.class) {
			invoker = new BankTakeReferInvoker((TakeRefer)cmd);
		}
		// GATE站点查询与签名关联的CALL站点，在此BANK负责转发给TOP站点
		else if (cmd.getClass() == TakeCallItem.class) {
			invoker = new BankTakeCallItemInvoker((TakeCallItem) cmd);
		}
		// 刷新注册用户（BANK转发给全部GATE站点）
		else if (cmd.getClass() == RefreshUser.class) {
			invoker = new BankRefreshUserInvoker((RefreshUser) cmd);
		}
		// BANK接受GATE刷新组件命令，转发给TOP站点，由TOP分发给下属的HOME子域集群
		else if (cmd.getClass() == RefreshPublish.class) {
			invoker = new BankRefreshPublishInvoker((RefreshPublish) cmd);
		}
		// 获得组件站点命令，从TOP站点传来
		else if(cmd.getClass() == TakeSigerSite.class) {
			invoker = new BankTakeSigerSiteInvoker((TakeSigerSite)cmd);
		}
		// 获得数据表命令，从TOP站点传来
		else if (cmd.getClass() == TakeTable.class) {
			invoker = new BankTakeTableInvoker((TakeTable) cmd);
		}

		// 查找分布任务组件的ACCOUNT地址
		else if (cmd.getClass() == AssertTaskHub.class) {
			invoker = new BankAssertTaskHubInvoker((AssertTaskHub) cmd);
		} 
//		else if (cmd.getClass() == AssertScalerHub.class) {
//			invoker = new BankAssertScalerHubInvoker((AssertScalerHub) cmd);
//		} 
		//		else if (cmd.getClass() == AssertSwiftHub.class) {
		//			invoker = new BankAssertSwiftHubInvoker((AssertSwiftHub) cmd);
		//		}
		// 转发关闭授权表
		else if (cmd.getClass() == ShiftCloseShareTable.class) {
			invoker = new BankShiftCloseShareTableInvoker((ShiftCloseShareTable) cmd);
		} else if (cmd.getClass() == ShiftAwardCloseActiveItem.class) {
			invoker = new BankShiftAwardCloseActiveItemInvoker((ShiftAwardCloseActiveItem) cmd);
		}
		// 显示FRONT站点的登录记录(这个操作必须是管理员)
		else if (cmd.getClass() == SeekFrontSite.class) {
			invoker = new BankSeekFrontSiteInvoker((SeekFrontSite) cmd);
		} else if (cmd.getClass() == SeekFrontUser.class) {
			invoker = new BankSeekFrontUserInvoker((SeekFrontUser) cmd);
		}
		// 检索用户分布区域
		else if (cmd.getClass() == SeekUserArea.class) {
			invoker = new BankSeekUserAreaInvoker((SeekUserArea) cmd);
		}
		// 获取系统管理员账号
		else if (cmd.getClass() == TakeAdministrator.class) {
			invoker = new BankTakeAdministratorInvoker((TakeAdministrator) cmd);
		}

		// BANK集群内部的子站点注册/注销后，通知关联站点
		else if (cmd.getClass() == ShiftPushGateSite.class) {
			invoker = new BankShiftPushGateSiteInvoker((ShiftPushGateSite) cmd);
		} else if (cmd.getClass() == ShiftDropGateSite.class) {
			invoker = new BankShiftDropGateSiteInvoker((ShiftDropGateSite) cmd);
		} else if (cmd.getClass() == ShiftPushHashSite.class) {
			invoker = new BankShiftPushHashSiteInvoker((ShiftPushHashSite) cmd);
		} else if (cmd.getClass() == ShiftDropHashSite.class) {
			invoker = new BankShiftDropHashSiteInvoker((ShiftDropHashSite) cmd);
		} else if (cmd.getClass() == ShiftPushAccountSite.class) {
			invoker = new BankShiftPushAccountSiteInvoker((ShiftPushAccountSite) cmd);
		} else if (cmd.getClass() == ShiftDropAccountSite.class) {
			invoker = new BankShiftDropAccountSiteInvoker((ShiftDropAccountSite) cmd);
		}

		// 网络流量测试
		else if (cmd.getClass() == Gust.class) {
			invoker = new CommonClientGustInvoker((Gust) cmd);
		} else if (cmd.getClass() == Swarm.class) {
			invoker = new CommonServerSwarmInvoker((Swarm) cmd);
		}
		// 站点不足，来自GATE站点，通知WATCH站点，当有注册的前提下！BANK只负责转发！
		else if (cmd.getClass() == SiteMissing.class) {
			invoker = new BankSiteMissingInvoker((SiteMissing) cmd);
		}
		// 设置DSM表压缩倍数，来自GATE站点的请求，BANK在此起中继作用，交给TOP，把TOP反馈给GATE
		else if (cmd.getClass() == SetDSMReduce.class) {
			invoker = new BankSetDSMReduceInvoker((SetDSMReduce) cmd);
		}
		// 内存/磁盘/用户虚拟空间不足报警，通过WATCH节点
		else if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new BankVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new BankMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new BankDiskMissingInvoker((DiskMissing) cmd);
		} else if (cmd.getClass() == MemberMissing.class) {
			invoker = new BankMemberMissingInvoker((MemberMissing) cmd);
		} else if (cmd.getClass() == MemberFull.class) {
			invoker = new BankMemberFullInvoker((MemberFull) cmd);
		} else if (cmd.getClass() == FrontMissing.class) {
			invoker = new BankFrontMissingInvoker((FrontMissing) cmd);
		} else if (cmd.getClass() == FrontFull.class) {
			invoker = new BankFrontFullInvoker((FrontFull) cmd);
		} else if (cmd.getClass() == LicenceTimeout.class) {
			invoker = new BankLicenceTimeoutInvoker((LicenceTimeout) cmd);
		}
		// 定位GATE站点模式
		else if (cmd.getClass() == ShadowMode.class) {
			invoker = new BankShadowModeInvoker((ShadowMode) cmd);
		}
		// 检查GATE注册用户与GATE站点编号一致性
		else if (cmd.getClass() == CheckShadowConsistency.class) {
			invoker = new BankCheckShadowConsistencyInvoker((CheckShadowConsistency) cmd);
		}
		// CPU/内存/磁盘空间限制
		else if (cmd.getClass() == MostCPU.class) {
			invoker = new BankMostCPUInvoker((MostCPU) cmd);
		} else if(cmd.getClass() == MostVMMemory.class) {
			invoker = new BankMostVMMemoryInvoker((MostVMMemory)cmd);
		} else if (cmd.getClass() == LeastMemory.class) {
			invoker = new BankLeastMemoryInvoker((LeastMemory) cmd);
		} else if (cmd.getClass() == LeastDisk.class) {
			invoker = new BankLeastDiskInvoker((LeastDisk) cmd);
		} else if (cmd.getClass() == CheckSitePath.class) {
			invoker = new BankCheckSitePathInvoker((CheckSitePath) cmd);
		} else if (cmd.getClass() == ReflectPort.class) {
			invoker = new BankReflectPortInvoker((ReflectPort) cmd);
		}
		// 远程关闭
		else if (cmd.getClass() == Shutdown.class) {
			invoker = new BankShutdownInvoker((Shutdown) cmd);
		}
		// 找包含系统组件的ACCOUNT站点
		else if (cmd.getClass() == TakeSystemTaskSite.class) {
			invoker = new BankTakeSystemTaskSiteInvoker((TakeSystemTaskSite) cmd);
		}
		// 集群用户的虚拟空间
		else if (cmd.getClass() == SetMemberCyber.class) {
			invoker = new BankSetMemberCyberInvoker((SetMemberCyber) cmd);
		} else if (cmd.getClass() == SetFrontCyber.class) {
			invoker = new BankSetFrontCyberInvoker((SetFrontCyber) cmd);
		} else if (cmd.getClass() == CheckUserCyber.class) {
			invoker = new BankCheckUserCyberInvoker((CheckUserCyber) cmd);
		}
		// WATCH节点检索BANK集群注册成员
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new BankAskClusterMemberInvoker((AskClusterMember) cmd);
		} else if (cmd.getClass() == PushRegisterMember.class) {
			invoker = new BankPushRegisterMemberInvoker((PushRegisterMember) cmd);
		} else if (cmd.getClass() == DropRegisterMember.class) {
			invoker = new BankDropRegisterMemberInvoker((DropRegisterMember) cmd);
		} else if (cmd.getClass() == PushOnlineMember.class) {
			invoker = new BankPushOnlineMemberInvoker((PushOnlineMember) cmd);
		} else if (cmd.getClass() == DropOnlineMember.class) {
			invoker = new BankDropOnlineMemberInvoker((DropOnlineMember) cmd);
		}
		// TIGGER操作类型
		else if (cmd.getClass() == OpenTigger.class) {
			invoker = new BankOpenTiggerInvoker((OpenTigger) cmd);
		} else if (cmd.getClass() == CloseTigger.class) {
			invoker = new BankCloseTiggerInvoker((CloseTigger) cmd);
		}
		// 获得工作节点
		else if (cmd.getClass() == TakeJobSite.class) {
			invoker = new BankTakeJobSiteInvoker((TakeJobSite) cmd);
		}
		// 删除分布式应用
		else if (cmd.getClass() == DropConductPackage.class) {
			invoker = new BankDropConductPackageInvoker((DropConductPackage) cmd);
		} else if (cmd.getClass() == DropEstablishPackage.class) {
			invoker = new BankDropEstablishPackageInvoker((DropEstablishPackage) cmd);
		} else if (cmd.getClass() == DropContactPackage.class) {
			invoker = new BankDropContactPackageInvoker((DropContactPackage) cmd);
		}
		else if(cmd.getClass() == CheckUserCost.class) {
			invoker = new BankCheckUserCostInvoker((CheckUserCost)cmd);
		}
		
		// 以上不成立，去自定义调用器判断有匹配的命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		// 没有匹配，转发一个不支持应答；否则转发给调用器管理池处理
		boolean success = (invoker != null);
		if (success) {
			success = BankInvokerPool.getInstance().launch(invoker);
		} else {
			super.unsupport(cmd);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#post(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

		// 删除账号（从本地发出）
		if (cmd.getClass() == ShiftDropUser.class) {
			invoker = new BankShiftDropUserInvoker((ShiftDropUser) cmd);
		}
		// 删除表（从本地发出）
		else if(cmd.getClass() == ShiftDropTable.class) {
			invoker = new BankShiftDropTableInvoker((ShiftDropTable)cmd);
		}
		// 转发删除授权表
		else if (cmd.getClass() == ShiftCloseShareTable.class) {
			invoker = new BankShiftCloseShareTableInvoker((ShiftCloseShareTable) cmd);
		}
		// 转发强制解除
		else if (cmd.getClass() == ShiftAwardCloseActiveItem.class) {
			invoker = new BankShiftAwardCloseActiveItemInvoker((ShiftAwardCloseActiveItem) cmd);
		}

		// 判断是自定义命令
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(cmd);
		}

		boolean success = (invoker != null);
		if (success) {
			success = BankInvokerPool.getInstance().launch(invoker);
		}

		return success;
	}

}