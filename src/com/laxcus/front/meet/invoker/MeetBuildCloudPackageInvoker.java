/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;

import com.laxcus.command.cloud.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 生成云应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 2/15/2020
 * @since laxcus 1.0
 */
public abstract class MeetBuildCloudPackageInvoker extends MeetInvoker {

	/**
	 * 构造生成云应用软件包调用器，指定命令
	 * @param cmd 生成云应用软件包
	 */
	protected MeetBuildCloudPackageInvoker(BuildCloudPackage cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildCloudPackage getCommand() {
		return (BuildCloudPackage) super.getCommand();
	}
	
	/**
	 * 输出结果
	 * @param success 成功或者否
	 * @param file 磁盘文件
	 */
	protected void flush(boolean success, File file) {
		String filename = Laxkit.canonical(file);
		// 设置标题
		createShowTitle(new String[] { "BUILD-CLOUD-PACKAGE/STATUS",  "BUILD-CLOUD-PACKAGE/FILE"});

		ShowItem item = new ShowItem();
		// 图标
		item.add(createConfirmTableCell(0, success));
		// 数据表
		item.add(new ShowStringCell(1, filename));
		// 显示
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 管理员不能执行操作
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful(false);
		}

		BuildCloudPackage cmd = getCommand();
		File file = cmd.getWriter();

		// 写入单元
		boolean success = false;
		try {
			// zos.setLevel(Deflater.BEST_COMPRESSION);

			// 云应用包写入器
			CloudPackageWriter writer = new CloudPackageWriter();

			// "README"参数
			writer.writeReadme(cmd.getReadmeElement());
			
			// "GUIDE"参数
			writer.writeElement(cmd.getGuideElement());

			// 逐个输出和保存
			for (CloudPackageElement element : cmd.elements()) {
				writer.writeElement(element);
			}
			// 输出和关闭
			writer.flush(file);
			writer.close();

			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}
		
		// 界面显示！
		flush(success, file);

		return useful(success);
	}

}