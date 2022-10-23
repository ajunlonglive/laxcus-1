/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 检索云端应用。<br>
 * FRONT节点发出，分散到CALL/DATA/WORK/BUILD节点执行。
 * 
 * @author scott.liang
 * @version 1.0 2/11/2020
 * @since laxcus 1.0
 */
public abstract class CommonSeekCloudWareInvoker extends CommonInvoker {

	/**
	 * 构造检索云端应用，指定命令
	 * @param cmd 检索云端应用
	 */
	protected CommonSeekCloudWareInvoker(SeekCloudWare cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekCloudWare getCommand() {
		return (SeekCloudWare) super.getCommand();
	}
	
	/**
	 * 分析和保存分析任务组件命名
	 * @param pool 分布任务组件池
	 * @param item 云应用包单元
	 */
	protected void loadTasks(RemoteTaskPool pool, CloudWareItem item) {
		SeekCloudWare cmd = getCommand();
		Siger issuer = cmd.getIssuer();
		
		TaskPart part = new TaskPart(issuer, pool.getFamily());
		TaskGroup document = pool.findGroup(part);
		// 有效，判断一致时
		if (document == null) {
			return;
		}
		
		for (TaskElement task : document.list()) {
			// 解析内部参数
			byte[] config = task.getBoot().getTaskText();
			TaskConfigReader reader = new TaskConfigReader(config);
			List<Tock> tocks = reader.readTocks();
			if (tocks == null || tocks.isEmpty()) {
				Logger.warning(this, "loadTasks", "cannot be resolve!");
				continue;
			}

			// 解析
			for(Tock tock : tocks) {
				Sock sock = tock.getSock();
				boolean success = (cmd.isAll() || cmd.contains(sock));
				if (!success) {
					continue;
				}
				
				Phase phase = new Phase(pool.getFamily(), tock.getSock(), tock.getSub());
				phase.setIssuer(issuer);
				
				CloudWareElement element = new CloudWareElement(phase);
				for (FileKey key : task.getJARs()) {
					FileKey sub = filte(key);
					element.addJar(sub);
				}
				for (FileKey key : task.getLibraries()) {
					FileKey sub = filte(key);
					element.addLibrary(sub);
				}
				// 保存
				item.add(element);
			}
		}
		
		Logger.debug(this, "loadTasks", "tasks count %d", item.size());
	}
	
	/**
	 * 过滤路径
	 * @param key
	 * @return 新的FileKey实例
	 */
	protected FileKey filte(FileKey key) {
		File file = new File(key.getPath());
		return new FileKey(file.getName(), key.getLength(), key.getModified());
	}

}