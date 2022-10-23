/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.text.*;
import java.util.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描云端磁盘调用器
 * 
 * @author scott.liang
 * @version 1.0 2/28/2022
 * @since laxcus 1.0
 */
public class MeetScanCloudDirectoryInvoker extends MeetHubServiceInvoker {

	/**
	 * 构造扫描云端磁盘调用器，指定命令
	 * @param cmd 命令
	 */
	public MeetScanCloudDirectoryInvoker(ScanCloudDirectory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanCloudDirectory getCommand() {
		return (ScanCloudDirectory) super.getCommand();
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

		ScanCloudDirectory cmd = getCommand();
		SRL srl = cmd.getSRL();
		Node hub = srl.getNode();
		
		// 判断网关存在
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
		int index = findEchoKey(0);
		ScanCloudDirectoryProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ScanCloudDirectoryProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 2. 出错或者拒绝
		boolean success = (product != null);
		if (!success) {
			faultX(FaultTip.FAILED_X, getCommand());
			return false;
		}

		ProductListener listener = getProductListener();
		if (listener != null) {
			listener.push(product);
		} else {
			print(product);
		}

		return useful();
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(ScanCloudDirectoryProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SCAN-CLOUD-DISK/TIME", "SCAN-CLOUD-DISK/TYPE", 
				"SCAN-CLOUD-DISK/LENGTH", "SCAN-CLOUD-DISK/NAME" });

		VPath path = product.getVPath();
		if (path == null) {
			Logger.error(this, "print", "not found path!");
			return;
		}
		
//		System.out.printf("打印结果 %s\n", getCommand());

		print(path);

		// 输出全部
		flushTable();
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

//	/**
//	 * 格式化路径
//	 * @param prefix
//	 * @param path
//	 * @return
//	 */
//	private String formatPath(String prefix, VPath path) {
//		if (prefix == null || prefix.isEmpty()) {
//			return path.getPath();
//		}
//		char last = prefix.charAt(prefix.length() - 1);
//		if (last != '/' && last !='\\') {
//			prefix = prefix + '/';
//		}
//		return prefix + path.getPath();
//	}
	
	/**
	 * 格式化路径
	 * @param path
	 * @return
	 */
	private String formatPath(VPath path) {
		boolean full = getCommand().isFullPath();
		if (full) {
			return path.getPath();
		} else {
			return path.getName();
		}
	}

	/**
	 * 显示单元
	 * @param path
	 */
	private void showItem(VPath path) {
		Date d = SimpleTimestamp.format(path.getLastModified());
		String dt = sdf.format(d);
		long length = path.getLength();

		ShowItem item = new ShowItem();
		// 时间
		item.add(new ShowStringCell(0, dt));
		// 类型
		if (path.isFile()) {
			String name = getXMLContent("SCAN-CLOUD-DISK/TYPE/FILE");
			name = String.format("%s(%d)", name, path.getLevel()+1);
			item.add(new ShowStringCell(1, name));
		} else if (path.isDirectory()) {
			String name = getXMLContent("SCAN-CLOUD-DISK/TYPE/DIR");
			name = String.format("%s(%d)", name, path.getLevel()+1);
			item.add(new ShowStringCell(1, name));
		} else if (path.isDisk()) {
			String name = getXMLContent("SCAN-CLOUD-DISK/TYPE/DISK");
			name = String.format("%s(%d)", name, path.getLevel()+1);
			item.add(new ShowStringCell(1, name));
		} else {
			item.add(new ShowStringCell(1, ""));
		}
		if (length > 0) {
			String s = ConfigParser.splitCapacity(length);
			item.add(new ShowStringCell(2, s));
		} else {
			item.add(new ShowStringCell(2, "--"));
		}
		if (path.isDisk()) {
			item.add(new ShowStringCell(3, "/"));
		} else {
			String s = formatPath(path);
			item.add(new ShowStringCell(3, s));
		}
		// 保存单元
		addShowItem(item);
	}

	private void print(VPath path) {
		// 单元
		showItem(path);
		
		ArrayList<VPath> files = new ArrayList<VPath>();
		ArrayList<VPath> dirs = new ArrayList<VPath>();
		// 分开
		for (VPath sub : path.list()) {
			if (sub.isDirectory()) {
				dirs.add(sub);
			} else if (sub.isFile()) {
				files.add(sub);
			}
		}

		// 显示文件
		for (VPath file : files) {
			showItem(file);
		}
		for (VPath dir : dirs) {
			print(dir);
		}
	}

	//	private void print(String prefix, VPath path) {
	//		// 单元
	//		printItem(prefix, path);
	//		
	//		// 子级
	//		ArrayList<VPath> dirs = new ArrayList<VPath>();
	//
	//		prefix = formatPath(prefix, path);
	//		for (VPath sub : path.list()) {
	//			if (sub.isDirectory()) {
	//				printItem(prefix, sub);
	//				String next = formatPath(prefix, sub);
	//				print(next, sub);
	////				dirs.add(sub);
	//			} else if (sub.isFile()) {
	//				printItem(prefix, sub);
	//			}
	//		}
	//
	////		// 显示子级目录的目录和文件
	////		for (VPath dir : dirs) {
	////			print(prefix, dir);
	////		}
	//	}

}
