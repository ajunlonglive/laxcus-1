/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.near;

import java.io.*;

import com.laxcus.command.contact.*;
import com.laxcus.task.*;
import com.laxcus.task.local.*;
import com.laxcus.util.*;

/**
 * CONTACT.NEAR阶段任务。部署在FRONT站点上。<br><br>
 * 
 * NEAR阶段任务是CONTACT命令阶段链条的最后一环，负责收集、显示、保存CONTACT分布计算的处理结果。<br>
 * CONTACT.NEAR阶段任务的作用与ESTABLISH.END阶段任务基本一致。
 * 
 * @author scott.liang
 * @version 1.1 12/12/2012
 * @since laxcus 1.0
 */
public abstract class NearTask extends LocalTask {

	/** NEAR阶段资源代理 **/
	private NearTrustor trustor;

	/**
	 * 构造CONTACT.NEAR阶段任务
	 */
	protected NearTask() {
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
	public Contact getCommand() {
		return (Contact) super.getCommand();
	}

	/**
	 * 设置NEAR阶段资源代理
	 * @param e NearTrustor实例
	 */
	protected void setNearTrustor(NearTrustor e) {
		super.setTailTrustor(e);
		trustor = e;
	}

	/**
	 * 返回NEAR阶段资源代理
	 * @return NearTrustor实例
	 */
	protected NearTrustor getNearTrustor() {
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