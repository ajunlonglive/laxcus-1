/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.io.*;

import com.laxcus.command.traffic.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;

/**
 * 数据传输速率测试调用器。<br><br>
 * 
 * 这个调用器属于“服务器”端，接受上传的数据。
 * 
 * @author scott.liang
 * @version 1.0 8/10/2018
 * @since laxcus 1.0
 */
public class CommonServerSwarmInvoker extends CommonInvoker {

	/**
	 * 构造数据传输速率测试调用器，指定命令。
	 * @param cmd 数据传输速率测试
	 */
	public CommonServerSwarmInvoker(Swarm cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Swarm getCommand() {
		return (Swarm) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// WATCH调用器监听地址
		Swarm cmd = getCommand();
		Cabin source = cmd.getSource();
		
		SwarmReflex reflex = new SwarmReflex();
		// 向来源反馈结果
		return replyTo(source, reflex);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 接收WATCH数据失败，退出
		if (isFaultCompleted()) {
			return false;
		}

		long length = -1;

		EchoBuffer buf = findBuffer(0);
		if (buf.isMemory()) {
			length = buf.getMemorySize();
		} else if (buf.isDisk()) {
			File file = buf.getFile();
			length = file.length();
		}

		Logger.debug(this, "ending", "data size:%d, save mode：%s !", length,
				(buf.isDisk() ? "Disk" : "Memory"));

		return useful();
	}

}
