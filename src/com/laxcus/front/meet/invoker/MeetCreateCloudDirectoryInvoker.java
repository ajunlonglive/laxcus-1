/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.cloud.store.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 建立云存储目录调用器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2021
 * @since laxcus 1.0
 */
public class MeetCreateCloudDirectoryInvoker extends MeetHubServiceInvoker {

	/**
	 * 构造建立云存储目录调用器
	 * @param cmd 建立云存储目录
	 */
	public MeetCreateCloudDirectoryInvoker(CreateCloudDirectory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateCloudDirectory getCommand() {
		return (CreateCloudDirectory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ProductListener listener = getProductListener();
		
		// 如果不是用户状态，拒绝执行
		if (!isUser()) {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.PERMISSION_MISSING);
			}
			return false;
		}
		
		// 生成命令
		CreateCloudDirectory cmd = getCommand();
		SRL srl = cmd.getSRL();
		Node hub = srl.getNode();

		// 判断网关CALL节点存在
		boolean success = checkCloudHub(hub);
		if (!success) {
			return false;
		}

		// 发送给调用器
		success = fireToHub(hub, cmd);
		Logger.debug(this, "launch", success, "submit to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CreateCloudDirectoryProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateCloudDirectoryProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(product);
			} else {
				// 以文本的方式，显示成功或者失败
				printResult(product);
			}
		} else {
			printFault();
		}
		return useful(success);
	}
	
	private void printResult(CreateCloudDirectoryProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "CREATE-DIRECTORY/STATE", "CREATE-DIRECTORY/DIRECTORY" });
		
		ShowItem item = new ShowItem();
		// 结果...
		int state = product.getState();
		if (StoreState.isFailed(state)) {
			String name = getXMLContent("CREATE-DIRECTORY/STATE/FAILED");
			item.add(new ShowStringCell(0, name));
		} else if (StoreState.isSuccessful(state)) {
			String name = getXMLContent("CREATE-DIRECTORY/STATE/SUCCESS");
			item.add(new ShowStringCell(0, name));
		} else if (StoreState.isExists(state)) {
			String name = getXMLContent("CREATE-DIRECTORY/STATE/EXISTS");
			item.add(new ShowStringCell(0, name));
		} else {
			String name = "None";
			item.add(new ShowStringCell(0, name));
		}
		
//		SDirectory dir = product.getDirectory();
		VPath dir = product.getPath();
		if (dir != null) {
			item.add(new ShowStringCell(1, dir.getPath()));
		} else {
			CreateCloudDirectory cmd = getCommand();
			SRL srl = cmd.getSRL();
			item.add(new ShowStringCell(1, srl.getPath()));
		}
		
		// 保存单元
		addShowItem(item);
		
		// 输出全部
		flushTable();
	}

}