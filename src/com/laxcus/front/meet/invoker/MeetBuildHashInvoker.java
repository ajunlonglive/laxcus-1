/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;
import java.security.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.hash.*;

/**
 * 计算散列码调用器。
 * 
 * @author scott.liang
 * @version 1.05 09/12/2016
 * @since laxcus 1.0
 */
public class MeetBuildHashInvoker extends MeetInvoker {

	/**
	 * 构造计算散列码调用器，指定命令
	 * @param cmd 计算散列码命令
	 */
	public MeetBuildHashInvoker(BuildHash cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildHash getCommand() {
		return (BuildHash) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BuildHash cmd = getCommand();
		String plant = cmd.getPlant();
		File file = new File(plant);
		try {
			if (file.exists()) {
				doFile(cmd);
			} else {
				doText(cmd);
			}
		} catch (NoSuchAlgorithmException e) {
			super.fault(e);
		} catch (UnsupportedEncodingException e) {
			super.fault(e);
		} catch (IOException e) {
			super.fault(e);
		}

		// 返回
		return useful();
	}

	/**
	 * 计算散列值
	 * @param cmd
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private void doText(BuildHash cmd) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		String text = cmd.getPlant();
		if (cmd.isIgnore()) {
			text = text.toLowerCase();
		}
		// 统一转为UTF8编码
		byte[] b = text.getBytes("UTF8");

		// 判断类型，选择一个
		if (cmd.isSHA1()) {
			SHA1Hash hash = Laxkit.doSHA1Hash(b);
			print(hash.toString());
		} else if(cmd.isSHA256()) {
			SHA256Hash hash = Laxkit.doSHA256Hash(b);
			print(hash.toString());
		} else if(cmd.isSHA512()) {
			SHA512Hash hash = Laxkit.doSHA512Hash(b);
			print(hash.toString());
		} else if (cmd.isMD5()) {
			MD5Hash hash = Laxkit.doMD5Hash(b);
			print(hash.toString());
		} else {
			throw new IllegalValueException("illegal type");
		}
	}
	
	/**
	 * 生成文件
	 * @param cmd
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private void doFile(BuildHash cmd) throws IOException, NoSuchAlgorithmException {
		File b = new File(cmd.getPlant());
		
		// 判断类型，选择一个
		if (cmd.isSHA1()) {
			SHA1Hash hash = Laxkit.doSHA1Hash(b);
			print(hash.toString());
		} else if(cmd.isSHA256()) {
			SHA256Hash hash = Laxkit.doSHA256Hash(b);
			print(hash.toString());
		} else if(cmd.isSHA512()) {
			SHA512Hash hash = Laxkit.doSHA512Hash(b);
			print(hash.toString());
		} else if (cmd.isMD5()) {
			MD5Hash hash = Laxkit.doMD5Hash(b);
			print(hash.toString());
		} else {
			throw new IllegalValueException("illegal type");
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 打印结果
	 * @param hash
	 */
	private void print(String hash) {
		createShowTitle(new String[] { "BUILD-HASH/CODE" });
		
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, hash));
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

}