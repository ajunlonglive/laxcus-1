/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;

import com.laxcus.call.pool.*;
import com.laxcus.command.cloud.store.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 下载文件调用器
 * 
 * @author scott.liang
 * @version 1.0 11/7/2021
 * @since laxcus 1.0
 */
public class CallDownloadCloudFileInvoker extends CallInvoker {

	/**
	 * 构造下载文件调用器
	 * @param cmd
	 */
	public CallDownloadCloudFileInvoker(DownloadCloudFileGuide cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DownloadCloudFileGuide getCommand() {
		return (DownloadCloudFileGuide) super.getCommand();
	}

	/**
	 * 判断允许上传
	 * @param srl
	 * @return
	 */
	private int exists(DownloadCloudFileGuide cmd){
		SRL srl = cmd.getSRL();

		// 判断文件存在
		boolean exists = StoreOnCallPool.getInstance().hasFile(getIssuer(), srl.getPath());
		return (exists ? StoreState.SUCCESSFUL : StoreState.NOT_FOUND);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DownloadCloudFileGuide cmd = getCommand();
		SRL srl = cmd.getSRL();
		// FRONT调用器监听地址
		Cabin source = cmd.getSource(); 

		// 判断允许或者否
		int state = exists(cmd);
		boolean exists = StoreState.isSuccessful(state);

		// 反馈给FRONT节点
		DownloadCloudFileProduct product = new DownloadCloudFileProduct(state);
		product.setSRL(srl);

		// 反馈结果
		boolean success = replyTo(source, product);

		// 以上不成功，向FRONT返回错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
		}

		Logger.debug(this, "launch", exists && success, "reply to %s", source);

		return exists && success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 如果接收FRONT站点数据失败，退出
		if (isFaultCompleted()) {
			super.replyFault();
			return false;
		}
		
		DownloadCloudFileGuide cmd = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				cmd = getObject(DownloadCloudFileGuide.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 2. 出错或者拒绝
		boolean success = (cmd != null);
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
			return false;
		}

		// 找到实例文件
		SRL srl = cmd.getSRL();
		File file =	StoreOnCallPool.getInstance().getLocalFile(getIssuer(), srl.getPath()); 

		// 返回文件
		if (file == null) {
			replyFault(Major.FAULTED, Minor.FILE_NOTFOUND);
			return useful(false);
		} 
		
		// 反馈结果
		Cabin source = findItemCabin(index); // cmd.getSource();
		byte[] b = readFile(file);
		ReplyItem item = new ReplyItem(source, b);
		success = replyTo(item);

		Logger.debug(this, "ending", success, "reply to %s, content length: %d", source, b.length);

		// 结束
		return useful(success);
	}

	/**
	 * 读文件内容
	 * @param file 文件
	 * @return 返回字节流
	 */
	private byte[] readFile(File file) {
		int len = (int) file.length();
		try {
			byte[] b = new byte[len];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

}