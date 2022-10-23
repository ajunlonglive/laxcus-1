/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.awt.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.command.cyber.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检测MASSIVE MIMO调用器
 * 
 * @author scott.liang
 * @version 1.0 2/21/2022
 * @since laxcus 1.0
 */
public class DesktopCheckMassiveMimoInvoker extends DesktopInvoker {
	
	/** 执行步骤 **/
	private int step;

	/**
	 * 构造检测MASSIVE MIMO调用器，指定命令
	 * @param cmd 命令
	 */
	public DesktopCheckMassiveMimoInvoker(CheckMassiveMimo cmd) {
		super(cmd);
		step = 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckMassiveMimo getCommand() {
		return (CheckMassiveMimo) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
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
	 * 判断允许这个节点
	 * @param hub 服务器节点
	 * @return 允许返回真，否则假
	 */
	private boolean allow(Node hub) {
		// 判断是ENTRANCE节点
		Node node = getLauncher().getRootHub();
		if (Laxkit.compareTo(node, hub) == 0) {
			return true;
		}

		// 判断是GATE节点
		node = getLauncher().getHub();
		if (Laxkit.compareTo(node, hub) == 0) {
			return true;
		}

		// 判断是云存储节点（CALL节点）
		if (getStaffPool().hasCloudSite(hub)) {
			return true;
		}

		// 判断是CALL节点
		if (CallOnFrontPool.getInstance().hasSite(hub)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 发送向导指令
	 * @return
	 */
	private boolean attempt() {
		CheckMassiveMimo cmd = getCommand();
		Node hub = cmd.getSite();

		// 判断对应的节点
		boolean success = allow(hub);  //checkCallHub(hub);
		if (!success) {
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.NOTFOUND_SITE_X, hub);
			}
			Logger.error(this, "attempt", "not found! %s", hub);

			return false;
		}

		// 发送给网关节点
		success = fireToHub(hub, cmd);

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
		CheckMassiveMimoProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CheckMassiveMimoProduct.class, index);
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
		
		// 判断网关的接收器数量
		int suckers = product.getMISuckers();
		if (suckers < 1) {
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
		
		// 生成一段字节数目，用来发送数据
		byte[] b = new byte[10240];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) 'W';
		}
		ReplyItem item = new ReplyItem(cabin, b);
		
		// 发送到服务器
		success = replyTo(item);
		
		// 不成功提示
		if (!success) {
//			faultX(FaultTip.CANNOT_SUBMIT_X, cabin);
			print(suckers, false);
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
		
		CheckMassiveMimoProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CheckMassiveMimoProduct.class, index);
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
		
		int state = product.getMISuckers();
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
	private void print(CheckMassiveMimoProduct product) {
		print(product.getMISuckers(), product.isSuccessful());
	}
	
	/**
	 * 显示结果
	 * @param suckers MI接收器数量
	 * @param successful 成功或者否
	 */
	private void print(int suckers, boolean successful) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "CHECK-MASSIVE-MIMO/STATE", "CHECK-MASSIVE-MIMO/SUCKERS" });
		
		ShowItem item = new ShowItem();
		
		if (suckers < 1) {
			String name = getXMLContent("CHECK-MASSIVE-MIMO/STATE/REFUSED");
			item.add(new ShowStringCell(0, name, Color.YELLOW));
		} else {
			if (successful) {
				String name = getXMLContent("CHECK-MASSIVE-MIMO/STATE/SUCCESS");
				item.add(new ShowStringCell(0, name, Color.BLUE));
			} else {
				String name = getXMLContent("CHECK-MASSIVE-MIMO/STATE/FAILED");
				item.add(new ShowStringCell(0, name, Color.RED));
			}
		}
		
		// 接收器
		item.add(new ShowIntegerCell(1, suckers));
		
		// 保存单元
		addShowItem(item);
		
		// 输出全部
		flushTable();
	}
	
//	/**
//	 * 显示结果
//	 * @param product
//	 */
//	private void print(CheckMassiveMimoProduct product) {
//		// 显示处理结果
//		printRuntime();
//		// 显示标题
//		createShowTitle(new String[] { "UPLOAD-FILE/STATE", "UPLOAD-FILE/FILE" });
//		
//		ShowItem item = new ShowItem();
//		// 结果...
//		int state = product.getSuckers();
//		
//		if (StoreState.isDiskMissing(state)) {
//			String name = getXMLContent("UPLOAD-FILE/STATE/DISK-MSSING");
//			item.add(new ShowStringCell(0, name, Color.RED));
//		} else if (StoreState.isFailed(state)) {
//			String name = getXMLContent("UPLOAD-FILE/STATE/FAILED");
//			item.add(new ShowStringCell(0, name, Color.RED));
//		} else if (StoreState.isSuccessful(state)) {
//			String name = getXMLContent("UPLOAD-FILE/STATE/SUCCESS");
//			item.add(new ShowStringCell(0, name, Color.BLUE));
//		} else if (StoreState.isExists(state)) {
//			String name = getXMLContent("UPLOAD-FILE/STATE/EXISTS");
//			item.add(new ShowStringCell(0, name, Color.RED));
//		} else {
//			String name = "None";
//			item.add(new ShowStringCell(0, name));
//		}
//		
//		SRL dir = product.getSRL();
//		if (dir != null) {
//			item.add(new ShowStringCell(1, dir.getPath()));
//		} else {
//			CheckMassiveMimo cmd = getCommand();
//			SRL srl = cmd.getSRL();
//			item.add(new ShowStringCell(1, srl.getPath()));
//		}
//		
//		// 保存单元
//		addShowItem(item);
//		
//		// 输出全部
//		flushTable();
//	}
}