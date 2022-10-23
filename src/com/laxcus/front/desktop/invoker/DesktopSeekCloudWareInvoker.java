/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.text.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索云应用调用器。
 * 
 * @author scott.liang
 * @version 1.0 2/10/2020
 * @since laxcus 1.0
 */
public class DesktopSeekCloudWareInvoker extends DesktopInvoker {

	/**
	 * 构造检索云应用调用器，指定命令
	 * @param cmd 检索云应用
	 */
	public DesktopSeekCloudWareInvoker(SeekCloudWare cmd) {
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是管理员账号，不能执行这个操作
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful(false);
		}
		
		SeekCloudWare cmd = getCommand();

		// 取出全部CALL节点地址
		List<Node> hubs = CallOnFrontPool.getInstance().getHubs();
		if (hubs.isEmpty()) {
			faultX(FaultTip.SITE_MISSING);
			return useful(false);
		}

		// 以容错模式投递给CALL节点
		int count = incompleteTo(hubs, cmd);
		boolean success = (count > 0);
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekCloudWareProduct product = new SeekCloudWareProduct();

		List<Integer> keys = getEchoKeys();

		for (int index : keys) {
			if (isSuccessCompleted(index)) {
				try {
					SeekCloudWareProduct sub = getObject(SeekCloudWareProduct.class, index);
					product.addAll(sub);
				} catch (VisitException e) {
					Logger.error(e);
				}
			}
		}
		// 打印结果
		print(product);

		return useful();
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(SeekCloudWareProduct product) {
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "SEEK-CLOUD-WARE/T1", "SEEK-CLOUD-WARE/T2" });
		
		List<CloudWareItem> items = product.list();
		for (int i = 0; i < items.size(); i++) {
			// 空格
			if (i > 0) {
				printGap(2);
			}
			CloudWareItem item = items.get(i);
			// 显示一个节点
			print(item);
		}
		
		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 记录一行参数
	 * @param title
	 * @param text
	 */
	private void plus(String title, Object text) {
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, title));
		item.add(new ShowStringCell(1, text));
		addShowItem(item);
	}
	
	/**
	 * 打印单元
	 * @param item
	 */
	private void print(CloudWareItem ware) {
		String site = findXMLTitle("SEEK-CLOUD-WARE/ITEM/SITE");
		String task = findXMLTitle("SEEK-CLOUD-WARE/ITEM/TASK-NAME");
		String assist = findXMLTitle("SEEK-CLOUD-WARE/ITEM/ASSIST");
		String library = findXMLTitle("SEEK-CLOUD-WARE/ITEM/LIBRARY");

		// 站点
		plus(site, ware.getSite());
		// 成员
		for(CloudWareElement element :	ware.list()) {
			// 输出阶段命名
			plus(task, element.getPhase().toString(true));
			// 附件
			for(FileKey key : element.getJars()) {
				plus(assist, line(key));
			}
			// 动态链接库
			for(FileKey key : element.getLibraries()) {
				plus(library, line(key));
			}
		}
	}
	
	/**
	 * 解析参数，生成它！
	 * @param key
	 * @return
	 */
	private String line(FileKey key) {
		SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		String modifiled = style.format(new Date(key.getModified()));
		String length = ConfigParser.splitCapacity(key.getLength());
		// 生成这符串
		return String.format("%s (%s %s)", key.getPath(), length, modifiled);
	}

}