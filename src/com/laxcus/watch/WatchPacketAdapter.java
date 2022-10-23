/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

import com.laxcus.fixp.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * WATCH站点数据包适配器。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class WatchPacketAdapter extends PacketAdapter {

	/**
	 * 构造WATCH站点数据包适配器
	 */
	public WatchPacketAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#active(com.laxcus.site.Node)
	 */
	@Override
	protected boolean active(Node node) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#apply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet apply(Packet packet) {
		Mark cmd = packet.getMark();
		if (Assert.isShutdown(cmd)) {
			shutdown(WatchLauncher.getInstance(), packet);
		} else if (Assert.isComeback(cmd)) {
			WatchLauncher.getInstance().hurry();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.PacketAdapter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	protected Packet reply(Packet packet) {
		Mark cmd = packet.getMark();
		// 收到激活回应
		if (Answer.isIsee(cmd.getAnswer())) {
			// 更新激活回馈时间
			WatchLauncher.getInstance().refreshEndTime();
			// 在状态栏显示动画图标闪烁
			WatchLauncher.getInstance().flash();
		} else if(Answer.isNotLogin(cmd.getAnswer())) {
			Logger.warning(this, "reply", "relogin! from %s", packet.getRemote());
			
			WatchLauncher.getInstance().kiss(); // 通知重新注册
		}
		return null;
	}

}