/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.watch.*;
import com.laxcus.launch.*;

/**
 * 重新加载许可证调用器。<br>
 * 当管理员修改节点conf/site.policy文件后，调用方法重置。
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public abstract class CommonReflectPortInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造重新加载许可证调用器，指定命令
	 * @param cmd 重新加载许可证
	 */
	protected CommonReflectPortInvoker(ReflectPort cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReflectPort getCommand() {
		return (ReflectPort) super.getCommand();
	}

	/**
	 * 加载本地许可证文件
	 * @return 返回加载结果
	 */
	protected ReflectPortProduct reload() {
		ReflectPort cmd = getCommand();

		ReflectPortProduct product = new ReflectPortProduct();
		SiteLauncher launcher = getLauncher();

		for (ReflectPortItem e : cmd.list()) {
			int port = e.getPort();
			boolean success = false;
			// 四种模式
			if (e.isStreamServer()) {
				success = launcher.setReflectStreamPort(port);
			} else if (e.isPacketServer()) {
				success = launcher.setReflectPacketPort(port);
			} else if (e.isSuckerServer()) {
				success = launcher.setReflectSuckerPort(port);
			} else if (e.isDispatcherServer()) {
				success = launcher.setReflectDispatcherPort(port);
			}
			// 保存记录
			ReflectPortItem sub = e.duplicate();
			sub.setSuccessful(success);
			product.add(sub);
		}
		
		// 通知节点立即重新注册，保证新的映射端口出现在管理节点上，用于分派！
		launcher.checkin(true);

		return product;
	}

}