/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.cloud.store.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 修改云端文件名称调用器
 * 
 * @author scott.liang
 * @version 1.0 1/26/2022
 * @since laxcus 1.0
 */
public class DesktopRenameCloudFileInvoker extends DesktopHubServiceInvoker {

	/**
	 * 构造修改云端文件名称调用器
	 * @param cmd 修改云端文件名称
	 */
	public DesktopRenameCloudFileInvoker(RenameCloudFile cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RenameCloudFile getCommand() {
		return (RenameCloudFile) super.getCommand();
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
		
		// 执行
		RenameCloudFile cmd = getCommand();
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
		RenameCloudFileProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RenameCloudFileProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ProductListener listener = getProductListener();
		if (listener != null) {
			listener.push(product);
		} else {
			boolean success = (product != null);
			if (success) {
				// 以文本的方式，显示成功或者失败
				printResult(product);
			} else {
				printFault();
			}
		}
		return useful(product != null);
	}
	
	private void printResult(RenameCloudFileProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "RENAME-CLOUD-FILE/STATE", "RENAME-CLOUD-FILE/FILE" });
		
		ShowItem item = new ShowItem();
		// 结果...
		int state = product.getState();
		if (StoreState.isFailed(state)) {
			String name = getXMLContent("RENAME-CLOUD-FILE/STATE/FAILED");
			item.add(new ShowStringCell(0, name));
		} else if (StoreState.isSuccessful(state)) {
			String name = getXMLContent("RENAME-CLOUD-FILE/STATE/SUCCESS");
			item.add(new ShowStringCell(0, name));
		} else if (StoreState.isExists(state)) {
			String name = getXMLContent("RENAME-CLOUD-FILE/STATE/EXISTS");
			item.add(new ShowStringCell(0, name));
		} else {
			String name = "None";
			item.add(new ShowStringCell(0, name));
		}
		
		SFile dir = product.getFile();
		if (dir != null) {
			item.add(new ShowStringCell(1, dir.getPath()));
		} else {
			RenameCloudFile cmd = getCommand();
			SRL srl = cmd.getSRL();
			item.add(new ShowStringCell(1, srl.getPath()));
		}
		
		// 保存单元
		addShowItem(item);
		
		// 输出全部
		flushTable();
	}

}