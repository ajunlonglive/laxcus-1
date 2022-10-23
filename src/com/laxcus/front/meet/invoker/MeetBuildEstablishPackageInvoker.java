/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.cloud.*;

/**
 * 生成ESTABLISH数据构建应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 2/13/2020
 * @since laxcus 1.0
 */
public class MeetBuildEstablishPackageInvoker extends MeetBuildCloudPackageInvoker {

	/**
	 * 构造生成ESTABLISH数据构建应用软件包调用器，指定命令
	 * @param cmd 生成ESTABLISH数据构建应用软件包
	 */
	public MeetBuildEstablishPackageInvoker(BuildEstablishPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildEstablishPackage getCommand() {
		return (BuildEstablishPackage) super.getCommand();
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		// 管理员不能执行操作
//		if(isAdministrator()) {
//			faultX(FaultTip.PERMISSION_MISSING);
//			return useful(false);
//		}
//
//		BuildEstablishPackage cmd = getCommand();
//		File file = cmd.getWriter();
//
//		// 提取全部单元
//		ArrayList<CloudPackageElement> array = new ArrayList<CloudPackageElement>();
//		array.add(cmd.getIssueElement());
//		array.add(cmd.getAssignElement());
//		array.add(cmd.getScanElement());
//		array.add(cmd.getSiftElement());
//		array.add(cmd.getRiseElement());
//		array.add(cmd.getEndElement());
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
