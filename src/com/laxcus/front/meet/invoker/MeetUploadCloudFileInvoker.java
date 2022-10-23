/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.awt.*;
import java.io.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 上传云文件调用器
 * 
 * @author scott.liang
 * @version 1.0 2/28/2022
 * @since laxcus 1.0
 */
public class MeetUploadCloudFileInvoker extends MeetHubServiceInvoker {
	
	/** 执行步骤 **/
	private int step;

	/**
	 * 构造上传云文件调用器，指定命令
	 * @param cmd 命令
	 */
	public MeetUploadCloudFileInvoker(UploadCloudFile cmd) {
		super(cmd);
		step = 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public UploadCloudFile getCommand() {
		return (UploadCloudFile) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果不是用户状态，拒绝执行
		if (!isUser()) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.PERMISSION_MISSING);
			}
			return false;
		}
		// 执行
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 分段执行
	 * @return
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = attempt();
			break;
		case 2:
			success = send();
			break;
		case 3:
			success = receive();
			break;
		}
		step++;
		// 不成功或者达到，退出
		if (!success || step > 3) {
			super.setQuit(true);
		}
		return success;
	}
	
	/**
	 * 发送向导指令
	 * @return
	 */
	private boolean attempt() {
		UploadCloudFile cmd = getCommand();
		SRL srl = cmd.getSRL();
		Node hub = srl.getNode();

		// 判断网关CALL节点存在
		boolean success = checkCloudHub(hub);
		if (!success) {
			return false;
		}

		UploadCloudFileGuide guide = new UploadCloudFileGuide();
		guide.setOverride(cmd.isOverride());
		guide.setLength(cmd.getContentLength());
		guide.setSRL(srl);
		// 发送给调用器
		success = fireToHub(hub, guide);

		Logger.debug(this, "attempt", success, "submit to %s", hub);

		return success;
	}
	
	/**
	 * 接收和发送数据流
	 * @return
	 */
	private boolean send(){
		ProductListener listener = getProductListener();
		
		// 1. 确认应答
		int index = findEchoKey(0);
		UploadCloudFileProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(UploadCloudFileProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 2. 出错或者拒绝
		boolean success = (product != null);
		if (!success) {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.FAILED_X, getCommand());
			}
			return false;
		}
		
		// 判断结果
		int state = product.getState();
		// 不成功，退出
		if (!StoreState.isSuccessful(state)) {
			if (listener != null) {
				listener.push(product);
			} else {
				print(product);
			}
			return false;
		}
		
		// 3. 传输过程中不做封装，INSERT以原始数据格式发送到CALL站点，CALL站点原样转发到DATA主站点
		Cabin cabin = product.getSource();
		
//		Cabin c1 = findItemCabin(index);		
//		Logger.debug(this, "send", success, "hub is %s -> %s", cabin, c1);
		
		ReplyItem item = null;
		UploadCloudFile cmd = getCommand();
		// 磁盘文件
		File file = cmd.getFile();
		if (file != null) {
			item = new ReplyItem(cabin, file);
		} else {
			item = new ReplyItem(cabin, cmd.getContent());
		}
		// 发送到服务器
		success = replyTo(item);
		
		// 不成功提示
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, cabin);
		}

		Logger.debug(this, "send", success, "send to %s", cabin);
		return success;
	}
	
	/**
	 * 第三阶段
	 * @return
	 */
	private boolean receive(){
		ProductListener listener = getProductListener();
		
		UploadCloudFileProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(UploadCloudFileProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 2. 出错或者拒绝
		boolean success = (product != null);
		if (!success) {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.FAILED_X, getCommand());
			}
			return false;
		}
		
		int state = product.getState();
		success = StoreState.isSuccessful(state);
		
		// 选择提示位置
		if (listener != null) {
			listener.push(product);
		} else {
			print(product);
		}
		
		Logger.debug(this, "receive", success, "execute");
		
		// 结束
		return useful(success);
	}
	
	/**
	 * 显示结果
	 * @param product
	 */
	private void print(UploadCloudFileProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "UPLOAD-FILE/STATE", "UPLOAD-FILE/FILE" });
		
		ShowItem item = new ShowItem();
		// 结果...
		int state = product.getState();
		
		if (StoreState.isDiskMissing(state)) {
			String name = getXMLContent("UPLOAD-FILE/STATE/DISK-MSSING");
			item.add(new ShowStringCell(0, name, Color.RED));
		} else if (StoreState.isFailed(state)) {
			String name = getXMLContent("UPLOAD-FILE/STATE/FAILED");
			item.add(new ShowStringCell(0, name, Color.RED));
		} else if (StoreState.isSuccessful(state)) {
			String name = getXMLContent("UPLOAD-FILE/STATE/SUCCESS");
			item.add(new ShowStringCell(0, name, Color.BLUE));
		} else if (StoreState.isExists(state)) {
			String name = getXMLContent("UPLOAD-FILE/STATE/EXISTS");
			item.add(new ShowStringCell(0, name, Color.RED));
		} else {
			String name = "None";
			item.add(new ShowStringCell(0, name));
		}
		
		SRL dir = product.getSRL();
		if (dir != null) {
			item.add(new ShowStringCell(1, dir.getPath()));
		} else {
			UploadCloudFile cmd = getCommand();
			SRL srl = cmd.getSRL();
			item.add(new ShowStringCell(1, srl.getPath()));
		}
		
		// 保存单元
		addShowItem(item);
		
		// 输出全部
		flushTable();
	}
}