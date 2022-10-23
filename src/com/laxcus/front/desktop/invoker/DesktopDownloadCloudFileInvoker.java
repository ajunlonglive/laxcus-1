/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.io.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 从云端下载文件调用器
 * 
 * @author scott.liang
 * @version 1.0 11/07/2021
 * @since laxcus 1.0
 */
public class DesktopDownloadCloudFileInvoker extends DesktopHubServiceInvoker {

	/** 执行步骤 **/
	private int step;

	/**
	 * 构造从云端下载文件调用器，指定命令
	 * @param cmd 命令
	 */
	public DesktopDownloadCloudFileInvoker(DownloadCloudFile cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DownloadCloudFile getCommand() {
		return (DownloadCloudFile) super.getCommand();
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
	 * 生成向导命令
	 * @return
	 */
	private DownloadCloudFileGuide createGuide() {
		DownloadCloudFile cmd = getCommand();
		SRL srl = cmd.getSRL();
		// 生成命令
		DownloadCloudFileGuide guide = new DownloadCloudFileGuide();
		guide.setSRL(srl);
		return guide;
	}

	/**
	 * 检查文件在本地
	 * @return 
	 */
	private boolean checkLocalFile() {
		DownloadCloudFile cmd = getCommand();
		File file = cmd.getFile();
		// 如果文件没有定义时，来自系统命令；否则是命令行输入
		if (file != null) {
			boolean exists = (file.exists() && file.isFile());
			// 存在且不让覆盖时，拒绝执行
			if (exists && !cmd.isOverride()) {
				String filename = Laxkit.canonical(file);
				super.faultX(FaultTip.EXISTED_X, filename);
				return false;
			}
		}

		return true;
	}

	/**
	 * 发送向导指令
	 * @return
	 */
	private boolean attempt() {
		ProductListener listener = getProductListener();
		
		// 判断文件存在
		if (!checkLocalFile()) {
			if (listener != null) {
				listener.push(null);
			} else {
				faultX(FaultTip.PARAMETER_MISSING);
			}
			return false;
		}

		DownloadCloudFileGuide guide = createGuide();
		SRL srl = guide.getSRL();
		Node hub = srl.getNode();

		// 判断网关CALL节点存在
		boolean success = checkCloudHub(hub);
		if (!success) {
			return false;
		}

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
		DownloadCloudFileProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DownloadCloudFileProduct.class, index);
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
		if (!StoreState.isSuccessful(state)) {
			if (listener != null) {
				listener.push(product);
			} else {
				print(false, null);
			}
			return false;
		}

		DownloadCloudFileGuide guide = createGuide();
		// 要求文件写入磁盘
		boolean ondisk = isDisk();
		setDisk(true);

		// 拿到地址，异步发送
		Cabin hub = findItemCabin(index); // product.getSource();
		ReplyItem item = new ReplyItem(hub, guide);
		// 发送到服务器
		success = replyTo(item);

		setDisk(ondisk);

		// 如果不成功...
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 第三个阶段
	 * @return 返回真或者假
	 */
	private boolean receive() {
		ProductListener listener = getProductListener();
		// 如果接收FRONT站点数据失败，退出
		if (isFaultCompleted()) {
			if (listener != null) {
				listener.push(null);
			} else {
				super.replyFault();
			}
			return false;
		}
		
		// 找到本地存储文件
		int index = findEchoKey(0);

		// 选择是文件或者内存
		File file = null;
		byte[] memory = null;
		// 取出异步缓存
		EchoBuffer buff = findBuffer(index);
		if (buff.isDisk()) {
			file = buff.getFile();
			Logger.debug(this, "receive", "file is '%s', length: %d", file,
					(file == null ? -1 : file.length()));
		} else if (buff.isMemory()) {
			memory = buff.getMemory();
			Logger.debug(this, "receive", "memory length: %d",
					(memory != null ? memory.length : -1));
		}

		boolean failed = (memory == null && file == null);

		// 选择提示位置
		if (listener != null) {
			DownloadCloudFile cmd = getCommand();
			if (failed) {
				listener.push(null);
			} else if (memory != null) {
				SRLMemoryProduct product = new SRLMemoryProduct(cmd.getSRL(), memory);
				listener.push(product);
			} else if (file != null) {
				byte[] b = readFile(file);
				SRLMemoryProduct product = new SRLMemoryProduct(cmd.getSRL(), b);
				product.setSRL(cmd.getSRL());
				listener.push(product);
			}
		} else {
			if (failed) {
				faultX(FaultTip.NOTFOUND_X, getCommand());
			} else if (memory != null) {
				saveTo(memory);
			} else if (file != null) {
				saveTo(file);
			}
		}
		// 返回结果
		return !failed;
	}

	/**
	 * 读文件
	 * @param file
	 * @return
	 */
	private byte[] readFile(File file) {
		// 写入磁盘
		try {
			byte[] b = new byte[10240];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			FileInputStream in = new FileInputStream(file);
			do {
				// 读取文件
				int len = in.read(b);
				if (len < 0) {
					break;
				}
				// 写入磁盘
				out.write(b, 0, len);
			} while (true);
			// 写入磁盘
			b = out.toByteArray();
			// 关闭
			in.close();

			return b;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 保存到指定位置
	 * @param memory
	 */
	private void saveTo(byte[] memory) {
		DownloadCloudFile cmd = getCommand();
		File dest = cmd.getFile();

		// 文件没有定义时...
		if (dest == null) {
			super.faultX(FaultTip.IMPLEMENT_FAULT);
			return;
		}

		// 写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(dest);
			out.write(memory);
			out.flush();
			out.close();
			print(true, dest);
			return;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 失败
		print(false, null);
	}

	/**
	 * 保存到指定位置
	 * @param source
	 */
	private void saveTo(File source) {
		DownloadCloudFile cmd = getCommand();
		File dest = cmd.getFile();

		// 文件没有定义时...
		if (dest == null) {
			super.faultX(FaultTip.IMPLEMENT_FAULT);
			return;
		}

		// 写入磁盘
		byte[] b = new byte[10240];
		try {
			long count = 0;
			long length = source.length();
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(dest);
			do {
				// 读取文件
				int len = in.read(b);
				if (len < 0) {
					break;
				}
				// 写入磁盘
				out.write(b, 0, len);
				count += len;
				if (count >= length) {
					break;
				}
			} while (true);
			// 输出
			out.flush();
			// 关闭
			in.close();
			out.close();
			// 显示
			boolean success = (count >= length);
			print(success, dest);
			return;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 失败
		print(false, null);
	}

	/**
	 * 显示结果
	 * @param product
	 */
	private void print(boolean success, File file) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "DOWNLOAD-FILE/STATE", "DOWNLOAD-FILE/SRL", "DOWNLOAD-FILE/FILE" });

		ShowItem item = new ShowItem();
		SRL srl = getCommand().getSRL();

		// 成功/失败
		if (success) {
			String name = getXMLContent("DOWNLOAD-FILE/STATE/SUCCESS");
			item.add(new ShowStringCell(0, name));
			item.add(new ShowStringCell(1, srl.toString()));
			String filename = Laxkit.canonical(file);
			item.add(new ShowStringCell(2, filename));
		} else {
			String name = getXMLContent("DOWNLOAD-FILE/STATE/FAILED");
			item.add(new ShowStringCell(0, name));
			item.add(new ShowStringCell(1, srl.toString()));
			item.add(new ShowStringCell(2, ""));
		}

		// 保存单元
		addShowItem(item);

		// 输出全部
		flushTable();
	}

	//	/**
	//	 * 第三阶段
	//	 * @return
	//	 */
	//	private boolean doThirdX(){
	//		DownloadFileProduct product = null;
	//		int index = findEchoKey(0);
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				product = getObject(DownloadFileProduct.class, index);
	//			}
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//		// 2. 出错或者拒绝
	//		boolean success = (product != null);
	//		if (!success) {
	//			faultX(FaultTip.FAILED_X, getCommand());
	//			return false;
	//		}
	//		
	//		int state = product.getState();
	//		success = StoreState.isSuccessful(state);
	//		
	//		// 选择提示位置
	//		EchoProductListener listener = getEchoProductListener();
	//		if (listener != null) {
	//			listener.push(product);
	//		} else {
	//			print(product);
	//		}
	//		
	//		Logger.debug(this, "doThird", success, "execute");
	//		
	//		return success;
	//	}

	//	/**
	//	 * 显示结果
	//	 * @param product
	//	 */
	//	private void print(DownloadFileProduct product) {
	//		// 显示处理结果
	//		printRuntime();
	//		// 显示标题
	//		createShowTitle(new String[] { "DOWNLOAD-FILE/STATE", "DOWNLOAD-FILE/FILE" });
	//		
	//		ShowItem item = new ShowItem();
	//		// 结果...
	//		int state = product.getState();
	//		
	//		if (StoreState.isDiskMissing(state)) {
	//			String name = getXMLContent("DOWNLOAD-FILE/STATE/DISK-MSSING");
	//			item.add(new ShowStringCell(0, name));
	//		} else if (StoreState.isFailed(state)) {
	//			String name = getXMLContent("DOWNLOAD-FILE/STATE/FAILED");
	//			item.add(new ShowStringCell(0, name));
	//		} else if (StoreState.isSuccessful(state)) {
	//			String name = getXMLContent("DOWNLOAD-FILE/STATE/SUCCESS");
	//			item.add(new ShowStringCell(0, name));
	//		} else if (StoreState.isExists(state)) {
	//			String name = getXMLContent("DOWNLOAD-FILE/STATE/EXISTS");
	//			item.add(new ShowStringCell(0, name));
	//		} else {
	//			String name = "None";
	//			item.add(new ShowStringCell(0, name));
	//		}
	//		
	//		SRL dir = product.getSRL();
	//		if (dir != null) {
	//			item.add(new ShowStringCell(1, dir.getPath()));
	//		} else {
	//			DownloadFile cmd = getCommand();
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


	//	/**
	//	 * 第三个阶段
	//	 * @return
	//	 */
	//	private boolean receive() {
	//		// 找到本地存储文件
	//		int index = findEchoKey(0);
	//		File file = findFile(index);
	//		// 没有文件
	//		if (file == null) {
	//			faultX(FaultTip.NOTFOUND_X, getCommand());
	//			return false;
	//		}
	//		
	//		Logger.debug(this, "receive", "file length is %d", file.length());
	//
	//		// 选择提示位置
	//		ProductListener listener = getProductListener();
	//		if (listener != null) {
	//			SRLMemoryProduct product = readProduct(file);
	//			listener.push(product);
	//		} else {
	//			saveTo(file);
	//		}
	//		
	//		return true;
	//	}


	//	if (file == null) {
	//		String tmp = System.getProperty("java.io.tmpdir");
	//		if (tmp == null) {
	//			super.faultX(FaultTip.PARAMETER_MISSING);
	//			return false;
	//		}
	//		// 生成临时文件
	//		for (int i = 100; true; i++) {
	//			String name = String.format("down%d.bin", i);
	//			file = new File(tmp, name);
	//			boolean b = (file.exists() && file.isFile());
	//			if (!b) {
	//				break;
	//			}
	//		}
	//		// 设置临时文件名
	//		cmd.setFile(file);
	//		temp = true;
	//	} 


	//	/**
	//	 * 文件保存到内存中
	//	 * @param file
	//	 * @return
	//	 */
	//	private SRLMemoryProduct readProduct(File file) {
	//		SRLMemoryProduct product = new SRLMemoryProduct();
	//		product.setSRL(getCommand().getSRL());
	//		
	//		// 写入磁盘
	//		byte[] b = new byte[10240];
	//		try {
	//			ByteArrayOutputStream out = new ByteArrayOutputStream();
	//			FileInputStream in = new FileInputStream(file);
	//			do {
	//				// 读取文件
	//				int len = in.read(b);
	//				if (len < 0) {
	//					break;
	//				}
	//				// 写入磁盘
	//				out.write(b, 0, len);
	//			} while (true);
	//			// 写入磁盘
	//			byte[] data = out.toByteArray();
	//			product.setData(data);
	//			
	//			// 关闭
	//			in.close();
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		
	//		return product;
	//	}


}