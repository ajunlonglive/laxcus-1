/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.util.*;

/**
 * 重新加载节点本地动态链接库配置命令调用器
 * 重新加载本地的本地动态链接库配置
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public abstract class CommonReloadLibraryInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造重新加载节点本地动态链接库配置命令调用器，指定命令
	 * @param cmd 重新加载节点本地动态链接库配置命令
	 */
	protected CommonReloadLibraryInvoker(ReloadLibrary cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReloadLibrary getCommand() {
		return (ReloadLibrary) super.getCommand();
	}
	
	/**
	 * 加载本地链接库文件
	 * @return 返回被加载的动态链接库数组
	 */
	protected ReloadLibraryItem reload() {
		// 重装加载本地链接库
		List<String> libs = JNILoader.reloadTotalLibraries();

		// 反馈给请求端
		ReloadLibraryItem item = new ReloadLibraryItem(getLocal(), true);
		item.addAll(libs);
		return item;
	}

}
