/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.ray.runtime.*;

/**
 * 销毁节点调用器
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class RayDestroySiteInvoker extends RayCastElementInvoker {

	/**
	 * 构造销毁节点调用器，指定命令
	 * @param cmd 销毁节点
	 */
	public RayDestroySiteInvoker(DestroySite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DestroySite getCommand() {
		return (DestroySite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DestroySite cmd = getCommand();
		Node node = cmd.getSite();

		// 销毁节点
		boolean success = destroySite(node);
		// 成功，取出参数
		if (success) {
			faultX(FaultTip.DESTROY_NODE_X, node);

			StringBuilder buff = new StringBuilder();

			// 显示结果
			List<DisableMember> list = cmd.list();
			for (DisableMember e : list) {
				Siger siger = e.getSiger();

				// 全部表，不要播放声音！
				List<Space> spaces = e.list();
				if (spaces.size() == 0) {
					fault(siger.toString(), false);

					// 保存日志
					if (buff.length() > 0) {
						buff.append("\r\n");
					}
					buff.append(siger.toString());
				} else {
					for (Space s : spaces) {
						String a = String.format("%s %s", siger, s);
						fault(a, false);

						if (buff.length() > 0) {
							buff.append("\r\n");
						}
						buff.append(a);
					}
				}
			}

			// 记录行为
			Tigger.error(cmd, buff.toString());
		}

		// 删除节点运行时记录
		RaySiteRuntimeBasket.getInstance().dropRuntime(node);

		// 不做区分的删除
		dropRegisterMember(node);
		dropOnlineMember(node);

		Logger.debug(this, "launch", success, "node is %s", node);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
