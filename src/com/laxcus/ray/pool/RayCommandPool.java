/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.login.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.log.client.*;
import com.laxcus.ray.invoker.*;

/**
 * RAY站点的异步命令管理池。<br>
 * 进入的异步命令先在管理池中托管，在环境许可情况下执行异步操作。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2013
 * @since laxcus 1.0
 */
public class RayCommandPool extends CommandPool {

	/** RAY站点管理池句柄 **/
	private static RayCommandPool selfHandle = new RayCommandPool();

	/**
	 * 构造RAY站点异步命令管理池
	 */
	private RayCommandPool() {
		super();
	}

	/**
	 * 返回管理池的静态句柄
	 * @return RAY命令管理池
	 */
	public static RayCommandPool getInstance() {
		return RayCommandPool.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#accept(com.laxcus.command.Command)
	 */
	@Override
	public boolean accept(Command cmd) {
		// 来访命令必须有回显地址
		if (cmd == null || cmd.getSource() == null) {
			return false;
		}

		// 保存命令
		boolean success = add(cmd);

		// 成功，保存它！
		if (success) {
			Tigger.command(cmd);
		}

		Logger.note(this, "accept", success, "from %s - %s", cmd.getSource(), cmd);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		EchoInvoker invoker = null;

		if (cmd.getClass() == PushSite.class) {
			invoker = new RayPushSiteInvoker((PushSite) cmd);
		} else if (cmd.getClass() == DropSite.class) {
			invoker = new RayDropSiteInvoker((DropSite) cmd);
		} else if (cmd.getClass() == DestroySite.class) {
			invoker = new RayDestroySiteInvoker((DestroySite) cmd);
		} else if (cmd.getClass() == AskSite.class) {
			invoker = new RayAskSiteInvoker((AskSite) cmd);
		}
		// 许可证超时
		else if (cmd.getClass() == LicenceTimeout.class) {
			invoker = new RayLicenceTimeoutInvoker((LicenceTimeout) cmd);
		}
		// 切换注册站点
		else if (cmd.getClass() == SwitchHub.class) {
			invoker = new RaySwitchHubInvoker((SwitchHub) cmd);
		}
		// 内存/磁盘空间不足，命令来自TOP/HOME/BANK站点
		else if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new RayVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new RayMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new RayDiskMissingInvoker((DiskMissing) cmd);
		}
		// 虚拟用户空间不足/耗尽，涉及用户账号、用户数据的节点
		else if (cmd.getClass() == MemberMissing.class) {
			invoker = new RayMemberMissingInvoker((MemberMissing) cmd);
		} else if (cmd.getClass() == MemberFull.class) {
			invoker = new RayMemberFullInvoker((MemberFull) cmd);
		}
		// FRONT虚拟用户空间不足/耗尽，来自CALL/GATE节点
		else if (cmd.getClass() == FrontMissing.class) {
			invoker = new RayFrontMissingInvoker((FrontMissing) cmd);
		} else if (cmd.getClass() == FrontFull.class) {
			invoker = new RayFrontFullInvoker((FrontFull) cmd);
		}
		// 向其他节点发出命令
		else if (cmd.getClass() == ShiftSeekSiteRuntime.class) {
			invoker = new RayShiftSeekSiteRuntimeInvoker((ShiftSeekSiteRuntime) cmd);
		}
		// 回收检查结果
		else if (cmd.getClass() == SiteRuntime.class) {
			invoker = new RaySiteRuntimeInvoker((SiteRuntime) cmd);
		}
		// TOP/HOME通知，站点不足
		else if (cmd.getClass() == SiteMissing.class) {
			invoker = new RaySiteMissingInvoker((SiteMissing) cmd);
		}
		// 刷新已经发布的组件
		else if(cmd.getClass() == RefreshPublish.class) {
			invoker = new RayLocalRefreshPublishInvoker((RefreshPublish)cmd);
		}
		// INVOKE/PRODUCE模式注册
		else if(cmd.getClass() == ShiftLoginSite.class) {
			invoker = new CommonShiftLoginSiteInvoker((ShiftLoginSite)cmd);
		}
		// HOME发来，存在故障表
		else if (cmd.getClass() == SubmitFaultTable.class) {
			invoker = new RaySubmitFaultTableInvoker((SubmitFaultTable) cmd);
		}
		
		// RAY发送命令，查询注册用户
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new RayAskClusterMemberInvoker((AskClusterMember) cmd);
		}
		// 推送/删除注册成员、在线成员
		if (cmd.getClass() == PushRegisterMember.class) {
			invoker = new RayPushRegisterMemberInvoker((PushRegisterMember) cmd);
		} else if (cmd.getClass() == DropRegisterMember.class) {
			invoker = new RayDropRegisterMemberInvoker((DropRegisterMember) cmd);
		} else if (cmd.getClass() == PushOnlineMember.class) {
			invoker = new RayPushOnlineMemberInvoker((PushOnlineMember) cmd);
		} else if (cmd.getClass() == DropOnlineMember.class) {
			invoker = new RayDropOnlineMemberInvoker((DropOnlineMember) cmd);
		}

		boolean success = (invoker != null);
		if (success) {
			success = RayInvokerPool.getInstance().launch(invoker);
		} else {
			unsupport(cmd);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#post(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

		// 询问分布站点，这个命令由RAY站点自己发出
		if (cmd.getClass() == AskSite.class) {
			invoker = new RayAskSiteInvoker((AskSite) cmd);
		}
		// 查询集群注册用户
		else if (cmd.getClass() == AskClusterMember.class) {
			invoker = new RayAskClusterMemberInvoker((AskClusterMember) cmd);
		}
		// 流量测试
		else if (cmd.getClass() == ShiftGust.class) {
			invoker = new RayShiftGustInvoker((ShiftGust) cmd);
		} else if (cmd.getClass() == ShiftSwarm.class) {
			invoker = new RayShiftSwarmInvoker((ShiftSwarm) cmd);
		}
		// 分发软件包
		else if (cmd.getClass() == ShiftDeploySystemPackage.class) {
			invoker = new RayShiftDeploySystemPackageInvoker((ShiftDeploySystemPackage) cmd);
		}

//		// 转发上传分布任务组件文件
//		else if (cmd.getClass() == ShiftUploadTask.class) {
//			invoker = new RayShiftUploadTaskInvoker((ShiftUploadTask) cmd);
//		}
//		// 转发上传码位计算器文件
//		else if (cmd.getClass() == ShiftUploadScaler.class) {
//			invoker = new RayShiftUploadScalerInvoker((ShiftUploadScaler) cmd);
//		}
//		// 转发上传快捷组件文件
//		else if (cmd.getClass() == ShiftUploadSwift.class) {
//			invoker = new RayShiftUploadSwiftInvoker((ShiftUploadSwift) cmd);
//		}
	
//		// 发布系统分布任务组件，包括组件/辅件/动态链接库，在HOME集群上操作
//		else if (cmd.getClass() == ShiftPublishSingleTaskComponent.class) {
//			invoker = new RayShiftPublishSingleTaskComponentInvoker((ShiftPublishSingleTaskComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleTaskAssistComponent.class) {
//			invoker = new RayShiftPublishSingleTaskAssistComponentInvoker((ShiftPublishSingleTaskAssistComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleTaskLibraryComponent.class) {
//			invoker = new RayShiftPublishSingleTaskLibraryComponentInvoker((ShiftPublishSingleTaskLibraryComponent) cmd);
//		}
		

		boolean success = (invoker != null);
		if (success) {
			success = RayInvokerPool.getInstance().launch(invoker);
		}
		return success;
	}

}