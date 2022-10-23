/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server.invoker;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.hash.*;

/**
 * 检测用户消耗资源调用器
 * 
 * @author scott.liang
 * @version 1.0 10/11/2022
 * @since laxcus 1.0
 */
public class LogCheckUserCostInvoker extends LogInvoker {

	private final String FILE_NAME = "^\\s*([0-9]{1,4})\\-([0-9]{1,2})\\-([0-9]{1,2})(\\([0-9]+\\))(?i)(\\.bil)\\s*$";
	private final String UIM_REGEX = "^\\s*([0-9a-fA-F]{64})\\s+([\\w\\W]+?)\\s+([0-9]+?)\\s+([0-9]+)\\s*$";
	private final String UIT_REGEX = "^\\s*([0-9]+?)\\s+([0-9]+?)\\s+([0-9]+?)\\s*$";
	private final String REGEX = "^\\s*(?i)(?:COST)\\:\\s+([\\w\\W]+?)\\s+(?i)(?:UIM)\\s+([\\w\\W]+?)\\s+(?i)(?:UIT)\\s+([\\w\\W]+)\\s*$";
	private final String RT_FULL = "^\\s*([0-9]{1,4})\\-([0-9]{1,2})\\-([0-9]{1,2})\\s+([0-9]{1,2})\\:([0-9]{1,2})\\:([0-9]{1,2})\\.([0-9]{1,3})\\s*$";

	/**
	 * 构造检测用户消耗资源调用器
	 * @param cmd 检测用户消耗资源
	 */
	public LogCheckUserCostInvoker(CheckUserCost cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckUserCost getCommand() {
		return (CheckUserCost) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckUserCost cmd = getCommand();

		CheckUserCostProduct product = new CheckUserCostProduct();

		// 日志根目录
		File billRoot = getLauncher().getBillRoot();

		// 类型
		byte[] types = cmd.getTypeBytes();
		for (byte type : types) {
			String tag = SiteTag.translate(type);
			File dir = new File(billRoot, tag);
			scanRoot(dir, cmd, type, product);
		}

		// 返回结果
		boolean success = replyProduct(product);
		return useful(success);
	}
	
	/**
	 * 扫描根目录
	 * @param dir
	 * @param cmd
	 * @param family
	 * @param product
	 */
	private void scanRoot(File dir, CheckUserCost cmd, byte family, CheckUserCostProduct product) {
		File[] subs = dir.listFiles();
		int size = (subs != null ? subs.length : 0);
		for (int i = 0; i < size; i++) {
			File sub = subs[i];
			scanBills(sub, cmd, family, product);
		}
	}
	
	/**
	 * 格式化日志里的时间
	 * @param input
	 * @return
	 */
	private long formatBillTime(String input) {
		Pattern pattern = Pattern.compile(RT_FULL);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return 0;
		}

		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int day = Integer.parseInt(matcher.group(3));

		int hour = Integer.parseInt(matcher.group(4));
		int minute = Integer.parseInt(matcher.group(5));
		int second = Integer.parseInt(matcher.group(6));
		int ms = Integer.parseInt(matcher.group(7));

		java.util.Calendar dar = java.util.Calendar.getInstance();
		dar.set(Calendar.YEAR, year);
		dar.set(Calendar.MONTH, month - 1);
		dar.set(Calendar.DAY_OF_MONTH, day);

		dar.set(Calendar.HOUR_OF_DAY, hour);
		dar.set(Calendar.MINUTE, minute);
		dar.set(Calendar.SECOND, second);
		dar.set(Calendar.MILLISECOND, ms);

		return dar.getTime().getTime();
	}

	/**
	 * 格式化日期，把时分秒去掉
	 * @param date
	 * @return
	 */
	private long formatDate(Date date) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(date);

		dar.set(Calendar.HOUR_OF_DAY, 0);
		dar.set(Calendar.MINUTE, 0);
		dar.set(Calendar.SECOND, 0);
		dar.set(Calendar.MILLISECOND, 0);

		return dar.getTime().getTime();
	}

	/**
	 * 检查匹配时间
	 * @param file
	 * @param cmd
	 * @return
	 */
	private boolean matchTime(File file, CheckUserCost cmd) {
		String name = file.getName();
		
		Pattern pattern = Pattern.compile(FILE_NAME);
		Matcher matcher = pattern.matcher(name);
		if (!matcher.matches()) {
			return false;
		}
		
		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int day = Integer.parseInt(matcher.group(3));

		java.util.Calendar dar = java.util.Calendar.getInstance();
		dar.set(Calendar.YEAR, year);
		dar.set(Calendar.MONTH, month - 1);
		dar.set(Calendar.DAY_OF_MONTH, day);
		
		dar.set(Calendar.HOUR_OF_DAY, 0);
		dar.set(Calendar.MINUTE, 0);
		dar.set(Calendar.SECOND, 0);
		dar.set(Calendar.MILLISECOND, 0);
		long currentTime = dar.getTime().getTime();

		long beginTime = formatDate(SimpleTimestamp.format(cmd.getBeginTime()));
		long endTime = formatDate(SimpleTimestamp.format(cmd.getEndTime()));
		boolean allow = (beginTime <= currentTime && currentTime <= endTime);

//		if(!allow) {
//			SimpleDateFormat rt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//			String s1 = rt.format( new Date(beginTime) );
//			String s2 = rt.format( new Date(currentTime));
//			String s3 = rt.format( new Date(endTime));
//			System.out.printf("不匹配 [%s]！%s <= %s && %s <= %s\n", name, s1, s2, s2, s3 );
//		}
		
		return allow;
	}
	
	/**
	 * 扫描日志
	 * @param dir
	 * @param cmd
	 * @param family
	 * @param product
	 */
	private void scanBills(File dir, CheckUserCost cmd, byte family, CheckUserCostProduct product) {
		File[] files = dir.listFiles();
		int size = (files != null ? files.length : 0);
		// 逐个检查每个文件
		for (int i = 0; i < size; i++) {
			File file = files[i];
			// 判断是目录或者文件，如果是目录继续深入
			if (file.isDirectory()) {
				scanBills(file, cmd, family, product);
				continue;
			} else if (file.isFile()) {
				if (matchTime(file, cmd)) {
					scanBill(file, cmd, family, product);
				}
			}

			//			// 是文件并且日期匹配时
			//			boolean match = (file.isFile() && matchTime(file, cmd));
			//			if (match) {
			//				scanLog(file, cmd, family, product);
			//			}
		}
	}

	/**
	 * 读一个文件
	 * @param file
	 * @return
	 */
	private byte[] readFile(File file) {
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {

		}
		return null;
	}
	
	/**
	 * 扫描一个日志文件，读取有效信息
	 * @param file
	 * @param cmd
	 * @param family
	 * @param product
	 */
	private void scanBill(File file, CheckUserCost cmd, byte family, CheckUserCostProduct product) {
		// 1. 检查内部的日期和用户签名
		byte[] b = readFile(file);
		if (b == null) {
			return;
		}

		String text = new com.laxcus.util.charset.UTF8().decode(b);
		if (text == null) {
			return;
		}
		
		long beginTime = SimpleTimestamp.format(cmd.getBeginTime()).getTime();
		long endTime = SimpleTimestamp.format(cmd.getEndTime()).getTime();

		Pattern uim = Pattern.compile(UIM_REGEX);
		Pattern uit = Pattern.compile(UIT_REGEX);
		
		Pattern pattern = Pattern.compile(REGEX);
		try {
			StringReader strReader = new StringReader(text);
			BufferedReader reader = new BufferedReader(strReader);
			do {
				String str = reader.readLine();
				if (str == null) {
					break;
				}
				// 判断日期和签名
				Matcher matcher = pattern.matcher(str);
				if (!matcher.matches()) {
					continue;
				}
				
				String date = matcher.group(1); // 日期格式： 2022-10-09 11:07:08.265
				String uimText = matcher.group(2); // UIM: USER INVOKER MESSAGE
				String uitText = matcher.group(3); // UIT: USER INVOKER TIME
				
				// 格式化时间
				long rt = formatBillTime(date);
				boolean allow = (beginTime <= rt && rt <= endTime);
				if (!allow) {
					continue;
				}

				// UIM 参数. UIM: USER INVOKER MESSAGE
				matcher = uim.matcher(uimText);
				if (!matcher.matches()) {
					continue;
				}
				if (!SHA256Hash.validate(matcher.group(1))) {
					continue;
				}
				SHA256Hash hash = new SHA256Hash(matcher.group(1));
				Siger siger = new Siger(hash);
				if (!cmd.hasUser(siger)) {
					continue;
				}
				
				String command = matcher.group(2);
				
				// 判断有匹配的命令
				if (cmd.getCommandCount() > 0) {
					if (!cmd.hasCommand(command)) {
						continue;
					}
				}
				
				long invokerId = Long.parseLong(matcher.group(3));
				int iterateIndex = Integer.parseInt(matcher.group(4));

				// UIT: USER INVOKER TIME
				matcher = uit.matcher(uitText);
				if (!matcher.matches()) {
					continue;
				}
				
				long initTime = Long.parseLong(matcher.group(1));
				long lastTime = Long.parseLong(matcher.group(2));
				long processTime = Long.parseLong(matcher.group(3));
				
				UserCostItem item = new UserCostItem();
				item.setFamily(family);
				item.setCommand(command);
				item.setInvokerId(invokerId);
				item.setIterateIndex(iterateIndex);
				item.setInitTime(initTime);
				item.setEndTime(lastTime);
				item.setProcessTime(processTime);
				
				// 加一个单元
				product.add(siger, item);
			} while (true);
			reader.close();
			strReader.close();
		} catch (IOException e) {

		}
	}
	
//	String str = String.format("UIM %s %s %d %d UIT %d %d %d", 
//			issuer, cmd, invokerId, iterateIndex, initTime, endTime, processTime);

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}