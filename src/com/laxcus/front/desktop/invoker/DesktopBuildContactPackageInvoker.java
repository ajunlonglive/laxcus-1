/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.cloud.*;

/**
 * 生成SWIFT应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopBuildContactPackageInvoker extends DesktopBuildCloudPackageInvoker {

	/**
	 * 构造生成SWIFT应用软件包调用器，指定命令
	 * @param cmd 生成SWIFT应用软件包
	 */
	public DesktopBuildContactPackageInvoker(BuildContactPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildContactPackage getCommand() {
		return (BuildContactPackage) super.getCommand();
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		// 管理员不能执行操作
//		if (isAdministrator()) {
//			faultX(FaultTip.PERMISSION_MISSING);
//			return useful(false);
//		}
//
//		BuildSwiftPackage cmd = getCommand();
//		File file = cmd.getWriter();
//
//		// 提取全部单元
//		ArrayList<CloudPackageElement> array = new ArrayList<CloudPackageElement>();
//		array.add(cmd.getDistantElement());
//		array.add(cmd.getNearElement());
//
//		// 写入单元
//		boolean success = false;
//		try {
//			// zos.setLevel(Deflater.BEST_COMPRESSION);
//
//			// 云应用包写入器
//			CloudPackageWriter writer = new CloudPackageWriter();
//
//			// "README"参数
//			writer.writeReadme(cmd.getReadmeElement());
//
//			// 逐个输出和保存
//			for (CloudPackageElement element : array) {
//				writer.writeElement(element);
//			}
//			// 输出和关闭
//			writer.flush(file);
//			writer.close();
//
//			success = true;
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//		
//		// 界面显示！
//		flush(success, file);
//
//		return useful(success);
//	}

	
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		// 管理员不能执行操作
//		if (isAdministrator()) {
//			faultX(FaultTip.PERMISSION_MISSING);
//			return useful(false);
//		}
//
//		BuildSwiftPackage cmd = getCommand();
//		File file = cmd.getWriter();
//
//		// 写入单元
//		boolean success = false;
//		try {
//			// 云应用包写入器
//			CloudPackageWriter writer = new CloudPackageWriter();
//			
//			// "README"参数
//			writer.writeReadme(cmd.getReadmeElement());
//			// "SWIFT"参数
//			writer.writeElement(cmd.getSwiftElement());
//			// 输出到磁盘和关闭流
//			writer.flush(file);
//			writer.close();
//			
//			success = true;
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//		
//		// 界面显示！
//		flush(success, file);
//
//		return useful(success);
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}