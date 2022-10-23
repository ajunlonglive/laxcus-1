/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.io.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.each.*;

/**
 * EACH签名调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopBuildEachInvoker extends DesktopInvoker {

	/**
	 * 构造EACH签名调用器，指定命令
	 * @param cmd EACH签名命令
	 */
	public DesktopBuildEachInvoker(BuildEach cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildEach getCommand() {
		return (BuildEach) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BuildEach cmd = getCommand();
		String plant = cmd.getPlant();
		File file = new File(plant);
		try {
			if (file.exists() && file.isFile()) {
				doFile(cmd);
			} else {
				doText(cmd);
			}
		} catch (Throwable e) {
			super.fault(e);
		}
		// 返回
		return useful();
	}

	/**
	 * 生成文件
	 * @param cmd
	 * @throws IOException 
	 */
	private void doFile(BuildEach cmd) throws IOException {
		File file = new File(cmd.getPlant());
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b, 0, b.length);
		in.close();
		// 生成数字签名和打印
		print(b);
	}

	/**
	 * 计算半截码值
	 * @param cmd
	 */
	private void doText(BuildEach cmd) {
		String text = cmd.getPlant();
		if (cmd.isIgnore()) {
			text = text.toLowerCase();
		}
		// 转为指定的编码
		byte[] b = null;
		if (cmd.isUTF8()) {
			b = new UTF8().encode(text);
		} else if (cmd.isUTF16()) {
			b = new UTF16().encode(text);
		} else if (cmd.isUTF32()) {
			b = new UTF32().encode(text);
		} else {
			b = text.getBytes();
		}
		// 生成数字签名和打印
		print(b);
	}

	/**
	 * 生成数字签名和打印
	 * @param b 字节数组
	 */
	private void print(byte[] b) {
		// 调用JNI接口，生成EACH签名
		long sign = EachTrustor.sign(b);
		// 打印结果
		createShowTitle(new String[] { "BUILD-EACH/SIGN" });
		// 在界面显示
		ShowItem item = new ShowItem();
		item.add(new ShowLongCell(0, sign));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
}