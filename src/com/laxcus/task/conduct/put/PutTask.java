/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.put;

import java.io.*;
import com.laxcus.command.conduct.*;
import com.laxcus.task.*;
import com.laxcus.task.local.*;
import com.laxcus.util.*;

/**
 * CONDUCT.PUT阶段任务。部署在FRONT站点上。<br><br>
 * 
 * PUT阶段任务是CONDUCT命令阶段链条的最后一环，负责收集、显示、保存CONDUCT分布计算的处理结果。<br>
 * CONDUCT.PUT阶段任务的作用与ESTABLISH.END阶段任务基本一致。
 * 
 * @author scott.liang
 * @version 1.1 12/12/2012
 * @since laxcus 1.0
 */
public abstract class PutTask extends LocalTask {

	/** PUT阶段资源代理 **/
	private PutTrustor trustor;

	/**
	 * 构造CONDUCT.PUT阶段任务
	 */
	protected PutTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		trustor = null;
		super.destroy();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public Conduct getCommand() {
		return (Conduct) super.getCommand();
	}

	/**
	 * 设置PUT阶段资源代理
	 * @param e PutTrustor实例
	 */
	protected void setPutTrustor(PutTrustor e) {
		super.setTailTrustor(e);
		trustor = e;
	}

	/**
	 * 返回PUT阶段资源代理
	 * @return PutTrustor实例
	 */
	protected PutTrustor getPutTrustor() {
		return trustor;
	}

	/**
	 * 默认显示操作，将磁盘文件中的数据显示出来
	 * @param files 磁盘文件数组
	 * @return 返回处理数据的长度
	 * @throws TaskException
	 */
	protected long defaultDisplay(File[] files) throws TaskException {
		ContentBuffer buff = new ContentBuffer();
		for (File file : files) {
			byte[] b = readFile(file);
			buff.append(b, 0, b.length);
		}
		byte[] b = buff.toByteArray();
		// 返回处理数据长度
		return display(b, 0, b.length);
	}
}